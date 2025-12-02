import { AlertCircle, RefreshCw } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { useLanguage } from '@/context/language-provider'

interface ErrorStateProps {
  title?: string
  message?: string
  onRetry?: () => void
}

export function ErrorState({ title, message, onRetry }: ErrorStateProps) {
  const { t } = useLanguage()

  return (
    <div
      className="flex flex-col items-center justify-center rounded-md border border-destructive/20 bg-destructive/5 p-8 text-center"
      role="alert"
    >
      <AlertCircle className="h-12 w-12 text-destructive" />
      <h3 className="mt-4 text-lg font-semibold">
        {title ?? t.common.error}
      </h3>
      <p className="mt-2 text-sm text-muted-foreground">
        {message ?? 'Something went wrong. Please try again.'}
      </p>
      {onRetry && (
        <Button variant="outline" onClick={onRetry} className="mt-4">
          <RefreshCw className="mr-2 h-4 w-4" />
          {t.common.error === 'Error' ? 'Try Again' : t.common.error}
        </Button>
      )}
    </div>
  )
}
