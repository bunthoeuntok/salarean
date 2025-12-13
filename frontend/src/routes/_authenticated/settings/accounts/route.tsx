import { createFileRoute } from '@tanstack/react-router'
import { SettingsAccounts } from '@/features/settings/accounts'

export const Route = createFileRoute('/_authenticated/settings/accounts')({
  component: SettingsAccounts,
})
