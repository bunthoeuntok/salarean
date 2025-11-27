import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, useNavigate, useSearch } from '@tanstack/react-router'
import { toast } from 'sonner'
import { AlertCircle, CheckCircle2 } from 'lucide-react'

import { Button } from '@/components/ui/button'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { PasswordInput } from '@/components/password-input'
import { PasswordStrength } from '@/features/auth/sign-up/components/password-strength'

import {
  resetPasswordSchema,
  type ResetPasswordFormData,
} from '@/lib/validations/auth.schema'
import { authService } from '@/services/auth.service'
import { useLanguage } from '@/context/language-provider'
import { getErrorCode } from '@/lib/handle-server-error'

export function ResetPasswordForm() {
  const navigate = useNavigate()
  const search = useSearch({ from: '/(auth)/reset-password' })
  const [isLoading, setIsLoading] = useState(false)
  const [isSuccess, setIsSuccess] = useState(false)
  const [tokenError, setTokenError] = useState<string | null>(null)
  const { t, translateError } = useLanguage()

  const token = search.token || ''

  const form = useForm<ResetPasswordFormData>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      token,
      newPassword: '',
      confirmPassword: '',
    },
    mode: 'onChange',
  })

  const password = form.watch('newPassword')

  async function onSubmit(data: ResetPasswordFormData) {
    setIsLoading(true)
    setTokenError(null)

    try {
      await authService.resetPassword({
        token: data.token,
        newPassword: data.newPassword,
      })

      setIsSuccess(true)
      toast.success(t.auth.resetPassword.successMessage)

      // Redirect to sign-in after delay
      setTimeout(() => {
        navigate({ to: '/sign-in' })
      }, 3000)
    } catch (error) {
      const errorCode = getErrorCode(error)

      // Handle specific token errors
      if (errorCode === 'RESET_TOKEN_INVALID' || errorCode === 'RESET_TOKEN_EXPIRED') {
        setTokenError(translateError(errorCode as Parameters<typeof translateError>[0]))
      } else {
        const errorMessage = translateError((error as { errorCode?: string })?.errorCode as Parameters<typeof translateError>[0] || 'INTERNAL_ERROR')
        toast.error(errorMessage)
      }
    } finally {
      setIsLoading(false)
    }
  }

  // Success state
  if (isSuccess) {
    return (
      <div className="space-y-4 text-center">
        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-green-100">
          <CheckCircle2 className="h-6 w-6 text-green-600" />
        </div>
        <div className="space-y-2">
          <h3 className="text-lg font-semibold">{t.common.success}</h3>
          <p className="text-sm text-muted-foreground">
            {t.auth.resetPassword.successMessage}
          </p>
        </div>
        <div className="pt-4">
          <Link
            to="/sign-in"
            className="text-sm text-primary hover:underline"
          >
            {t.auth.signIn.signInButton}
          </Link>
        </div>
      </div>
    )
  }

  // Token error state
  if (tokenError) {
    return (
      <div className="space-y-4 text-center">
        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-destructive/10">
          <AlertCircle className="h-6 w-6 text-destructive" />
        </div>
        <div className="space-y-2">
          <h3 className="text-lg font-semibold">{t.common.error}</h3>
          <p className="text-sm text-muted-foreground">{tokenError}</p>
        </div>
        <div className="pt-4 space-y-2">
          <Link to="/forgot-password">
            <Button className="w-full">{t.auth.forgotPassword.sendButton}</Button>
          </Link>
          <p className="text-sm text-muted-foreground">
            {t.common.or}{' '}
            <Link to="/sign-in" className="text-primary hover:underline">
              {t.auth.forgotPassword.backToSignIn}
            </Link>
          </p>
        </div>
      </div>
    )
  }

  // No token provided
  if (!token) {
    return (
      <div className="space-y-4 text-center">
        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-destructive/10">
          <AlertCircle className="h-6 w-6 text-destructive" />
        </div>
        <div className="space-y-2">
          <h3 className="text-lg font-semibold">{t.common.error}</h3>
          <p className="text-sm text-muted-foreground">
            {t.validation.required}
          </p>
        </div>
        <div className="pt-4 space-y-2">
          <Link to="/forgot-password">
            <Button className="w-full">{t.auth.forgotPassword.sendButton}</Button>
          </Link>
          <p className="text-sm text-muted-foreground">
            {t.common.or}{' '}
            <Link to="/sign-in" className="text-primary hover:underline">
              {t.auth.forgotPassword.backToSignIn}
            </Link>
          </p>
        </div>
      </div>
    )
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
        <FormField
          control={form.control}
          name="newPassword"
          render={({ field }) => (
            <FormItem>
              <FormLabel>{t.auth.resetPassword.newPassword}</FormLabel>
              <FormControl>
                <PasswordInput
                  placeholder={t.auth.resetPassword.newPasswordPlaceholder}
                  autoComplete="new-password"
                  disabled={isLoading}
                  {...field}
                />
              </FormControl>
              <PasswordStrength password={password} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="confirmPassword"
          render={({ field }) => (
            <FormItem>
              <FormLabel>{t.auth.resetPassword.confirmPassword}</FormLabel>
              <FormControl>
                <PasswordInput
                  placeholder={t.auth.resetPassword.confirmPasswordPlaceholder}
                  autoComplete="new-password"
                  disabled={isLoading}
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <Button type="submit" className="w-full" disabled={isLoading}>
          {isLoading ? t.auth.resetPassword.resetting : t.auth.resetPassword.resetButton}
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
