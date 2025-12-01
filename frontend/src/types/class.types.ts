/**
 * Class entity types
 */

export type ClassStatus = 'ACTIVE' | 'INACTIVE' | 'COMPLETED'
export type ClassLevel = 'PRIMARY' | 'SECONDARY' | 'HIGH_SCHOOL'
export type ClassType = 'NORMAL' | 'SCIENCE' | 'SOCIAL_SCIENCE'

export interface Class {
  id: string
  schoolId: string
  teacherId: string
  grade: number
  section?: string
  academicYear: string
  maxCapacity: number
  studentCount: number
  level: ClassLevel
  type: ClassType
  status: ClassStatus
  createdAt: string
  updatedAt: string
}

export interface ClassListParams {
  page?: number
  size?: number
  sort?: string
  search?: string
  status?: string
  academicYear?: string
  grade?: number
}

export interface CreateClassRequest {
  grade: number
  section?: string
  academicYear: string
  maxCapacity: number
  level: ClassLevel
  type: ClassType
}

export interface UpdateClassRequest {
  grade?: number
  section?: string
  academicYear?: string
  maxCapacity?: number
  level?: ClassLevel
  type?: ClassType
  status?: ClassStatus
}
