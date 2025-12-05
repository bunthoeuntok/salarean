import { useEffect, useState } from 'react'
import { toast } from 'sonner'

interface CountdownToastProps {
  message: string
  expiresAt: string
  actionLabel: string
  onAction: () => void
  toastId?: string | number
}

export function CountdownToast({ message, expiresAt, actionLabel, onAction, toastId }: CountdownToastProps) {
  const [timeRemaining, setTimeRemaining] = useState<string>('')

  useEffect(() => {
    const updateCountdown = () => {
      const now = new Date().getTime()
      const expiry = new Date(expiresAt).getTime()
      const diff = expiry - now

      // Debug logging
      console.log('Countdown Debug:', {
        now: new Date(now).toISOString(),
        expiresAt,
        expiry: new Date(expiry).toISOString(),
        diff,
        diffMinutes: Math.floor(diff / 60000)
      })

      if (diff <= 0) {
        setTimeRemaining('00:00')
        // Auto-dismiss toast when countdown expires
        if (toastId) {
          toast.dismiss(toastId)
        }
        return
      }

      const minutes = Math.floor(diff / 60000)
      const seconds = Math.floor((diff % 60000) / 1000)
      setTimeRemaining(`${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`)
    }

    // Update immediately
    updateCountdown()

    // Update every second
    const interval = setInterval(updateCountdown, 1000)

    return () => clearInterval(interval)
  }, [expiresAt, toastId])

  return (
    <div className="flex flex-col gap-2">
      <div className="flex items-center justify-between gap-4">
        <span className="font-medium">{message}</span>
        <button
          onClick={onAction}
          className="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 border border-input bg-background hover:bg-accent hover:text-accent-foreground h-9 px-3"
        >
          {actionLabel}
        </button>
      </div>
      <div className="text-xs text-muted-foreground">
        You can undo this within <span className="font-mono font-semibold">{timeRemaining}</span>
      </div>
    </div>
  )
}
