import api, { apiRequest } from '@/lib/api'
import type {
  AuthResponse,
  AuthUser,
  LoginRequest,
  RegisterRequest,
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
    return apiRequest<AuthUser>(api.get('/api/auth/me'))
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
}
