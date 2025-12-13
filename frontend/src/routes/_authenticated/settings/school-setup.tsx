import { createFileRoute } from '@tanstack/react-router'
import { SettingsSchoolSetup } from '@/features/settings/school-setup'

export const Route = createFileRoute('/_authenticated/settings/school-setup')({
  component: SettingsSchoolSetup,
})
