/**
 * Assessment type entity types for grade-service
 */

export type AssessmentCategory = 'MONTHLY_EXAM' | 'SEMESTER_EXAM'

export interface AssessmentType {
  id: string
  name: string
  nameKhmer: string
  code: string
  category: AssessmentCategory
  defaultWeight: number
  maxScore: number
  displayOrder: number
}

export interface CreateAssessmentTypeRequest {
  name: string
  nameKhmer: string
  code: string
  category: AssessmentCategory
  defaultWeight: number
  maxScore: number
  displayOrder: number
}

export interface UpdateAssessmentTypeRequest {
  name: string
  nameKhmer: string
  code: string
  category: AssessmentCategory
  defaultWeight: number
  maxScore: number
  displayOrder: number
}
