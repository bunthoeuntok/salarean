import api, { apiRequest } from '@/lib/api'
import type { PagedResponse } from '@/types/api.types'
import type {
  BatchTransferRequest,
  BatchTransferResponse,
  Class,
  ClassListParams,
  CreateClassRequest,
  EligibleClassResponse,
  GetClassStudentsParams,
  StudentEnrollmentListResponse,
  UndoTransferResponse,
  UpdateClassRequest,
} from '@/types/class.types'

/**
 * Class service for class management API calls
 */
export const classService = {
  /**
   * Get paginated list of classes
   */
  async getClasses(params: ClassListParams = {}): Promise<PagedResponse<Class>> {
    const queryParams = new URLSearchParams()

    if (params.page !== undefined) queryParams.append('page', params.page.toString())
    if (params.size !== undefined) queryParams.append('size', params.size.toString())
    if (params.sort) queryParams.append('sort', params.sort)
    if (params.search) queryParams.append('search', params.search)
    if (params.status) queryParams.append('status', params.status)
    if (params.academicYear) queryParams.append('academicYear', params.academicYear)
    if (params.grade !== undefined) queryParams.append('grade', String(params.grade))

    const queryString = queryParams.toString()
    const url = `/api/classes${queryString ? `?${queryString}` : ''}`

    return apiRequest<PagedResponse<Class>>(api.get(url))
  },

  /**
   * Get a single class by ID
   */
  async getClass(id: string): Promise<Class> {
    return apiRequest<Class>(api.get(`/api/classes/${id}`))
  },

  /**
   * Create a new class
   */
  async createClass(data: CreateClassRequest): Promise<Class> {
    return apiRequest<Class>(api.post('/api/classes', data))
  },

  /**
   * Update an existing class
   */
  async updateClass(id: string, data: UpdateClassRequest): Promise<Class> {
    return apiRequest<Class>(api.put(`/api/classes/${id}`, data))
  },

  /**
   * Delete a class
   */
  async deleteClass(id: string): Promise<void> {
    await api.delete(`/api/classes/${id}`)
  },

  /**
   * Get students enrolled in a class with optional status filter
   */
  async getClassStudents(
    params: GetClassStudentsParams
  ): Promise<StudentEnrollmentListResponse> {
    const queryParams = new URLSearchParams()

    if (params.status) queryParams.append('status', params.status)
    if (params.sort) queryParams.append('sort', params.sort)

    const queryString = queryParams.toString()
    const url = `/api/classes/${params.classId}/students${queryString ? `?${queryString}` : ''}`

    return apiRequest<StudentEnrollmentListResponse>(api.get(url))
  },

  /**
   * Get eligible destination classes for batch student transfer
   * Returns classes that:
   * - Are ACTIVE
   * - Have the same grade level as the source class
   * - Are not the source class itself
   * - Have available capacity
   */
  async getEligibleDestinations(sourceClassId: string): Promise<EligibleClassResponse[]> {
    return apiRequest<EligibleClassResponse[]>(
      api.get(`/api/classes/${sourceClassId}/eligible-destinations`)
    )
  },

  /**
   * Execute batch student transfer
   * Transfers multiple students from source class to destination class
   * Returns transfer ID, success count, and any failed transfers
   */
  async batchTransfer(
    sourceClassId: string,
    request: BatchTransferRequest
  ): Promise<BatchTransferResponse> {
    return apiRequest<BatchTransferResponse>(
      api.post(`/api/classes/${sourceClassId}/batch-transfer`, request)
    )
  },

  /**
   * Undo a batch transfer operation
   * Reverses a transfer within 5 minutes
   * Returns count of reverted students
   */
  async undoTransfer(transferId: string): Promise<UndoTransferResponse> {
    return apiRequest<UndoTransferResponse>(api.post(`/api/classes/transfers/${transferId}/undo`))
  },
}
