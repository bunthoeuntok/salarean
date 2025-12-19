import { createContext, useContext, useEffect, useState } from 'react'
import { getCookie, setCookie, removeCookie } from '@/lib/cookies'
import {
  type Language,
  type UITranslations,
  getBrowserLanguage,
  getUITranslations,
  getErrorMessage,
} from '@/lib/i18n'
import type { ErrorCode } from '@/types/error-codes.types'

const DEFAULT_LANGUAGE: Language = 'en'
const LANGUAGE_COOKIE_NAME = 'language'
const LANGUAGE_COOKIE_MAX_AGE = 60 * 60 * 24 * 365 // 1 year

type LanguageContextType = {
  defaultLanguage: Language
  language: Language
  setLanguage: (language: Language) => void
  resetLanguage: () => void
  t: UITranslations
  translateError: (errorCode: ErrorCode) => string
}

const LanguageContext = createContext<LanguageContextType | null>(null)

export function LanguageProvider({ children }: { children: React.ReactNode }) {
  const [language, _setLanguage] = useState<Language>(() => {
    const cookieValue = getCookie(LANGUAGE_COOKIE_NAME) as Language
    if (cookieValue === 'en' || cookieValue === 'km') {
      return cookieValue
    }
    return getBrowserLanguage()
  })

  const [translations, setTranslations] = useState<UITranslations>(() =>
    getUITranslations(language)
  )

  useEffect(() => {
    setTranslations(getUITranslations(language))
    // Update html lang attribute
    document.documentElement.setAttribute('lang', language)
  }, [language])

  const setLanguage = (lang: Language) => {
    _setLanguage(lang)
    setCookie(LANGUAGE_COOKIE_NAME, lang, LANGUAGE_COOKIE_MAX_AGE)
  }

  const resetLanguage = () => {
    _setLanguage(DEFAULT_LANGUAGE)
    removeCookie(LANGUAGE_COOKIE_NAME)
  }

  const translateError = (errorCode: ErrorCode): string => {
    return getErrorMessage(errorCode, language)
  }

  return (
    <LanguageContext
      value={{
        defaultLanguage: DEFAULT_LANGUAGE,
        language,
        setLanguage,
        resetLanguage,
        t: translations,
        translateError,
      }}
    >
      {children}
    </LanguageContext>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export function useLanguage() {
  const context = useContext(LanguageContext)
  if (!context) {
    throw new Error('useLanguage must be used within a LanguageProvider')
  }
  return context
}
