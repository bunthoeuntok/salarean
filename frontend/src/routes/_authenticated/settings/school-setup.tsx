import { createFileRoute } from '@tanstack/react-router'
import { z } from 'zod'
import { SettingsSchoolSetup } from '@/features/settings/school-setup'

const schoolSetupSearchSchema = z.object({
  reason: z.enum(['required']).optional(),
})

export type SchoolSetupSearch = z.infer<typeof schoolSetupSearchSchema>

export const Route = createFileRoute('/_authenticated/settings/school-setup')({
  validateSearch: schoolSetupSearchSchema,
  component: SettingsSchoolSetup,
})
