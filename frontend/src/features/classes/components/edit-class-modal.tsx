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
import { Textarea } from '@/components/ui/textarea'
import { useLanguage } from '@/context/language-provider'
import { classService } from '@/services/class.service'
import type { Class, ClassStatus, UpdateClassRequest } from '@/types/class.types'

// Grade options (1-12)
const GRADE_OPTIONS = Array.from({ length: 12 }, (_, i) => ({
  label: `Grade ${i + 1}`,
  value: String(i + 1),
}))

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
  name: z.string(),
  description: z.string().optional(),
  academicYear: z.string(),
  grade: z.string(),
  section: z.string().optional(),
  capacity: z.number(),
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
      name: z.string().min(1, t.validation.required).max(100),
      description: z.string().max(500).optional(),
      academicYear: z.string().min(1, t.validation.required),
      grade: z.string().min(1, t.validation.required),
      section: z.string().max(10).optional(),
      capacity: z.coerce.number().min(1, t.validation.required).max(100),
      status: z.enum(['ACTIVE', 'INACTIVE', 'COMPLETED'] as const),
    })
  }, [t])

  const statusOptions: { value: ClassStatus; label: string }[] = [
    { value: 'ACTIVE', label: t.classes.status.ACTIVE },
    { value: 'INACTIVE', label: t.classes.status.INACTIVE },
    { value: 'COMPLETED', label: t.classes.status.COMPLETED },
  ]

  const form = useForm<FormData>({
    resolver: zodResolver(updateClassSchema),
    defaultValues: {
      name: classData.name,
      description: classData.description || '',
      academicYear: classData.academicYear,
      grade: classData.grade,
      section: classData.section || '',
      capacity: classData.capacity,
      status: classData.status,
    },
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
      ...data,
      description: data.description || undefined,
      section: data.section || undefined,
    }
    updateMutation.mutate(request)
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className='space-y-4'>
        <FormField
          control={form.control}
          name='name'
          render={({ field }) => (
            <FormItem>
              <FormLabel>{t.classes.modal.fields.name}</FormLabel>
              <FormControl>
                <Input
                  placeholder={t.classes.modal.fields.namePlaceholder}
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className='grid grid-cols-2 gap-4'>
          <FormField
            control={form.control}
            name='academicYear'
            render={({ field }) => (
              <FormItem>
                <FormLabel>{t.classes.modal.fields.academicYear}</FormLabel>
                <Select onValueChange={field.onChange} defaultValue={field.value}>
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
            name='grade'
            render={({ field }) => (
              <FormItem>
                <FormLabel>{t.classes.modal.fields.grade}</FormLabel>
                <Select onValueChange={field.onChange} defaultValue={field.value}>
                  <FormControl>
                    <SelectTrigger className='w-full'>
                      <SelectValue
                        placeholder={t.classes.modal.fields.gradePlaceholder}
                      />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {GRADE_OPTIONS.map((option) => (
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
            name='capacity'
            render={({ field }) => (
              <FormItem>
                <FormLabel>{t.classes.modal.fields.capacity}</FormLabel>
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

        <FormField
          control={form.control}
          name='status'
          render={({ field }) => (
            <FormItem>
              <FormLabel>{t.classes.modal.fields.status}</FormLabel>
              <Select onValueChange={field.onChange} defaultValue={field.value}>
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

        <FormField
          control={form.control}
          name='description'
          render={({ field }) => (
            <FormItem>
              <FormLabel>{t.classes.modal.fields.description}</FormLabel>
              <FormControl>
                <Textarea
                  placeholder={t.classes.modal.fields.descriptionPlaceholder}
                  className='resize-none h-20'
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <DialogFooter className='gap-2'>
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
      <DialogContent className='sm:max-w-[600px]'>
        <DialogHeader>
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
