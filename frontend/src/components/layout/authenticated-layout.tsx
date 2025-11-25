'use client'

import { useAuthStore } from '@/store/auth-store'
import { GraduationCap } from 'lucide-react'

type AuthenticatedLayoutProps = {
  children: React.ReactNode
}

export function AuthenticatedLayout({ children }: AuthenticatedLayoutProps) {
  const { user } = useAuthStore()

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container flex h-14 items-center justify-between">
          {/* Logo */}
          <div className="flex items-center gap-2">
            <GraduationCap className="h-6 w-6 text-primary" />
            <span className="font-semibold">Salarean SMS</span>
          </div>

          {/* User info placeholder - will be replaced with user menu in T056-T058 */}
          <div className="flex items-center gap-4">
            <span className="text-sm text-muted-foreground">
              {user?.email || 'Teacher'}
            </span>
          </div>
        </div>
      </header>

      {/* Main content */}
      <main className="container py-6">{children}</main>
    </div>
  )
}
