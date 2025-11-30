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

/**
 * Student type that combines fields from both:
 * - StudentSummary (list view): includes currentClassName, primaryParentContact
 * - StudentResponse (single view): includes detailed fields
 */
export interface Student {
  id: string
  studentCode: string
  firstName: string
  lastName: string
  firstNameKhmer?: string
  lastNameKhmer?: string
  dateOfBirth: string
  age?: number
  gender: Gender
  photoUrl?: string
  address?: string
  emergencyContact?: string
  enrollmentDate?: string
  status: StudentStatus
  currentClassId?: string
  currentClassName?: string      // From StudentSummary (list view)
  primaryParentContact?: string  // From StudentSummary (list view)
  parentContacts?: ParentContact[]
  createdAt?: string
  updatedAt?: string
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

/**
 * Request for updating student info (without enrollment fields).
 * Use enrollment endpoints to change class enrollment.
 */
export interface UpdateStudentRequest {
  firstName: string
  lastName: string
  firstNameKhmer?: string
  lastNameKhmer?: string
  dateOfBirth: string
  gender: Gender
  address?: string
  parentContacts: ParentContactRequest[]
}
