package com.sms.auth.service;

import com.sms.auth.dto.*;
import com.sms.auth.exception.*;
import com.sms.auth.model.Session;
import com.sms.auth.model.User;
import com.sms.auth.repository.SessionRepository;
import com.sms.auth.repository.UserRepository;
import com.sms.auth.security.JwtTokenProvider;
import com.sms.auth.validation.PasswordValidator;
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
            .lastActivityAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(24))
            .ipAddress(getClientIp(httpRequest))
            .userAgent(httpRequest.getHeader("User-Agent"))
            .build();
        sessionRepository.save(session);

        return buildAuthResponse(user, token, user.getCreatedAt());
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String identifier = request.getEmailOrPhone();
        String clientIp = getClientIp(httpRequest);

        // Check rate limiting
        if (rateLimitService.isRateLimited(identifier)) {
            throw new RateLimitExceededException(
                "Too many failed login attempts. Please try again in 15 minutes."
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
            .lastActivityAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(24))
            .ipAddress(clientIp)
            .userAgent(httpRequest.getHeader("User-Agent"))
            .build();
        sessionRepository.save(session);

        return buildAuthResponse(user, token, LocalDateTime.now());
    }

    private AuthResponse buildAuthResponse(User user, String token, LocalDateTime loginTime) {
        return AuthResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .preferredLanguage(user.getPreferredLanguage())
            .token(token)
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
