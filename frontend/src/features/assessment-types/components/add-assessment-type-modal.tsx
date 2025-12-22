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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { useLanguage } from '@/context/language-provider'
import { assessmentTypeService } from '@/services/assessment-type.service'
import type { CreateAssessmentTypeRequest, AssessmentCategory } from '@/types/assessment-type.types'

interface AddAssessmentTypeModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

const CATEGORIES: AssessmentCategory[] = ['MONTHLY_EXAM', 'SEMESTER_EXAM']

export function AddAssessmentTypeModal({ open, onOpenChange }: AddAssessmentTypeModalProps) {
  const { t, translateError } = useLanguage()
  const queryClient = useQueryClient()

  const createAssessmentTypeSchema = useMemo(() => {
    return z.object({
      name: z.string().min(1, t.validation.required).max(100),
      nameKhmer: z.string().min(1, t.validation.required).max(100),
      code: z.string().min(1, t.validation.required).max(30),
      category: z.enum(['MONTHLY_EXAM', 'SEMESTER_EXAM'] as const),
      defaultWeight: z.coerce.number().min(0).max(100),
      maxScore: z.coerce.number().min(1).max(1000),
      displayOrder: z.coerce.number().min(1),
    })
  }, [t])

  type FormData = z.infer<typeof createAssessmentTypeSchema>

  const form = useForm<FormData>({
    resolver: zodResolver(createAssessmentTypeSchema),
    defaultValues: {
      name: '',
      nameKhmer: '',
      code: '',
      category: 'MONTHLY_EXAM',
      defaultWeight: 0,
      maxScore: 100,
      displayOrder: 1,
    },
  })

  const createMutation = useMutation({
    mutationFn: (data: CreateAssessmentTypeRequest) => assessmentTypeService.createAssessmentType(data),
    onSuccess: () => {
      toast.success(t.assessmentTypes.modal.success.created)
      queryClient.invalidateQueries({ queryKey: ['assessmentTypes'] })
      handleClose()
    },
    onError: (error: Error) => {
      toast.error(translateError(error.message as Parameters<typeof translateError>[0]))
    },
  })

  const onSubmit = (data: FormData) => {
    const request: CreateAssessmentTypeRequest = {
      name: data.name,
      nameKhmer: data.nameKhmer,
      code: data.code,
      category: data.category,
      defaultWeight: data.defaultWeight,
      maxScore: data.maxScore,
      displayOrder: data.displayOrder,
    }
    createMutation.mutate(request)
  }

  const handleClose = () => {
    form.reset()
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className='sm:max-w-[600px] top-[25%] translate-y-[-25%] p-0 flex flex-col'>
        <DialogHeader className='shrink-0 bg-muted/50 border-b px-6 py-6'>
          <DialogTitle>{t.assessmentTypes.modal.addTitle}</DialogTitle>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className='flex flex-col flex-1 overflow-hidden px-6 pt-4 space-y-4'>
            <div className='grid grid-cols-2 gap-4'>
              <FormField
                control={form.control}
                name='name'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.assessmentTypes.columns.name} <span className='text-destructive'>*</span></FormLabel>
                    <FormControl>
                      <Input
                        placeholder={t.assessmentTypes.modal.fields.namePlaceholder}
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
                    <FormLabel>{t.assessmentTypes.columns.nameKhmer} <span className='text-destructive'>*</span></FormLabel>
                    <FormControl>
                      <Input
                        placeholder={t.assessmentTypes.modal.fields.nameKhmerPlaceholder}
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
                    <FormLabel>{t.assessmentTypes.columns.code} <span className='text-destructive'>*</span></FormLabel>
                    <FormControl>
                      <Input
                        placeholder={t.assessmentTypes.modal.fields.codePlaceholder}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name='category'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.assessmentTypes.columns.category} <span className='text-destructive'>*</span></FormLabel>
                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder={t.assessmentTypes.modal.fields.categoryPlaceholder} />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {CATEGORIES.map((category) => (
                          <SelectItem key={category} value={category}>
                            {t.assessmentTypes.categories[category]}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <div className='grid grid-cols-3 gap-4'>
              <FormField
                control={form.control}
                name='defaultWeight'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.assessmentTypes.columns.defaultWeight} <span className='text-destructive'>*</span></FormLabel>
                    <FormControl>
                      <Input
                        type='number'
                        min={0}
                        max={100}
                        step={0.01}
                        placeholder={t.assessmentTypes.modal.fields.defaultWeightPlaceholder}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name='maxScore'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.assessmentTypes.columns.maxScore} <span className='text-destructive'>*</span></FormLabel>
                    <FormControl>
                      <Input
                        type='number'
                        min={1}
                        max={1000}
                        placeholder={t.assessmentTypes.modal.fields.maxScorePlaceholder}
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
                    <FormLabel>{t.assessmentTypes.modal.fields.displayOrder} <span className='text-destructive'>*</span></FormLabel>
                    <FormControl>
                      <Input
                        type='number'
                        min={1}
                        placeholder={t.assessmentTypes.modal.fields.displayOrderPlaceholder}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <DialogFooter className='shrink-0 gap-2 px-6 py-4 -mx-6 border-t mt-auto'>
              <Button type='button' variant='outline' onClick={handleClose}>
                {t.assessmentTypes.modal.buttons.cancel}
              </Button>
              <Button type='submit' disabled={createMutation.isPending}>
                {createMutation.isPending ? (
                  <>
                    <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                    {t.assessmentTypes.modal.buttons.saving}
                  </>
                ) : (
                  t.assessmentTypes.modal.buttons.save
                )}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
