import { useState } from 'react'
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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { useCopySchedule } from '@/hooks/use-schedule'
import { useClasses } from '@/hooks/use-classes'

interface CopyScheduleDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  targetClassId: string
}

export function CopyScheduleDialog({
  open,
  onOpenChange,
  targetClassId,
}: CopyScheduleDialogProps) {
  const { language } = useLanguage()
  const [selectedClassId, setSelectedClassId] = useState<string>('')

  const { classes } = useClasses()
  const copySchedule = useCopySchedule(targetClassId)

  // Filter out the current class from the list
  const availableClasses = classes.filter((c) => c.id !== targetClassId)

  const handleCopy = async () => {
    if (!selectedClassId) return
    await copySchedule.mutateAsync(selectedClassId)
    onOpenChange(false)
    setSelectedClassId('')
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>
            {language === 'km' ? 'ចម្លងកាលវិភាគ' : 'Copy Schedule'}
          </DialogTitle>
          <DialogDescription>
            {language === 'km'
              ? 'ជ្រើសរើសថ្នាក់មួយដើម្បីចម្លងកាលវិភាគពី'
              : 'Select a class to copy the schedule from'}
          </DialogDescription>
        </DialogHeader>

        <div className="py-4">
          <Select value={selectedClassId} onValueChange={setSelectedClassId}>
            <SelectTrigger>
              <SelectValue
                placeholder={language === 'km' ? 'ជ្រើសរើសថ្នាក់' : 'Select a class'}
              />
            </SelectTrigger>
            <SelectContent>
              {availableClasses.map((cls) => (
                <SelectItem key={cls.id} value={cls.id}>
                  {cls.name} - {cls.academicYear}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {availableClasses.length === 0 && (
            <p className="mt-2 text-center text-sm text-muted-foreground">
              {language === 'km' ? 'មិនមានថ្នាក់ផ្សេងទៀតទេ' : 'No other classes available'}
            </p>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            {language === 'km' ? 'បោះបង់' : 'Cancel'}
          </Button>
          <Button
            onClick={handleCopy}
            disabled={copySchedule.isPending || !selectedClassId}
          >
            {copySchedule.isPending
              ? (language === 'km' ? 'កំពុងចម្លង...' : 'Copying...')
              : (language === 'km' ? 'ចម្លង' : 'Copy')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
