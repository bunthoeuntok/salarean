import { z } from 'zod'
import { useMemo } from 'react'
import { useLanguage } from '@/context/language-provider'

/**
 * Cambodia phone number regex
 * Accepts: +855, 855, or 0 prefix followed by valid carrier codes
 */
export const khmerPhoneRegex =
  /^(\+855|855|0)(1[0-2]|1[5-8]|69|7[0-9]|8[5-9]|9[0-9])\d{6}$/

/**
 * Hook that returns Zod validation schemas with translated error messages
 * based on the current language setting
 */
export function useValidationSchemas() {
  const { t } = useLanguage()

  return useMemo(() => {
    // Email validation schema
    const emailSchema = z
      .string()
      .min(1, t.validation.required)
      .email(t.validation.invalidEmail)

    // Cambodia phone number validation schema
    const phoneSchema = z
      .string()
      .min(1, t.validation.required)
      .regex(khmerPhoneRegex, t.validation.invalidPhone)

    // Password validation schema
    const passwordSchema = z
      .string()
      .min(1, t.validation.required)
      .min(8, t.validation.passwordTooShort)
      .regex(/[A-Z]/, t.validation.passwordMissingUppercase)
      .regex(/[a-z]/, t.validation.passwordMissingLowercase)
      .regex(/[0-9]/, t.validation.passwordMissingDigit)
      .regex(/[@#$%^&+=!*()_-]/, t.validation.passwordMissingSpecial)

    // Login form schema
    const loginSchema = z.object({
      emailOrPhone: z
        .string()
        .min(1, t.validation.required)
        .refine(
          (value) => {
            const emailResult = z.string().email().safeParse(value)
            if (emailResult.success) return true
            return khmerPhoneRegex.test(value)
          },
          { message: `${t.validation.invalidEmail} / ${t.validation.invalidPhone}` }
        ),
      password: z.string().min(1, t.validation.required),
    })

    // Registration form schema
    const registerSchema = z
      .object({
        email: emailSchema,
        phoneNumber: phoneSchema,
        password: passwordSchema,
        confirmPassword: z.string().min(1, t.validation.required),
        preferredLanguage: z.enum(['en', 'km']).default('en'),
      })
      .refine((data) => data.password === data.confirmPassword, {
        message: t.validation.passwordsDontMatch,
        path: ['confirmPassword'],
      })

    // Forgot password form schema
    const forgotPasswordSchema = z.object({
      email: emailSchema,
    })

    // Reset password form schema
    const resetPasswordSchema = z
      .object({
        token: z.string().min(1, t.validation.required),
        newPassword: passwordSchema,
        confirmPassword: z.string().min(1, t.validation.required),
      })
      .refine((data) => data.newPassword === data.confirmPassword, {
        message: t.validation.passwordsDontMatch,
        path: ['confirmPassword'],
      })

    // Change password form schema
    const changePasswordSchema = z
      .object({
        currentPassword: z.string().min(1, t.validation.required),
        newPassword: passwordSchema,
        confirmPassword: z.string().min(1, t.validation.required),
      })
      .refine((data) => data.newPassword === data.confirmPassword, {
        message: t.validation.passwordsDontMatch,
        path: ['confirmPassword'],
      })

    // Profile update schema
    const profileSchema = z.object({
      name: z
        .string()
        .min(2, t.validation.required)
        .max(100)
        .optional()
        .or(z.literal('')),
      email: emailSchema,
      phoneNumber: phoneSchema,
      preferredLanguage: z.enum(['en', 'km']),
    })

    return {
      emailSchema,
      phoneSchema,
      passwordSchema,
      loginSchema,
      registerSchema,
      forgotPasswordSchema,
      resetPasswordSchema,
      changePasswordSchema,
      profileSchema,
    }
  }, [t])
}

// Type exports for form data - defined explicitly for better type inference
export type LoginFormData = {
  emailOrPhone: string
  password: string
}

export type RegisterFormData = {
  email: string
  phoneNumber: string
  password: string
  confirmPassword: string
  preferredLanguage: 'en' | 'km'
}

export type ForgotPasswordFormData = {
  email: string
}

export type ResetPasswordFormData = {
  token: string
  newPassword: string
  confirmPassword: string
}

export type ChangePasswordFormData = {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

export type ProfileFormData = {
  name?: string
  email: string
  phoneNumber: string
  preferredLanguage: 'en' | 'km'
}
