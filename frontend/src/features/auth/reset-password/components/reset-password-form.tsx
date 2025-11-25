'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useRouter, useSearchParams } from 'next/navigation'
import Link from 'next/link'
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
import { useAuthStore } from '@/store/auth-store'
import { handleServerError, getErrorCode } from '@/lib/handle-server-error'

export function ResetPasswordForm() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [isLoading, setIsLoading] = useState(false)
  const [isSuccess, setIsSuccess] = useState(false)
  const [tokenError, setTokenError] = useState<string | null>(null)
  const { language } = useAuthStore()

  const token = searchParams.get('token') || ''

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
      toast.success('Password reset successfully!')

      // Redirect to sign-in after delay
      setTimeout(() => {
        router.push('/sign-in')
      }, 3000)
    } catch (error) {
      const errorCode = getErrorCode(error)

      // Handle specific token errors
      if (errorCode === 'RESET_TOKEN_INVALID' || errorCode === 'RESET_TOKEN_EXPIRED') {
        setTokenError(
          errorCode === 'RESET_TOKEN_EXPIRED'
            ? 'This reset link has expired. Please request a new one.'
            : 'This reset link is invalid. Please request a new one.'
        )
      } else {
        const errorMessage = handleServerError(error, language)
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
          <h3 className="text-lg font-semibold">Password Reset Complete</h3>
          <p className="text-sm text-muted-foreground">
            Your password has been reset successfully. You will be redirected to
            sign in shortly.
          </p>
        </div>
        <div className="pt-4">
          <Link
            href="/sign-in"
            className="text-sm text-primary hover:underline"
          >
            Sign in now
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
          <h3 className="text-lg font-semibold">Link Expired or Invalid</h3>
          <p className="text-sm text-muted-foreground">{tokenError}</p>
        </div>
        <div className="pt-4 space-y-2">
          <Link href="/forgot-password">
            <Button className="w-full">Request New Reset Link</Button>
          </Link>
          <p className="text-sm text-muted-foreground">
            or{' '}
            <Link href="/sign-in" className="text-primary hover:underline">
              return to sign in
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
          <h3 className="text-lg font-semibold">Missing Reset Token</h3>
          <p className="text-sm text-muted-foreground">
            No reset token was provided. Please use the link from your email.
          </p>
        </div>
        <div className="pt-4 space-y-2">
          <Link href="/forgot-password">
            <Button className="w-full">Request Password Reset</Button>
          </Link>
          <p className="text-sm text-muted-foreground">
            or{' '}
            <Link href="/sign-in" className="text-primary hover:underline">
              return to sign in
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
              <FormLabel>New Password</FormLabel>
              <FormControl>
                <PasswordInput
                  placeholder="Enter your new password"
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
              <FormLabel>Confirm New Password</FormLabel>
              <FormControl>
                <PasswordInput
                  placeholder="Confirm your new password"
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
          {isLoading ? 'Resetting...' : 'Reset Password'}
        </Button>

        <p className="text-center text-sm text-muted-foreground">
          Remember your password?{' '}
          <Link href="/sign-in" className="text-primary hover:underline">
            Sign in
          </Link>
        </p>
      </form>
    </Form>
  )
}
