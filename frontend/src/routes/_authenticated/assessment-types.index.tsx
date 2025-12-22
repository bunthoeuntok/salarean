import { createFileRoute } from '@tanstack/react-router'
import { z } from 'zod'
import { AssessmentTypesPage } from '@/features/assessment-types'

const searchSchema = z.object({
  page: z.number().optional(),
  size: z.number().optional(),
  search: z.string().optional(),
  sort: z.string().optional(),
  sortDir: z.enum(['asc', 'desc']).optional(),
  filters: z.record(z.array(z.string())).optional(),
})

export const Route = createFileRoute('/_authenticated/assessment-types/')({
  validateSearch: searchSchema,
  component: AssessmentTypesPage,
})
