package com.sms.SERVICENAME.security;  // TODO: Replace SERVICENAME with your service name

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Token Provider
 *
 * Purpose: Handles all JWT token operations (generation, validation, parsing)
 *
 * CUSTOMIZATION REQUIRED:
 * 1. Update package name: Replace 'SERVICENAME' with your service name
 * 2. (Optional) Customize claims in generateToken() method
 * 3. (Optional) Add custom extraction methods for your claims
 *
 * CONFIGURATION REQUIRED in application.yml:
 * jwt:
 *   secret: ${JWT_SECRET:your-256-bit-secret-key-here}
 *   expiration: 86400000  # 24 hours
 *
 * NO OTHER CHANGES NEEDED - This template works as-is after package rename
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final long EXPIRATION_MS = 86400000; // 24 hours

    /**
     * Generate JWT token with user ID and custom claims
     *
     * @param userId User's unique identifier
     * @param language User's preferred language
     * @return JWT token string
     */
    public String generateToken(UUID userId, String language) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_MS);
        String jti = UUID.randomUUID().toString();  // JWT ID (unique token identifier)

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
            .setSubject(userId.toString())          // Standard claim: subject (user ID)
            .setId(jti)                             // Standard claim: JWT ID
            .setIssuedAt(now)                       // Standard claim: issued at
            .setExpiration(expiryDate)              // Standard claim: expiration
            .claim("lang", language)                // Custom claim: language
            .claim("roles", new String[]{"TEACHER"})  // TODO: Customize roles as needed
            .signWith(key, SignatureAlgorithm.HS256)  // Sign with HMAC-SHA256
            .compact();
    }

    /**
     * Extract JWT ID from token
     *
     * @param token JWT token string
     * @return JWT ID
     */
    public String getJtiFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getId();
    }

    /**
     * Extract user ID from token
     *
     * @param token JWT token string
     * @return User ID
     */
    public UUID getUserIdFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String subject = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
        return UUID.fromString(subject);
    }

    /**
     * Validate JWT token
     *
     * Checks:
     * - Signature verification (token not tampered)
     * - Expiration (token not expired)
     * - Format (valid JWT structure)
     *
     * @param token JWT token string
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Note: Refresh tokens are NOT JWTs - they're UUID-based tokens stored in the database
    // Refresh token generation and management should be handled by a separate RefreshTokenService
}
