import { useMemo } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Loader2, CalendarIcon } from 'lucide-react'
import { format } from 'date-fns'
import { formatDateDisplay } from '@/lib/khmer-calendar'

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
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
import { Textarea } from '@/components/ui/textarea'
import { Calendar } from '@/components/ui/calendar'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { Badge } from '@/components/ui/badge'
import { cn } from '@/lib/utils'
import { useLanguage } from '@/context/language-provider'
import { studentService } from '@/services/student.service'
import { useClasses } from '@/hooks/use-classes'
import type { Student, TransferStudentRequest } from '@/types/student.types'

const _transferSchema = z.object({
  targetClassId: z.string(),
  transferDate: z.date(),
  reason: z.string(),
})

type FormData = z.infer<typeof _transferSchema>

interface TransferStudentModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  student: Student | null
}

export function TransferStudentModal({ open, onOpenChange, student }: TransferStudentModalProps) {
  const { t, translateError, language } = useLanguage()
  const queryClient = useQueryClient()

  // Create schema with translated messages
  const transferSchema = useMemo(() => {
    return z.object({
      targetClassId: z.string().min(1, t.validation.required),
      transferDate: z.date({ required_error: t.validation.required }),
      reason: z.string().min(1, t.validation.required).max(500),
    })
  }, [t])

  // Fetch classes from global store
  const { getEligibleDestinations } = useClasses()

  const form = useForm<FormData>({
    resolver: zodResolver(transferSchema),
    defaultValues: {
      targetClassId: '',
      reason: '',
    },
  })

  const transferMutation = useMutation({
    mutationFn: (data: TransferStudentRequest) =>
      studentService.transferStudent(student!.id, data),
    onSuccess: () => {
      toast.success(t.students.transfer.success)
      queryClient.invalidateQueries({ queryKey: ['students'] })
      onOpenChange(false)
      form.reset()
    },
    onError: (error: Error) => {
      toast.error(translateError(error.message as Parameters<typeof translateError>[0]))
    },
  })

  const onSubmit = (data: FormData) => {
    const request: TransferStudentRequest = {
      targetClassId: data.targetClassId,
      transferDate: format(data.transferDate, 'yyyy-MM-dd'),
      reason: data.reason,
    }
    transferMutation.mutate(request)
  }

  const handleClose = () => {
    onOpenChange(false)
    form.reset()
  }

  // Get eligible destination classes using the store function
  // Filters: ACTIVE status, same grade level, has capacity, not source class
  const availableClasses = useMemo(() => {
    if (!student?.currentClassId) return []
    return getEligibleDestinations(student.currentClassId)
  }, [getEligibleDestinations, student])

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className='sm:max-w-[500px] flex flex-col p-0'>
        <DialogHeader className='shrink-0 bg-muted/50 border-b px-6 py-6'>
          <DialogTitle>
            {t.students.transfer.title}
            {student && (
              <span className='font-normal text-muted-foreground ml-2'>
                - {student.fullName || `${student.firstName} ${student.lastName}`}
              </span>
            )}
          </DialogTitle>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className='flex flex-col flex-1 overflow-hidden px-6 py-4'>
            <div className='space-y-4'>
              {/* Current Class Display */}
              <div className='space-y-2'>
                <label className='text-sm font-medium'>{t.students.transfer.currentClass}</label>
                <div className='flex items-center gap-2 p-3 bg-muted rounded-md'>
                  {student?.currentClassName ? (
                    <Badge variant='secondary'>{student.currentClassName}</Badge>
                  ) : (
                    <span className='text-muted-foreground text-sm'>{t.students.transfer.noCurrentClass}</span>
                  )}
                </div>
              </div>

              <FormField
                control={form.control}
                name='targetClassId'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.students.transfer.targetClass} <span className='text-destructive'>*</span></FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue placeholder={t.students.transfer.targetClassPlaceholder} />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {availableClasses.map((cls) => (
                          <SelectItem key={cls.id} value={cls.id}>
                            {t.common.grade} {cls.grade}{cls.section ? ` - ${cls.section}` : ''} ({cls.academicYear})
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
                name='transferDate'
                render={({ field }) => (
                  <FormItem className='flex flex-col'>
                    <FormLabel>{t.students.transfer.transferDate} <span className='text-destructive'>*</span></FormLabel>
                    <Popover>
                      <PopoverTrigger asChild>
                        <FormControl>
                          <Button
                            variant='outline'
                            className={cn(
                              'w-full justify-start text-left font-normal',
                              !field.value && 'text-muted-foreground'
                            )}
                          >
                            <CalendarIcon className='mr-2 h-4 w-4' />
                            {field.value ? (
                              formatDateDisplay(field.value, language)
                            ) : (
                              <span>{t.students.transfer.transferDatePlaceholder}</span>
                            )}
                          </Button>
                        </FormControl>
                      </PopoverTrigger>
                      <PopoverContent className='w-auto p-0' align='start'>
                        <Calendar
                          mode='single'
                          captionLayout='dropdown'
                          startMonth={new Date(2020, 0)}
                          endMonth={new Date(new Date().getFullYear() + 1, 11)}
                          defaultMonth={field.value || new Date()}
                          selected={field.value}
                          onSelect={field.onChange}
                        />
                      </PopoverContent>
                    </Popover>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name='reason'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.students.transfer.reason} <span className='text-destructive'>*</span></FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder={t.students.transfer.reasonPlaceholder}
                        className='resize-none h-24'
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <DialogFooter className='shrink-0 gap-2 pt-6'>
              <Button type='button' variant='outline' onClick={handleClose}>
                {t.students.transfer.cancel}
              </Button>
              <Button type='submit' disabled={transferMutation.isPending}>
                {transferMutation.isPending ? (
                  <>
                    <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                    {t.students.transfer.transferring}
                  </>
                ) : (
                  t.students.transfer.submit
                )}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
