import { create } from 'zustand'
import type { StudentEnrollmentItem } from '@/types/class.types'

interface StudentSelectionState {
  selectedStudentsByClass: Map<string, Set<string>>
  studentDataByClass: Map<string, Map<string, StudentEnrollmentItem>>
  toggleStudent: (classId: string, student: StudentEnrollmentItem) => void
  toggleAll: (classId: string, students: StudentEnrollmentItem[]) => void
  clearSelection: (classId: string) => void
  getSelectedStudents: (classId: string) => StudentEnrollmentItem[]
  getSelectedCount: (classId: string) => number
  isStudentSelected: (classId: string, studentId: string) => boolean
  areAllStudentsSelected: (classId: string, students: StudentEnrollmentItem[]) => boolean
}

export const useStudentSelectionStore = create<StudentSelectionState>((set, get) => ({
  selectedStudentsByClass: new Map(),
  studentDataByClass: new Map(),

  toggleStudent: (classId, student) => {
    set((state) => {
      const newSelectedByClass = new Map(state.selectedStudentsByClass)
      const newDataByClass = new Map(state.studentDataByClass)
      const selectedIds = new Set<string>(newSelectedByClass.get(classId) || new Set<string>())
      const studentData = new Map<string, StudentEnrollmentItem>(newDataByClass.get(classId) || new Map<string, StudentEnrollmentItem>())

      if (selectedIds.has(student.studentId)) {
        selectedIds.delete(student.studentId)
        studentData.delete(student.studentId)
      } else {
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
        newSelectedByClass.set(classId, new Set())
        newDataByClass.set(classId, new Map())
      } else {
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
