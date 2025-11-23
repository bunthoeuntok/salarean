package com.sms.auth.service;

import com.sms.common.dto.ErrorCode;
import com.sms.auth.exception.InvalidPasswordException;
import com.sms.auth.exception.ResetTokenInvalidException;
import com.sms.auth.exception.UserNotFoundException;
import com.sms.auth.model.User;
import com.sms.auth.repository.UserRepository;
import com.sms.auth.validation.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final long RESET_TOKEN_VALIDITY_MINUTES = 15;
    private static final String REDIS_KEY_PREFIX = "password_reset:";

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final TokenService tokenService;

    /**
     * Generate a password reset token and store it in Redis
     * @param email User's email address
     */
    @Transactional
    public void generateResetToken(String email) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Password reset requested for non-existent email: {}", email);
                    throw new UserNotFoundException(ErrorCode.EMAIL_NOT_FOUND, "Email not found");
                });

        // Generate random reset token
        String resetToken = UUID.randomUUID().toString();

        // Create Redis key: password_reset:{token}
        String redisKey = REDIS_KEY_PREFIX + resetToken;

        // Store user ID in Redis with 15-minute expiration
        redisTemplate.opsForValue().set(redisKey, user.getId().toString(), RESET_TOKEN_VALIDITY_MINUTES, TimeUnit.MINUTES);

        logger.info("Generated password reset token for user: {}", user.getId());

        // TODO: In production, send email with reset link containing the token
        // For now, we just log it (in real app, integrate with email service)
        logger.info("Password reset token (for testing): {}", resetToken);
    }

    /**
     * Validate reset token and update user's password
     * @param token Reset token
     * @param newPassword New password
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Validate password strength
        if (!passwordValidator.isValid(newPassword)) {
            throw new InvalidPasswordException(passwordValidator.getRequirementsMessage());
        }

        // Build Redis key
        String redisKey = REDIS_KEY_PREFIX + token;

        // Get user ID from Redis
        String userIdStr = (String) redisTemplate.opsForValue().get(redisKey);

        if (userIdStr == null) {
            logger.warn("Invalid or expired reset token used: {}", token);
            throw new ResetTokenInvalidException(ErrorCode.RESET_TOKEN_INVALID, "Invalid or expired reset token");
        }

        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID in reset token: {}", userIdStr);
            throw new ResetTokenInvalidException(ErrorCode.RESET_TOKEN_INVALID, "Invalid reset token format");
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found for reset token: {}", userId);
                    throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found");
                });

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete the reset token from Redis (one-time use)
        redisTemplate.delete(redisKey);

        // Invalidate all existing sessions and refresh tokens (security best practice)
        tokenService.revokeAllUserTokens(userId);

        logger.info("Password reset successful for user: {}", userId);
    }

    /**
     * Check if a reset token is valid (useful for validating before showing reset form)
     * @param token Reset token
     * @return true if token is valid and not expired
     */
    public boolean isTokenValid(String token) {
        String redisKey = REDIS_KEY_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
    }
}
