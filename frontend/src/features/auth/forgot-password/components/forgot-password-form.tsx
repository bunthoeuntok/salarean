import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link } from '@tanstack/react-router'
import { CheckCircle2 } from 'lucide-react'

import { Button } from '@/components/ui/button'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'

import { useValidationSchemas, type ForgotPasswordFormData } from '@/hooks/use-validation-schemas'
import { authService } from '@/services/auth.service'
import { useLanguage } from '@/context/language-provider'

export function ForgotPasswordForm() {
  const [isLoading, setIsLoading] = useState(false)
  const [isSubmitted, setIsSubmitted] = useState(false)
  const { t } = useLanguage()
  const { forgotPasswordSchema } = useValidationSchemas()

  const form = useForm<ForgotPasswordFormData>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: {
      email: '',
    },
  })

  async function onSubmit(data: ForgotPasswordFormData) {
    setIsLoading(true)

    try {
      await authService.forgotPassword(data)
      // Always show success for security (prevents email enumeration)
      setIsSubmitted(true)
    } catch {
      // Still show success even on error for security
      setIsSubmitted(true)
    } finally {
      setIsLoading(false)
    }
  }

  // Success message after submission
  if (isSubmitted) {
    return (
      <div className="space-y-4 text-center">
        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-green-100">
          <CheckCircle2 className="h-6 w-6 text-green-600" />
        </div>
        <div className="space-y-2">
          <h3 className="text-lg font-semibold">{t.common.success}</h3>
          <p className="text-sm text-muted-foreground">
            {t.auth.forgotPassword.successMessage}
          </p>
        </div>
        <div className="pt-4">
          <Link
            to="/sign-in"
            className="text-sm text-primary hover:underline"
          >
            {t.auth.forgotPassword.backToSignIn}
          </Link>
        </div>
      </div>
    )
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
        <FormField
          control={form.control}
          name="email"
          render={({ field }) => (
            <FormItem>
              <FormLabel>{t.auth.forgotPassword.email}</FormLabel>
              <FormControl>
                <Input
                  type="email"
                  placeholder={t.auth.forgotPassword.emailPlaceholder}
                  autoComplete="email"
                  disabled={isLoading}
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <Button type="submit" className="w-full" disabled={isLoading}>
          {isLoading ? t.auth.forgotPassword.sending : t.auth.forgotPassword.sendButton}
        </Button>

        <p className="text-center text-sm text-muted-foreground">
          {t.auth.signUp.hasAccount}{' '}
          <Link to="/sign-in" className="text-primary hover:underline">
            {t.auth.signIn.signInButton}
          </Link>
        </p>
      </form>
    </Form>
  )
}
