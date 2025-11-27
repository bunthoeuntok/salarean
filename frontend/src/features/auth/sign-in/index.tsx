import { SignInForm } from './components/sign-in-form'

export function SignInPage() {
  return (
    <>
      <div className="flex flex-col space-y-2 text-start">
        <h2 className="text-lg font-semibold tracking-tight">Sign in</h2>
        <p className="text-muted-foreground text-sm">
          Enter your email or phone number below
          <br />
          to log into your account
        </p>
      </div>
      <SignInForm />
      <p className="text-muted-foreground px-8 text-center text-sm">
        By clicking sign in, you agree to our{' '}
        <a
          href="/terms"
          className="hover:text-primary underline underline-offset-4"
        >
          Terms of Service
        </a>{' '}
        and{' '}
        <a
          href="/privacy"
          className="hover:text-primary underline underline-offset-4"
        >
          Privacy Policy
        </a>
        .
      </p>
    </>
  )
}
