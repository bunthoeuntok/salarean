/**
 * Class entity types
 */

export type ClassStatus = 'ACTIVE' | 'INACTIVE' | 'COMPLETED'

export interface Class {
  id: string
  name: string
  description?: string
  academicYear: string
  grade: string
  section?: string
  capacity: number
  currentEnrollment: number
  status: ClassStatus
  teacherId: string
  teacherName?: string
  scheduleInfo?: string
  createdAt: string
  updatedAt: string
}

export interface ClassListParams {
  page?: number
  size?: number
  sort?: string
  search?: string
  status?: ClassStatus
  academicYear?: string
  grade?: string
}

export interface CreateClassRequest {
  name: string
  description?: string
  academicYear: string
  grade: string
  section?: string
  capacity: number
}

export interface UpdateClassRequest extends Partial<CreateClassRequest> {
  status?: ClassStatus
}
