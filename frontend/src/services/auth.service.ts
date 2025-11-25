import api, { apiRequest } from '@/lib/api'
import type {
  AuthResponse,
  AuthUser,
  LoginRequest,
  RegisterRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest,
} from '@/types/auth.types'

/**
 * Auth service for authentication API calls
 */
export const authService = {
  /**
   * Login with email/phone and password
   * @param credentials - Login credentials (emailOrPhone, password)
   * @returns Promise with auth response including user data
   */
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    return apiRequest<AuthResponse>(
      api.post('/api/auth/login', credentials)
    )
  },

  /**
   * Get current authenticated user
   * Used for session validation on app load
   * @returns Promise with current user data
   */
  async getCurrentUser(): Promise<AuthUser> {
    return apiRequest<AuthUser>(api.get('/api/profile/me'))
  },

  /**
   * Register a new teacher account
   * @param data - Registration data (email, phoneNumber, password)
   * @returns Promise with auth response including user data
   */
  async register(data: RegisterRequest): Promise<AuthResponse> {
    return apiRequest<AuthResponse>(
      api.post('/api/auth/register', data)
    )
  },

  /**
   * Logout current user
   * Invalidates refresh token on the server
   * @param refreshToken - The refresh token to invalidate
   * @returns Promise that resolves on successful logout
   */
  async logout(refreshToken: string): Promise<void> {
    await api.post('/api/auth/logout', { refreshToken })
  },

  /**
   * Request password reset email
   * @param data - Email address for password reset
   * @returns Promise that resolves on successful request
   * Note: Always returns success for security (prevents email enumeration)
   */
  async forgotPassword(data: ForgotPasswordRequest): Promise<void> {
    await api.post('/api/auth/forgot-password', data)
  },

  /**
   * Reset password using token from email
   * @param data - Reset token and new password
   * @returns Promise that resolves on successful password reset
   */
  async resetPassword(data: ResetPasswordRequest): Promise<void> {
    await api.post('/api/auth/reset-password', data)
  },
}
