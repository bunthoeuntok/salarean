/**
 * Common error codes shared across all Salarean SMS microservices.
 * Maps to: sms-common/src/main/java/com/sms/common/dto/ErrorCode.java
 */
export type CommonErrorCode =
  // Success
  | 'SUCCESS'
  // Validation Errors
  | 'VALIDATION_ERROR'
  | 'INVALID_INPUT'
  | 'REQUIRED_FIELD_MISSING'
  | 'INVALID_DATE_FORMAT'
  | 'INVALID_PHONE_FORMAT'
  | 'INVALID_EMAIL_FORMAT'
  // Authentication & Authorization
  | 'UNAUTHORIZED'
  | 'FORBIDDEN'
  | 'INVALID_TOKEN'
  | 'TOKEN_EXPIRED'
  | 'TOKEN_REPLAY_DETECTED'
  | 'SESSION_EXPIRED'
  // Resource Errors
  | 'RESOURCE_NOT_FOUND'
  | 'RESOURCE_ALREADY_EXISTS'
  | 'RESOURCE_DELETED'
  // File Upload Errors
  | 'FILE_SIZE_EXCEEDED'
  | 'INVALID_FILE_FORMAT'
  | 'FILE_UPLOAD_FAILED'
  | 'CORRUPTED_FILE'
  // System Errors
  | 'INTERNAL_ERROR'
  | 'SERVICE_UNAVAILABLE'
  | 'RATE_LIMIT_EXCEEDED'
  | 'DATABASE_ERROR'

/**
 * Auth-service specific error codes.
 * Maps to: auth-service/src/main/java/com/sms/auth/dto/AuthErrorCode.java
 */
export type AuthErrorCode =
  // Authentication & Login
  | 'INVALID_CREDENTIALS'
  | 'ACCOUNT_LOCKED'
  // Registration & Validation
  | 'DUPLICATE_EMAIL'
  | 'DUPLICATE_PHONE'
  // Password Validation
  | 'INVALID_PASSWORD'
  | 'WEAK_PASSWORD'
  | 'PASSWORD_TOO_SHORT'
  | 'PASSWORD_MISSING_UPPERCASE'
  | 'PASSWORD_MISSING_LOWERCASE'
  | 'PASSWORD_MISSING_DIGIT'
  | 'PASSWORD_MISSING_SPECIAL'
  | 'PASSWORD_TOO_COMMON'
  // User Management
  | 'USER_NOT_FOUND'
  | 'EMAIL_NOT_FOUND'
  // Password Reset
  | 'RESET_TOKEN_INVALID'
  | 'RESET_TOKEN_EXPIRED'
  // Profile Management
  | 'PROFILE_UPDATE_FAILED'
  | 'INVALID_LANGUAGE'
  // Photo Upload
  | 'PHOTO_SIZE_EXCEEDED'
  | 'INVALID_PHOTO_FORMAT'
  | 'PHOTO_UPLOAD_FAILED'
  | 'CORRUPTED_IMAGE'

/**
 * Frontend-specific error codes (not returned from backend)
 */
export type FrontendErrorCode =
  | 'NETWORK_ERROR' // Client has no network connection

/**
 * Union of all error codes that can be returned from API
 */
export type ErrorCode = CommonErrorCode | AuthErrorCode | FrontendErrorCode
