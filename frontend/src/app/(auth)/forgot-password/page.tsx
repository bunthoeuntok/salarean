import { Suspense } from 'react'
import { ForgotPasswordPage } from '@/features/auth/forgot-password'

export default function ForgotPassword() {
  return (
    <Suspense>
      <ForgotPasswordPage />
    </Suspense>
  )
}
