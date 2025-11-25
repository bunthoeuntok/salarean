import { Suspense } from 'react'
import { SignInPage } from '@/features/auth/sign-in'

export default function SignIn() {
  return (
    <Suspense>
      <SignInPage />
    </Suspense>
  )
}
