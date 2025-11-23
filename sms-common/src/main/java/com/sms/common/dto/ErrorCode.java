package com.sms.common.dto;

/**
 * Common error codes shared across all Salarean SMS microservices.
 *
 * Error codes are machine-readable identifiers used for:
 * - Client-side i18n (mapping to Khmer/English messages)
 * - Consistent error handling across services
 * - API contract stability
 *
 * Naming Convention: UPPER_SNAKE_CASE, self-documenting
 * Example: PASSWORD_TOO_SHORT (not ERR_001)
 *
 * Service-Specific Error Codes:
 * - Each service can define additional error codes in their own enums
 * - Example: AuthErrorCode, StudentErrorCode, etc.
 * - Common codes defined here ensure consistency
 *
 * Frontend Responsibility:
 * - Map error codes to localized messages
 * - Handle all UI text translations (English/Khmer)
 */
public enum ErrorCode {

    // ============================================
    // SUCCESS
    // ============================================

    /** Operation completed successfully */
    SUCCESS,


    // ============================================
    // VALIDATION ERRORS
    // ============================================

    /** Generic validation error */
    VALIDATION_ERROR,

    /** Input data is invalid */
    INVALID_INPUT,

    /** Required field is missing */
    REQUIRED_FIELD_MISSING,

    /** Invalid date format */
    INVALID_DATE_FORMAT,

    /** Invalid phone number format */
    INVALID_PHONE_FORMAT,

    /** Invalid email format */
    INVALID_EMAIL_FORMAT,


    // ============================================
    // AUTHENTICATION & AUTHORIZATION
    // ============================================

    /** User is not authenticated */
    UNAUTHORIZED,

    /** User does not have permission */
    FORBIDDEN,

    /** Invalid JWT token */
    INVALID_TOKEN,

    /** JWT token has expired */
    TOKEN_EXPIRED,

    /** Session has expired */
    SESSION_EXPIRED,

    /** Invalid credentials (wrong password) */
    INVALID_CREDENTIALS,


    // ============================================
    // RESOURCE ERRORS
    // ============================================

    /** Requested resource not found */
    RESOURCE_NOT_FOUND,

    /** Resource already exists (duplicate) */
    RESOURCE_ALREADY_EXISTS,

    /** Resource has been deleted */
    RESOURCE_DELETED,


    // ============================================
    // FILE UPLOAD ERRORS
    // ============================================

    /** File size exceeds maximum allowed */
    FILE_SIZE_EXCEEDED,

    /** Photo size exceeds maximum (5MB) */
    PHOTO_SIZE_EXCEEDED,

    /** Invalid file format */
    INVALID_FILE_FORMAT,

    /** Invalid photo format (not JPEG/PNG) */
    INVALID_PHOTO_FORMAT,

    /** File upload failed */
    FILE_UPLOAD_FAILED,

    /** Photo upload failed */
    PHOTO_UPLOAD_FAILED,

    /** Corrupted file/image */
    CORRUPTED_FILE,

    /** Corrupted image data */
    CORRUPTED_IMAGE,


    // ============================================
    // SYSTEM ERRORS
    // ============================================

    /** Internal server error */
    INTERNAL_ERROR,

    /** Service is temporarily unavailable */
    SERVICE_UNAVAILABLE,

    /** Rate limit exceeded */
    RATE_LIMIT_EXCEEDED,

    /** Database error */
    DATABASE_ERROR,


    // ============================================
    // NOTES FOR SERVICE-SPECIFIC ERROR CODES
    // ============================================

    /*
     * Each service should define its own service-specific error codes
     * in separate enums. Examples:
     *
     * AuthErrorCode (auth-service):
     * - DUPLICATE_EMAIL
     * - DUPLICATE_PHONE
     * - WEAK_PASSWORD
     * - PASSWORD_TOO_SHORT
     * - ACCOUNT_LOCKED
     * - RESET_TOKEN_INVALID
     *
     * StudentErrorCode (student-service):
     * - STUDENT_NOT_FOUND
     * - DUPLICATE_STUDENT_CODE
     * - INVALID_ENROLLMENT_DATE
     * - CLASS_CAPACITY_EXCEEDED
     *
     * This approach:
     * - Keeps common codes in sms-common
     * - Allows service autonomy for specific codes
     * - Prevents coupling between services
     */
}
