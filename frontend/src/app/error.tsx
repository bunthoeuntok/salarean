'use client'

import { GeneralError } from '@/features/errors/general-error'

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string }
  reset: () => void
}) {
  return <GeneralError error={error} reset={reset} />
}
