import { create } from 'zustand'
import type { StudentEnrollmentItem } from '@/types/class.types'

interface StudentSelectionState {
  /** Map of classId -> Set of selected student IDs */
  selectedStudentsByClass: Map<string, Set<string>>

  /** Map of classId -> Map of studentId -> StudentEnrollmentItem */
  studentDataByClass: Map<string, Map<string, StudentEnrollmentItem>>

  /** Toggle selection for a single student */
  toggleStudent: (classId: string, student: StudentEnrollmentItem) => void

  /** Toggle all students for a class (select all or deselect all) */
  toggleAll: (classId: string, students: StudentEnrollmentItem[]) => void

  /** Clear all selections for a specific class */
  clearSelection: (classId: string) => void

  /** Get selected students for a class */
  getSelectedStudents: (classId: string) => StudentEnrollmentItem[]

  /** Get count of selected students for a class */
  getSelectedCount: (classId: string) => number

  /** Check if a student is selected */
  isStudentSelected: (classId: string, studentId: string) => boolean

  /** Check if all students are selected for a class */
  areAllStudentsSelected: (classId: string, students: StudentEnrollmentItem[]) => boolean
}

export const useStudentSelectionStore = create<StudentSelectionState>((set, get) => ({
  selectedStudentsByClass: new Map(),
  studentDataByClass: new Map(),

  toggleStudent: (classId, student) => {
    set((state) => {
      const newSelectedByClass = new Map(state.selectedStudentsByClass)
      const newDataByClass = new Map(state.studentDataByClass)

      // Get or create selection set for this class
      const selectedIds = new Set<string>(newSelectedByClass.get(classId) || new Set<string>())

      // Get or create student data map for this class
      const studentData = new Map<string, StudentEnrollmentItem>(newDataByClass.get(classId) || new Map<string, StudentEnrollmentItem>())

      if (selectedIds.has(student.studentId)) {
        // Deselect
        selectedIds.delete(student.studentId)
        studentData.delete(student.studentId)
      } else {
        // Select
        selectedIds.add(student.studentId)
        studentData.set(student.studentId, student)
      }

      newSelectedByClass.set(classId, selectedIds)
      newDataByClass.set(classId, studentData)

      return {
        selectedStudentsByClass: newSelectedByClass,
        studentDataByClass: newDataByClass,
      }
    })
  },

  toggleAll: (classId, students) => {
    set((state) => {
      const newSelectedByClass = new Map(state.selectedStudentsByClass)
      const newDataByClass = new Map(state.studentDataByClass)

      const currentSelections = newSelectedByClass.get(classId) || new Set()
      const allSelected = students.every((s) => currentSelections.has(s.studentId))

      if (allSelected) {
        // Deselect all
        newSelectedByClass.set(classId, new Set())
        newDataByClass.set(classId, new Map())
      } else {
        // Select all
        const selectedIds = new Set(students.map((s) => s.studentId))
        const studentData = new Map(students.map((s) => [s.studentId, s]))
        newSelectedByClass.set(classId, selectedIds)
        newDataByClass.set(classId, studentData)
      }

      return {
        selectedStudentsByClass: newSelectedByClass,
        studentDataByClass: newDataByClass,
      }
    })
  },

  clearSelection: (classId) => {
    set((state) => {
      const newSelectedByClass = new Map(state.selectedStudentsByClass)
      const newDataByClass = new Map(state.studentDataByClass)

      newSelectedByClass.set(classId, new Set())
      newDataByClass.set(classId, new Map())

      return {
        selectedStudentsByClass: newSelectedByClass,
        studentDataByClass: newDataByClass,
      }
    })
  },

  getSelectedStudents: (classId) => {
    const studentData = get().studentDataByClass.get(classId)
    if (!studentData) return []
    return Array.from(studentData.values())
  },

  getSelectedCount: (classId) => {
    const selectedIds = get().selectedStudentsByClass.get(classId)
    return selectedIds?.size || 0
  },

  isStudentSelected: (classId, studentId) => {
    const selectedIds = get().selectedStudentsByClass.get(classId)
    return selectedIds?.has(studentId) ?? false
  },

  areAllStudentsSelected: (classId, students) => {
    const selectedIds = get().selectedStudentsByClass.get(classId)
    if (!selectedIds || students.length === 0) return false
    return students.every((s) => selectedIds.has(s.studentId))
  },
}))
