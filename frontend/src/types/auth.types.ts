/**
 * Authenticated user profile from backend
 */
export interface AuthUser {
  userId: string // UUID
  email: string
  phoneNumber: string
  preferredLanguage: 'en' | 'km'
  createdAt: string // ISO datetime
  lastLoginAt: string // ISO datetime
}

/**
 * Login request payload
 */
export interface LoginRequest {
  emailOrPhone: string
  password: string
}

/**
 * Registration request payload
 */
export interface RegisterRequest {
  email: string
  phoneNumber: string
  password: string
  preferredLanguage?: 'en' | 'km' // defaults to 'en'
}

/**
 * Auth response from login/register (without tokens - they're in cookies)
 */
export interface AuthResponse {
  userId: string
  email: string
  phoneNumber: string
  preferredLanguage: 'en' | 'km'
  expiresIn: number // Access token expiry in seconds
  createdAt: string
  lastLoginAt: string
}

/**
 * Forgot password request
 */
export interface ForgotPasswordRequest {
  email: string
}

/**
 * Reset password request
 */
export interface ResetPasswordRequest {
  token: string
  newPassword: string
}

/**
 * Change password request (authenticated)
 */
export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}
