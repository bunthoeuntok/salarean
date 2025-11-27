import { useLanguage } from '@/context/language-provider'
import { ForgotPasswordForm } from './components/forgot-password-form'

export function ForgotPasswordPage() {
  const { t } = useLanguage()

  return (
    <>
      <div className="flex flex-col space-y-2 text-start">
        <h2 className="text-lg font-semibold tracking-tight">
          {t.auth.forgotPassword.title}
        </h2>
        <p className="text-muted-foreground text-sm">
          {t.auth.forgotPassword.subtitle}
        </p>
      </div>
      <ForgotPasswordForm />
    </>
  )
}
