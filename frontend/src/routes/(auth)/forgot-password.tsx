import { createFileRoute, redirect } from '@tanstack/react-router'
import { useAuthStore } from '@/store/auth-store'
import { AuthLayout } from '@/features/auth/auth-layout'
import { ForgotPasswordPage } from '@/features/auth/forgot-password'

export const Route = createFileRoute('/(auth)/forgot-password')({
  beforeLoad: () => {
    const { accessToken } = useAuthStore.getState()
    if (accessToken) {
      throw redirect({ to: '/dashboard' })
    }
  },
  component: () => (
    <AuthLayout>
      <ForgotPasswordPage />
    </AuthLayout>
  ),
})
