import api, { apiRequest } from '@/lib/api'
import type { PagedResponse } from '@/types/api.types'
import type {
  Student,
  StudentListParams,
  CreateStudentRequest,
  UpdateStudentRequest,
  EnrollStudentRequest,
  EnrollmentResponse,
  TransferStudentRequest,
  EnrollmentHistoryResponse,
} from '@/types/student.types'

/**
 * Student service for student management API calls
 */
export const studentService = {
  /**
   * Get paginated list of students
   */
  async getStudents(params: StudentListParams = {}): Promise<PagedResponse<Student>> {
    const queryParams = new URLSearchParams()

    if (params.page !== undefined) queryParams.append('page', params.page.toString())
    if (params.size !== undefined) queryParams.append('size', params.size.toString())
    if (params.sort) queryParams.append('sort', params.sort)
    if (params.search) queryParams.append('search', params.search)
    if (params.status) queryParams.append('status', params.status)
    if (params.gender) queryParams.append('gender', params.gender)
    if (params.level) queryParams.append('level', params.level)
    if (params.grade !== undefined) queryParams.append('grade', params.grade.toString())
    if (params.classId) queryParams.append('classId', params.classId)
    if (params.academicYear) queryParams.append('academicYear', params.academicYear)

    const queryString = queryParams.toString()
    const url = `/api/students${queryString ? `?${queryString}` : ''}`

    return apiRequest<PagedResponse<Student>>(api.get(url))
  },

  /**
   * Get a single student by ID
   */
  async getStudent(id: string): Promise<Student> {
    return apiRequest<Student>(api.get(`/api/students/${id}`))
  },

  /**
   * Create a new student
   */
  async createStudent(data: CreateStudentRequest): Promise<Student> {
    return apiRequest<Student>(api.post('/api/students', data))
  },

  /**
   * Update an existing student
   */
  async updateStudent(id: string, data: UpdateStudentRequest): Promise<Student> {
    return apiRequest<Student>(api.put(`/api/students/${id}`, data))
  },

  /**
   * Delete a student
   */
  async deleteStudent(id: string): Promise<void> {
    await api.delete(`/api/students/${id}`)
  },

  /**
   * Enroll a student in a class
   */
  async enrollStudent(studentId: string, data: EnrollStudentRequest): Promise<EnrollmentResponse> {
    return apiRequest<EnrollmentResponse>(api.post(`/api/students/${studentId}/enroll`, data))
  },

  /**
   * Transfer a student to a new class
   */
  async transferStudent(studentId: string, data: TransferStudentRequest): Promise<EnrollmentResponse> {
    return apiRequest<EnrollmentResponse>(api.post(`/api/students/${studentId}/transfer`, data))
  },

  /**
   * Get enrollment history for a student
   */
  async getEnrollmentHistory(studentId: string): Promise<EnrollmentHistoryResponse> {
    return apiRequest<EnrollmentHistoryResponse>(api.get(`/api/students/${studentId}/enrollment-history`))
  },
}
