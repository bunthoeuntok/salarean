/**
 * Types for class schedule/timetable management
 */

export type ClassShift = 'MORNING' | 'AFTERNOON' | 'FULLDAY'

export interface TimeSlot {
  periodNumber: number | null  // null for breaks
  startTime: string  // HH:mm format
  endTime: string
  label: string
  labelKm: string
  isBreak: boolean
}

export interface TimeSlotTemplate {
  id: string
  teacherId: string | null
  name: string
  nameKm: string | null
  shift: ClassShift
  slots: TimeSlot[]
  isDefault: boolean
  periodCount: number
  createdAt: string
  updatedAt: string
}

export interface ScheduleEntry {
  id: string
  dayOfWeek: number  // 1=Monday, 6=Saturday
  periodNumber: number
  subjectId: string
  room: string | null
  notes: string | null
  createdAt: string
  updatedAt: string
}

export interface ClassSchedule {
  id: string
  classId: string
  timeSlotTemplateId: string | null
  template: TimeSlotTemplate | null
  customSlots: TimeSlot[] | null
  effectiveSlots: TimeSlot[]
  academicYear: string
  isActive: boolean
  entries: ScheduleEntry[]
  createdAt: string
  updatedAt: string
}

// Request types
export interface CreateTimeSlotTemplateRequest {
  name: string
  nameKm?: string
  shift: ClassShift
  slots: Omit<TimeSlot, 'label' | 'labelKm'> & { label?: string; labelKm?: string }[]
}

export interface CreateClassScheduleRequest {
  classId: string
  timeSlotTemplateId?: string
  customSlots?: TimeSlot[]
  academicYear: string
}

export interface ScheduleEntryDto {
  id?: string
  dayOfWeek: number
  periodNumber: number
  subjectId: string
  room?: string
  notes?: string
}

export interface UpdateScheduleEntriesRequest {
  entries: ScheduleEntryDto[]
  clearExisting?: boolean
}

// Day names for display
export const DAY_NAMES: Record<number, { en: string; km: string }> = {
  1: { en: 'Monday', km: 'ច័ន្ទ' },
  2: { en: 'Tuesday', km: 'អង្គារ' },
  3: { en: 'Wednesday', km: 'ពុធ' },
  4: { en: 'Thursday', km: 'ព្រហស្បតិ៍' },
  5: { en: 'Friday', km: 'សុក្រ' },
  6: { en: 'Saturday', km: 'សៅរ៍' },
}

// Shift display names
export const SHIFT_NAMES: Record<ClassShift, { en: string; km: string }> = {
  MORNING: { en: 'Morning', km: 'ព្រឹក' },
  AFTERNOON: { en: 'Afternoon', km: 'រសៀល' },
  FULLDAY: { en: 'Full Day', km: 'ពេញថ្ងៃ' },
}
