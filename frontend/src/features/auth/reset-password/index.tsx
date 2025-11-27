import { Suspense } from 'react'
import { useLanguage } from '@/context/language-provider'
import { ResetPasswordForm } from './components/reset-password-form'

function ResetPasswordContent() {
  const { t } = useLanguage()

  return (
    <>
      <div className="flex flex-col space-y-2 text-start">
        <h2 className="text-lg font-semibold tracking-tight">
          {t.auth.resetPassword.title}
        </h2>
        <p className="text-muted-foreground text-sm">
          {t.auth.resetPassword.subtitle}
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
