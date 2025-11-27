import { SignUpForm } from './components/sign-up-form'

export function SignUpPage() {
  return (
    <>
      <div className="flex flex-col space-y-2 text-start">
        <h2 className="text-lg font-semibold tracking-tight">Create account</h2>
        <p className="text-muted-foreground text-sm">
          Enter your details below
          <br />
          to create a new teacher account
        </p>
      </div>
      <SignUpForm />
      <p className="text-muted-foreground px-8 text-center text-sm">
        By clicking create account, you agree to our{' '}
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
