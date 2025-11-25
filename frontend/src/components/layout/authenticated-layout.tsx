'use client'

import { Logo } from '@/assets/logo'
import { UserMenu } from './user-menu'

type AuthenticatedLayoutProps = {
  children: React.ReactNode
}

export function AuthenticatedLayout({ children }: AuthenticatedLayoutProps) {
  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container flex h-14 items-center justify-between">
          <Logo size="sm" />
          <UserMenu />
        </div>
      </header>

      {/* Main content */}
      <main className="container py-6">{children}</main>
    </div>
  )
}
