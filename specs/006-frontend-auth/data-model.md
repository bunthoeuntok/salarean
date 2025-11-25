# Data Model: Frontend Authentication Integration

**Feature**: 006-frontend-auth
**Date**: 2025-11-25

## Overview

This document defines the frontend TypeScript types and state structures for the authentication feature. The frontend does not manage database entities directly; it consumes API responses and maintains client-side state.

## TypeScript Type Definitions

### API Response Types

```typescript
// types/api.types.ts

/**
 * Standard API response wrapper matching backend ApiResponse<T>
 */
export interface ApiResponse<T> {
  errorCode: string  // "SUCCESS" or error code enum value
  data: T | null
}

/**
 * Pagination wrapper for list responses
 */
export interface PagedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number // current page (0-indexed)
}
```

### Authentication Types

```typescript
// types/auth.types.ts

/**
 * Authenticated user profile from backend
 */
export interface AuthUser {
  userId: string           // UUID
  email: string
  phoneNumber: string
  preferredLanguage: 'en' | 'km'
  createdAt: string        // ISO datetime
  lastLoginAt: string      // ISO datetime
}

/**
 * Login request payload
 */
export interface LoginRequest {
  emailOrPhone: string
  password: string
}

/**
 * Registration request payload
 */
export interface RegisterRequest {
  email: string
  phoneNumber: string
  password: string
  preferredLanguage?: 'en' | 'km'  // defaults to 'en'
}

/**
 * Auth response from login/register (without tokens - they're in cookies)
 */
export interface AuthResponse {
  userId: string
  email: string
  phoneNumber: string
  preferredLanguage: 'en' | 'km'
  expiresIn: number        // Access token expiry in seconds
  createdAt: string
  lastLoginAt: string
}

/**
 * Forgot password request
 */
export interface ForgotPasswordRequest {
  email: string
}

/**
 * Reset password request
 */
export interface ResetPasswordRequest {
  token: string
  newPassword: string
}

/**
 * Change password request (authenticated)
 */
export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}
```

### State Store Types

```typescript
// stores/auth-store.ts

import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface AuthState {
  // State
  isAuthenticated: boolean
  user: AuthUser | null
  preferredLanguage: 'en' | 'km'
  isLoading: boolean

  // Actions
  setUser: (user: AuthUser | null) => void
  setAuthenticated: (authenticated: boolean) => void
  setLanguage: (lang: 'en' | 'km') => void
  setLoading: (loading: boolean) => void
  reset: () => void
}

const initialState = {
  isAuthenticated: false,
  user: null,
  preferredLanguage: 'en' as const,
  isLoading: true, // True initially until session check completes
}
```

### Form Validation Schemas

```typescript
// lib/validations/auth.schema.ts

import { z } from 'zod'

/**
 * Cambodia phone number regex
 * Formats: +855XXXXXXXXX, 855XXXXXXXXX, 0XXXXXXXXX
 */
const khmerPhoneRegex = /^(\+855|855|0)(1[0-2]|1[5-8]|69|7[0-9]|8[5-9]|9[0-9])\d{6}$/

/**
 * Password strength requirements matching backend
 */
export const passwordSchema = z.string()
  .min(8, 'Password must be at least 8 characters')
  .regex(/[A-Z]/, 'Password must contain an uppercase letter')
  .regex(/[a-z]/, 'Password must contain a lowercase letter')
  .regex(/[0-9]/, 'Password must contain a digit')
  .regex(/[@#$%^&+=!*()_-]/, 'Password must contain a special character')

/**
 * Login form schema
 */
export const loginSchema = z.object({
  emailOrPhone: z.string()
    .min(1, 'Email or phone is required'),
  password: z.string()
    .min(1, 'Password is required'),
})

export type LoginFormData = z.infer<typeof loginSchema>

/**
 * Registration form schema
 */
export const registerSchema = z.object({
  email: z.string()
    .min(1, 'Email is required')
    .email('Invalid email format'),
  phoneNumber: z.string()
    .min(1, 'Phone number is required')
    .regex(khmerPhoneRegex, 'Invalid Cambodia phone number format'),
  password: passwordSchema,
  confirmPassword: z.string()
    .min(1, 'Please confirm your password'),
  preferredLanguage: z.enum(['en', 'km']).default('en'),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword'],
})

export type RegisterFormData = z.infer<typeof registerSchema>

/**
 * Forgot password form schema
 */
export const forgotPasswordSchema = z.object({
  email: z.string()
    .min(1, 'Email is required')
    .email('Invalid email format'),
})

export type ForgotPasswordFormData = z.infer<typeof forgotPasswordSchema>

/**
 * Reset password form schema
 */
export const resetPasswordSchema = z.object({
  token: z.string().min(1, 'Reset token is required'),
  newPassword: passwordSchema,
  confirmPassword: z.string().min(1, 'Please confirm your password'),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword'],
})

export type ResetPasswordFormData = z.infer<typeof resetPasswordSchema>
```

