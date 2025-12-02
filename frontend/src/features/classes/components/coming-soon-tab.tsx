import { Clock } from 'lucide-react'
import { useLanguage } from '@/context/language-provider'

interface ComingSoonTabProps {
  featureName: string
}

export function ComingSoonTab({ featureName }: ComingSoonTabProps) {
  const { t } = useLanguage()

  return (
    <div className="flex flex-col items-center justify-center rounded-md border border-dashed p-12 text-center">
      <Clock className="h-12 w-12 text-muted-foreground/50" />
      <h3 className="mt-4 text-lg font-semibold">{featureName}</h3>
      <p className="mt-2 text-sm text-muted-foreground">
        {t.common.comingSoon}
      </p>
    </div>
  )
}
