import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2, Plus } from 'lucide-react'
import { toast } from 'sonner'
import { useLanguage } from '@/context/language-provider'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { createSchool } from '@/services/location.service'
import {
  schoolCreationSchema,
  type SchoolCreationFormData,
} from '@/lib/validations/school-setup'
import { useSchoolSetupStore } from '@/store/school-setup-store'

interface AddSchoolDialogProps {
  provinceId: string
  districtId: string
}

export function AddSchoolDialog({ provinceId, districtId }: AddSchoolDialogProps) {
  const { t } = useLanguage()
  const queryClient = useQueryClient()
  const { setSchoolId } = useSchoolSetupStore()
  const [open, setOpen] = useState(false)

  const form = useForm<SchoolCreationFormData>({
    resolver: zodResolver(schoolCreationSchema),
    defaultValues: {
      name: '',
      nameKhmer: '',
      address: '',
      provinceId: provinceId,
      districtId: districtId,
      type: undefined,
    },
  })

  const createMutation = useMutation({
    mutationFn: createSchool,
    onSuccess: (newSchool) => {
      toast.success(t.schoolSetup.addSchool?.success || 'School created successfully')
      queryClient.invalidateQueries({ queryKey: ['schools', districtId] })
      setSchoolId(newSchool.id)
      setOpen(false)
      form.reset()
    },
    onError: () => {
      toast.error(t.schoolSetup.addSchool?.error || 'Failed to create school')
    },
  })

  const onSubmit = (data: SchoolCreationFormData) => {
    createMutation.mutate({
      ...data,
      provinceId,
      districtId,
    })
  }

  const handleOpenChange = (newOpen: boolean) => {
    setOpen(newOpen)
    if (!newOpen) {
      form.reset({
        name: '',
        nameKhmer: '',
        address: '',
        provinceId: provinceId,
        districtId: districtId,
        type: undefined,
      })
    }
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm">
          <Plus className="mr-2 h-4 w-4" />
          {t.schoolSetup.step1.addNewSchool}
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>{t.schoolSetup.addSchool?.title || 'Add New School'}</DialogTitle>
          <DialogDescription>
            {t.schoolSetup.addSchool?.description || 'Enter the school details to add a new school to this district.'}
          </DialogDescription>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>{t.schoolSetup.addSchool?.name || 'School Name (English)'}</FormLabel>
                  <FormControl>
                    <Input
                      placeholder={t.schoolSetup.addSchool?.namePlaceholder || 'Enter school name'}
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="nameKhmer"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>{t.schoolSetup.addSchool?.nameKhmer || 'School Name (Khmer)'}</FormLabel>
                  <FormControl>
                    <Input
                      placeholder={t.schoolSetup.addSchool?.nameKhmerPlaceholder || 'បញ្ចូលឈ្មោះសាលា'}
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="type"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>{t.schoolSetup.addSchool?.type || 'School Type'}</FormLabel>
                  <Select onValueChange={field.onChange} defaultValue={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder={t.schoolSetup.addSchool?.typePlaceholder || 'Select school type'} />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      <SelectItem value="PRIMARY">{t.schoolSetup.types.PRIMARY}</SelectItem>
                      <SelectItem value="SECONDARY">{t.schoolSetup.types.SECONDARY}</SelectItem>
                      <SelectItem value="HIGH_SCHOOL">{t.schoolSetup.types.HIGH_SCHOOL}</SelectItem>
                      <SelectItem value="VOCATIONAL">{t.schoolSetup.types.VOCATIONAL}</SelectItem>
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="address"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>{t.schoolSetup.addSchool?.address || 'Address'}</FormLabel>
                  <FormControl>
                    <Textarea
                      placeholder={t.schoolSetup.addSchool?.addressPlaceholder || 'Enter school address'}
                      className="resize-none"
                      rows={3}
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => handleOpenChange(false)}
              >
                {t.common.cancel}
              </Button>
              <Button type="submit" disabled={createMutation.isPending}>
                {createMutation.isPending && (
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                )}
                {createMutation.isPending
                  ? (t.schoolSetup.addSchool?.creating || 'Creating...')
                  : (t.schoolSetup.addSchool?.createButton || 'Create School')}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
