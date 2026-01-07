import { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Trash2 } from 'lucide-react'
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
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Input } from '@/components/ui/input'
import { useUpdateScheduleEntry } from '@/hooks/use-schedule'
import { useQuery } from '@tanstack/react-query'
import { subjectService } from '@/services/subject.service'
import type { ScheduleEntry } from '@/types/schedule.types'
import { DAY_NAMES } from '@/types/schedule.types'

const formSchema = z.object({
  subjectId: z.string().min(1, 'Subject is required'),
  room: z.string().optional(),
  notes: z.string().optional(),
})

type FormValues = z.infer<typeof formSchema>

interface EditEntryDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  classId: string
  dayOfWeek: number
  periodNumber: number
  existingEntry?: ScheduleEntry
}

export function EditEntryDialog({
  open,
  onOpenChange,
  classId,
  dayOfWeek,
  periodNumber,
  existingEntry,
}: EditEntryDialogProps) {
  const { language, t } = useLanguage()
  const [isDeleting, setIsDeleting] = useState(false)

  const updateEntry = useUpdateScheduleEntry(classId)

  // Fetch subjects
  const { data: subjects } = useQuery({
    queryKey: ['subjects'],
    queryFn: () => subjectService.getSubjects(),
    staleTime: 10 * 60 * 1000,
  })

  const form = useForm<FormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      subjectId: existingEntry?.subjectId || '',
      room: existingEntry?.room || '',
      notes: existingEntry?.notes || '',
    },
  })

  // Reset form when entry changes
  useEffect(() => {
    if (existingEntry) {
      form.reset({
        subjectId: existingEntry.subjectId,
        room: existingEntry.room || '',
        notes: existingEntry.notes || '',
      })
    } else {
      form.reset({
        subjectId: '',
        room: '',
        notes: '',
      })
    }
  }, [existingEntry, form])

  const onSubmit = async (values: FormValues) => {
    await updateEntry.mutateAsync({
      dayOfWeek,
      periodNumber,
      subjectId: values.subjectId,
      room: values.room || undefined,
      notes: values.notes || undefined,
    })
    onOpenChange(false)
  }

  const handleDelete = async () => {
    if (!existingEntry) return
    setIsDeleting(true)
    // To delete, we update with empty/null values by not including this entry
    // For now, we'll just close the dialog - a proper implementation would
    // require a delete endpoint or updating the schedule service
    setIsDeleting(false)
    onOpenChange(false)
  }

  const dayName = DAY_NAMES[dayOfWeek][language === 'km' ? 'km' : 'en']

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>
            {existingEntry
              ? (language === 'km' ? 'កែប្រែមុខវិជ្ជា' : 'Edit Subject')
              : (language === 'km' ? 'បន្ថែមមុខវិជ្ជា' : 'Add Subject')}
          </DialogTitle>
          <DialogDescription>
            {dayName} - {language === 'km' ? `មុខវិជ្ជាទី${periodNumber}` : `Period ${periodNumber}`}
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="subjectId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>{language === 'km' ? 'មុខវិជ្ជា' : 'Subject'}</FormLabel>
                  <Select onValueChange={field.onChange} value={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue
                          placeholder={language === 'km' ? 'ជ្រើសរើសមុខវិជ្ជា' : 'Select a subject'}
                        />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {subjects?.map((subject) => (
                        <SelectItem key={subject.id} value={subject.id}>
                          {language === 'km' ? subject.nameKhmer : subject.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="room"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>{language === 'km' ? 'បន្ទប់' : 'Room'}</FormLabel>
                  <FormControl>
                    <Input
                      placeholder={language === 'km' ? 'ឧ: 101' : 'e.g., 101'}
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="notes"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>{language === 'km' ? 'កំណត់ចំណាំ' : 'Notes'}</FormLabel>
                  <FormControl>
                    <Input
                      placeholder={language === 'km' ? 'កំណត់ចំណាំបន្ថែម' : 'Additional notes'}
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <DialogFooter className="gap-2 sm:gap-0">
              {existingEntry && (
                <Button
                  type="button"
                  variant="destructive"
                  onClick={handleDelete}
                  disabled={isDeleting}
                  className="mr-auto"
                >
                  <Trash2 className="mr-2 h-4 w-4" />
                  {isDeleting
                    ? (language === 'km' ? 'កំពុងលុប...' : 'Removing...')
                    : (language === 'km' ? 'លុប' : 'Remove')}
                </Button>
              )}
              <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                {t.common?.cancel || 'Cancel'}
              </Button>
              <Button type="submit" disabled={updateEntry.isPending}>
                {updateEntry.isPending
                  ? (language === 'km' ? 'កំពុងរក្សាទុក...' : 'Saving...')
                  : (language === 'km' ? 'រក្សាទុក' : 'Save')}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
