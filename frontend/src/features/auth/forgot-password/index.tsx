import { ForgotPasswordForm } from './components/forgot-password-form'

export function ForgotPasswordPage() {
  return (
    <>
      <div className="flex flex-col space-y-2 text-start">
        <h2 className="text-lg font-semibold tracking-tight">Forgot password</h2>
        <p className="text-muted-foreground text-sm">
          Enter your email address and we&apos;ll send you
          <br />
          a link to reset your password
        </p>
      </div>
      <ForgotPasswordForm />
    </>
  )
}
