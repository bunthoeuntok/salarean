'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
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
} from '@/lib/validations/auth.schema'
import { authService } from '@/services/auth.service'
import { useAuthStore } from '@/store/auth-store'
import { handleServerError } from '@/lib/handle-server-error'

export function SignUpForm() {
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(false)
  const { setUser, language } = useAuthStore()

  const form = useForm<RegisterFormData>({
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
      const response = await authService.register({
        email: data.email,
        phoneNumber: data.phoneNumber,
        password: data.password,
        preferredLanguage: data.preferredLanguage,
      })

      // Set user in store (auto sign-in after registration)
      setUser({
        userId: response.userId,
        email: response.email,
        phoneNumber: response.phoneNumber,
        preferredLanguage: response.preferredLanguage,
        createdAt: response.createdAt,
        lastLoginAt: response.lastLoginAt,
      })

      toast.success('Account created successfully!')

      // Redirect to dashboard
      router.push('/dashboard')
    } catch (error) {
      const errorMessage = handleServerError(error, language)
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
              <FormLabel>Email</FormLabel>
              <FormControl>
                <Input
                  type="email"
                  placeholder="teacher@school.com"
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
              <FormLabel>Phone Number</FormLabel>
              <FormControl>
                <Input
                  type="tel"
                  placeholder="0123456789"
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
              <FormLabel>Password</FormLabel>
              <FormControl>
                <PasswordInput
                  placeholder="Create a strong password"
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
              <FormLabel>Confirm Password</FormLabel>
              <FormControl>
                <PasswordInput
                  placeholder="Confirm your password"
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
          {isLoading ? 'Creating account...' : 'Create Account'}
        </Button>

        <p className="text-center text-sm text-muted-foreground">
          Already have an account?{' '}
          <Link href="/sign-in" className="text-primary hover:underline">
            Sign in
          </Link>
        </p>
      </form>
    </Form>
  )
}
