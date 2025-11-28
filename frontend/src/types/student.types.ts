/**
 * Student entity types
 */

export type Gender = 'MALE' | 'FEMALE'
export type StudentStatus = 'ACTIVE' | 'INACTIVE' | 'GRADUATED' | 'TRANSFERRED'

export interface Student {
  id: string
  studentCode: string
  firstName: string
  lastName: string
  firstNameKhmer?: string
  lastNameKhmer?: string
  dateOfBirth: string
  gender: Gender
  email?: string
  phoneNumber?: string
  address?: string
  status: StudentStatus
  enrolledClassId?: string
  enrolledClassName?: string
  photoUrl?: string
  createdAt: string
  updatedAt: string
}

export interface StudentListParams {
  page?: number
  size?: number
  sort?: string
  search?: string
  status?: string  // Can be comma-separated values for multiple filters
  gender?: string  // Can be comma-separated values for multiple filters
  classId?: string
}

export interface CreateStudentRequest {
  firstName: string
  lastName: string
  firstNameKhmer?: string
  lastNameKhmer?: string
  dateOfBirth: string
  gender: Gender
  email?: string
  phoneNumber?: string
  address?: string
}

export interface UpdateStudentRequest extends Partial<CreateStudentRequest> {
  status?: StudentStatus
}
