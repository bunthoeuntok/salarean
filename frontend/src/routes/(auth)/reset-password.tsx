import { createFileRoute, redirect } from '@tanstack/react-router'
import { z } from 'zod'
import { useAuthStore } from '@/store/auth-store'
import { AuthLayout } from '@/features/auth/auth-layout'
import { ResetPasswordPage } from '@/features/auth/reset-password'

const searchSchema = z.object({
  token: z.string().optional(),
})

export const Route = createFileRoute('/(auth)/reset-password')({
  validateSearch: searchSchema,
  beforeLoad: () => {
    const { accessToken } = useAuthStore.getState()
    if (accessToken) {
      throw redirect({ to: '/dashboard' })
    }
  },
  component: () => (
    <AuthLayout>
      <ResetPasswordPage />
    </AuthLayout>
  ),
})
