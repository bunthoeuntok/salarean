import { useState, useEffect, useMemo, useCallback } from 'react'
import type { ClassLevel } from '@/types/class.types'
import type { Class } from '@/types/class.types'
import { getFilteredGradeOptions, isGradeValidForLevel } from '@/lib/utils/class-filters'

interface UseClassFilteringOptions {
  /**
   * Initial level value (from URL params or form state)
   */
  initialLevel?: ClassLevel
  /**
   * Initial grade value (from URL params or form state)
   */
  initialGrade?: string
  /**
   * Available levels based on teacher's school type (restricts grade options)
   */
  availableLevels?: ClassLevel[]
  /**
   * Available classes to filter
   */
  classes?: Class[]
  /**
   * Include "No Class" option in class filter
   */
  includeNoClassOption?: boolean
  /**
   * Label for "No Class" option
   */
  noClassLabel?: string
  /**
   * Callback when level changes (for URL params sync)
   */
  onLevelChange?: (level?: ClassLevel) => void
  /**
   * Callback when grade changes (for URL params sync)
   */
  onGradeChange?: (grade?: string) => void
  /**
   * Callback when invalid grade is cleared (for form validation)
   */
  onGradeCleared?: () => void
}

interface UseClassFilteringReturn {
  // Current selections
  selectedLevel?: ClassLevel
  selectedGrade?: string

  // Filtered options
  filteredGradeOptions: Array<{ label: string; value: string; icon?: React.ComponentType<{ className?: string }> }>
  filteredClassOptions: Array<{ label: string; value: string }>

  // Handlers
  handleLevelChange: (values: string[]) => void
  handleGradeChange: (values: string[]) => void

  // Utilities
  isGradeValid: (grade: string) => boolean
}

/**
 * Custom hook for managing class filtering logic (level → grade → class).
 *
 * Handles:
 * - Grade filtering based on level selection
 * - Class filtering based on level and grade
 * - Auto-clearing invalid selections
 * - State synchronization
 *
 * @example
 * // In a filter toolbar
 * const { filteredGradeOptions, handleLevelChange } = useClassFiltering({
 *   initialLevel: filters.level?.[0],
 *   classes: classesData?.content,
 * })
 *
 * @example
 * // In a form modal
 * const { filteredGradeOptions, isGradeValid } = useClassFiltering({
 *   initialLevel: form.watch('level'),
 *   onGradeCleared: () => form.setValue('grade', ''),
 * })
 */
export function useClassFiltering(options: UseClassFilteringOptions = {}): UseClassFilteringReturn {
  const {
    initialLevel,
    initialGrade,
    availableLevels,
    classes = [],
    includeNoClassOption = false,
    noClassLabel = 'No Class',
    onLevelChange,
    onGradeChange,
    onGradeCleared,
  } = options

  const [selectedLevel, setSelectedLevel] = useState<ClassLevel | undefined>(initialLevel)
  const [selectedGrade, setSelectedGrade] = useState<string | undefined>(initialGrade)

  // Sync with external state changes (e.g., URL params or form state)
  useEffect(() => {
    setSelectedLevel(initialLevel)
  }, [initialLevel])

  useEffect(() => {
    setSelectedGrade(initialGrade)
  }, [initialGrade])

  // Filter grade options based on selected level and available levels
  const filteredGradeOptions = useMemo(() => {
    return getFilteredGradeOptions(selectedLevel, availableLevels)
  }, [selectedLevel, availableLevels])

  // Check if current grade is valid for selected level
  const isGradeValid = useCallback((grade: string): boolean => {
    if (!selectedLevel || !grade) return true
    return isGradeValidForLevel(grade, selectedLevel)
  }, [selectedLevel])

  // Auto-clear conflicting filters
  useEffect(() => {
    if (selectedGrade && selectedLevel) {
      if (!isGradeValid(selectedGrade)) {
        // Different behavior for modals vs filters:
        // - Modals: clear invalid grade (onGradeCleared provided)
        // - Filters: prioritize grade, clear level (onGradeCleared not provided)
        if (onGradeCleared) {
          // Modal use case: clear invalid grade
          setSelectedGrade(undefined)
          onGradeCleared()
        } else {
          // Filter use case: grade is more specific, clear level instead
          setSelectedLevel(undefined)
          onLevelChange?.(undefined)
        }
      }
    }
  }, [selectedLevel, selectedGrade, isGradeValid, onGradeCleared, onLevelChange])

  // Filter class options based on level and grade
  const filteredClassOptions = useMemo(() => {
    let filtered = [...classes]

    // If grade is selected, prioritize it (grade is more specific than level)
    if (selectedGrade) {
      const gradeNumber = Number(selectedGrade)
      filtered = filtered.filter((c) => c.grade == gradeNumber)
    } else if (selectedLevel) {
      // Only filter by level if no specific grade is selected
      filtered = filtered.filter((c) => c.level == selectedLevel)
    }

    const options = filtered.map((c) => ({
      label: `Grade ${c.grade}${c.section ? ` - ${c.section}` : ''}`,
      value: c.id,
    }))

    // Add "No Class" option if requested
    if (includeNoClassOption) {
      return [{ label: noClassLabel, value: 'NONE' }, ...options]
    }

    return options
  }, [classes, selectedLevel, selectedGrade, includeNoClassOption, noClassLabel])

  // Level change handler
  const handleLevelChange = useCallback((values: string[]) => {
    const newLevel = values.length > 0 ? (values[0] as ClassLevel) : undefined
    setSelectedLevel(newLevel)
    onLevelChange?.(newLevel)
  }, [onLevelChange])

  // Grade change handler
  const handleGradeChange = useCallback((values: string[]) => {
    const newGrade = values.length > 0 ? values[0] : undefined
    setSelectedGrade(newGrade)
    onGradeChange?.(newGrade)
  }, [onGradeChange])

  return {
    selectedLevel,
    selectedGrade,
    filteredGradeOptions,
    filteredClassOptions,
    handleLevelChange,
    handleGradeChange,
    isGradeValid,
  }
}
