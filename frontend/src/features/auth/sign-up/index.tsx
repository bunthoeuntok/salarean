import { useLanguage } from '@/context/language-provider'
import { SignUpForm } from './components/sign-up-form'

export function SignUpPage() {
  const { t } = useLanguage()

  return (
    <>
      <div className="flex flex-col space-y-2 text-start">
        <h2 className="text-lg font-semibold tracking-tight">
          {t.auth.signUp.title}
        </h2>
        <p className="text-muted-foreground text-sm">
          {t.auth.signUp.subtitle}
        </p>
      </div>
      <SignUpForm />
      <p className="text-muted-foreground px-8 text-center text-sm">
        {t.auth.signUp.terms}{' '}
        <a
          href="/terms"
          className="hover:text-primary underline underline-offset-4"
        >
          {t.auth.signUp.termsLink}
        </a>{' '}
        {t.common.and}{' '}
        <a
          href="/privacy"
          className="hover:text-primary underline underline-offset-4"
        >
          {t.auth.signUp.privacyLink}
        </a>
        .
      </p>
    </>
  )
}
