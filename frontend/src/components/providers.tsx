'use client'

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useState, useEffect, Suspense } from 'react'
import { ThemeProvider } from 'next-themes'
import { Toaster } from './ui/sonner'
import { NavigationProgress } from './navigation-progress'
import { useAuthStore } from '@/store/auth-store'
import { authService } from '@/services/auth.service'

function AuthInitializer({ children }: { children: React.ReactNode }) {
  const { setUser, setLoading, reset } = useAuthStore()

  useEffect(() => {
    async function checkSession() {
      try {
        // Try to get current user (validates session via HTTP-only cookie)
        const user = await authService.getCurrentUser()
        setUser(user)
      } catch {
        // Session invalid or expired, reset auth state
        reset()
      }
    }

    checkSession()
  }, [setUser, setLoading, reset])

  return <>{children}</>
}

export function Providers({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 60 * 1000, // 1 minute
            refetchOnWindowFocus: false,
          },
        },
      })
  )

  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider
        attribute="class"
        defaultTheme="system"
        enableSystem
        disableTransitionOnChange
      >
        <AuthInitializer>
          <Suspense fallback={null}>
            <NavigationProgress />
          </Suspense>
          {children}
          <Toaster />
        </AuthInitializer>
      </ThemeProvider>
    </QueryClientProvider>
  )
}
