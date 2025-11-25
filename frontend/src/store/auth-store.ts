import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'
import type { AuthUser } from '@/types/auth.types'
import type { Language } from '@/lib/i18n/types'
import { getBrowserLanguage } from '@/lib/i18n'

interface AuthState {
  user: AuthUser | null
  accessToken: string | null
  refreshToken: string | null
  language: Language
  isLoading: boolean
  isAuthenticated: boolean
}

interface AuthActions {
  setUser: (user: AuthUser | null) => void
  setTokens: (accessToken: string, refreshToken: string) => void
  clearTokens: () => void
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

export const useAuthStore = create<AuthStore>()(
  persist(
    (set) => ({
      // State
      user: null,
      accessToken: null,
      refreshToken: null,
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

      setTokens: (accessToken, refreshToken) => {
        set({ accessToken, refreshToken })
      },

      clearTokens: () => {
        set({ accessToken: null, refreshToken: null })
      },

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
          accessToken: null,
          refreshToken: null,
          isAuthenticated: false,
        }),

      reset: () =>
        set({
          user: null,
          accessToken: null,
          refreshToken: null,
          isAuthenticated: false,
          isLoading: false,
        }),
    }),
    {
      name: 'auth-storage',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)
