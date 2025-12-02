import { useQuery } from '@tanstack/react-query'
import { classService } from '@/services/class.service'
import type {
  EnrollmentStatus,
  StudentEnrollmentListResponse,
} from '@/types/class.types'

/**
 * Query key factory for class students
 */
export const classStudentsKeys = {
  all: ['classStudents'] as const,
  byClass: (classId: string) => [...classStudentsKeys.all, classId] as const,
  byClassWithFilters: (classId: string, status?: EnrollmentStatus, sort?: string) =>
    [...classStudentsKeys.byClass(classId), { status, sort }] as const,
}

interface UseClassStudentsOptions {
  /**
   * Class ID to fetch students for
   */
  classId: string
  /**
   * Optional status filter (ACTIVE, COMPLETED, TRANSFERRED, WITHDRAWN)
   */
  status?: EnrollmentStatus
  /**
   * Sort field and direction (e.g., "studentName,asc")
   */
  sort?: string
  /**
   * Whether to enable the query
   */
  enabled?: boolean
}

/**
 * TanStack Query hook for fetching students enrolled in a class.
 *
 * Features:
 * - Server-side status filtering
 * - Client-side search filtering (via TanStack Table)
 * - Automatic refetching on filter changes
 * - Cache management with query keys
 *
 * @example
 * ```tsx
 * const { data, isLoading, error } = useClassStudents({
 *   classId: 'abc-123',
 *   status: 'ACTIVE',
 *   sort: 'studentName,asc',
 * })
 *
 * // Access students
 * const students = data?.students ?? []
 * const totalCount = data?.totalCount ?? 0
 * ```
 */
export function useClassStudents({
  classId,
  status,
  sort = 'studentName,asc',
  enabled = true,
}: UseClassStudentsOptions) {
  return useQuery<StudentEnrollmentListResponse>({
    queryKey: classStudentsKeys.byClassWithFilters(classId, status, sort),
    queryFn: () =>
      classService.getClassStudents({
        classId,
        status,
        sort,
      }),
    enabled: enabled && !!classId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}
