import { createFileRoute } from '@tanstack/react-router'
import { SchoolSetupSettings } from '@/features/settings/school-setup'

export const Route = createFileRoute('/_authenticated/settings/school-setup')({
  component: SchoolSetupSettings,
})
