import { useMemo } from 'react'
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
import { Checkbox } from '@/components/ui/checkbox'
import { Textarea } from '@/components/ui/textarea'
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
import type { CreateSubjectRequest } from '@/types/subject.types'

interface AddSubjectModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function AddSubjectModal({ open, onOpenChange }: AddSubjectModalProps) {
  const { t, translateError } = useLanguage()
  const queryClient = useQueryClient()

  // Get available grades based on teacher's school type
  const { availableGrades } = useAvailableLevels()

  // Create schema with translated messages
  const createSubjectSchema = useMemo(() => {
    return z.object({
      name: z.string().min(1, t.validation.required).max(100),
      nameKhmer: z.string().min(1, t.validation.required).max(100),
      code: z.string().min(1, t.validation.required).max(20),
      description: z.string().optional(),
      displayOrder: z.coerce.number().min(1),
      gradeLevels: z.array(z.number()).min(1, t.validation.required),
    })
  }, [t])

  type FormData = z.infer<typeof createSubjectSchema>

  const form = useForm<FormData>({
    resolver: zodResolver(createSubjectSchema),
    defaultValues: {
      name: '',
      nameKhmer: '',
      code: '',
      description: '',
      displayOrder: 1,
      gradeLevels: [],
    },
  })

  const createMutation = useMutation({
    mutationFn: (data: CreateSubjectRequest) => subjectService.createSubject(data),
    onSuccess: () => {
      toast.success(t.subjects.modal.success.created)
      queryClient.invalidateQueries({ queryKey: ['subjects'] })
      handleClose()
    },
    onError: (error: Error) => {
      toast.error(translateError(error.message as Parameters<typeof translateError>[0]))
    },
  })

  const onSubmit = (data: FormData) => {
    const request: CreateSubjectRequest = {
      name: data.name,
      nameKhmer: data.nameKhmer,
      code: data.code,
      description: data.description || undefined,
      displayOrder: data.displayOrder,
      gradeLevels: data.gradeLevels,
    }
    createMutation.mutate(request)
  }

  const handleClose = () => {
    form.reset()
    onOpenChange(false)
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
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className='sm:max-w-[600px] top-[25%] translate-y-[-25%] p-0 flex flex-col'>
        <DialogHeader className='shrink-0 bg-muted/50 border-b px-6 py-6'>
          <DialogTitle>{t.subjects.modal.addTitle}</DialogTitle>
        </DialogHeader>

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
              name='description'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>{t.subjects.modal.fields.description}</FormLabel>
                  <FormControl>
                    <Textarea
                      placeholder={t.subjects.modal.fields.descriptionPlaceholder}
                      className='resize-none'
                      rows={2}
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

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
                          id={`add-grade-${grade}`}
                          // eslint-disable-next-line react-hooks/incompatible-library
                          checked={form.watch('gradeLevels').includes(grade)}
                          onCheckedChange={(checked) => handleGradeLevelChange(grade, !!checked)}
                        />
                        <label
                          htmlFor={`add-grade-${grade}`}
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
              <Button type='button' variant='outline' onClick={handleClose}>
                {t.subjects.modal.buttons.cancel}
              </Button>
              <Button type='submit' disabled={createMutation.isPending}>
                {createMutation.isPending ? (
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
      </DialogContent>
    </Dialog>
  )
}
