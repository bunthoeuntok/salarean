/**
 * Types for semester configuration
 */

export interface ExamScheduleItem {
  assessmentCode: string
  month: number
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

// Month names for display
export const MONTH_NAMES: Record<number, { en: string; km: string }> = {
  1: { en: 'January', km: 'មករា' },
  2: { en: 'February', km: 'កុម្ភៈ' },
  3: { en: 'March', km: 'មីនា' },
  4: { en: 'April', km: 'មេសា' },
  5: { en: 'May', km: 'ឧសភា' },
  6: { en: 'June', km: 'មិថុនា' },
  7: { en: 'July', km: 'កក្កដា' },
  8: { en: 'August', km: 'សីហា' },
  9: { en: 'September', km: 'កញ្ញា' },
  10: { en: 'October', km: 'តុលា' },
  11: { en: 'November', km: 'វិច្ឆិកា' },
  12: { en: 'December', km: 'ធ្នូ' },
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
