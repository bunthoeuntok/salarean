import { ArrowRightLeft } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

interface FloatingActionButtonProps {
  /** Number of selected students */
  selectedCount: number
  /** Whether the button is visible (typically when selectedCount > 0) */
  visible?: boolean
  /** Click handler for the transfer button */
  onClick: () => void
  /** Label text for the button */
  label: string
  /** Optional className for custom styling */
  className?: string
}

/**
 * Floating action button that appears at the bottom right of the screen
 * when students are selected. Shows count of selected students and
 * triggers the transfer dialog.
 */
export function FloatingActionButton({
  selectedCount,
  visible = selectedCount > 0,
  onClick,
  label,
  className,
}: FloatingActionButtonProps) {
  if (!visible) {
    return null
  }

  return (
    <div
      className={cn(
        'fixed bottom-6 right-6 z-50',
        'animate-in fade-in slide-in-from-bottom-4 duration-300',
        className
      )}
    >
      <Button
        size="lg"
        onClick={onClick}
        className="flex items-center gap-3 px-6 py-6 shadow-lg hover:shadow-xl transition-shadow"
        aria-label={`${label} (${selectedCount} selected)`}
      >
        <div className="flex items-center gap-2">
          <ArrowRightLeft className="h-5 w-5" />
          <span className="font-medium">{label}</span>
        </div>
        <div className="flex h-6 w-6 items-center justify-center rounded-full bg-primary-foreground text-primary text-sm font-bold">
          {selectedCount}
        </div>
      </Button>
    </div>
  )
}
