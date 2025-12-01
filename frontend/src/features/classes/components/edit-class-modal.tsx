import { useMemo } from 'react'
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
import { useClassFiltering } from '@/hooks/useClassFiltering'
import type { Class, ClassStatus, ClassLevel, ClassType, UpdateClassRequest } from '@/types/class.types'

// Generate academic year options (current year and next 2 years)
const generateAcademicYearOptions = () => {
  const currentYear = new Date().getFullYear()
  return Array.from({ length: 5 }, (_, i) => {
    const startYear = currentYear - 2 + i
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
  status: z.enum(['ACTIVE', 'INACTIVE', 'COMPLETED'] as const),
})

type FormData = z.infer<typeof baseClassSchema>

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

  const academicYearOptions = useMemo(() => generateAcademicYearOptions(), [])

  // Create schema with translated messages
  const updateClassSchema = useMemo(() => {
    return z.object({
      academicYear: z.string().min(1, t.validation.required),
      grade: z.string().min(1, t.validation.required),
      section: z.string().max(10).optional(),
      maxCapacity: z.coerce.number().min(1, t.validation.required).max(100),
      level: z.enum(['PRIMARY', 'SECONDARY', 'HIGH_SCHOOL'] as const),
      type: z.enum(['NORMAL', 'SCIENCE', 'SOCIAL_SCIENCE'] as const),
      status: z.enum(['ACTIVE', 'INACTIVE', 'COMPLETED'] as const),
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

  const statusOptions: { value: ClassStatus; label: string }[] = [
    { value: 'ACTIVE', label: t.classes.status.ACTIVE },
    { value: 'INACTIVE', label: t.classes.status.INACTIVE },
    { value: 'COMPLETED', label: t.classes.status.COMPLETED },
  ]

  const form = useForm<FormData>({
    resolver: zodResolver(updateClassSchema),
    defaultValues: {
      academicYear: classData.academicYear,
      grade: String(classData.grade),
      section: classData.section || '',
      maxCapacity: classData.maxCapacity,
      level: classData.level,
      type: classData.type,
      status: classData.status,
    },
  })

  // Use class filtering hook for level → grade filtering with form integration
  const { filteredGradeOptions } = useClassFiltering({
    initialLevel: form.watch('level'),
    onGradeCleared: () => form.setValue('grade', ''),
  })

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
      academicYear: data.academicYear,
      grade: Number(data.grade),
      section: data.section || undefined,
      maxCapacity: data.maxCapacity,
      level: data.level,
      type: data.type,
      status: data.status,
    }
    updateMutation.mutate(request)
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className='flex flex-col flex-1 overflow-hidden px-6 pt-4 space-y-4'>
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

        <div className='grid grid-cols-2 gap-4'>
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

        <div className='grid grid-cols-2 gap-4'>
          <FormField
            control={form.control}
            name='status'
            render={({ field }) => (
              <FormItem>
                <FormLabel>{t.classes.modal.fields.status} <span className='text-destructive'>*</span></FormLabel>
                <Select onValueChange={field.onChange} value={field.value}>
                  <FormControl>
                    <SelectTrigger className='w-full'>
                      <SelectValue
                        placeholder={t.classes.modal.fields.statusPlaceholder}
                      />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {statusOptions.map((option) => (
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
