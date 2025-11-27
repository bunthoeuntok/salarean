import { Suspense } from 'react'
import { ResetPasswordForm } from './components/reset-password-form'

function ResetPasswordContent() {
  return (
    <>
      <div className="flex flex-col space-y-2 text-start">
        <h2 className="text-lg font-semibold tracking-tight">Reset password</h2>
        <p className="text-muted-foreground text-sm">
          Enter your new password below
        </p>
      </div>
      <ResetPasswordForm />
    </>
  )
}

export function ResetPasswordPage() {
  return (
    <Suspense>
      <ResetPasswordContent />
    </Suspense>
  )
}
