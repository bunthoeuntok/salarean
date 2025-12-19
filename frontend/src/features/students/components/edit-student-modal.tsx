import { useState, useMemo } from 'react'
import { useForm, useFieldArray } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, Trash2, Loader2, CalendarIcon } from 'lucide-react'
import { format, parseISO } from 'date-fns'
import { formatDateDisplay } from '@/lib/khmer-calendar'

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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
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
import type { Student, Gender, Relationship, UpdateStudentRequest } from '@/types/student.types'

// Schema type for type inference
const _baseParentContactSchema = z.object({
  fullName: z.string(),
  phoneNumber: z.string(),
  relationship: z.enum(['MOTHER', 'FATHER', 'GUARDIAN', 'OTHER'] as const),
  isPrimary: z.boolean(),
})

const _baseStudentSchema = z.object({
  firstName: z.string(),
  lastName: z.string(),
  firstNameKhmer: z.string().optional(),
  lastNameKhmer: z.string().optional(),
  dateOfBirth: z.date(),
  gender: z.enum(['M', 'F'] as const),
  address: z.string().optional(),
  parentContacts: z.array(_baseParentContactSchema),
})

type FormData = z.infer<typeof _baseStudentSchema>

interface EditStudentModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  studentId: string | null
}

interface EditStudentFormProps {
  studentData: Student
  onClose: () => void
  studentId: string
}

