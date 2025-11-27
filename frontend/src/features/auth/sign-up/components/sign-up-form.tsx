import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, useNavigate } from '@tanstack/react-router'
import { toast } from 'sonner'

import { Button } from '@/components/ui/button'
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { PasswordInput } from '@/components/password-input'
import { PasswordStrength } from './password-strength'

import {
  registerSchema,
  type RegisterFormData,
  type RegisterFormInput,
} from '@/lib/validations/auth.schema'
import { authService } from '@/services/auth.service'
import { useAuthStore } from '@/store/auth-store'
import { useLanguage } from '@/context/language-provider'

export function SignUpForm() {
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()
  const { setUser, setTokens } = useAuthStore()
  const { t, language, translateError } = useLanguage()

  const form = useForm<RegisterFormInput, unknown, RegisterFormData>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      email: '',
      phoneNumber: '',
      password: '',
      confirmPassword: '',
      preferredLanguage: language,
    },
    mode: 'onChange',
  })

  const password = form.watch('password')

  async function onSubmit(data: RegisterFormData) {
    setIsLoading(true)

    try {
      // Register returns tokens (auto sign-in)
      const authResponse = await authService.register({
        email: data.email,
        phoneNumber: data.phoneNumber,
        password: data.password,
        preferredLanguage: data.preferredLanguage,
      })

      // Store tokens in auth store
      setTokens(authResponse.token, authResponse.refreshToken)

      // Fetch full profile after successful registration
      const user = await authService.getCurrentUser()
      setUser(user)

      toast.success(t.common.success)

      // Redirect to dashboard
      navigate({ to: '/dashboard' })
    } catch (error) {
      const errorMessage = translateError((error as { errorCode?: string })?.errorCode as Parameters<typeof translateError>[0] || 'INTERNAL_ERROR')
      toast.error(errorMessage)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
        <FormField
          control={form.control}
          name="email"
          render={({ field }) => (
            <FormItem>
              <FormLabel>{t.auth.signUp.email}</FormLabel>
              <FormControl>
                <Input
                  type="email"
                  placeholder={t.auth.signUp.emailPlaceholder}
                  autoComplete="email"
                  disabled={isLoading}
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="phoneNumber"
          render={({ field }) => (
            <FormItem>
              <FormLabel>{t.auth.signUp.phone}</FormLabel>
              <FormControl>
                <Input
                  type="tel"
                  placeholder={t.auth.signUp.phonePlaceholder}
                  autoComplete="tel"
                  disabled={isLoading}
                  {...field}
                />
              </FormControl>
              <FormDescription>
                Cambodia format: +855, 855, or 0 followed by carrier code
              </FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="password"
          render={({ field }) => (
            <FormItem>
              <FormLabel>{t.auth.signUp.password}</FormLabel>
              <FormControl>
                <PasswordInput
                  placeholder={t.auth.signUp.passwordPlaceholder}
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
              <FormLabel>{t.auth.signUp.confirmPassword}</FormLabel>
              <FormControl>
                <PasswordInput
                  placeholder={t.auth.signUp.confirmPasswordPlaceholder}
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
          {isLoading ? t.auth.signUp.signingUp : t.auth.signUp.signUpButton}
        </Button>

        <p className="text-center text-sm text-muted-foreground">
          {t.auth.signUp.hasAccount}{' '}
          <Link to="/sign-in" className="text-primary hover:underline">
            {t.auth.signUp.signIn}
          </Link>
        </p>
      </form>
    </Form>
  )
}
