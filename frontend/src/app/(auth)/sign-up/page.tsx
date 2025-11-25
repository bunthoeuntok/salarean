import { Suspense } from 'react'
import { SignUpPage } from '@/features/auth/sign-up'

export default function SignUp() {
  return (
    <Suspense>
      <SignUpPage />
    </Suspense>
  )
}
