import api, { apiRequest } from '@/lib/api'
import type { SemesterConfig, SemesterConfigRequest } from '@/types/semester-config'

/**
 * Semester config service for grade-service semester configuration API calls
 */
export const semesterConfigService = {
  /**
   * Get semester config (teacher's custom or default fallback)
   */
  async getConfig(academicYear: string, semesterExamCode: string): Promise<SemesterConfig> {
    return apiRequest<SemesterConfig>(
      api.get(`/api/semester-configs/${academicYear}/${semesterExamCode}`)
    )
  },

  /**
   * Get all semester configs for an academic year
   */
  async getConfigsByAcademicYear(academicYear: string): Promise<SemesterConfig[]> {
    return apiRequest<SemesterConfig[]>(
      api.get(`/api/semester-configs/${academicYear}`)
    )
  },

  /**
   * Save teacher-specific configuration
   */
  async saveTeacherConfig(data: SemesterConfigRequest): Promise<SemesterConfig> {
    return apiRequest<SemesterConfig>(
      api.post('/api/semester-configs/teacher', data)
    )
  },

  /**
   * Delete teacher-specific configuration (will fall back to default)
   */
  async deleteTeacherConfig(academicYear: string, semesterExamCode: string): Promise<void> {
    return apiRequest<void>(
      api.delete(`/api/semester-configs/teacher/${academicYear}/${semesterExamCode}`)
    )
  },

  // =============================================
  // Admin Operations
  // =============================================

  /**
   * Get all default configurations (admin only)
   */
  async getAllDefaultConfigs(): Promise<SemesterConfig[]> {
    return apiRequest<SemesterConfig[]>(
      api.get('/api/semester-configs/admin/defaults')
    )
  },

  /**
   * Get default configurations for an academic year
   */
  async getDefaultConfigsByAcademicYear(academicYear: string): Promise<SemesterConfig[]> {
    return apiRequest<SemesterConfig[]>(
      api.get(`/api/semester-configs/admin/defaults/${academicYear}`)
    )
  },

  /**
   * Create or update default configuration (admin only)
   */
  async saveDefaultConfig(data: SemesterConfigRequest): Promise<SemesterConfig> {
    return apiRequest<SemesterConfig>(
      api.post('/api/semester-configs/admin/defaults', data)
    )
  },

  /**
   * Delete default configuration (admin only)
   */
  async deleteDefaultConfig(academicYear: string, semesterExamCode: string): Promise<void> {
    return apiRequest<void>(
      api.delete(`/api/semester-configs/admin/defaults/${academicYear}/${semesterExamCode}`)
    )
  },

  /**
   * Get list of academic years with default configs
   */
  async getAvailableAcademicYears(): Promise<string[]> {
    return apiRequest<string[]>(
      api.get('/api/semester-configs/admin/academic-years')
    )
  },
}
