import { useMemo, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Loader2 } from 'lucide-react'

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
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
import { useLanguage } from '@/context/language-provider'
import { classService } from '@/services/class.service'
import type { CreateClassRequest, ClassLevel, ClassType } from '@/types/class.types'

// Grade options (1-12)
const GRADE_OPTIONS = Array.from({ length: 12 }, (_, i) => ({
  label: `Grade ${i + 1}`,
  value: String(i + 1),
}))

// Generate academic year options (current year and next 2 years)
const generateAcademicYearOptions = () => {
  const currentYear = new Date().getFullYear()
  return Array.from({ length: 3 }, (_, i) => {
    const startYear = currentYear + i
    const endYear = startYear + 1
    return {
      label: `${startYear}-${endYear}`,
      value: `${startYear}-${endYear}`,
    }
  })
}

const baseClassSchema = z.object({
  academicYear: z.string(),
  grade: z.string(),
  section: z.string().optional(),
  maxCapacity: z.number(),
  level: z.enum(['PRIMARY', 'SECONDARY', 'HIGH_SCHOOL'] as const),
  type: z.enum(['NORMAL', 'SCIENCE', 'SOCIAL_SCIENCE'] as const),
})

type FormData = z.infer<typeof baseClassSchema>

interface AddClassModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function AddClassModal({ open, onOpenChange }: AddClassModalProps) {
  const { t, translateError } = useLanguage()
  const queryClient = useQueryClient()

  const academicYearOptions = useMemo(() => generateAcademicYearOptions(), [])

  // Filter grade options based on selected level
  const getFilteredGradeOptions = (level: ClassLevel) => {
    const gradeRanges: Record<ClassLevel, { min: number; max: number }> = {
      PRIMARY: { min: 1, max: 6 },
      SECONDARY: { min: 7, max: 9 },
      HIGH_SCHOOL: { min: 10, max: 12 },
    }

    const range = gradeRanges[level]
    return GRADE_OPTIONS.filter((option) => {
      const grade = Number(option.value)
      return grade >= range.min && grade <= range.max
    })
  }

  // Create schema with translated messages
  const createClassSchema = useMemo(() => {
    return z.object({
      academicYear: z.string().min(1, t.validation.required),
      grade: z.string().min(1, t.validation.required),
      section: z.string().max(10).optional(),
      maxCapacity: z.coerce.number().min(1, t.validation.required).max(100),
      level: z.enum(['PRIMARY', 'SECONDARY', 'HIGH_SCHOOL'] as const, {
        required_error: t.validation.required,
      }),
      type: z.enum(['NORMAL', 'SCIENCE', 'SOCIAL_SCIENCE'] as const, {
        required_error: t.validation.required,
      }),
    })
  }, [t])

  const levelOptions: { value: ClassLevel; label: string }[] = [
    { value: 'PRIMARY', label: t.classes.level.PRIMARY },
    { value: 'SECONDARY', label: t.classes.level.SECONDARY },
    { value: 'HIGH_SCHOOL', label: t.classes.level.HIGH_SCHOOL },
  ]

  const typeOptions: { value: ClassType; label: string }[] = [
    { value: 'NORMAL', label: t.classes.type.NORMAL },
    { value: 'SCIENCE', label: t.classes.type.SCIENCE },
    { value: 'SOCIAL_SCIENCE', label: t.classes.type.SOCIAL_SCIENCE },
  ]

  const form = useForm<FormData>({
    resolver: zodResolver(createClassSchema),
    defaultValues: {
      academicYear: '',
      grade: '',
      section: '',
      maxCapacity: 30,
      level: 'PRIMARY',
      type: 'NORMAL',
    },
  })

  // Watch level field to filter grade options
  const selectedLevel = form.watch('level')
  const filteredGradeOptions = useMemo(() => {
    return getFilteredGradeOptions(selectedLevel)
  }, [selectedLevel])

