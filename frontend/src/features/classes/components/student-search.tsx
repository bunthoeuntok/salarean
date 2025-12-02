import { Search, X } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { useLanguage } from '@/context/language-provider'

interface StudentSearchProps {
  value: string
  onChange: (value: string) => void
  resultsCount?: number
}

export function StudentSearch({ value, onChange, resultsCount }: StudentSearchProps) {
  const { t } = useLanguage()

  const handleClear = () => {
    onChange('')
  }

  return (
    <div className="relative flex-1">
      <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
      <Input
        type="search"
        placeholder={t.classes.detail?.searchPlaceholder ?? 'Search students by name or code...'}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="pl-9 pr-9"
        aria-label={t.classes.detail?.searchAriaLabel ?? 'Search students by name or code'}
      />
      {value && (
        <Button
          type="button"
          variant="ghost"
          size="sm"
          className="absolute right-1 top-1/2 h-7 w-7 -translate-y-1/2 p-0"
          onClick={handleClear}
          aria-label={t.common.cancel}
        >
          <X className="h-4 w-4" />
        </Button>
      )}
      {resultsCount !== undefined && (
        <div
          aria-live="polite"
          aria-atomic="true"
          className="sr-only"
        >
          {resultsCount === 0
            ? (t.classes.detail?.noResults ?? 'No students found')
            : `${resultsCount} ${resultsCount === 1 ? 'student' : 'students'} found`}
        </div>
      )}
    </div>
  )
}
