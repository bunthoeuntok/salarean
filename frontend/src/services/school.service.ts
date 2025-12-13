import api, { apiRequest } from '@/lib/api'
import type { ApiResponse } from '@/types/api.types'
import type { TeacherSchoolFormData } from '@/lib/validations/school-setup'

/**
 * Teacher-school association response from API
 */
export interface TeacherSchoolResponse {
  id: string
  userId: string
  schoolId: string
  schoolName: string
  principalName: string
  principalGender: 'M' | 'F'
  createdAt: string
  updatedAt: string
}

// Cache for teacher-school data to avoid repeated API calls
let teacherSchoolCache: {
  data: TeacherSchoolResponse | null
  timestamp: number
  fetched: boolean
} | null = null

const CACHE_DURATION_MS = 5 * 60 * 1000 // 5 minutes

/**
 * Check if cache is valid
 */
function isCacheValid(): boolean {
  if (!teacherSchoolCache) return false
  return Date.now() - teacherSchoolCache.timestamp < CACHE_DURATION_MS
}

/**
 * Invalidate the cache (call after creating/updating)
 */
export function invalidateTeacherSchoolCache(): void {
  teacherSchoolCache = null
}

/**
 * School service for teacher-school association API calls
 */
export const schoolService = {
  /**
   * Create or update teacher-school association
   */
  async createTeacherSchool(data: TeacherSchoolFormData): Promise<TeacherSchoolResponse> {
    const result = await apiRequest<TeacherSchoolResponse>(
      api.post('/api/teacher-school', data)
    )
    // Invalidate cache after creating/updating
    invalidateTeacherSchoolCache()
    return result
  },

  /**
   * Fetch teacher-school association for authenticated user
   * Returns null if no association exists (teacher hasn't completed setup)
   * Uses in-memory cache to avoid repeated API calls
   */
  async getTeacherSchool(): Promise<TeacherSchoolResponse | null> {
    // Return cached data if valid
    if (isCacheValid() && teacherSchoolCache) {
      return teacherSchoolCache.data
    }

    try {
      const response = await api.get<ApiResponse<TeacherSchoolResponse>>(
        '/api/teacher-school'
      )

      // Check if API returned RESOURCE_NOT_FOUND error code
      if (response.data.errorCode === 'RESOURCE_NOT_FOUND') {
        // Cache the "not found" result
        teacherSchoolCache = {
          data: null,
          timestamp: Date.now(),
          fetched: true,
        }
        return null
      }

      // Cache the successful result
      teacherSchoolCache = {
        data: response.data.data,
        timestamp: Date.now(),
        fetched: true,
      }

      return response.data.data
    } catch (error: unknown) {
      // Return null if 404 (no association exists)
      if (
        error &&
        typeof error === 'object' &&
        'response' in error &&
        (error as { response?: { status?: number } }).response?.status === 404
      ) {
        // Cache the "not found" result
        teacherSchoolCache = {
          data: null,
          timestamp: Date.now(),
          fetched: true,
        }
        return null
      }
      throw error
    }
  },
}

// Named exports for backward compatibility
export const createTeacherSchool = schoolService.createTeacherSchool
export const fetchTeacherSchool = schoolService.getTeacherSchool
