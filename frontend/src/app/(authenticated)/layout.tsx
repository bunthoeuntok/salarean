'use client'

import { useEffect } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { AuthenticatedLayout } from '@/components/layout/authenticated-layout'
import { useAuthStore } from '@/store/auth-store'

export default function AuthenticatedRouteLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const router = useRouter()
  const pathname = usePathname()
  const { isAuthenticated, isLoading } = useAuthStore()

  useEffect(() => {
    // Redirect unauthenticated users to sign-in
    if (!isLoading && !isAuthenticated) {
      const redirectUrl = encodeURIComponent(pathname)
      router.replace(`/sign-in?redirect=${redirectUrl}`)
    }
  }, [isAuthenticated, isLoading, pathname, router])

  // Show loading while checking auth state
  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    )
  }

  // Don't render protected content if user is not authenticated
  if (!isAuthenticated) {
    return null
  }

  return <AuthenticatedLayout>{children}</AuthenticatedLayout>
}
