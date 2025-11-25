import type { ErrorCode } from '@/types/error-codes.types'
import type { Language, Translations, UITranslations } from './types'
import { translations } from './translations'

const DEFAULT_LANGUAGE: Language = 'en'

/**
 * Get browser's preferred language, defaulting to English
 */
export function getBrowserLanguage(): Language {
  if (typeof window === 'undefined') return DEFAULT_LANGUAGE

  const browserLang = navigator.language.split('-')[0]
  return browserLang === 'km' ? 'km' : 'en'
}

/**
 * Get translations for a specific language
 */
export function getTranslations(lang: Language): Translations {
  return translations[lang] || translations[DEFAULT_LANGUAGE]
}

/**
 * Get error message for an error code in the specified language
 */
export function getErrorMessage(errorCode: ErrorCode, lang: Language): string {
  const t = getTranslations(lang)
  return t.errors[errorCode] || t.errors.INTERNAL_ERROR
}

/**
 * Get UI translations for a specific language
 */
export function getUITranslations(lang: Language): UITranslations {
  return getTranslations(lang).ui
}

/**
 * Hook-friendly translation helper
 * Returns a function that translates error codes
 */
export function createErrorTranslator(lang: Language) {
  return (errorCode: ErrorCode): string => getErrorMessage(errorCode, lang)
}

// Re-export types
export type { Language, Translations, UITranslations } from './types'
