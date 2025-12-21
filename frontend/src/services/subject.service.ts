import api, { apiRequest } from '@/lib/api'
import type { Subject, UpdateSubjectRequest, CreateSubjectRequest } from '@/types/subject.types'

/**
 * Subject service for grade-service subject API calls
 */
export const subjectService = {
  /**
   * Get all subjects ordered by display order
   */
  async getSubjects(): Promise<Subject[]> {
    return apiRequest<Subject[]>(api.get('/api/subjects'))
  },

  /**
   * Get a single subject by ID
   */
  async getSubject(id: string): Promise<Subject> {
    return apiRequest<Subject>(api.get(`/api/subjects/${id}`))
  },

  /**
   * Get a subject by code
   */
  async getSubjectByCode(code: string): Promise<Subject> {
    return apiRequest<Subject>(api.get(`/api/subjects/code/${code}`))
  },

  /**
   * Get all core subjects
   */
  async getCoreSubjects(): Promise<Subject[]> {
    return apiRequest<Subject[]>(api.get('/api/subjects/core'))
  },

  /**
   * Get subjects for a specific grade level
   */
  async getSubjectsForGrade(gradeLevel: number): Promise<Subject[]> {
    return apiRequest<Subject[]>(api.get(`/api/subjects/grade/${gradeLevel}`))
  },

  /**
   * Update a subject
   */
  async updateSubject(id: string, data: UpdateSubjectRequest): Promise<Subject> {
    return apiRequest<Subject>(api.put(`/api/subjects/${id}`, data))
  },

  /**
   * Create a new subject
   */
  async createSubject(data: CreateSubjectRequest): Promise<Subject> {
    return apiRequest<Subject>(api.post('/api/subjects', data))
  },
}