// Inner form component - only mounts when studentData is available
function EditStudentForm({ studentData, onClose, studentId }: EditStudentFormProps) {
  const { t, translateError, language } = useLanguage()
  const queryClient = useQueryClient()
  const [activeTab, setActiveTab] = useState('student-info')

  // Create schema with translated messages
  const updateStudentSchema = useMemo(() => {
    const parentContactSchema = z.object({
      fullName: z.string().min(1, t.validation.required),
      phoneNumber: z
        .string()
        .min(1, t.validation.required)
        .regex(/^\+855\d{8,9}$/, t.validation.invalidPhone),
      relationship: z.enum(['MOTHER', 'FATHER', 'GUARDIAN', 'OTHER'] as const),
      isPrimary: z.boolean(),
    })

    return z.object({
      firstName: z.string().min(1, t.validation.required).max(100),
      lastName: z.string().min(1, t.validation.required).max(100),
      firstNameKhmer: z.string().max(100).optional(),
      lastNameKhmer: z.string().max(100).optional(),
      dateOfBirth: z.date({ required_error: t.validation.required }),
      gender: z.enum(['M', 'F'] as const, { required_error: t.validation.required }),
      address: z.string().max(500).optional(),
      parentContacts: z
        .array(parentContactSchema)
        .min(1, t.students.modal.parentContact.atLeastOne),
    })
  }, [t])

  // Form with default values from studentData - this runs fresh on mount
  const form = useForm<FormData>({
    resolver: zodResolver(updateStudentSchema),
    defaultValues: {
      firstName: studentData.firstName,
      lastName: studentData.lastName,
      firstNameKhmer: studentData.firstNameKhmer || '',
      lastNameKhmer: studentData.lastNameKhmer || '',
      dateOfBirth: parseISO(studentData.dateOfBirth),
      gender: studentData.gender,
      address: studentData.address || '',
      parentContacts: studentData.parentContacts?.length
        ? studentData.parentContacts.map((pc) => ({
            fullName: pc.fullName,
            phoneNumber: pc.phoneNumber,
            relationship: pc.relationship,
            isPrimary: pc.isPrimary,
          }))
        : [
            {
              fullName: '',
              phoneNumber: '+855',
              relationship: 'MOTHER' as Relationship,
              isPrimary: true,
            },
          ],
    },
  })

  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name: 'parentContacts',
  })

  const updateMutation = useMutation({
    mutationFn: (data: UpdateStudentRequest) => studentService.updateStudent(studentId, data),
    onSuccess: () => {
      toast.success(t.students.modal.success.updated)
      queryClient.invalidateQueries({ queryKey: ['students'] })
      queryClient.invalidateQueries({ queryKey: ['student', studentId] })
      onClose()
    },
    onError: (error: Error) => {
      toast.error(translateError(error.message as Parameters<typeof translateError>[0]))
    },
  })

  const onSubmit = (data: FormData) => {
    const request: UpdateStudentRequest = {
      ...data,
      dateOfBirth: format(data.dateOfBirth, 'yyyy-MM-dd'),
      firstNameKhmer: data.firstNameKhmer || undefined,
      lastNameKhmer: data.lastNameKhmer || undefined,
      address: data.address || undefined,
    }
    updateMutation.mutate(request)
  }

  const addParentContact = () => {
    append({
      fullName: '',
      phoneNumber: '+855',
      relationship: 'FATHER',
      isPrimary: false,
    })
  }

  const handlePrimaryChange = (index: number, checked: boolean) => {
    if (checked) {
      fields.forEach((_, i) => {
        if (i !== index) {
          form.setValue(`parentContacts.${i}.isPrimary`, false)
        }
      })
    }
    form.setValue(`parentContacts.${index}.isPrimary`, checked)
  }

  const relationshipOptions: { value: Relationship; label: string }[] = [
    { value: 'MOTHER', label: t.students.modal.parentContact.relationships.MOTHER },
    { value: 'FATHER', label: t.students.modal.parentContact.relationships.FATHER },
    { value: 'GUARDIAN', label: t.students.modal.parentContact.relationships.GUARDIAN },
    { value: 'OTHER', label: t.students.modal.parentContact.relationships.OTHER },
  ]

  const genderOptions: { value: Gender; label: string }[] = [
    { value: 'M', label: t.students.gender.M },
    { value: 'F', label: t.students.gender.F },
  ]

  const { errors } = form.formState
  const hasStudentInfoErrors = !!(
    errors.firstName ||
    errors.lastName ||
    errors.firstNameKhmer ||
    errors.lastNameKhmer ||
    errors.dateOfBirth ||
    errors.gender ||
    errors.address
  )
  const hasParentContactErrors = !!errors.parentContacts

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className='flex flex-col flex-1 overflow-hidden px-6 pt-4'>
        <Tabs value={activeTab} onValueChange={setActiveTab} className='flex flex-col flex-1 overflow-hidden'>
          <TabsList className='grid w-full grid-cols-2 shrink-0'>
            <TabsTrigger
              value='student-info'
            >
              {t.students.modal.tabs.studentInfo}
              {hasStudentInfoErrors && <span className='text-destructive'>*</span>}
            </TabsTrigger>
            <TabsTrigger
              value='parent-contact'
            >
              {t.students.modal.tabs.parentContact}
              {hasParentContactErrors && <span className='text-destructive'>*</span>}
            </TabsTrigger>
          </TabsList>

          {/* Student Information Tab */}
          <TabsContent value='student-info' className='flex-1 overflow-y-auto space-y-4 mt-4 pr-1'>
            <div className='grid grid-cols-2 gap-4'>
              <FormField
                control={form.control}
                name='firstName'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.students.modal.fields.firstName} <span className='text-destructive'>*</span></FormLabel>
                    <FormControl>
                      <Input
                        placeholder={t.students.modal.fields.firstNamePlaceholder}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name='lastName'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.students.modal.fields.lastName} <span className='text-destructive'>*</span></FormLabel>
                    <FormControl>
                      <Input
                        placeholder={t.students.modal.fields.lastNamePlaceholder}
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
                name='firstNameKhmer'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.students.modal.fields.firstNameKhmer}</FormLabel>
                    <FormControl>
                      <Input
                        placeholder={t.students.modal.fields.firstNameKhmerPlaceholder}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name='lastNameKhmer'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.students.modal.fields.lastNameKhmer}</FormLabel>
                    <FormControl>
                      <Input
                        placeholder={t.students.modal.fields.lastNameKhmerPlaceholder}
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
                name='dateOfBirth'
                render={({ field }) => (
                  <FormItem className='flex flex-col'>
                    <FormLabel>{t.students.modal.fields.dateOfBirth} <span className='text-destructive'>*</span></FormLabel>
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
                              <span>{t.students.modal.fields.dateOfBirthPlaceholder}</span>
                            )}
                          </Button>
                        </FormControl>
                      </PopoverTrigger>
                      <PopoverContent className='w-auto p-0' align='start'>
                        <Calendar
                          mode='single'
                          captionLayout='dropdown'
                          startMonth={new Date(1950, 0)}
                          defaultMonth={field.value || new Date()}
                          selected={field.value}
                          onSelect={field.onChange}
                          disabled={(date: Date) =>
                            date > new Date() || date < new Date('1900-01-01')
                          }
                        />
                      </PopoverContent>
                    </Popover>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name='gender'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.students.modal.fields.gender} <span className='text-destructive'>*</span></FormLabel>
                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                      <FormControl>
                        <SelectTrigger className='w-full'>
                          <SelectValue
                            placeholder={t.students.modal.fields.genderPlaceholder}
                          />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {genderOptions.map((option) => (
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

            <div className='grid grid-cols-1 gap-4'>
              <FormField
                control={form.control}
                name='address'
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{t.students.modal.fields.address}</FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder={t.students.modal.fields.addressPlaceholder}
                        className='resize-none h-24'
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
          </TabsContent>

          {/* Parent Contact Tab */}
          <TabsContent value='parent-contact' className='flex-1 overflow-y-auto space-y-4 mt-4 pr-1'>
            <div className='sticky -top-px z-10 flex items-center justify-between bg-muted px-4 py-2'>
              <h4 className='text-sm font-medium'>
                {t.students.modal.parentContact.title}
              </h4>
              <Button
                type='button'
                variant='outline'
                size='sm'
                onClick={addParentContact}
              >
                <Plus className='mr-2 h-4 w-4' />
                {t.students.modal.parentContact.addContact}
              </Button>
            </div>

            {form.formState.errors.parentContacts?.root && (
              <p className='text-sm text-destructive'>
                {t.students.modal.parentContact.atLeastOne}
              </p>
            )}

            <div className='space-y-4'>
              {fields.map((field, index) => (
                <div
                  key={field.id}
                  className='rounded-lg border space-y-4'
                >
                  <div className='flex items-center justify-between bg-muted/50 px-4 py-2 rounded-md'>
                    <span className='text-sm font-medium'>
                      {t.students.modal.parentContact.contact} {index + 1}
                    </span>
                    {fields.length > 1 && (
                      <Button
                        type='button'
                        variant='ghost'
                        size='sm'
                        onClick={() => remove(index)}
                      >
                        <Trash2 className='h-4 w-4 text-destructive' />
                      </Button>
                    )}
                  </div>

                  <div className='grid grid-cols-2 gap-4 px-4'>
                    <FormField
                      control={form.control}
                      name={`parentContacts.${index}.fullName`}
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>
                            {t.students.modal.parentContact.fullName} <span className='text-destructive'>*</span>
                          </FormLabel>
                          <FormControl>
                            <Input
                              placeholder={
                                t.students.modal.parentContact.fullNamePlaceholder
                              }
                              {...field}
                            />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    <FormField
                      control={form.control}
                      name={`parentContacts.${index}.phoneNumber`}
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>
                            {t.students.modal.parentContact.phoneNumber} <span className='text-destructive'>*</span>
                          </FormLabel>
                          <FormControl>
                            <Input
                              placeholder={
                                t.students.modal.parentContact.phoneNumberPlaceholder
                              }
                              {...field}
                            />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                  </div>

                  <div className='grid grid-cols-2 items-center gap-4 px-4 pb-4'>
                    <FormField
                      control={form.control}
                      name={`parentContacts.${index}.relationship`}
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>
                            {t.students.modal.parentContact.relationship} <span className='text-destructive'>*</span>
                          </FormLabel>
                          <Select
                            onValueChange={field.onChange}
                            defaultValue={field.value}
                          >
                            <FormControl>
                              <SelectTrigger className='w-full'>
                                <SelectValue
                                  placeholder={
                                    t.students.modal.parentContact
                                      .relationshipPlaceholder
                                  }
                                />
                              </SelectTrigger>
                            </FormControl>
                            <SelectContent>
                              {relationshipOptions.map((option) => (
                                <SelectItem
                                  key={option.value}
                                  value={option.value}
                                >
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
                      name={`parentContacts.${index}.isPrimary`}
                      render={({ field }) => (
                        <FormItem className='flex flex-row items-center space-x-3 space-y-0 pt-8'>
                          <FormControl>
                            <Checkbox
                              checked={field.value}
                              onCheckedChange={(checked) =>
                                handlePrimaryChange(index, !!checked)
                              }
                            />
                          </FormControl>
                          <FormLabel className='font-normal cursor-pointer'>
                            {t.students.modal.parentContact.isPrimary}
                          </FormLabel>
                        </FormItem>
                      )}
                    />
                  </div>
                </div>
              ))}
            </div>
          </TabsContent>
        </Tabs>

        <DialogFooter className='shrink-0 gap-2 px-6 py-4 -mx-6'>
          <Button type='button' variant='outline' onClick={onClose}>
            {t.students.modal.buttons.cancel}
          </Button>
          <Button type='submit' disabled={updateMutation.isPending}>
            {updateMutation.isPending ? (
              <>
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                {t.students.modal.buttons.saving}
              </>
            ) : (
              t.students.modal.buttons.save
            )}
          </Button>
        </DialogFooter>
      </form>
    </Form>
  )
}

// Main modal component
export function EditStudentModal({ open, onOpenChange, studentId }: EditStudentModalProps) {
  const { t } = useLanguage()

  // Fetch student data
  const { data: studentData, isLoading: isLoadingStudent } = useQuery({
    queryKey: ['student', studentId],
    queryFn: () => studentService.getStudent(studentId!),
    enabled: !!studentId && open,
  })

  const handleClose = () => {
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className='sm:max-w-[900px] h-[70vh] flex flex-col p-0'>
        <DialogHeader className='shrink-0 bg-muted/50 border-b px-6 py-6'>
          <DialogTitle>{t.students.modal.editTitle}</DialogTitle>
        </DialogHeader>

        {isLoadingStudent || !studentData ? (
          <div className='flex flex-1 items-center justify-center'>
            <Loader2 className='h-8 w-8 animate-spin text-muted-foreground' />
          </div>
        ) : (
          <EditStudentForm
            key={studentData.id}
            studentData={studentData}
            onClose={handleClose}
            studentId={studentId!}
          />
        )}
      </DialogContent>
    </Dialog>
  )
}
