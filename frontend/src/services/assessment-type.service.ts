import api, { apiRequest } from '@/lib/api'
import type {
  AssessmentType,
  CreateAssessmentTypeRequest,
  UpdateAssessmentTypeRequest,
  AssessmentCategory,
} from '@/types/assessment-type.types'

/**
 * Assessment type service for grade-service assessment type API calls
 */
export const assessmentTypeService = {
  /**
   * Get all assessment types ordered by display order
   */
  async getAssessmentTypes(): Promise<AssessmentType[]> {
    return apiRequest<AssessmentType[]>(api.get('/api/assessment-types'))
  },

  /**
   * Get a single assessment type by ID
   */
  async getAssessmentType(id: string): Promise<AssessmentType> {
    return apiRequest<AssessmentType>(api.get(`/api/assessment-types/${id}`))
  },

  /**
   * Get an assessment type by code
   */
  async getAssessmentTypeByCode(code: string): Promise<AssessmentType> {
    return apiRequest<AssessmentType>(api.get(`/api/assessment-types/code/${code}`))
  },

  /**
   * Get assessment types by category
   */
  async getAssessmentTypesByCategory(category: AssessmentCategory): Promise<AssessmentType[]> {
    return apiRequest<AssessmentType[]>(api.get(`/api/assessment-types/category/${category}`))
  },

  /**
   * Create a new assessment type
   */
  async createAssessmentType(data: CreateAssessmentTypeRequest): Promise<AssessmentType> {
    return apiRequest<AssessmentType>(api.post('/api/assessment-types', data))
  },

  /**
   * Update an assessment type
   */
  async updateAssessmentType(id: string, data: UpdateAssessmentTypeRequest): Promise<AssessmentType> {
    return apiRequest<AssessmentType>(api.put(`/api/assessment-types/${id}`, data))
  },
}