### Error Code Types

```typescript
// types/error-codes.types.ts

/**
 * Common error codes (from sms-common ErrorCode)
 */
export type CommonErrorCode =
  | 'SUCCESS'
  | 'INVALID_INPUT'
  | 'NOT_FOUND'
  | 'UNAUTHORIZED'
  | 'FORBIDDEN'
  | 'INTERNAL_ERROR'
  | 'RATE_LIMITED'

/**
 * Auth-specific error codes (from AuthErrorCode)
 */
export type AuthErrorCode =
  | 'INVALID_CREDENTIALS'
  | 'ACCOUNT_LOCKED'
  | 'DUPLICATE_EMAIL'
  | 'DUPLICATE_PHONE'
  | 'INVALID_PASSWORD'
  | 'WEAK_PASSWORD'
  | 'PASSWORD_TOO_SHORT'
  | 'PASSWORD_MISSING_UPPERCASE'
  | 'PASSWORD_MISSING_LOWERCASE'
  | 'PASSWORD_MISSING_DIGIT'
  | 'PASSWORD_MISSING_SPECIAL'
  | 'PASSWORD_TOO_COMMON'
  | 'USER_NOT_FOUND'
  | 'EMAIL_NOT_FOUND'
  | 'RESET_TOKEN_INVALID'
  | 'RESET_TOKEN_EXPIRED'

export type ErrorCode = CommonErrorCode | AuthErrorCode
```

### i18n Translation Types

```typescript
// lib/i18n/types.ts

export type Language = 'en' | 'km'

export interface TranslationMap {
  [key: string]: string
}

export interface Translations {
  en: TranslationMap
  km: TranslationMap
}
```

## State Flow Diagrams

### Authentication State Machine

```
┌──────────────┐
│   LOADING    │ ← Initial state (checking session)
└──────────────┘
       │
       ▼ (session check completes)
┌──────────────┐     Login/Register     ┌──────────────┐
│UNAUTHENTICATED│ ──────────────────────▶│ AUTHENTICATED │
└──────────────┘                         └──────────────┘
       ▲                                        │
       │              Logout / 401              │
       └────────────────────────────────────────┘
```

### Token Refresh Flow

```
Request → 401 Response → Retry with /refresh
                              │
                    ┌─────────┴─────────┐
                    ▼                   ▼
              Refresh OK           Refresh Failed
                    │                   │
                    ▼                   ▼
            Retry Original        Reset Auth State
               Request            Redirect to /sign-in
```

## Data Validation Rules

| Field | Type | Validation Rules |
|-------|------|-----------------|
| email | string | Required, valid email format |
| phoneNumber | string | Required, Cambodia format: +855/855/0 + operator prefix + 6 digits |
| password | string | Min 8 chars, uppercase, lowercase, digit, special char |
| emailOrPhone | string | Required, accepts either email or phone format |
| preferredLanguage | enum | 'en' or 'km', defaults to 'en' |

## Entity Relationships

```
┌─────────────────┐
│    AuthUser     │
├─────────────────┤
│ userId (PK)     │──────┐
│ email           │      │
│ phoneNumber     │      │
│ preferredLang   │      │
│ createdAt       │      │
│ lastLoginAt     │      │
└─────────────────┘      │
                         │
┌─────────────────┐      │ Stored in
│   AuthState     │◀─────┘ Zustand
├─────────────────┤
│ isAuthenticated │
│ user (AuthUser) │
│ preferredLang   │
│ isLoading       │
└─────────────────┘
```

## Notes

1. **No Token Storage in Frontend**: Tokens are stored in HTTP-only cookies, inaccessible to JavaScript
2. **Session Validation**: On app load, call `/api/auth/me` to validate session and populate AuthState
3. **Language Persistence**: `preferredLanguage` stored in localStorage and synced with backend on profile updates
4. **Form State**: React Hook Form manages form state; types ensure consistency with API contracts