  // Clear grade field if current grade is outside the new level's range
  useEffect(() => {
    const currentGrade = form.getValues('grade')
    if (currentGrade) {
      const isGradeValid = filteredGradeOptions.some(option => option.value === currentGrade)
      if (!isGradeValid) {
        form.setValue('grade', '')
      }
    }
  }, [selectedLevel, filteredGradeOptions, form])

  const createMutation = useMutation({
    mutationFn: (data: CreateClassRequest) => classService.createClass(data),
    onSuccess: () => {
      toast.success(t.classes.modal.success.created)
      queryClient.invalidateQueries({ queryKey: ['classes'] })
      onOpenChange(false)
      form.reset()
    },
    onError: (error: Error) => {
      toast.error(translateError(error.message as Parameters<typeof translateError>[0]))
    },
  })

  const onSubmit = (data: FormData) => {
    const request: CreateClassRequest = {
      academicYear: data.academicYear,
      grade: Number(data.grade),
      section: data.section || undefined,
      maxCapacity: data.maxCapacity,
      level: data.level,
      type: data.type,
    }
    createMutation.mutate(request)
  }

  const handleClose = () => {
    onOpenChange(false)
    form.reset()
  }

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className='sm:max-w-[900px] top-[25%] translate-y-[-25%]  flex flex-col p-0'>
        <DialogHeader className='shrink-0 bg-muted/50 border-b px-6 py-6'>
          <DialogTitle>{t.classes.modal.addTitle}</DialogTitle>
        </DialogHeader>

        <Form {...form} >
          <form onSubmit={form.handleSubmit(onSubmit)}  className='flex flex-col flex-1 overflow-hidden px-6 space-y-2'>
            <div className='grid grid-cols-2 gap-4'>
              <FormField
                control={form.control}
                name='academicYear'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.classes.modal.fields.academicYear} <span className='text-destructive'>*</span></FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue
                            placeholder={t.classes.modal.fields.academicYearPlaceholder}
                          />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {academicYearOptions.map((option) => (
                          <SelectItem key={option.value} value={option.value}>
                            {option.label}
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
                name='level'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.classes.modal.fields.level} <span className='text-destructive'>*</span></FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue
                            placeholder={t.classes.modal.fields.levelPlaceholder}
                          />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {levelOptions.map((option) => (
                          <SelectItem key={option.value} value={option.value}>
                            {option.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <div className='grid grid-cols-2 gap-4'>
              <FormField
                control={form.control}
                name='grade'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.classes.modal.fields.grade} <span className='text-destructive'>*</span></FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue
                            placeholder={t.classes.modal.fields.gradePlaceholder}
                          />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {filteredGradeOptions.map((option) => (
                          <SelectItem key={option.value} value={option.value}>
                            {option.label}
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
                name='type'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.classes.modal.fields.type} <span className='text-destructive'>*</span></FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue
                            placeholder={t.classes.modal.fields.typePlaceholder}
                          />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {typeOptions.map((option) => (
                          <SelectItem key={option.value} value={option.value}>
                            {option.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <div className='grid grid-cols-2 gap-4 pb-4'>
              <FormField
                control={form.control}
                name='section'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.classes.modal.fields.section}</FormLabel>
                    <FormControl>
                      <Input
                        placeholder={t.classes.modal.fields.sectionPlaceholder}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name='maxCapacity'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.classes.modal.fields.capacity} <span className='text-destructive'>*</span></FormLabel>
                    <FormControl>
                      <Input
                        type='number'
                        min={1}
                        max={100}
                        placeholder={t.classes.modal.fields.capacityPlaceholder}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <DialogFooter className='shrink-0 gap-2 px-6 py-4 -mx-6 border-t'>
              <Button type='button' variant='outline' onClick={handleClose}>
                {t.classes.modal.buttons.cancel}
              </Button>
              <Button type='submit' disabled={createMutation.isPending}>
                {createMutation.isPending ? (
                  <>
                    <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                    {t.classes.modal.buttons.saving}
                  </>
                ) : (
                  t.classes.modal.buttons.save
                )}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
