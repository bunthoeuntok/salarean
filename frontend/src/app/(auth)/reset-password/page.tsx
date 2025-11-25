import { Suspense } from 'react'
import { ResetPasswordPage } from '@/features/auth/reset-password'

export default function ResetPassword() {
  return (
    <Suspense>
      <ResetPasswordPage />
    </Suspense>
  )
}
