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
import { cn } from '@/lib/utils'
import { useLanguage } from '@/context/language-provider'
import { studentService } from '@/services/student.service'
import { useClasses } from '@/hooks/use-classes'
import type { Student, EnrollStudentRequest } from '@/types/student.types'

const baseEnrollSchema = z.object({
  classId: z.string(),
  enrollmentDate: z.date(),
  notes: z.string().optional(),
})

type FormData = z.infer<typeof baseEnrollSchema>

interface EnrollStudentModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  student: Student | null
}

export function EnrollStudentModal({ open, onOpenChange, student }: EnrollStudentModalProps) {
  const { t, translateError, language } = useLanguage()
  const queryClient = useQueryClient()

  // Create schema with translated messages
  const enrollSchema = useMemo(() => {
    return z.object({
      classId: z.string().min(1, t.validation.required),
      enrollmentDate: z.date({ required_error: t.validation.required }),
      notes: z.string().max(500).optional(),
    })
  }, [t])

  // Fetch classes from global store
  const { classes: allClasses } = useClasses()

  // Filter only ACTIVE classes for enrollment
  const activeClasses = useMemo(() => {
    return allClasses.filter((cls) => cls.status === 'ACTIVE')
  }, [allClasses])

  const form = useForm<FormData>({
    resolver: zodResolver(enrollSchema),
    defaultValues: {
      classId: '',
      notes: '',
    },
  })

  const enrollMutation = useMutation({
    mutationFn: (data: EnrollStudentRequest) =>
      studentService.enrollStudent(student!.id, data),
    onSuccess: () => {
      toast.success(t.students.enroll.success)
      queryClient.invalidateQueries({ queryKey: ['students'] })
      onOpenChange(false)
      form.reset()
    },
    onError: (error: Error) => {
      toast.error(translateError(error.message as Parameters<typeof translateError>[0]))
    },
  })

  const onSubmit = (data: FormData) => {
    const request: EnrollStudentRequest = {
      classId: data.classId,
      enrollmentDate: format(data.enrollmentDate, 'yyyy-MM-dd'),
      notes: data.notes || undefined,
    }
    enrollMutation.mutate(request)
  }

  const handleClose = () => {
    onOpenChange(false)
    form.reset()
  }

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className='sm:max-w-[500px] flex flex-col p-0'>
        <DialogHeader className='shrink-0 bg-muted/50 border-b px-6 py-6'>
          <DialogTitle>
            {t.students.enroll.title}
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
              <FormField
                control={form.control}
                name='classId'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.students.enroll.class} <span className='text-destructive'>*</span></FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue placeholder={t.students.enroll.classPlaceholder} />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {activeClasses.map((cls) => (
                          <SelectItem key={cls.id} value={cls.id}>
                            Grade {cls.grade}{cls.section ? ` - ${cls.section}` : ''} ({cls.academicYear})
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
                name='enrollmentDate'
                render={({ field }) => (
                  <FormItem className='flex flex-col'>
                    <FormLabel>{t.students.enroll.enrollmentDate} <span className='text-destructive'>*</span></FormLabel>
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
                              <span>{t.students.enroll.enrollmentDatePlaceholder}</span>
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
                name='notes'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.students.enroll.notes}</FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder={t.students.enroll.notesPlaceholder}
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
                {t.students.enroll.cancel}
              </Button>
              <Button type='submit' disabled={enrollMutation.isPending}>
                {enrollMutation.isPending ? (
                  <>
                    <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                    {t.students.enroll.enrolling}
                  </>
                ) : (
                  t.students.enroll.submit
                )}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
