import api, { apiRequest } from '@/lib/api'
import type { PagedResponse } from '@/types/api.types'
import type {
  Class,
  ClassListParams,
  CreateClassRequest,
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
}
