import axios from 'axios'
import type { ApiResponse } from '@/types/api.types'

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Required for HTTP-only cookies
})

// Response interceptor to handle token refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Handle 401 errors by attempting token refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        // Attempt to refresh token (cookies sent automatically)
        await axios.post(`${API_URL}/api/auth/refresh`, {}, {
          withCredentials: true,
        })

        // Retry original request
        return api(originalRequest)
      } catch {
        // Refresh failed, redirect to login
        if (typeof window !== 'undefined') {
          window.location.href = '/sign-in'
        }
      }
    }

    return Promise.reject(error)
  }
)

/**
 * Type-safe API request helper
 */
export async function apiRequest<T>(
  request: Promise<{ data: ApiResponse<T> }>
): Promise<T> {
  const response = await request

  if (response.data.errorCode !== 'SUCCESS') {
    throw new Error(response.data.errorCode)
  }

  return response.data.data as T
}

export default api
