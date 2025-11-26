import { useState } from 'react'
import { useNavigate } from '@tanstack/react-router'
import { LogOut, User } from 'lucide-react'
import { toast } from 'sonner'

import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

import { useAuthStore } from '@/store/auth-store'
import { authService } from '@/services/auth.service'

export function UserMenu() {
  const navigate = useNavigate()
  const [isLoading, setIsLoading] = useState(false)
  const { user, refreshToken, logout: resetAuthStore } = useAuthStore()

  async function handleSignOut() {
    setIsLoading(true)

    try {
      // Call logout API to invalidate refresh token on server
      if (refreshToken) {
        await authService.logout(refreshToken)
      }

      // Reset auth store
      resetAuthStore()

      // Redirect to sign-in
      navigate({ to: '/sign-in' })
    } catch {
      // Even if API fails, reset local state and redirect
      // (token may already be expired)
      resetAuthStore()
      toast.error('Sign out completed with warnings')
      navigate({ to: '/sign-in' })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="sm" className="gap-2">
          <User className="h-4 w-4" />
          <span className="hidden sm:inline-block max-w-[150px] truncate">
            {user?.email || 'Teacher'}
          </span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-56">
        <DropdownMenuLabel className="font-normal">
          <div className="flex flex-col space-y-1">
            <p className="text-sm font-medium leading-none">
              {user?.email || 'Teacher'}
            </p>
            {user?.phoneNumber && (
              <p className="text-xs leading-none text-muted-foreground">
                {user.phoneNumber}
              </p>
            )}
          </div>
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuItem
          onClick={handleSignOut}
          disabled={isLoading}
          className="text-destructive focus:text-destructive"
        >
          <LogOut className="mr-2 h-4 w-4" />
          {isLoading ? 'Signing out...' : 'Sign out'}
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
