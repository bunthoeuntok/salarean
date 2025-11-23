package com.sms.auth.service;

import com.sms.common.dto.ErrorCode;
import com.sms.auth.exception.InvalidTokenException;
import com.sms.auth.model.RefreshToken;
import com.sms.auth.repository.RefreshTokenRepository;
import com.sms.auth.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 30;

    private final RefreshTokenRepository refreshTokenRepository;
    private final SessionRepository sessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    public TokenService(RefreshTokenRepository refreshTokenRepository,
                       SessionRepository sessionRepository,
                       RedisTemplate<String, Object> redisTemplate,
                       PasswordEncoder passwordEncoder) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.sessionRepository = sessionRepository;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new refresh token for the user
     */
    @Transactional
    public String createRefreshToken(UUID userId, String ipAddress, String userAgent) {
        // Generate random UUID token
        UUID tokenId = UUID.randomUUID();
        String plainToken = tokenId.toString();

        // Hash the token
        String tokenHash = passwordEncoder.encode(plainToken);

        // Create expiration date (30 days from now)
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS);

        // Save to database
        RefreshToken refreshToken = RefreshToken.builder()
                .id(tokenId)
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .hasBeenUsed(false)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        refreshTokenRepository.save(refreshToken);

        // Cache in Redis (key: "refresh_token:{userId}:{tokenId}")
        String redisKey = buildRedisKey(userId, tokenId);
        redisTemplate.opsForValue().set(redisKey, refreshToken, REFRESH_TOKEN_VALIDITY_DAYS, TimeUnit.DAYS);

        logger.info("Created refresh token for user: {}", userId);

        // Return plain token (only time it's visible to client)
        return plainToken;
    }

    /**
     * Validate refresh token and check for replay attacks
     */
    @Transactional
    public RefreshToken validateRefreshToken(String plainToken) {
        UUID tokenId;
        try {
            tokenId = UUID.fromString(plainToken);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid refresh token format: {}", plainToken);
            throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, "Invalid token format");
        }

        // Try Redis first (fast path)
        RefreshToken refreshToken = getFromRedis(tokenId);

        // Fallback to database if not in Redis
        if (refreshToken == null) {
            refreshToken = refreshTokenRepository.findById(tokenId)
                    .orElseThrow(() -> {
                        logger.warn("Refresh token not found: {}", tokenId);
                        return new InvalidTokenException(ErrorCode.INVALID_TOKEN, "Token not found");
                    });

            // Warm the cache
            String redisKey = buildRedisKey(refreshToken.getUserId(), tokenId);
            redisTemplate.opsForValue().set(redisKey, refreshToken, REFRESH_TOKEN_VALIDITY_DAYS, TimeUnit.DAYS);
        }

        // Check if token has expired
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            logger.warn("Refresh token expired: {}", tokenId);
            throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, "Token expired");
        }

        // Check if token has already been used (replay attack detection)
        if (Boolean.TRUE.equals(refreshToken.getHasBeenUsed())) {
            logger.error("SECURITY ALERT: Token replay detected for user: {}. Invalidating all sessions.",
                        refreshToken.getUserId());

            // Invalidate all user sessions (possible compromise)
            invalidateAllUserSessions(refreshToken.getUserId());

            throw new InvalidTokenException(ErrorCode.TOKEN_REPLAY_DETECTED,
                                          "Token has been reused - all sessions invalidated");
        }

        // Verify token hash matches
        if (!passwordEncoder.matches(plainToken, refreshToken.getTokenHash())) {
            logger.warn("Token hash mismatch for token: {}", tokenId);
            throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, "Invalid token");
        }

        return refreshToken;
    }

    /**
     * Mark a refresh token as used (for rotation)
     */
    @Transactional
    public void markAsUsed(UUID tokenId, UUID userId) {
        RefreshToken token = refreshTokenRepository.findById(tokenId)
                .orElseThrow(() -> new InvalidTokenException(ErrorCode.INVALID_TOKEN, "Token not found"));

        token.setHasBeenUsed(true);
        token.setUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(token);

        // Update Redis cache
        String redisKey = buildRedisKey(userId, tokenId);
        redisTemplate.opsForValue().set(redisKey, token, REFRESH_TOKEN_VALIDITY_DAYS, TimeUnit.DAYS);

        // Delete the old token after marking as used (cleanup)
        refreshTokenRepository.deleteById(tokenId);
        redisTemplate.delete(redisKey);

        logger.info("Marked refresh token as used and deleted: {}", tokenId);
    }

    /**
     * Revoke all refresh tokens for a user
     */
    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        // Delete from database
        refreshTokenRepository.deleteByUserId(userId);

        // Delete from Redis (pattern: refresh_token:{userId}:*)
        String pattern = "refresh_token:" + userId + ":*";
        redisTemplate.delete(redisTemplate.keys(pattern));

        logger.info("Revoked all refresh tokens for user: {}", userId);
    }

    /**
     * Revoke all tokens except the current one (for password change)
     */
    @Transactional
    public void revokeAllUserTokensExcept(UUID userId, UUID currentTokenId) {
        // Delete from database
        refreshTokenRepository.deleteByUserIdAndIdNot(userId, currentTokenId);

        // Delete from Redis except current token
        String pattern = "refresh_token:" + userId + ":*";
        redisTemplate.keys(pattern).forEach(key -> {
            if (!key.endsWith(":" + currentTokenId.toString())) {
                redisTemplate.delete(key);
            }
        });

        logger.info("Revoked all refresh tokens except {} for user: {}", currentTokenId, userId);
    }

    /**
     * Invalidate all user sessions (called on token replay detection)
     */
    private void invalidateAllUserSessions(UUID userId) {
        // Delete all sessions
        sessionRepository.deleteByUserId(userId);

        // Delete all refresh tokens
        revokeAllUserTokens(userId);

        logger.warn("Invalidated all sessions and tokens for user: {} due to security incident", userId);
    }

    /**
     * Build Redis key for refresh token
     */
    private String buildRedisKey(UUID userId, UUID tokenId) {
        return String.format("refresh_token:%s:%s", userId, tokenId);
    }

    /**
     * Get refresh token from Redis cache
     */
    private RefreshToken getFromRedis(UUID tokenId) {
        // We need userId to build the key, so we'll try to find it in DB first
        // This is a limitation of our key structure - in production, consider using just tokenId as key
        return null; // Fallback to DB for simplicity
    }
}
