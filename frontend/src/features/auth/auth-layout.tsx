import { Logo } from '@/assets/logo'
import { cn } from '@/lib/utils'
import { LanguageProvider } from '@/context/language-provider'
import { LanguageSwitcher } from '@/components/language-switcher'

type AuthLayoutProps = {
  children: React.ReactNode
}

function AuthLayoutContent({ children }: AuthLayoutProps) {
  return (
    <div className="container relative grid h-svh flex-col items-center justify-center lg:max-w-none lg:grid-cols-2 lg:px-0">
      {/* Language switcher - top right */}
      <div className="absolute top-4 right-4 z-30">
        <LanguageSwitcher />
      </div>

      {/* Left side - Form */}
      <div className="lg:p-8">
        <div className="mx-auto flex w-full flex-col justify-center space-y-2 py-8 sm:w-[480px] sm:p-8">
          <div className="mb-4 flex items-center justify-center">
            <Logo size="lg" />
          </div>
        </div>
        <div className="mx-auto flex w-full max-w-md flex-col justify-center space-y-2">
          {children}
        </div>
      </div>

      {/* Right side - Decorative */}
      <div
        className={cn(
          'relative hidden h-full flex-col bg-muted p-10 text-white lg:flex',
          'bg-gradient-to-br from-primary via-primary/90 to-primary/70'
        )}
      >
        <div className="absolute inset-0 bg-[url('data:image/svg+xml,%3Csvg%20width%3D%2260%22%20height%3D%2260%22%20viewBox%3D%220%200%2060%2060%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%3Cg%20fill%3D%22none%22%20fill-rule%3D%22evenodd%22%3E%3Cg%20fill%3D%22%23ffffff%22%20fill-opacity%3D%220.08%22%3E%3Cpath%20d%3D%22M36%2034v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6%2034v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6%204V0H4v4H0v2h4v4h2V6h4V4H6z%22%2F%3E%3C%2Fg%3E%3C%2Fg%3E%3C%2Fsvg%3E')] opacity-50" />
        <div className="relative z-20 flex items-center text-lg font-medium">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            className="mr-2 h-6 w-6"
          >
            <path d="M22 10v6M2 10l10-5 10 5-10 5z" />
            <path d="M6 12v5c3 3 9 3 12 0v-5" />
          </svg>
          Salarean
        </div>
        <div className="relative z-20 mt-auto">
          <blockquote className="space-y-2">
            <p className="text-lg">
              "Empowering educators with modern tools for student management,
              attendance tracking, and academic excellence."
            </p>
            <footer className="text-sm opacity-80">School Management System</footer>
          </blockquote>
        </div>
      </div>
    </div>
  )
}

export function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <LanguageProvider>
      <AuthLayoutContent>{children}</AuthLayoutContent>
    </LanguageProvider>
  )
}
