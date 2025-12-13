import { queryOptions } from '@tanstack/react-query'
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

/**
 * Query keys for teacher-school queries
 */
export const teacherSchoolKeys = {
  all: ['teacher-school'] as const,
  detail: () => [...teacherSchoolKeys.all, 'detail'] as const,
}

/**
 * Fetch teacher-school association for authenticated user
 * Returns null if no association exists (teacher hasn't completed setup)
 */
async function fetchTeacherSchool(): Promise<TeacherSchoolResponse | null> {
  try {
    const response = await api.get<ApiResponse<TeacherSchoolResponse>>(
      '/api/teacher-school'
    )

    // Check if API returned RESOURCE_NOT_FOUND error code
    if (response.data.errorCode === 'RESOURCE_NOT_FOUND') {
      return null
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
      return null
    }
    throw error
  }
}

/**
 * Query options for teacher-school data
 * Used by both useQuery hook and router beforeLoad
 */
export const teacherSchoolQueryOptions = queryOptions({
  queryKey: teacherSchoolKeys.detail(),
  queryFn: fetchTeacherSchool,
  staleTime: 5 * 60 * 1000, // 5 minutes
  gcTime: 10 * 60 * 1000, // 10 minutes garbage collection
})

/**
 * School service for teacher-school association API calls
 */
export const schoolService = {
  /**
   * Create or update teacher-school association
   */
  async createTeacherSchool(
    data: TeacherSchoolFormData
  ): Promise<TeacherSchoolResponse> {
    const result = await apiRequest<TeacherSchoolResponse>(
      api.post('/api/teacher-school', data)
    )
    return result
  },

  /**
   * Fetch teacher-school association
   * @deprecated Use teacherSchoolQueryOptions with useQuery or queryClient.ensureQueryData
   */
  getTeacherSchool: fetchTeacherSchool,
}

// Named exports for backward compatibility
export const createTeacherSchool = schoolService.createTeacherSchool
