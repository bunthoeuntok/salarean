/**
 * Student entity types
 */

export type Gender = 'M' | 'F'
export type StudentStatus = 'ACTIVE' | 'INACTIVE'
export type Relationship = 'MOTHER' | 'FATHER' | 'GUARDIAN' | 'OTHER'

export interface ParentContact {
  id: string
  fullName: string
  phoneNumber: string
  relationship: Relationship
  isPrimary: boolean
}

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
  primaryParentContact?: string
  address?: string
  emergencyContact?: string
  status: StudentStatus
  enrolledClassId?: string
  currentClassName?: string
  enrollmentDate?: string
  parentContacts?: ParentContact[]
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

export interface ParentContactRequest {
  fullName: string
  phoneNumber: string
  relationship: Relationship
  isPrimary: boolean
}

export interface CreateStudentRequest {
  firstName: string
  lastName: string
  firstNameKhmer?: string
  lastNameKhmer?: string
  dateOfBirth: string
  gender: Gender
  classId: string
  address?: string
  emergencyContact?: string
  enrollmentDate: string
  parentContacts: ParentContactRequest[]
}

export interface UpdateStudentRequest extends Partial<CreateStudentRequest> {
  status?: StudentStatus
}
