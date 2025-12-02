import { createFileRoute } from '@tanstack/react-router'
import { z } from 'zod'
import { ClassDetailPage } from '@/features/classes/class-detail'

const tabSchema = z.object({
  tab: z.enum(['students', 'schedule', 'attendance', 'grades']).catch('students'),
})

export type ClassDetailTab = z.infer<typeof tabSchema>['tab']

export const Route = createFileRoute('/_authenticated/classes/$id')({
  component: ClassDetailPage,
  validateSearch: tabSchema,
})
