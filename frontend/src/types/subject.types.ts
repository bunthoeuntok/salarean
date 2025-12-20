/**
 * Subject entity types for grade-service
 */

export interface Subject {
  id: string
  name: string
  nameKhmer: string
  code: string
  gradeLevels: number[]
  isCore: boolean
  displayOrder: number
}

export interface SubjectListParams {
  gradeLevel?: number
  isCore?: boolean
}
