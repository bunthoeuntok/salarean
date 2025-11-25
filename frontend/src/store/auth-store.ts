import { create } from 'zustand'
import type { AuthUser } from '@/types/auth.types'
import type { Language } from '@/lib/i18n/types'
import { getBrowserLanguage } from '@/lib/i18n'

interface AuthState {
  user: AuthUser | null
  language: Language
  isLoading: boolean
  isAuthenticated: boolean
}

interface AuthActions {
  setUser: (user: AuthUser | null) => void
  setLanguage: (language: Language) => void
  setLoading: (isLoading: boolean) => void
  logout: () => void
  reset: () => void
}

type AuthStore = AuthState & AuthActions

const getInitialLanguage = (): Language => {
  if (typeof window === 'undefined') return 'en'

  // Check localStorage for saved preference
  const saved = localStorage.getItem('preferredLanguage')
  if (saved === 'en' || saved === 'km') return saved

  // Fall back to browser detection
  return getBrowserLanguage()
}

export const useAuthStore = create<AuthStore>()((set) => ({
  // State
  user: null,
  language: getInitialLanguage(),
  isLoading: true,
  isAuthenticated: false,

  // Actions
  setUser: (user) =>
    set({
      user,
      isAuthenticated: !!user,
      isLoading: false,
    }),

  setLanguage: (language) => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('preferredLanguage', language)
    }
    set({ language })
  },

  setLoading: (isLoading) => set({ isLoading }),

  logout: () =>
    set({
      user: null,
      isAuthenticated: false,
    }),

  reset: () =>
    set({
      user: null,
      isAuthenticated: false,
      isLoading: false,
    }),
}))
