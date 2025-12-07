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
 * Example: INVALID_INPUT (not ERR_001)
 *
 * Service-Specific Error Codes:
 * Each service SHOULD define its own service-specific error codes in separate enums.
 *
 * Examples:
 * - AuthErrorCode (auth-service) - DUPLICATE_EMAIL, WEAK_PASSWORD, ACCOUNT_LOCKED, etc.
 * - StudentErrorCode (student-service) - DUPLICATE_STUDENT_CODE, INVALID_ENROLLMENT_DATE, etc.
 * - AttendanceErrorCode (attendance-service) - ALREADY_CHECKED_IN, INVALID_ATTENDANCE_DATE, etc.
 *
 * The "3-Service Rule":
 * Only add error codes here if they are used by 3 or more services.
 * Otherwise, keep them service-specific.
 *
 * Frontend Responsibility:
 * - Map error codes to localized messages (English/Khmer)
 * - Handle all UI text translations
 */
public enum ErrorCode {

    // ============================================
    // SUCCESS
    // ============================================

    /** Operation completed successfully */
    SUCCESS,


    // ============================================
    // VALIDATION ERRORS (Generic)
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
    // AUTHENTICATION & AUTHORIZATION (Common)
    // ============================================

    /** User is not authenticated (no valid token) */
    UNAUTHORIZED,

    /** User does not have permission to access resource */
    FORBIDDEN,

    /** JWT token is invalid or malformed */
    INVALID_TOKEN,

    /** JWT token has expired */
    TOKEN_EXPIRED,

    /** Token replay attack detected (token reused) */
    TOKEN_REPLAY_DETECTED,

    /** User session has expired */
    SESSION_EXPIRED,

    /** User is not authorized to access this specific resource (ownership violation) */
    UNAUTHORIZED_ACCESS,

    /** Teacher context is missing (authentication succeeded but teacher ID not set) */
    TEACHER_CONTEXT_MISSING,


    // ============================================
    // RESOURCE ERRORS (Generic)
    // ============================================

    /** Requested resource not found */
    RESOURCE_NOT_FOUND,

    /** Resource already exists (duplicate key/identifier) */
    RESOURCE_ALREADY_EXISTS,

    /** Resource has been soft-deleted */
    RESOURCE_DELETED,


    // ============================================
    // FILE UPLOAD ERRORS (Generic)
    // ============================================

    /** File size exceeds maximum allowed limit */
    FILE_SIZE_EXCEEDED,

    /** Invalid file format or extension */
    INVALID_FILE_FORMAT,

    /** File upload operation failed */
    FILE_UPLOAD_FAILED,

    /** File data is corrupted or unreadable */
    CORRUPTED_FILE,


    // ============================================
    // SYSTEM ERRORS (Common)
    // ============================================

    /** Internal server error (unexpected exception) */
    INTERNAL_ERROR,

    /** Service is temporarily unavailable */
    SERVICE_UNAVAILABLE,

    /** Rate limit exceeded (too many requests) */
    RATE_LIMIT_EXCEEDED,

    /** Database operation failed */
    DATABASE_ERROR
}
