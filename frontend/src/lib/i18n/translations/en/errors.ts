import type { ErrorCodeTranslations } from '../../types'

export const errorsEn: ErrorCodeTranslations = {
  // Success
  SUCCESS: 'Operation completed successfully',

  // Common validation errors
  VALIDATION_ERROR: 'Validation error',
  INVALID_INPUT: 'Invalid input',
  REQUIRED_FIELD_MISSING: 'Required field is missing',
  INVALID_DATE_FORMAT: 'Invalid date format',
  INVALID_PHONE_FORMAT: 'Invalid phone number format',
  INVALID_EMAIL_FORMAT: 'Invalid email format',

  // Authentication & Authorization
  UNAUTHORIZED: 'Please sign in to continue',
  FORBIDDEN: 'You do not have permission to access this resource',
  INVALID_TOKEN: 'Invalid session. Please sign in again',
  TOKEN_EXPIRED: 'Session expired. Please sign in again',
  TOKEN_REPLAY_DETECTED: 'Security error detected. Please sign in again',
  SESSION_EXPIRED: 'Your session has expired. Please sign in again',

  // Resource errors
  RESOURCE_NOT_FOUND: 'Resource not found',
  RESOURCE_ALREADY_EXISTS: 'Resource already exists',
  RESOURCE_DELETED: 'This resource has been deleted',

  // File upload errors
  FILE_SIZE_EXCEEDED: 'File size exceeds the maximum limit',
  INVALID_FILE_FORMAT: 'Invalid file format',
  FILE_UPLOAD_FAILED: 'File upload failed',
  CORRUPTED_FILE: 'File is corrupted or unreadable',

  // System errors
  INTERNAL_ERROR: 'An unexpected error occurred. Please try again',
  SERVICE_UNAVAILABLE: 'Service is temporarily unavailable',
  RATE_LIMIT_EXCEEDED: 'Too many requests. Please wait and try again',
  DATABASE_ERROR: 'Database error occurred',

  // Auth-specific errors
  INVALID_CREDENTIALS: 'Invalid email/phone or password',
  ACCOUNT_LOCKED: 'Account locked due to too many failed attempts',
  DUPLICATE_EMAIL: 'This email is already registered',
  DUPLICATE_PHONE: 'This phone number is already registered',
  INVALID_PASSWORD: 'Invalid password',
  WEAK_PASSWORD: 'Password does not meet requirements',
  PASSWORD_TOO_SHORT: 'Password must be at least 8 characters',
  PASSWORD_MISSING_UPPERCASE: 'Password must contain an uppercase letter',
  PASSWORD_MISSING_LOWERCASE: 'Password must contain a lowercase letter',
  PASSWORD_MISSING_DIGIT: 'Password must contain a digit',
  PASSWORD_MISSING_SPECIAL: 'Password must contain a special character',
  PASSWORD_TOO_COMMON: 'This password is too common',
  USER_NOT_FOUND: 'User not found',
  EMAIL_NOT_FOUND: 'Email not found',
  RESET_TOKEN_INVALID: 'Password reset link is invalid',
  RESET_TOKEN_EXPIRED: 'Password reset link has expired',
  PROFILE_UPDATE_FAILED: 'Failed to update profile',
  INVALID_LANGUAGE: 'Invalid language selection',
  PHOTO_SIZE_EXCEEDED: 'Photo size exceeds 5MB limit',
  INVALID_PHOTO_FORMAT: 'Photo must be JPEG, PNG, or WebP',
  PHOTO_UPLOAD_FAILED: 'Failed to upload photo',
  CORRUPTED_IMAGE: 'Image file is corrupted',
}
