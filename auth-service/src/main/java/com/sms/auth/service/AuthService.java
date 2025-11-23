package com.sms.auth.service;

import com.sms.auth.config.SecurityProperties;
import com.sms.auth.dto.*;
import com.sms.auth.exception.*;
import com.sms.auth.model.Session;
import com.sms.auth.model.User;
import com.sms.auth.repository.SessionRepository;
import com.sms.auth.repository.UserRepository;
import com.sms.auth.security.JwtTokenProvider;
import com.sms.auth.validation.PasswordValidator;
import com.sms.common.dto.ErrorCode;
import com.sms.common.util.DateUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final JwtTokenProvider jwtTokenProvider;
    private final RateLimitService rateLimitService;
    private final TokenService tokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        // Validate password strength
        if (!passwordValidator.isValid(request.getPassword())) {
            throw new InvalidPasswordException(passwordValidator.getRequirementsMessage());
        }

        // Check duplicates
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("This email is already registered");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicatePhoneException("This phone number is already registered");
        }

        // Create user
        User user = User.builder()
            .email(request.getEmail())
            .phoneNumber(request.getPhoneNumber())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .preferredLanguage(request.getPreferredLanguage())
            .build();
        user = userRepository.save(user);

        // Generate JWT
        String token = jwtTokenProvider.generateToken(user.getId(), user.getPreferredLanguage());
        String jti = jwtTokenProvider.getJtiFromToken(token);

        // Create session
        Session session = Session.builder()
            .user(user)
            .tokenJti(jti)
            .lastActivityAt(DateUtils.nowDateTime())
            .expiresAt(DateUtils.expiresInHours(SecurityProperties.JWT_EXPIRATION_HOURS))
            .ipAddress(getClientIp(httpRequest))
            .userAgent(httpRequest.getHeader("User-Agent"))
            .build();
        sessionRepository.save(session);

        // Create refresh token
        String refreshToken = tokenService.createRefreshToken(
            user.getId(),
            getClientIp(httpRequest),
            httpRequest.getHeader("User-Agent")
        );

        return buildAuthResponseWithRefreshToken(user, token, refreshToken, user.getCreatedAt());
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String identifier = request.getEmailOrPhone();
        String clientIp = getClientIp(httpRequest);

        // Check rate limiting
        if (rateLimitService.isRateLimited(identifier)) {
            throw new RateLimitExceededException(
                "Too many failed login attempts. Please try again in " +
                com.sms.auth.config.SecurityProperties.ACCOUNT_LOCK_DURATION_MINUTES + " minutes."
            );
        }

        // Find user by email or phone
        User user = userRepository.findByEmail(identifier)
            .or(() -> userRepository.findByPhoneNumber(identifier))
            .orElse(null);

        // Verify credentials
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            rateLimitService.recordAttempt(identifier, clientIp, false, "INVALID_PASSWORD");
            throw new InvalidCredentialsException("Invalid email/phone or password");
        }

        // Record successful attempt
        rateLimitService.recordAttempt(identifier, clientIp, true, null);

        // Generate JWT
        String token = jwtTokenProvider.generateToken(user.getId(), user.getPreferredLanguage());
        String jti = jwtTokenProvider.getJtiFromToken(token);

        // Create session
        Session session = Session.builder()
            .user(user)
            .tokenJti(jti)
            .lastActivityAt(DateUtils.nowDateTime())
            .expiresAt(DateUtils.expiresInHours(SecurityProperties.JWT_EXPIRATION_HOURS))
            .ipAddress(clientIp)
            .userAgent(httpRequest.getHeader("User-Agent"))
            .build();
        sessionRepository.save(session);

        // Create refresh token
        String refreshToken = tokenService.createRefreshToken(
            user.getId(),
            clientIp,
            httpRequest.getHeader("User-Agent")
        );

        return buildAuthResponseWithRefreshToken(user, token, refreshToken, DateUtils.nowDateTime());
    }

    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        // Validate the refresh token
        var refreshToken = tokenService.validateRefreshToken(request.getRefreshToken());

        // Mark old token as used and delete it
        tokenService.markAsUsed(refreshToken.getId(), refreshToken.getUserId());

        // Get user
        User user = userRepository.findById(refreshToken.getUserId())
            .orElseThrow(() -> new InvalidTokenException(ErrorCode.INVALID_TOKEN, "User not found"));

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateToken(user.getId(), user.getPreferredLanguage());
        String jti = jwtTokenProvider.getJtiFromToken(newAccessToken);

        // Create new session
        Session session = Session.builder()
            .user(user)
            .tokenJti(jti)
            .lastActivityAt(DateUtils.nowDateTime())
            .expiresAt(DateUtils.expiresInHours(24))
            .ipAddress(getClientIp(httpRequest))
            .userAgent(httpRequest.getHeader("User-Agent"))
            .build();
        sessionRepository.save(session);

        // Generate new refresh token
        String newRefreshToken = tokenService.createRefreshToken(
            user.getId(),
            getClientIp(httpRequest),
            httpRequest.getHeader("User-Agent")
        );

        return new RefreshTokenResponse(newAccessToken, newRefreshToken, SecurityProperties.JWT_EXPIRATION_SECONDS);
    }

    @Transactional
    public void logout(String accessToken) {
        // Get JTI from access token
        String jti = jwtTokenProvider.getJtiFromToken(accessToken);

        // Get user ID from token
        var userId = jwtTokenProvider.getUserIdFromToken(accessToken);

        // Delete current session
        sessionRepository.deleteByTokenJti(jti);

        // Revoke all refresh tokens for the user
        tokenService.revokeAllUserTokens(userId);
    }

    private AuthResponse buildAuthResponseWithRefreshToken(User user, String accessToken,
                                                          String refreshToken, LocalDateTime loginTime) {
        return AuthResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .preferredLanguage(user.getPreferredLanguage())
            .token(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(86400L) // 24 hours in seconds
            .createdAt(user.getCreatedAt())
            .lastLoginAt(loginTime)
            .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
