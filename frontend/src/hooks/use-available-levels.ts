import { useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { teacherSchoolQueryOptions, type SchoolType } from '@/services/school.service'
import type { ClassLevel } from '@/types/class.types'
import { GRADE_RANGES } from '@/lib/utils/class-filters'

/**
 * Maps school type to available class levels
 * - PRIMARY school → only PRIMARY classes
 * - SECONDARY school → only SECONDARY classes
 * - HIGH_SCHOOL school → only HIGH_SCHOOL classes
 * - VOCATIONAL or undefined → all standard class levels (fallback)
 */
export const getAvailableLevelsForSchoolType = (schoolType: SchoolType | undefined): ClassLevel[] => {
  switch (schoolType) {
    case 'PRIMARY':
      return ['PRIMARY']
    case 'SECONDARY':
      return ['SECONDARY']
    case 'HIGH_SCHOOL':
      return ['HIGH_SCHOOL']
    default:
      return ['PRIMARY', 'SECONDARY', 'HIGH_SCHOOL']
  }
}

/**
 * Hook to get available class levels based on the teacher's school type.
 * Queries the teacher-school association and returns the appropriate levels.
 */
export function useAvailableLevels() {
  const { data: teacherSchool, isLoading } = useQuery(teacherSchoolQueryOptions)

  const availableLevels = useMemo(
    () => getAvailableLevelsForSchoolType(teacherSchool?.schoolType),
    [teacherSchool?.schoolType]
  )

  // Compute available grade numbers based on available levels
  const availableGrades = useMemo(() => {
    return availableLevels.flatMap((level) => {
      const range = GRADE_RANGES[level]
      return Array.from({ length: range.max - range.min + 1 }, (_, i) => range.min + i)
    })
  }, [availableLevels])

  const defaultLevel = availableLevels[0] || 'PRIMARY'

  return {
    availableLevels,
    availableGrades,
    defaultLevel,
    isLoading,
    schoolType: teacherSchool?.schoolType,
  }
}
