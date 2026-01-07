import { useState, useEffect } from 'react'
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
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { ScrollArea } from '@/components/ui/scroll-area'
import { useTimeSlotTemplates, useCreateTemplate } from '@/hooks/use-schedule'
import type { ClassShift, TimeSlot, TimeSlotTemplate } from '@/types/schedule.types'
import { SHIFT_NAMES } from '@/types/schedule.types'
import { cn } from '@/lib/utils'
import { Check, Settings2, Plus, Trash2, ArrowLeft, Coffee, Eye } from 'lucide-react'
import { Switch } from '@/components/ui/switch'

interface CreateScheduleDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  classShift?: ClassShift
  onSelect: (templateId?: string, customSlots?: TimeSlot[]) => void
  isLoading: boolean
}

type ViewMode = 'select' | 'customize'

export function CreateScheduleDialog({
  open,
  onOpenChange,
  classShift,
  onSelect,
  isLoading,
}: CreateScheduleDialogProps) {
  const { language } = useLanguage()
  const [viewMode, setViewMode] = useState<ViewMode>('select')
  const [selectedTemplateId, setSelectedTemplateId] = useState<string | null>(null)
  const [editingSlots, setEditingSlots] = useState<TimeSlot[]>([])
  const [templateName, setTemplateName] = useState('')
  const [templateNameKm, setTemplateNameKm] = useState('')
  const [saveAsTemplate, setSaveAsTemplate] = useState(false)

  // Fetch templates for the class shift
  const { data: templates, isLoading: loadingTemplates } = useTimeSlotTemplates(classShift)
  const createTemplate = useCreateTemplate()

  // Reset state when dialog opens/closes
  useEffect(() => {
    if (!open) {
      setViewMode('select')
      setSelectedTemplateId(null)
      setEditingSlots([])
      setTemplateName('')
      setTemplateNameKm('')
      setSaveAsTemplate(false)
    }
  }, [open])

  const selectedTemplate = templates?.find((t) => t.id === selectedTemplateId)

  const handleCustomize = () => {
    if (selectedTemplate) {
      // Clone the slots from selected template
      setEditingSlots([...selectedTemplate.slots])
      setTemplateName(`${selectedTemplate.name} (Custom)`)
      setTemplateNameKm(selectedTemplate.nameKm ? `${selectedTemplate.nameKm} (ផ្ទាល់ខ្លួន)` : '')
      setViewMode('customize')
    }
  }

  const handleBackToSelect = () => {
    setViewMode('select')
  }

  const handleSlotChange = (index: number, field: keyof TimeSlot, value: string | number | boolean | null) => {
    setEditingSlots((prev) => {
      const updated = [...prev]
      updated[index] = { ...updated[index], [field]: value }
      return updated
    })
  }

  const handleAddSlot = (isBreak: boolean) => {
    const lastSlot = editingSlots[editingSlots.length - 1]
    const periodCount = editingSlots.filter((s) => !s.isBreak).length

    const newSlot: TimeSlot = {
      periodNumber: isBreak ? null : periodCount + 1,
      startTime: lastSlot?.endTime || '08:00',
      endTime: lastSlot ? addMinutes(lastSlot.endTime, 45) : '08:45',
      label: isBreak ? 'Break' : `Period ${periodCount + 1}`,
      labelKm: isBreak ? 'សម្រាក' : `មុខវិជ្ជាទី${periodCount + 1}`,
      isBreak,
    }
    setEditingSlots((prev) => [...prev, newSlot])
  }

  const handleRemoveSlot = (index: number) => {
    setEditingSlots((prev) => {
      const updated = prev.filter((_, i) => i !== index)
      // Renumber periods
      let periodNum = 1
      return updated.map((slot) => {
        if (!slot.isBreak) {
          return { ...slot, periodNumber: periodNum++ }
        }
        return slot
      })
    })
  }

  const handleSelect = async () => {
    if (viewMode === 'customize') {
      // Sort slots by start time
      const sortedSlots = [...editingSlots].sort((a, b) => a.startTime.localeCompare(b.startTime))

      if (saveAsTemplate && classShift) {
        // Save as a new template first
        try {
          const newTemplate = await createTemplate.mutateAsync({
            name: templateName || 'Custom Template',
            nameKm: templateNameKm || undefined,
            shift: classShift,
            slots: sortedSlots,
          })
          onSelect(newTemplate.id)
        } catch {
          // Error handled by hook
        }
      } else {
        // Use custom slots directly without saving as template
        onSelect(undefined, sortedSlots)
      }
    } else {
      onSelect(selectedTemplateId || undefined)
    }
  }

  const addMinutes = (time: string, minutes: number): string => {
    const [hours, mins] = time.split(':').map(Number)
    const totalMins = hours * 60 + mins + minutes
    const newHours = Math.floor(totalMins / 60) % 24
    const newMins = totalMins % 60
    return `${String(newHours).padStart(2, '0')}:${String(newMins).padStart(2, '0')}`
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>
            {viewMode === 'customize'
              ? (language === 'km' ? 'ប្ដូរគំរូពេលវេលា' : 'Customize Time Slots')
              : (language === 'km' ? 'បង្កើតកាលវិភាគ' : 'Create Schedule')}
          </DialogTitle>
          <DialogDescription>
            {viewMode === 'customize'
              ? (language === 'km'
                ? 'កែសម្រួលពេលវេលាសម្រាប់កាលវិភាគរបស់អ្នក'
                : 'Modify time slots for your schedule')
              : (language === 'km'
                ? 'ជ្រើសរើសគំរូពេលវេលាសម្រាប់កាលវិភាគថ្នាក់របស់អ្នក'
                : 'Select a time slot template for your class schedule')}
            {classShift && viewMode === 'select' && (
              <span className="mt-1 block text-xs">
                {language === 'km' ? 'វេន: ' : 'Shift: '}
                <strong>{SHIFT_NAMES[classShift][language === 'km' ? 'km' : 'en']}</strong>
              </span>
            )}
          </DialogDescription>
        </DialogHeader>

        {viewMode === 'select' ? (
          // Template Selection View
          <>
            {/* Customize Button */}
            {selectedTemplateId && (
              <div className="flex justify-end">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handleCustomize}
                  className="gap-2"
                >
                  <Settings2 className="h-4 w-4" />
                  {language === 'km' ? 'ប្ដូរតាមតម្រូវការ' : 'Customize'}
                </Button>
              </div>
            )}

            <div className="space-y-3 py-4">
              {loadingTemplates ? (
                <>
                  <Skeleton className="h-20 w-full" />
                  <Skeleton className="h-20 w-full" />
                </>
              ) : templates && templates.length > 0 ? (
                templates.map((template) => (
                  <TemplateCard
                    key={template.id}
                    template={template}
                    isSelected={selectedTemplateId === template.id}
                    onSelect={() => setSelectedTemplateId(template.id)}
                    language={language}
                  />
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
          </>
        ) : (
          // Customize View
          <>
            <ScrollArea className="max-h-[400px] pr-4">
              <div className="space-y-4 py-2">
                {/* Template Name (for saving) */}
                <div className="flex items-center gap-4 rounded-lg border bg-muted/30 p-3">
                  <Switch
                    id="save-template"
                    checked={saveAsTemplate}
                    onCheckedChange={setSaveAsTemplate}
                  />
                  <Label htmlFor="save-template" className="flex-1 cursor-pointer text-sm">
                    {language === 'km' ? 'រក្សាទុកជាគំរូថ្មី' : 'Save as new template'}
                  </Label>
                </div>

                {saveAsTemplate && (
                  <div className="grid gap-3">
                    <div>
                      <Label className="text-xs">
                        {language === 'km' ? 'ឈ្មោះគំរូ (អង់គ្លេស)' : 'Template Name (English)'}
                      </Label>
                      <Input
                        value={templateName}
                        onChange={(e) => setTemplateName(e.target.value)}
                        placeholder="e.g., Custom Morning"
                      />
                    </div>
                    <div>
                      <Label className="text-xs">
                        {language === 'km' ? 'ឈ្មោះគំរូ (ខ្មែរ)' : 'Template Name (Khmer)'}
                      </Label>
                      <Input
                        value={templateNameKm}
                        onChange={(e) => setTemplateNameKm(e.target.value)}
                        placeholder="ឧ: ព្រឹកផ្ទាល់ខ្លួន"
                      />
                    </div>
                  </div>
                )}

                {/* Time Slots Editor */}
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <Label className="text-sm font-medium">
                      {language === 'km' ? 'ពេលវេលា' : 'Time Slots'}
                    </Label>
                    <div className="flex gap-1">
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => handleAddSlot(false)}
                        className="h-7 gap-1 px-2 text-xs"
                      >
                        <Plus className="h-3 w-3" />
                        {language === 'km' ? 'មុខវិជ្ជា' : 'Period'}
                      </Button>
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => handleAddSlot(true)}
                        className="h-7 gap-1 px-2 text-xs"
                      >
                        <Coffee className="h-3 w-3" />
                        {language === 'km' ? 'សម្រាក' : 'Break'}
                      </Button>
                    </div>
                  </div>

                  <div className="space-y-2">
                    {editingSlots.map((slot, index) => (
                      <SlotEditor
                        key={index}
                        slot={slot}
                        index={index}
                        language={language}
                        onChange={handleSlotChange}
                        onRemove={handleRemoveSlot}
                      />
                    ))}
                  </div>
                </div>
              </div>
            </ScrollArea>

            <DialogFooter className="gap-2 sm:gap-0">
              <Button
                variant="ghost"
                onClick={handleBackToSelect}
                className="mr-auto gap-2"
              >
                <ArrowLeft className="h-4 w-4" />
                {language === 'km' ? 'ត្រឡប់' : 'Back'}
              </Button>
              <Button variant="outline" onClick={() => onOpenChange(false)}>
                {language === 'km' ? 'បោះបង់' : 'Cancel'}
              </Button>
              <Button
                onClick={handleSelect}
                disabled={isLoading || createTemplate.isPending || editingSlots.length === 0}
              >
                {isLoading || createTemplate.isPending
                  ? (language === 'km' ? 'កំពុងបង្កើត...' : 'Creating...')
                  : (language === 'km' ? 'បង្កើត' : 'Create')}
              </Button>
            </DialogFooter>
          </>
        )}
      </DialogContent>
    </Dialog>
  )
}

interface TemplateCardProps {
  template: TimeSlotTemplate
  isSelected: boolean
  onSelect: () => void
  language: string
}

function TemplateCard({ template, isSelected, onSelect, language }: TemplateCardProps) {
  // Sort slots by start time for preview
  const sortedSlots = [...template.slots].sort((a, b) => a.startTime.localeCompare(b.startTime))

  return (
    <div
      className={cn(
        'rounded-lg border p-4 transition-colors',
        isSelected
          ? 'border-primary bg-primary/5'
          : 'hover:border-muted-foreground/50'
      )}
    >
      <div
        className="flex cursor-pointer items-center justify-between"
        onClick={onSelect}
        role="button"
        tabIndex={0}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault()
            onSelect()
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
        <div className="flex items-center gap-2">
          <Popover>
            <PopoverTrigger asChild>
              <Button
                variant="ghost"
                size="sm"
                className="h-8 gap-1.5 px-2"
                onClick={(e) => e.stopPropagation()}
              >
                <Eye className="h-4 w-4" />
                <span className="hidden sm:inline">
                  {language === 'km' ? 'មើល' : 'Preview'}
                </span>
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-72 p-0" align="end">
              <div className="border-b bg-muted/50 px-3 py-2">
                <div className="font-medium">
                  {language === 'km' ? template.nameKm : template.name}
                </div>
                <div className="text-xs text-muted-foreground">
                  {template.periodCount} {language === 'km' ? 'មុខវិជ្ជា' : 'periods'}
                </div>
              </div>
              <ScrollArea className="max-h-64">
                <div className="divide-y">
                  {sortedSlots.map((slot, index) => (
                    <div
                      key={index}
                      className={cn(
                        'flex items-center gap-3 px-3 py-2',
                        slot.isBreak && 'bg-amber-50/50 dark:bg-amber-950/20'
                      )}
                    >
                      <div className="w-20 shrink-0 text-xs text-muted-foreground">
                        {slot.startTime} - {slot.endTime}
                      </div>
                      <div className="flex-1">
                        {slot.isBreak ? (
                          <span className="flex items-center gap-1 text-xs text-amber-700 dark:text-amber-400">
                            <Coffee className="h-3 w-3" />
                            {language === 'km' ? slot.labelKm : slot.label}
                          </span>
                        ) : (
                          <span className="text-sm">
                            {language === 'km' ? slot.labelKm : slot.label}
                          </span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </ScrollArea>
            </PopoverContent>
          </Popover>
          {isSelected && (
            <Check className="h-5 w-5 text-primary" />
          )}
        </div>
      </div>
    </div>
  )
}

interface SlotEditorProps {
  slot: TimeSlot
  index: number
  language: string
  onChange: (index: number, field: keyof TimeSlot, value: string | number | boolean | null) => void
  onRemove: (index: number) => void
}

function SlotEditor({ slot, index, language, onChange, onRemove }: SlotEditorProps) {
  return (
    <div
      className={cn(
        'rounded-lg border p-3',
        slot.isBreak ? 'bg-amber-50/50 dark:bg-amber-950/20' : 'bg-muted/30'
      )}
    >
      {/* Header row: Period/Break indicator + Delete button */}
      <div className="mb-2 flex items-center justify-between">
        {slot.isBreak ? (
          <span className="flex items-center gap-1.5 text-sm font-medium text-amber-700 dark:text-amber-400">
            <Coffee className="h-4 w-4" />
            {language === 'km' ? 'សម្រាក' : 'Break'}
          </span>
        ) : (
          <span className="text-sm font-medium">
            {language === 'km' ? `មុខវិជ្ជាទី ${slot.periodNumber}` : `Period ${slot.periodNumber}`}
          </span>
        )}
        <Button
          type="button"
          variant="ghost"
          size="icon"
          onClick={() => onRemove(index)}
          className="h-7 w-7 text-destructive hover:bg-destructive/10 hover:text-destructive"
        >
          <Trash2 className="h-4 w-4" />
        </Button>
      </div>

      {/* Time row */}
      <div className="mb-2 flex items-center gap-2">
        <div className="flex-1">
          <Label className="mb-1 block text-xs text-muted-foreground">
            {language === 'km' ? 'ចាប់ផ្តើម' : 'Start'}
          </Label>
          <Input
            type="time"
            value={slot.startTime}
            onChange={(e) => onChange(index, 'startTime', e.target.value)}
            className="h-9"
          />
        </div>
        <span className="mt-5 text-muted-foreground">→</span>
        <div className="flex-1">
          <Label className="mb-1 block text-xs text-muted-foreground">
            {language === 'km' ? 'បញ្ចប់' : 'End'}
          </Label>
          <Input
            type="time"
            value={slot.endTime}
            onChange={(e) => onChange(index, 'endTime', e.target.value)}
            className="h-9"
          />
        </div>
      </div>

      {/* Labels row */}
      <div className="flex items-center gap-2">
        <div className="flex-1">
          <Label className="mb-1 block text-xs text-muted-foreground">
            {language === 'km' ? 'ស្លាក (EN)' : 'Label (EN)'}
          </Label>
          <Input
            value={slot.label}
            onChange={(e) => onChange(index, 'label', e.target.value)}
            placeholder="Period 1"
            className="h-9"
          />
        </div>
        <div className="flex-1">
          <Label className="mb-1 block text-xs text-muted-foreground">
            {language === 'km' ? 'ស្លាក (ខ្មែរ)' : 'Label (KM)'}
          </Label>
          <Input
            value={slot.labelKm}
            onChange={(e) => onChange(index, 'labelKm', e.target.value)}
            placeholder="មុខវិជ្ជាទី១"
            className="h-9"
          />
        </div>
      </div>
    </div>
  )
}
