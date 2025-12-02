import { z } from 'zod'

/**
 * Enrollment status values for filtering
 */
export const enrollmentStatusValues = ['ALL', 'ACTIVE', 'COMPLETED', 'TRANSFERRED', 'WITHDRAWN'] as const

export type EnrollmentStatusFilter = (typeof enrollmentStatusValues)[number]

/**
 * Zod schema for student filters
 */
export const studentFiltersSchema = z.object({
  search: z.string().optional().default(''),
  status: z.enum(enrollmentStatusValues).optional().default('ALL'),
})

export type StudentFiltersInput = z.input<typeof studentFiltersSchema>
export type StudentFiltersOutput = z.output<typeof studentFiltersSchema>
