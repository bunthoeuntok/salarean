import { z } from 'zod'

/**
 * Cambodia phone number regex
 * Accepts: +855, 855, or 0 prefix followed by valid carrier codes
 * Valid carrier codes: 10-18, 69, 70-79, 85-89, 90-99
 * Total digits after prefix: 6
 */
export const khmerPhoneRegex =
  /^(\+855|855|0)(1[0-2]|1[5-8]|69|7[0-9]|8[5-9]|9[0-9])\d{6}$/

/**
 * Email validation schema
 */
export const emailSchema = z
  .string()
  .min(1, 'Email is required')
  .email('Invalid email format')

/**
 * Cambodia phone number validation schema
 */
export const phoneSchema = z
  .string()
  .min(1, 'Phone number is required')
  .regex(khmerPhoneRegex, 'Invalid Cambodia phone number format')

/**
 * Password validation schema - matches backend requirements
 * Requirements:
 * - Minimum 8 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one digit
 * - At least one special character (@#$%^&+=!*()_-)
 */
export const passwordSchema = z
  .string()
  .min(1, 'Password is required')
  .min(8, 'Password must be at least 8 characters')
  .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
  .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
  .regex(/[0-9]/, 'Password must contain at least one digit')
  .regex(
    /[@#$%^&+=!*()_-]/,
    'Password must contain at least one special character (@#$%^&+=!*()_-)'
  )

/**
 * Login form schema
 * Accepts email or Cambodia phone number
 */
export const loginSchema = z.object({
  emailOrPhone: z
    .string()
    .min(1, 'Email or phone number is required')
    .refine(
      (value) => {
        // Check if it's a valid email
        const emailResult = z.string().email().safeParse(value)
        if (emailResult.success) return true

        // Check if it's a valid Cambodia phone number
        return khmerPhoneRegex.test(value)
      },
      { message: 'Please enter a valid email or Cambodia phone number' }
    ),
  password: z.string().min(1, 'Password is required'),
})

export type LoginFormData = z.infer<typeof loginSchema>

/**
 * Registration form schema
 */
export const registerSchema = z
  .object({
    email: emailSchema,
    phoneNumber: phoneSchema,
    password: passwordSchema,
    confirmPassword: z.string().min(1, 'Please confirm your password'),
    preferredLanguage: z.enum(['en', 'km']).default('en'),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords don't match",
    path: ['confirmPassword'],
  })

export type RegisterFormData = z.infer<typeof registerSchema>
export type RegisterFormInput = z.input<typeof registerSchema>

/**
 * Forgot password form schema
 */
export const forgotPasswordSchema = z.object({
  email: emailSchema,
})

export type ForgotPasswordFormData = z.infer<typeof forgotPasswordSchema>

/**
 * Reset password form schema
 */
export const resetPasswordSchema = z
  .object({
    token: z.string().min(1, 'Reset token is required'),
    newPassword: passwordSchema,
    confirmPassword: z.string().min(1, 'Please confirm your password'),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: "Passwords don't match",
    path: ['confirmPassword'],
  })

export type ResetPasswordFormData = z.infer<typeof resetPasswordSchema>

/**
 * Change password form schema (authenticated users)
 */
export const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(1, 'Current password is required'),
    newPassword: passwordSchema,
    confirmPassword: z.string().min(1, 'Please confirm your password'),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: "Passwords don't match",
    path: ['confirmPassword'],
  })
  .refine((data) => data.currentPassword !== data.newPassword, {
    message: 'New password must be different from current password',
    path: ['newPassword'],
  })

export type ChangePasswordFormData = z.infer<typeof changePasswordSchema>
