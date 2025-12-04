import { create } from 'zustand'
import type { Class } from '@/types/class.types'

interface ClassState {
  /** All classes loaded in the application */
  classes: Class[]
}

interface ClassActions {
  /** Set all classes */
  setClasses: (classes: Class[]) => void

  /** Get a class by ID */
  getClassById: (classId: string) => Class | undefined

  /** Get eligible destination classes for batch transfer */
  getEligibleDestinations: (sourceClassId: string) => Class[]

  /** Clear all classes */
  clearClasses: () => void
}

type ClassStore = ClassState & ClassActions

export const useClassStore = create<ClassStore>((set, get) => ({
  // State
  classes: [],

  // Actions
  setClasses: (classes) => {
    set({ classes })
  },

  getClassById: (classId) => {
    return get().classes.find((c) => c.id === classId)
  },

  getEligibleDestinations: (sourceClassId) => {
    const { classes, getClassById } = get()
    const sourceClass = getClassById(sourceClassId)

    if (!sourceClass) return []

    // Filter classes that are eligible for transfer:
    // 1. ACTIVE status
    // 2. Same grade level as source class
    // 3. Not the source class itself
    // 4. Has available capacity (studentCount < maxCapacity)
    return classes.filter((cls) => {
      // Must be active
      if (cls.status !== 'ACTIVE') return false

      // Must match grade level
      if (cls.grade !== sourceClass.grade) return false

      // Cannot transfer to same class
      if (cls.id === sourceClassId) return false

      // Must have available capacity
      const hasCapacity = cls.studentCount < cls.maxCapacity

      return hasCapacity
    })
  },

  clearClasses: () => {
    set({ classes: [] })
  },
}))
