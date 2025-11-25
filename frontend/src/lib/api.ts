import axios from 'axios'
import { toast } from 'sonner'
import type { ApiResponse } from '@/types/api.types'
import { useAuthStore } from '@/store/auth-store'

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Required for HTTP-only cookies
})

// Flag to prevent multiple refresh attempts
let isRefreshing = false
let failedQueue: Array<{
  resolve: (value?: unknown) => void
  reject: (error?: unknown) => void
}> = []

const processQueue = (error: unknown | null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve()
    }
  })
  failedQueue = []
}

// Response interceptor to handle token refresh and network errors
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Handle network errors (no response)
    if (!error.response) {
      // Check if it's a network error
      if (error.message === 'Network Error' || error.code === 'ERR_NETWORK') {
        toast.error('Network error. Please check your connection and try again.')
      }
      return Promise.reject(error)
    }

    // Handle server errors (5xx)
    if (error.response?.status >= 500) {
      toast.error('Service temporarily unavailable. Please try again later.')
      return Promise.reject(error)
    }

    // Handle 401 errors by attempting token refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Skip refresh for auth endpoints to prevent loops
      const skipRefreshUrls = [
        '/api/auth/refresh',
        '/api/auth/login',
        '/api/auth/register'
      ]
      if (skipRefreshUrls.some(url => originalRequest.url?.includes(url))) {
        return Promise.reject(error)
      }

      if (isRefreshing) {
        // Queue the request while refresh is in progress
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(() => api(originalRequest))
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        // Attempt to refresh token (cookies sent automatically)
        await axios.post(`${API_URL}/api/auth/refresh`, {}, {
          withCredentials: true,
        })

        processQueue(null)

        // Retry original request after successful refresh
        return api(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError)

        // Refresh failed - reset auth state and redirect to login
        if (typeof window !== 'undefined') {
          // Reset auth store
          useAuthStore.getState().reset()

          // Only redirect if not already on an auth page
          const authPaths = ['/sign-in', '/sign-up', '/forgot-password', '/reset-password']
          const isOnAuthPage = authPaths.some(path => window.location.pathname.startsWith(path))

          if (!isOnAuthPage) {
            // Show session expired toast
            toast.error('Your session has expired. Please sign in again.')

            // Redirect to sign-in
            window.location.href = '/sign-in'
          }
        }

        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
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
