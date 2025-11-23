package com.sms.auth.config;

/**
 * Security-related constants for auth-service.
 *
 * These are service-specific and should NOT be in CommonConstants.
 * Each microservice should define its own security policies.
 */
public final class SecurityProperties {

    private SecurityProperties() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ============================================
    // JWT TOKEN SETTINGS
    // ============================================

    /** JWT access token expiration: 24 hours */
    public static final int JWT_EXPIRATION_HOURS = 24;

    /** JWT access token expiration in milliseconds */
    public static final long JWT_EXPIRATION_MS = JWT_EXPIRATION_HOURS * 60 * 60 * 1000L;

    /** JWT refresh token expiration: 30 days */
    public static final int REFRESH_TOKEN_EXPIRATION_DAYS = 30;


    // ============================================
    // SESSION SETTINGS
    // ============================================

    /** Session timeout: 24 hours (same as JWT) */
    public static final int SESSION_TIMEOUT_HOURS = 24;

    /** Maximum concurrent sessions per user */
    public static final int MAX_CONCURRENT_SESSIONS = 5;


    // ============================================
    // PASSWORD VALIDATION
    // ============================================

    /** Minimum password length */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /** Maximum password length */
    public static final int MAX_PASSWORD_LENGTH = 128;


    // ============================================
    // RATE LIMITING
    // ============================================

    /** Maximum login attempts before account lock */
    public static final int MAX_LOGIN_ATTEMPTS = 5;

    /** Account lock duration in minutes */
    public static final int ACCOUNT_LOCK_DURATION_MINUTES = 15;

    /** Rate limit: Maximum API requests per minute per user */
    public static final int MAX_REQUESTS_PER_MINUTE = 60;


    // ============================================
    // CACHE TTL
    // ============================================

    /** Cache TTL for user profiles: 1 hour */
    public static final int CACHE_TTL_USER_PROFILE_SECONDS = 60 * 60;
}
