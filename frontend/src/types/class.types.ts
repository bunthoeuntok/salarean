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
  level?: ClassLevel
  type?: ClassType
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

/**
 * Enrollment status for students in a class
 */
export type EnrollmentStatus = 'ACTIVE' | 'COMPLETED' | 'TRANSFERRED' | 'WITHDRAWN'

/**
 * Single student enrollment item in a class roster
 */
export interface StudentEnrollmentItem {
  studentId: string
  fullName: string
  fullNameKhmer: string | null
  studentCode: string
  gender: 'M' | 'F'
  dateOfBirth: string
  photoUrl: string | null
  enrollmentDate: string
  enrollmentStatus: EnrollmentStatus
}

/**
 * Response containing all students in a class
 */
export interface StudentEnrollmentListResponse {
  students: StudentEnrollmentItem[]
  totalCount: number
}

/**
 * Filter parameters for fetching class students
 */
export interface StudentFilters {
  status?: EnrollmentStatus
  sort?: string
}

/**
 * Parameters for fetching class students API call
 */
export interface GetClassStudentsParams {
  classId: string
  status?: EnrollmentStatus
  sort?: string
}
