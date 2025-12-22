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
import type { AssessmentType, UpdateAssessmentTypeRequest, AssessmentCategory } from '@/types/assessment-type.types'

const _baseAssessmentTypeSchema = z.object({
  name: z.string(),
  nameKhmer: z.string(),
  code: z.string(),
  category: z.enum(['MONTHLY_EXAM', 'SEMESTER_EXAM'] as const),
  defaultWeight: z.number(),
  maxScore: z.number(),
  displayOrder: z.number(),
})

type FormData = z.infer<typeof _baseAssessmentTypeSchema>

interface EditAssessmentTypeModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  assessmentTypeId: string | null
}

interface EditAssessmentTypeFormProps {
  assessmentTypeData: AssessmentType
  onClose: () => void
  assessmentTypeId: string
}

const CATEGORIES: AssessmentCategory[] = ['MONTHLY_EXAM', 'SEMESTER_EXAM']

function EditAssessmentTypeForm({ assessmentTypeData, onClose, assessmentTypeId }: EditAssessmentTypeFormProps) {
  const { t, translateError } = useLanguage()
  const queryClient = useQueryClient()

  const updateAssessmentTypeSchema = useMemo(() => {
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

  const form = useForm<FormData>({
    resolver: zodResolver(updateAssessmentTypeSchema),
    defaultValues: {
      name: assessmentTypeData.name,
      nameKhmer: assessmentTypeData.nameKhmer,
      code: assessmentTypeData.code,
      category: assessmentTypeData.category,
      defaultWeight: assessmentTypeData.defaultWeight,
      maxScore: assessmentTypeData.maxScore,
      displayOrder: assessmentTypeData.displayOrder,
    },
  })

  const updateMutation = useMutation({
    mutationFn: (data: UpdateAssessmentTypeRequest) =>
      assessmentTypeService.updateAssessmentType(assessmentTypeId, data),
    onSuccess: () => {
      toast.success(t.assessmentTypes.modal.success.updated)
      queryClient.invalidateQueries({ queryKey: ['assessmentTypes'] })
      queryClient.invalidateQueries({ queryKey: ['assessmentType', assessmentTypeId] })
      onClose()
    },
    onError: (error: Error) => {
      toast.error(translateError(error.message as Parameters<typeof translateError>[0]))
    },
  })

  const onSubmit = (data: FormData) => {
    const request: UpdateAssessmentTypeRequest = {
      name: data.name,
      nameKhmer: data.nameKhmer,
      code: data.code,
      category: data.category,
      defaultWeight: data.defaultWeight,
      maxScore: data.maxScore,
      displayOrder: data.displayOrder,
    }
    updateMutation.mutate(request)
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
          <Button type='button' variant='outline' onClick={onClose}>
            {t.assessmentTypes.modal.buttons.cancel}
          </Button>
          <Button type='submit' disabled={updateMutation.isPending}>
            {updateMutation.isPending ? (
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
  )
}

export function EditAssessmentTypeModal({ open, onOpenChange, assessmentTypeId }: EditAssessmentTypeModalProps) {
  const { t } = useLanguage()

  const { data: assessmentTypeData, isLoading: isLoadingAssessmentType } = useQuery({
    queryKey: ['assessmentType', assessmentTypeId],
    queryFn: () => assessmentTypeService.getAssessmentType(assessmentTypeId!),
    enabled: !!assessmentTypeId && open,
  })

  const handleClose = () => {
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className='sm:max-w-[600px] top-[25%] translate-y-[-25%] p-0 flex flex-col'>
        <DialogHeader className='shrink-0 bg-muted/50 border-b px-6 py-6'>
          <DialogTitle>{t.assessmentTypes.modal.editTitle}</DialogTitle>
        </DialogHeader>

        {isLoadingAssessmentType || !assessmentTypeData ? (
          <div className='flex items-center justify-center py-8'>
            <Loader2 className='h-8 w-8 animate-spin text-muted-foreground' />
          </div>
        ) : (
          <EditAssessmentTypeForm
            key={assessmentTypeData.id}
            assessmentTypeData={assessmentTypeData}
            onClose={handleClose}
            assessmentTypeId={assessmentTypeId!}
          />
        )}
      </DialogContent>
    </Dialog>
  )
}
