import { createFileRoute, redirect } from '@tanstack/react-router'
import { z } from 'zod'
import { useAuthStore } from '@/store/auth-store'
import { AuthLayout } from '@/features/auth/auth-layout'
import { SignInPage } from '@/features/auth/sign-in'

const searchSchema = z.object({
  redirect: z.string().optional(),
})

export const Route = createFileRoute('/(auth)/sign-in')({
  validateSearch: searchSchema,
  beforeLoad: () => {
    const { accessToken } = useAuthStore.getState()
    if (accessToken) {
      throw redirect({ to: '/dashboard' })
    }
  },
  component: () => (
    <AuthLayout>
      <SignInPage />
    </AuthLayout>
  ),
})
