import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, useNavigate, useSearch } from '@tanstack/react-router'
import { toast } from 'sonner'

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
import { PasswordInput } from '@/components/password-input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'

import { useValidationSchemas, type LoginFormData } from '@/hooks/use-validation-schemas'
import { authService } from '@/services/auth.service'
import { useAuthStore } from '@/store/auth-store'
import { useAcademicYearStore } from '@/store/academic-year-store'
import { useLanguage } from '@/context/language-provider'

export function SignInForm() {
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()
  const search = useSearch({ from: '/(auth)/sign-in' })
  const { t, translateError } = useLanguage()
  const { loginSchema } = useValidationSchemas()

  const { setUser, setTokens } = useAuthStore()
  const { selectedAcademicYear, availableYears, setAcademicYear } = useAcademicYearStore()

  const form = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      emailOrPhone: '',
      password: '',
    },
  })

  async function onSubmit(data: LoginFormData) {
    setIsLoading(true)

    try {
      // Login returns tokens
      const authResponse = await authService.login(data)

      // Store tokens in auth store
      setTokens(authResponse.token, authResponse.refreshToken)

      // Fetch full profile after successful login
      const user = await authService.getCurrentUser()
      setUser(user)

      // Redirect to dashboard or the page they were trying to access
      const redirectTo = search.redirect || '/dashboard'
      navigate({ to: redirectTo })
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
        <div className="space-y-2">
          <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
            {t.auth.signIn.academicYear}
          </label>
          <Select
            value={selectedAcademicYear}
            onValueChange={setAcademicYear}
            disabled={isLoading}
          >
            <SelectTrigger className="w-full">
              <SelectValue placeholder={t.auth.signIn.academicYearPlaceholder} />
            </SelectTrigger>
            <SelectContent>
              {availableYears.map((year) => (
                <SelectItem key={year} value={year}>
                  {year}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <FormField
          control={form.control}
          name="emailOrPhone"
          render={({ field }) => (
            <FormItem>
              <FormLabel>{t.auth.signIn.emailOrPhone}</FormLabel>
              <FormControl>
                <Input
                  placeholder={t.auth.signIn.emailOrPhonePlaceholder}
                  autoComplete="username"
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
          name="password"
          render={({ field }) => (
            <FormItem>
              <div className="flex items-center justify-between">
                <FormLabel>{t.auth.signIn.password}</FormLabel>
                <Link
                  to="/forgot-password"
                  className="text-sm text-muted-foreground hover:text-primary"
                >
                  {t.auth.signIn.forgotPassword}
                </Link>
              </div>
              <FormControl>
                <PasswordInput
                  placeholder={t.auth.signIn.passwordPlaceholder}
                  autoComplete="current-password"
                  disabled={isLoading}
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <Button type="submit" className="w-full" disabled={isLoading}>
          {isLoading ? t.auth.signIn.signingIn : t.auth.signIn.signInButton}
        </Button>

        <p className="text-center text-sm text-muted-foreground">
          {t.auth.signIn.noAccount}{' '}
          <Link to="/sign-up" className="text-primary hover:underline">
            {t.auth.signIn.signUp}
          </Link>
        </p>
      </form>
    </Form>
  )
}
