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
import { Checkbox } from '@/components/ui/checkbox'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { useLanguage } from '@/context/language-provider'
import { subjectService } from '@/services/subject.service'
import { useAvailableLevels } from '@/hooks/use-available-levels'
import type { Subject, UpdateSubjectRequest } from '@/types/subject.types'

const _baseSubjectSchema = z.object({
  name: z.string(),
  nameKhmer: z.string(),
  code: z.string(),
  displayOrder: z.number(),
  gradeLevels: z.array(z.number()),
})

type FormData = z.infer<typeof _baseSubjectSchema>

interface EditSubjectModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  subjectId: string | null
}

interface EditSubjectFormProps {
  subjectData: Subject
  onClose: () => void
  subjectId: string
}

// Inner form component - only mounts when subjectData is available
function EditSubjectForm({ subjectData, onClose, subjectId }: EditSubjectFormProps) {
  const { t, translateError } = useLanguage()
  const queryClient = useQueryClient()

  // Get available grades based on teacher's school type
  const { availableGrades } = useAvailableLevels()

  // Create schema with translated messages
  const updateSubjectSchema = useMemo(() => {
    return z.object({
      name: z.string().min(1, t.validation.required).max(100),
      nameKhmer: z.string().min(1, t.validation.required).max(100),
      code: z.string().min(1, t.validation.required).max(20),
      displayOrder: z.coerce.number().min(1),
      gradeLevels: z.array(z.number()).min(1, t.validation.required),
    })
  }, [t])

  const form = useForm<FormData>({
    resolver: zodResolver(updateSubjectSchema),
    defaultValues: {
      name: subjectData.name,
      nameKhmer: subjectData.nameKhmer,
      code: subjectData.code,
      displayOrder: subjectData.displayOrder,
      gradeLevels: subjectData.gradeLevels || [],
    },
  })

  const updateMutation = useMutation({
    mutationFn: (data: UpdateSubjectRequest) => subjectService.updateSubject(subjectId, data),
    onSuccess: () => {
      toast.success(t.subjects.modal.success.updated)
      queryClient.invalidateQueries({ queryKey: ['subjects'] })
      queryClient.invalidateQueries({ queryKey: ['subject', subjectId] })
      onClose()
    },
    onError: (error: Error) => {
      toast.error(translateError(error.message as Parameters<typeof translateError>[0]))
    },
  })

  const onSubmit = (data: FormData) => {
    const request: UpdateSubjectRequest = {
      name: data.name,
      nameKhmer: data.nameKhmer,
      code: data.code,
      displayOrder: data.displayOrder,
      gradeLevels: data.gradeLevels,
    }
    updateMutation.mutate(request)
  }

  const handleGradeLevelChange = (grade: number, checked: boolean) => {
    const currentLevels = form.getValues('gradeLevels')
    if (checked) {
      form.setValue('gradeLevels', [...currentLevels, grade].sort((a, b) => a - b))
    } else {
      form.setValue('gradeLevels', currentLevels.filter((g) => g !== grade))
    }
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className='flex flex-col flex-1 overflow-hidden px-6 pt-4 space-y-4'>
        <div className='grid grid-cols-2 gap-4'>
          <FormField
            control={form.control}
            name='name'
            render={({ field }) => (
              <FormItem>
                <FormLabel>{t.subjects.columns.name} <span className='text-destructive'>*</span></FormLabel>
                <FormControl>
                  <Input
                    placeholder={t.subjects.modal.fields.namePlaceholder}
                    {...field}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name='nameKhmer'
            render={({ field }) => (
              <FormItem>
                <FormLabel>{t.subjects.columns.nameKhmer} <span className='text-destructive'>*</span></FormLabel>
                <FormControl>
                  <Input
                    placeholder={t.subjects.modal.fields.nameKhmerPlaceholder}
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
            name='code'
            render={({ field }) => (
              <FormItem>
                <FormLabel>{t.subjects.columns.code} <span className='text-destructive'>*</span></FormLabel>
                <FormControl>
                  <Input
                    placeholder={t.subjects.modal.fields.codePlaceholder}
                    {...field}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name='displayOrder'
            render={({ field }) => (
              <FormItem>
                <FormLabel>{t.subjects.modal.fields.displayOrder} <span className='text-destructive'>*</span></FormLabel>
                <FormControl>
                  <Input
                    type='number'
                    min={1}
                    placeholder={t.subjects.modal.fields.displayOrderPlaceholder}
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
          name='gradeLevels'
          render={() => (
            <FormItem>
              <FormLabel>{t.subjects.columns.gradeLevels} <span className='text-destructive'>*</span></FormLabel>
              <div className='grid grid-cols-2 space-y-2 mt-2'>
                {availableGrades.map((grade) => (
                  <div key={grade} className='flex items-center space-x-2'>
                    <Checkbox
                      id={`grade-${grade}`}
                      // eslint-disable-next-line react-hooks/incompatible-library
                      checked={form.watch('gradeLevels').includes(grade)}
                      onCheckedChange={(checked) => handleGradeLevelChange(grade, !!checked)}
                    />
                    <label
                      htmlFor={`grade-${grade}`}
                      className='text-sm font-normal cursor-pointer'
                    >
                      {t.common.grade} {grade}
                    </label>
                  </div>
                ))}
              </div>
              <FormMessage />
            </FormItem>
          )}
        />

        <DialogFooter className='shrink-0 gap-2 px-6 py-4 -mx-6 border-t mt-auto'>
          <Button type='button' variant='outline' onClick={onClose}>
            {t.subjects.modal.buttons.cancel}
          </Button>
          <Button type='submit' disabled={updateMutation.isPending}>
            {updateMutation.isPending ? (
              <>
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                {t.subjects.modal.buttons.saving}
              </>
            ) : (
              t.subjects.modal.buttons.save
            )}
          </Button>
        </DialogFooter>
      </form>
    </Form>
  )
}

// Main modal component
export function EditSubjectModal({ open, onOpenChange, subjectId }: EditSubjectModalProps) {
  const { t } = useLanguage()

  // Fetch subject data
  const { data: subjectData, isLoading: isLoadingSubject } = useQuery({
    queryKey: ['subject', subjectId],
    queryFn: () => subjectService.getSubject(subjectId!),
    enabled: !!subjectId && open,
  })

  const handleClose = () => {
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className='sm:max-w-[600px] top-[25%] translate-y-[-25%] p-0 flex flex-col'>
        <DialogHeader className='shrink-0 bg-muted/50 border-b px-6 py-6'>
          <DialogTitle>{t.subjects.modal.editTitle}</DialogTitle>
        </DialogHeader>

        {isLoadingSubject || !subjectData ? (
          <div className='flex items-center justify-center py-8'>
            <Loader2 className='h-8 w-8 animate-spin text-muted-foreground' />
          </div>
        ) : (
          <EditSubjectForm
            key={subjectData.id}
            subjectData={subjectData}
            onClose={handleClose}
            subjectId={subjectId!}
          />
        )}
      </DialogContent>
    </Dialog>
  )
}
