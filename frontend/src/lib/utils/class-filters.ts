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
 * Filter grade options based on selected level
 */
export function getFilteredGradeOptions(level?: ClassLevel) {
  if (!level) {
    return GRADE_OPTIONS
  }

  const range = GRADE_RANGES[level]
  return GRADE_OPTIONS.filter((option) => {
    const grade = Number(option.value)
    return grade >= range.min && grade <= range.max
  })
}

/**
 * Check if a grade is valid for a given level
 */
export function isGradeValidForLevel(grade: string | number, level: ClassLevel): boolean {
  const gradeNumber = typeof grade === 'string' ? Number(grade) : grade
  const range = GRADE_RANGES[level]
  return gradeNumber >= range.min && gradeNumber <= range.max
}
