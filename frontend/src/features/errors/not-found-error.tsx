'use client'

import { useRouter } from 'next/navigation'
import { FileQuestion, Home, ArrowLeft } from 'lucide-react'
import { Button } from '@/components/ui/button'

export function NotFoundError() {
  const router = useRouter()

  return (
    <div className="flex min-h-[50vh] flex-col items-center justify-center p-4 text-center">
      <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-muted">
        <FileQuestion className="h-8 w-8 text-muted-foreground" />
      </div>

      <h1 className="mt-6 text-6xl font-bold">404</h1>
      <h2 className="mt-2 text-xl font-semibold">Page Not Found</h2>
      <p className="mt-2 max-w-md text-muted-foreground">
        The page you&apos;re looking for doesn&apos;t exist or has been moved.
      </p>

      <div className="mt-6 flex gap-4">
        <Button variant="outline" onClick={() => router.back()}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Go Back
        </Button>
        <Button onClick={() => router.push('/')}>
          <Home className="mr-2 h-4 w-4" />
          Go Home
        </Button>
      </div>
    </div>
  )
}
