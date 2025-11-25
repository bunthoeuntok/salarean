import type { ErrorCode } from '@/types/error-codes.types'

/**
 * Supported languages
 */
export type Language = 'en' | 'km'

/**
 * Translation key types for error code messages
 */
export type ErrorCodeTranslations = Record<ErrorCode, string>

/**
 * UI text translations structure
 */
export interface UITranslations {
  // Auth pages
  auth: {
    signIn: {
      title: string
      subtitle: string
      emailOrPhone: string
      emailOrPhonePlaceholder: string
      password: string
      passwordPlaceholder: string
      forgotPassword: string
      signInButton: string
      signingIn: string
      noAccount: string
      signUp: string
    }
    signUp: {
      title: string
      subtitle: string
      email: string
      emailPlaceholder: string
      phone: string
      phonePlaceholder: string
      password: string
      passwordPlaceholder: string
      confirmPassword: string
      confirmPasswordPlaceholder: string
      language: string
      languageEnglish: string
      languageKhmer: string
      signUpButton: string
      signingUp: string
      hasAccount: string
      signIn: string
      terms: string
      termsLink: string
      privacyLink: string
    }
    forgotPassword: {
      title: string
      subtitle: string
      email: string
      emailPlaceholder: string
      sendButton: string
      sending: string
      backToSignIn: string
      successMessage: string
    }
    resetPassword: {
      title: string
      subtitle: string
      newPassword: string
      newPasswordPlaceholder: string
      confirmPassword: string
      confirmPasswordPlaceholder: string
      resetButton: string
      resetting: string
      successMessage: string
    }
    signOut: {
      title: string
      message: string
      confirmButton: string
      cancelButton: string
    }
  }
  // Common UI elements
  common: {
    loading: string
    error: string
    success: string
    cancel: string
    confirm: string
    save: string
    delete: string
    edit: string
    back: string
    next: string
    previous: string
    submit: string
    close: string
    or: string
    and: string
  }
  // Validation messages
  validation: {
    required: string
    invalidEmail: string
    invalidPhone: string
    passwordTooShort: string
    passwordMissingUppercase: string
    passwordMissingLowercase: string
    passwordMissingDigit: string
    passwordMissingSpecial: string
    passwordsDontMatch: string
  }
}

/**
 * Complete translations structure
 */
export interface Translations {
  errors: ErrorCodeTranslations
  ui: UITranslations
}

/**
 * All translations keyed by language
 */
export type TranslationsByLanguage = Record<Language, Translations>
