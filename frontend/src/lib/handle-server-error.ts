import { AxiosError } from 'axios'
import type { ApiResponse } from '@/types/api.types'
import type { ErrorCode } from '@/types/error-codes.types'
import type { Language } from './i18n/types'
import { getErrorMessage } from './i18n'

/**
 * Extract error code from various error types
 */
export function getErrorCode(error: unknown): ErrorCode {
  // Handle Axios errors with API response format
  if (error instanceof AxiosError) {
    // Check for network errors first
    if (!error.response) {
      if (error.message === 'Network Error' || error.code === 'ERR_NETWORK') {
        return 'NETWORK_ERROR'
      }
      return 'SERVICE_UNAVAILABLE'
    }

    const data = error.response?.data as ApiResponse<unknown> | undefined
    if (data?.errorCode) {
      return data.errorCode as ErrorCode
    }

    // Handle common HTTP status codes
    switch (error.response?.status) {
      case 401:
        return 'UNAUTHORIZED'
      case 403:
        return 'FORBIDDEN'
      case 404:
        return 'RESOURCE_NOT_FOUND'
      case 429:
        return 'RATE_LIMIT_EXCEEDED'
      case 500:
      case 502:
      case 503:
        return 'SERVICE_UNAVAILABLE'
      default:
        return 'INTERNAL_ERROR'
    }
  }

  // Handle Error with error code as message
  if (error instanceof Error) {
    // Check if message is a valid error code
    const code = error.message as ErrorCode
    if (isValidErrorCode(code)) {
      return code
    }
  }

  return 'INTERNAL_ERROR'
}

/**
 * Check if a string is a valid error code
 */
function isValidErrorCode(code: string): code is ErrorCode {
  const validCodes: ErrorCode[] = [
    'SUCCESS',
    'VALIDATION_ERROR',
    'INVALID_INPUT',
    'REQUIRED_FIELD_MISSING',
    'INVALID_DATE_FORMAT',
    'INVALID_PHONE_FORMAT',
    'INVALID_EMAIL_FORMAT',
    'UNAUTHORIZED',
    'FORBIDDEN',
    'INVALID_TOKEN',
    'TOKEN_EXPIRED',
    'TOKEN_REPLAY_DETECTED',
    'SESSION_EXPIRED',
    'RESOURCE_NOT_FOUND',
    'RESOURCE_ALREADY_EXISTS',
    'RESOURCE_DELETED',
    'FILE_SIZE_EXCEEDED',
    'INVALID_FILE_FORMAT',
    'FILE_UPLOAD_FAILED',
    'CORRUPTED_FILE',
    'INTERNAL_ERROR',
    'SERVICE_UNAVAILABLE',
    'RATE_LIMIT_EXCEEDED',
    'DATABASE_ERROR',
    'INVALID_CREDENTIALS',
    'ACCOUNT_LOCKED',
    'DUPLICATE_EMAIL',
    'DUPLICATE_PHONE',
    'INVALID_PASSWORD',
    'WEAK_PASSWORD',
    'PASSWORD_TOO_SHORT',
    'PASSWORD_MISSING_UPPERCASE',
    'PASSWORD_MISSING_LOWERCASE',
    'PASSWORD_MISSING_DIGIT',
    'PASSWORD_MISSING_SPECIAL',
    'PASSWORD_TOO_COMMON',
    'USER_NOT_FOUND',
    'EMAIL_NOT_FOUND',
    'RESET_TOKEN_INVALID',
    'RESET_TOKEN_EXPIRED',
    'PROFILE_UPDATE_FAILED',
    'INVALID_LANGUAGE',
    'PHOTO_SIZE_EXCEEDED',
    'INVALID_PHOTO_FORMAT',
    'PHOTO_UPLOAD_FAILED',
    'CORRUPTED_IMAGE',
    // Frontend-specific errors
    'NETWORK_ERROR',
  ]
  return validCodes.includes(code as ErrorCode)
}

/**
 * Handle server error and return localized message
 */
export function handleServerError(error: unknown, lang: Language): string {
  const errorCode = getErrorCode(error)
  return getErrorMessage(errorCode, lang)
}

/**
 * Check if error is an authentication error
 */
export function isAuthError(error: unknown): boolean {
  const code = getErrorCode(error)
  return ['UNAUTHORIZED', 'INVALID_TOKEN', 'TOKEN_EXPIRED', 'SESSION_EXPIRED'].includes(code)
}
