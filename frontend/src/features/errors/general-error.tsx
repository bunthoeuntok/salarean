'use client'

import { useRouter } from 'next/navigation'
import { AlertTriangle, RefreshCw, Home } from 'lucide-react'
import { Button } from '@/components/ui/button'

type GeneralErrorProps = {
  error?: Error & { digest?: string }
  reset?: () => void
}

export function GeneralError({ error, reset }: GeneralErrorProps) {
  const router = useRouter()

  return (
    <div className="flex min-h-[50vh] flex-col items-center justify-center p-4 text-center">
      <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-destructive/10">
        <AlertTriangle className="h-8 w-8 text-destructive" />
      </div>

      <h1 className="mt-6 text-2xl font-bold">Something went wrong</h1>
      <p className="mt-2 max-w-md text-muted-foreground">
        An unexpected error occurred. Please try again or contact support if the
        problem persists.
      </p>

      {error?.message && process.env.NODE_ENV === 'development' && (
        <div className="mt-4 max-w-lg rounded-md bg-muted p-4">
          <p className="font-mono text-xs text-muted-foreground">
            {error.message}
          </p>
          {error.digest && (
            <p className="mt-1 font-mono text-xs text-muted-foreground">
              Digest: {error.digest}
            </p>
          )}
        </div>
      )}

      <div className="mt-6 flex gap-4">
        {reset && (
          <Button variant="outline" onClick={reset}>
            <RefreshCw className="mr-2 h-4 w-4" />
            Try Again
          </Button>
        )}
        <Button onClick={() => router.push('/')}>
          <Home className="mr-2 h-4 w-4" />
          Go Home
        </Button>
      </div>
    </div>
  )
}
