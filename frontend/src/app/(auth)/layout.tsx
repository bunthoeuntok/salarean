'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { AuthLayout } from '@/features/auth/auth-layout'
import { useAuthStore } from '@/store/auth-store'

export default function AuthGroupLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const router = useRouter()
  const { isAuthenticated, isLoading } = useAuthStore()

  useEffect(() => {
    // Redirect authenticated users to dashboard
    if (!isLoading && isAuthenticated) {
      router.replace('/dashboard')
    }
  }, [isAuthenticated, isLoading, router])

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
