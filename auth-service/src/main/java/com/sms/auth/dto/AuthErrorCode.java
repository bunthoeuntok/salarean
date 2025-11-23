package com.sms.auth.dto;

/**
 * Auth-service specific error codes.
 *
 * These error codes are specific to authentication, authorization,
 * user registration, and profile management functionality.
 *
 * For common error codes shared across all services, see:
 * {@link com.sms.common.dto.ErrorCode}
 *
 * Naming Convention: UPPER_SNAKE_CASE, self-documenting
 * Example: DUPLICATE_EMAIL (not ERR_001)
 *
 * Frontend Responsibility:
 * - Map error codes to localized messages (English/Khmer)
 * - Handle all UI text translations
 */
public enum AuthErrorCode {

    // ============================================
    // AUTHENTICATION & LOGIN
    // ============================================

    /** Invalid credentials (wrong email/phone or password) */
    INVALID_CREDENTIALS,

    /** Account is locked due to too many failed login attempts */
    ACCOUNT_LOCKED,


    // ============================================
    // REGISTRATION & VALIDATION
    // ============================================

    /** Email address already registered */
    DUPLICATE_EMAIL,

    /** Phone number already registered */
    DUPLICATE_PHONE,


    // ============================================
    // PASSWORD VALIDATION
    // ============================================

    /** Generic password validation error */
    INVALID_PASSWORD,

    /** Password does not meet strength requirements */
    WEAK_PASSWORD,

    /** Password is shorter than minimum length (8 characters) */
    PASSWORD_TOO_SHORT,

    /** Password missing uppercase letter */
    PASSWORD_MISSING_UPPERCASE,

    /** Password missing lowercase letter */
    PASSWORD_MISSING_LOWERCASE,

    /** Password missing digit (0-9) */
    PASSWORD_MISSING_DIGIT,

    /** Password missing special character (@#$%^&+=!*()_-) */
    PASSWORD_MISSING_SPECIAL,

    /** Password is too common (in blacklist) */
    PASSWORD_TOO_COMMON,


    // ============================================
    // USER MANAGEMENT
    // ============================================

    /** User account not found by ID */
    USER_NOT_FOUND,

    /** Email address not found in system */
    EMAIL_NOT_FOUND,


    // ============================================
    // PASSWORD RESET
    // ============================================

    /** Password reset token is invalid or not found */
    RESET_TOKEN_INVALID,

    /** Password reset token has expired */
    RESET_TOKEN_EXPIRED,


    // ============================================
    // PROFILE MANAGEMENT
    // ============================================

    /** Profile update operation failed */
    PROFILE_UPDATE_FAILED,

    /** Invalid language code (must be 'en' or 'km') */
    INVALID_LANGUAGE,


    // ============================================
    // PHOTO UPLOAD
    // ============================================

    /** Photo file size exceeds maximum (5MB) */
    PHOTO_SIZE_EXCEEDED,

    /** Invalid photo format (must be JPEG, PNG, or WebP) */
    INVALID_PHOTO_FORMAT,

    /** Photo upload operation failed */
    PHOTO_UPLOAD_FAILED,

    /** Photo file is corrupted or invalid */
    CORRUPTED_IMAGE
}
