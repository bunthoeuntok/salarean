import { useMemo, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
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
import { useAcademicYearStore } from '@/store/academic-year-store'
import { useClassFiltering } from '@/hooks/use-class-filtering'
import { useAvailableLevels } from '@/hooks/use-available-levels'
import type { Class, ClassLevel, ClassType, UpdateClassRequest } from '@/types/class.types'

const _baseClassSchema = z.object({
  grade: z.string(),
  section: z.string().optional(),
  maxCapacity: z.number(),
  level: z.enum(['PRIMARY', 'SECONDARY', 'HIGH_SCHOOL'] as const),
  type: z.enum(['NORMAL', 'SCIENCE', 'SOCIAL_SCIENCE'] as const)
})

type FormData = z.infer<typeof _baseClassSchema>

interface EditClassModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  classId: string | null
}

interface EditClassFormProps {
  classData: Class
  onClose: () => void
  classId: string
}

// Inner form component - only mounts when classData is available
function EditClassForm({ classData, onClose, classId }: EditClassFormProps) {
  const { t, translateError } = useLanguage()
  const queryClient = useQueryClient()

  // Get academic year from store
  const selectedAcademicYear = useAcademicYearStore((state) => state.selectedAcademicYear)

  // Get available levels based on teacher's school type
  const { availableLevels } = useAvailableLevels()

  // Create schema with translated messages
  const updateClassSchema = useMemo(() => {
    return z.object({
      grade: z.string().min(1, t.validation.required),
      section: z.string().max(10).optional(),
      maxCapacity: z.coerce.number().min(1, t.validation.required).max(100),
      level: z.enum(['PRIMARY', 'SECONDARY', 'HIGH_SCHOOL'] as const),
      type: z.enum(['NORMAL', 'SCIENCE', 'SOCIAL_SCIENCE'] as const)
    })
  }, [t])

  // Filter level options based on school type
  const levelOptions: { value: ClassLevel; label: string }[] = useMemo(() => {
    const allLevels: { value: ClassLevel; label: string }[] = [
      { value: 'PRIMARY', label: t.classes.level.PRIMARY },
      { value: 'SECONDARY', label: t.classes.level.SECONDARY },
      { value: 'HIGH_SCHOOL', label: t.classes.level.HIGH_SCHOOL },
    ]
    return allLevels.filter((option) => availableLevels.includes(option.value))
  }, [t, availableLevels])

  const form = useForm<FormData>({
    resolver: zodResolver(updateClassSchema),
    defaultValues: {
      grade: String(classData.grade),
      section: classData.section || '',
      maxCapacity: classData.maxCapacity,
      level: classData.level,
      type: classData.type,
    },
  })

  // Watch grade for type filtering
  // eslint-disable-next-line react-hooks/incompatible-library
  const selectedGrade = form.watch('grade')

  // Use class filtering hook for level → grade filtering with form integration
  const { filteredGradeOptions } = useClassFiltering({
    initialLevel: form.watch('level'),
    availableLevels,
    onGradeCleared: () => form.setValue('grade', ''),
  })

  // Filter type options based on grade (11-12 = SCIENCE/SOCIAL_SCIENCE, others = NORMAL only)
  const typeOptions = useMemo(() => {
    const gradeNum = Number(selectedGrade)
    if (gradeNum === 11 || gradeNum === 12) {
      return [
        { value: 'SCIENCE' as ClassType, label: t.classes.type.SCIENCE },
        { value: 'SOCIAL_SCIENCE' as ClassType, label: t.classes.type.SOCIAL_SCIENCE },
      ]
    }
    return [{ value: 'NORMAL' as ClassType, label: t.classes.type.NORMAL }]
  }, [selectedGrade, t])

  // Auto-reset type when grade changes and current type is invalid
  useEffect(() => {
    const gradeNum = Number(selectedGrade)
    const currentType = form.getValues('type')
    if (gradeNum === 11 || gradeNum === 12) {
      if (currentType === 'NORMAL') {
        form.setValue('type', 'SCIENCE')
      }
    } else if (selectedGrade) {
      if (currentType !== 'NORMAL') {
        form.setValue('type', 'NORMAL')
      }
    }
  }, [selectedGrade, form])

  const updateMutation = useMutation({
    mutationFn: (data: UpdateClassRequest) => classService.updateClass(classId, data),
    onSuccess: () => {
      toast.success(t.classes.modal.success.updated)
      queryClient.invalidateQueries({ queryKey: ['classes'] })
      queryClient.invalidateQueries({ queryKey: ['class', classId] })
      onClose()
    },
    onError: (error: Error) => {
      toast.error(translateError(error.message as Parameters<typeof translateError>[0]))
    },
  })

  const onSubmit = (data: FormData) => {
    const request: UpdateClassRequest = {
      academicYear: selectedAcademicYear,
      grade: Number(data.grade),
      section: data.section || undefined,
      maxCapacity: data.maxCapacity,
      level: data.level,
      type: data.type
    }
    updateMutation.mutate(request)
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className='flex flex-col flex-1 overflow-hidden px-6 pt-4 space-y-4'>
        <div className='grid grid-cols-2 gap-4'>
          <FormField
            control={form.control}
            name='level'
            render={({ field }) => (
              <FormItem>
                <FormLabel>{t.classes.modal.fields.level} <span className='text-destructive'>*</span></FormLabel>
                <Select onValueChange={field.onChange} value={field.value} disabled={levelOptions.length <= 1}>
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
        </div>

        <div className='grid grid-cols-2 gap-4'>
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
        </div>

        <div className='grid grid-cols-2 gap-4'>
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
          <Button type='button' variant='outline' onClick={onClose}>
            {t.classes.modal.buttons.cancel}
          </Button>
          <Button type='submit' disabled={updateMutation.isPending}>
            {updateMutation.isPending ? (
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
  )
}

// Main modal component
export function EditClassModal({ open, onOpenChange, classId }: EditClassModalProps) {
  const { t } = useLanguage()

  // Fetch class data
  const { data: classData, isLoading: isLoadingClass } = useQuery({
    queryKey: ['class', classId],
    queryFn: () => classService.getClass(classId!),
    enabled: !!classId && open,
  })

  const handleClose = () => {
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className='sm:max-w-[900px] top-[25%] translate-y-[-25%] p-0 flex flex-col'>
        <DialogHeader className='shrink-0 bg-muted/50 border-b px-6 py-6'>
          <DialogTitle>{t.classes.modal.editTitle}</DialogTitle>
        </DialogHeader>

        {isLoadingClass || !classData ? (
          <div className='flex items-center justify-center py-8'>
            <Loader2 className='h-8 w-8 animate-spin text-muted-foreground' />
          </div>
        ) : (
          <EditClassForm
            key={classData.id}
            classData={classData}
            onClose={handleClose}
            classId={classId!}
          />
        )}
      </DialogContent>
    </Dialog>
  )
}
