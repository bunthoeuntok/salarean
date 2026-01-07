/**
 * Types for semester configuration
 */

export interface ExamScheduleItem {
  assessmentCode: string
  title: string
  displayOrder: number
}

export interface SemesterConfig {
  id: string
  teacherId: string | null
  academicYear: string
  semesterExamCode: string
  examSchedule: ExamScheduleItem[]
  monthlyExamCount: number
  isDefault: boolean
  createdAt: string
  updatedAt: string
}

export interface SemesterConfigRequest {
  academicYear: string
  semesterExamCode: string
  examSchedule: ExamScheduleItem[]
}


// Assessment type display names
export const ASSESSMENT_NAMES: Record<string, { en: string; km: string }> = {
  MONTHLY_1: { en: 'Monthly Exam 1', km: 'ប្រឡងប្រចាំខែ ១' },
  MONTHLY_2: { en: 'Monthly Exam 2', km: 'ប្រឡងប្រចាំខែ ២' },
  MONTHLY_3: { en: 'Monthly Exam 3', km: 'ប្រឡងប្រចាំខែ ៣' },
  MONTHLY_4: { en: 'Monthly Exam 4', km: 'ប្រឡងប្រចាំខែ ៤' },
  MONTHLY_5: { en: 'Monthly Exam 5', km: 'ប្រឡងប្រចាំខែ ៥' },
  MONTHLY_6: { en: 'Monthly Exam 6', km: 'ប្រឡងប្រចាំខែ ៦' },
  SEMESTER_1: { en: 'Semester 1 Exam', km: 'ប្រឡងឆមាស ១' },
  SEMESTER_2: { en: 'Semester 2 Exam', km: 'ប្រឡងឆមាស ២' },
}
