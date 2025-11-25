'use client'

import { Suspense, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { AuthLayout } from '@/features/auth/auth-layout'
import { useAuthStore } from '@/store/auth-store'

function AuthRedirectHandler({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { isAuthenticated, isLoading } = useAuthStore()

  useEffect(() => {
    // Redirect authenticated users to their destination or dashboard
    if (!isLoading && isAuthenticated) {
      const redirectTo = searchParams.get('redirect') || '/dashboard'
      router.replace(redirectTo)
    }
  }, [isAuthenticated, isLoading, router, searchParams])

  // Show loading while checking auth state
  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    )
  }

  // Don't render auth pages if user is authenticated
  if (isAuthenticated) {
    return null
  }

  return <AuthLayout>{children}</AuthLayout>
}

export default function AuthGroupLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <Suspense
      fallback={
        <div className="flex min-h-screen items-center justify-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
        </div>
      }
    >
      <AuthRedirectHandler>{children}</AuthRedirectHandler>
    </Suspense>
  )
}
