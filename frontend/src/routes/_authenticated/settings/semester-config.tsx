import { createFileRoute } from '@tanstack/react-router'
import { SettingsSemesterConfig } from '@/features/settings/semester-config'

export const Route = createFileRoute('/_authenticated/settings/semester-config')({
  component: SettingsSemesterConfig,
})
