import { createFileRoute, redirect } from '@tanstack/react-router'
import { useAuthStore } from '@/store/auth-store'
import { AuthLayout } from '@/features/auth/auth-layout'
import { SignUpPage } from '@/features/auth/sign-up'

export const Route = createFileRoute('/(auth)/sign-up')({
  beforeLoad: () => {
    const { accessToken } = useAuthStore.getState()
    if (accessToken) {
      throw redirect({ to: '/dashboard' })
    }
  },
  component: () => (
    <AuthLayout>
      <SignUpPage />
    </AuthLayout>
  ),
})
