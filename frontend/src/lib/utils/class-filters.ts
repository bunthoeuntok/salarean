import type { ClassLevel } from '@/types/class.types'
import { GraduationCap } from 'lucide-react'

/**
 * Grade ranges for each class level
 */
export const GRADE_RANGES: Record<ClassLevel, { min: number; max: number }> = {
  PRIMARY: { min: 1, max: 6 },
  SECONDARY: { min: 7, max: 9 },
  HIGH_SCHOOL: { min: 10, max: 12 },
}

/**
 * All grade options (1-12)
 */
export const GRADE_OPTIONS = Array.from({ length: 12 }, (_, i) => ({
  label: `Grade ${i + 1}`,
  value: String(i + 1),
  icon: GraduationCap,
}))

/**
 * Filter grade options based on selected level and/or available levels
 * @param level - Currently selected level (filters to specific level's grades)
 * @param availableLevels - Available levels based on school type (restricts options when no level selected)
 */
export function getFilteredGradeOptions(level?: ClassLevel, availableLevels?: ClassLevel[]) {
  if (level) {
    // If specific level selected, show only that level's grades
    const range = GRADE_RANGES[level]
    return GRADE_OPTIONS.filter((option) => {
      const grade = Number(option.value)
      return grade >= range.min && grade <= range.max
    })
  }

  if (availableLevels && availableLevels.length > 0) {
    // If no level selected but availableLevels provided, show grades for all available levels
    return GRADE_OPTIONS.filter((option) => {
      const grade = Number(option.value)
      return availableLevels.some((lvl) => {
        const range = GRADE_RANGES[lvl]
        return grade >= range.min && grade <= range.max
      })
    })
  }

  // Fallback: show all grades
  return GRADE_OPTIONS
}

/**
 * Check if a grade is valid for a given level
 */
export function isGradeValidForLevel(grade: string | number, level: ClassLevel): boolean {
  const gradeNumber = typeof grade === 'string' ? Number(grade) : grade
  const range = GRADE_RANGES[level]
  return gradeNumber >= range.min && gradeNumber <= range.max
}
