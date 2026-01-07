import api, { apiRequest } from '@/lib/api'
import type {
  ClassSchedule,
  ClassShift,
  CreateClassScheduleRequest,
  CreateTimeSlotTemplateRequest,
  TimeSlotTemplate,
  UpdateScheduleEntriesRequest,
} from '@/types/schedule.types'

/**
 * Schedule service for class timetable management API calls
 */
export const scheduleService = {
  // ===================== TIME SLOT TEMPLATES =====================

  /**
   * Get all available templates for the current teacher
   */
  async getTemplates(): Promise<TimeSlotTemplate[]> {
    return apiRequest<TimeSlotTemplate[]>(api.get('/api/schedules/templates'))
  },

  /**
   * Get templates by shift type
   */
  async getTemplatesByShift(shift: ClassShift): Promise<TimeSlotTemplate[]> {
    return apiRequest<TimeSlotTemplate[]>(api.get(`/api/schedules/templates/shift/${shift}`))
  },

  /**
   * Get a single template by ID
   */
  async getTemplate(templateId: string): Promise<TimeSlotTemplate> {
    return apiRequest<TimeSlotTemplate>(api.get(`/api/schedules/templates/${templateId}`))
  },

  /**
   * Create a custom time slot template
   */
  async createTemplate(data: CreateTimeSlotTemplateRequest): Promise<TimeSlotTemplate> {
    return apiRequest<TimeSlotTemplate>(api.post('/api/schedules/templates', data))
  },

  /**
   * Delete a custom template
   */
  async deleteTemplate(templateId: string): Promise<void> {
    await api.delete(`/api/schedules/templates/${templateId}`)
  },

  // ===================== CLASS SCHEDULES =====================

  /**
   * Get schedule for a class
   */
  async getClassSchedule(classId: string): Promise<ClassSchedule | null> {
    return apiRequest<ClassSchedule | null>(api.get(`/api/schedules/class/${classId}`))
  },

  /**
   * Create a schedule for a class
   */
  async createClassSchedule(data: CreateClassScheduleRequest): Promise<ClassSchedule> {
    return apiRequest<ClassSchedule>(api.post('/api/schedules/class', data))
  },

  /**
   * Update schedule entries for a class
   */
  async updateScheduleEntries(
    classId: string,
    data: UpdateScheduleEntriesRequest
  ): Promise<ClassSchedule> {
    return apiRequest<ClassSchedule>(api.put(`/api/schedules/class/${classId}/entries`, data))
  },

  /**
   * Delete a class schedule
   */
  async deleteClassSchedule(classId: string): Promise<void> {
    await api.delete(`/api/schedules/class/${classId}`)
  },

  /**
   * Clear all entries from a class schedule
   */
  async clearScheduleEntries(classId: string): Promise<void> {
    await api.post(`/api/schedules/class/${classId}/clear`)
  },

  /**
   * Copy schedule from one class to another
   */
  async copySchedule(targetClassId: string, sourceClassId: string): Promise<ClassSchedule> {
    return apiRequest<ClassSchedule>(
      api.post(`/api/schedules/class/${targetClassId}/copy-from/${sourceClassId}`)
    )
  },
}
