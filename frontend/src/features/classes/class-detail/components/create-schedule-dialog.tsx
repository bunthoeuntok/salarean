import { useLanguage } from '@/context/language-provider'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Skeleton } from '@/components/ui/skeleton'
import { useTimeSlotTemplates } from '@/hooks/use-schedule'
import type { ClassShift } from '@/types/schedule.types'
import { SHIFT_NAMES } from '@/types/schedule.types'
import { cn } from '@/lib/utils'
import { Check } from 'lucide-react'
import { useState } from 'react'

interface CreateScheduleDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  classShift?: ClassShift
  onSelect: (templateId?: string) => void
  isLoading: boolean
}

export function CreateScheduleDialog({
  open,
  onOpenChange,
  classShift,
  onSelect,
  isLoading,
}: CreateScheduleDialogProps) {
  const { language } = useLanguage()
  const [selectedTemplateId, setSelectedTemplateId] = useState<string | null>(null)

  // Fetch templates for the class shift
  const { data: templates, isLoading: loadingTemplates } = useTimeSlotTemplates(classShift)

  const handleSelect = () => {
    onSelect(selectedTemplateId || undefined)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>
            {language === 'km' ? 'បង្កើតកាលវិភាគ' : 'Create Schedule'}
          </DialogTitle>
          <DialogDescription>
            {language === 'km'
              ? 'ជ្រើសរើសគំរូពេលវេលាសម្រាប់កាលវិភាគថ្នាក់របស់អ្នក'
              : 'Select a time slot template for your class schedule'}
            {classShift && (
              <span className="mt-1 block text-xs">
                {language === 'km' ? 'វេន: ' : 'Shift: '}
                <strong>{SHIFT_NAMES[classShift][language === 'km' ? 'km' : 'en']}</strong>
              </span>
            )}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-3 py-4">
          {loadingTemplates ? (
            <>
              <Skeleton className="h-20 w-full" />
              <Skeleton className="h-20 w-full" />
            </>
          ) : templates && templates.length > 0 ? (
            templates.map((template) => (
              <div
                key={template.id}
                className={cn(
                  'flex cursor-pointer items-center justify-between rounded-lg border p-4 transition-colors',
                  selectedTemplateId === template.id
                    ? 'border-primary bg-primary/5'
                    : 'hover:border-muted-foreground/50'
                )}
                onClick={() => setSelectedTemplateId(template.id)}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault()
                    setSelectedTemplateId(template.id)
                  }
                }}
              >
                <div>
                  <div className="font-medium">
                    {language === 'km' ? template.nameKm : template.name}
                  </div>
                  <div className="text-sm text-muted-foreground">
                    {template.periodCount} {language === 'km' ? 'មុខវិជ្ជា' : 'periods'}
                    {template.isDefault && (
                      <span className="ml-2 rounded bg-muted px-1.5 py-0.5 text-xs">
                        {language === 'km' ? 'លំនាំដើម' : 'Default'}
                      </span>
                    )}
                  </div>
                </div>
                {selectedTemplateId === template.id && (
                  <Check className="h-5 w-5 text-primary" />
                )}
              </div>
            ))
          ) : (
            <p className="text-center text-sm text-muted-foreground">
              {language === 'km' ? 'មិនមានគំរូទេ' : 'No templates available'}
            </p>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            {language === 'km' ? 'បោះបង់' : 'Cancel'}
          </Button>
          <Button
            onClick={handleSelect}
            disabled={isLoading || !selectedTemplateId}
          >
            {isLoading
              ? (language === 'km' ? 'កំពុងបង្កើត...' : 'Creating...')
              : (language === 'km' ? 'បង្កើត' : 'Create')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
