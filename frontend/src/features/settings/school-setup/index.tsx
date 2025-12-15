import { useState, useEffect } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useSearch } from '@tanstack/react-router'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Check, Loader2, AlertTriangle, School } from 'lucide-react'
import { toast } from 'sonner'
import { cn } from '@/lib/utils'
import { useLanguage } from '@/context/language-provider'
import { Separator } from '@/components/ui/separator'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import type { SchoolSetupSearch } from '@/routes/_authenticated/settings/school-setup'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { ProvinceSelector } from '@/features/school-setup/components/province-selector'
import { DistrictSelector } from '@/features/school-setup/components/district-selector'
import { SchoolTable } from '@/features/school-setup/components/school-table'
import { useSchoolSetupStore } from '@/store/school-setup-store'
import {
  createTeacherSchool,
  teacherSchoolKeys,
  teacherSchoolQueryOptions,
} from '@/services/school.service'
import {
  teacherSchoolSchema,
  type TeacherSchoolFormData,
} from '@/lib/validations/school-setup'

const steps = [
  { id: 1, name: 'Select School' },
  { id: 2, name: 'Principal Info' },
]

export function SettingsSchoolSetup() {
  const { t } = useLanguage()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const search = useSearch({ from: '/_authenticated/settings/school-setup' }) as SchoolSetupSearch
  const { selectedSchoolId, setAllSelections, reset } = useSchoolSetupStore()
  const [currentStep, setCurrentStep] = useState(1)
  const [isEditing, setIsEditing] = useState(false)
  const isRedirected = search.reason === 'required'

  // Fetch existing teacher-school data
  const { data: existingData, isLoading } = useQuery(teacherSchoolQueryOptions)

  const form = useForm<TeacherSchoolFormData>({
    resolver: zodResolver(teacherSchoolSchema),
    defaultValues: {
      schoolId: '',
      principalName: '',
      principalGender: undefined,
    },
  })

  // Load existing data into form when available (for summary view)
  useEffect(() => {
    if (existingData && !isEditing) {
      // Pre-populate form with existing data
      form.reset({
        schoolId: existingData.schoolId,
        principalName: existingData.principalName,
        principalGender: existingData.principalGender,
      })
    }
  }, [existingData, isEditing, form])

  // Populate store selections when entering edit mode
  useEffect(() => {
    if (isEditing && existingData) {
      console.log('Edit mode - existingData:', {
        provinceId: existingData.provinceId,
        districtId: existingData.districtId,
        schoolId: existingData.schoolId,
      })
      // Set store selections for province/district/school
      if (existingData.provinceId && existingData.districtId) {
        setAllSelections(
          existingData.provinceId,
          existingData.districtId,
          existingData.schoolId
        )
      } else {
        console.warn('Missing provinceId or districtId in existingData')
      }
    }
  }, [isEditing, existingData, setAllSelections])

  const createMutation = useMutation({
    mutationFn: createTeacherSchool,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: teacherSchoolKeys.all })
      toast.success(t.schoolSetup.success.setupComplete)
      reset()
      setIsEditing(false)
      navigate({ to: '/' })
    },
    onError: () => {
      toast.error(t.schoolSetup.errors.setupFailed)
    },
  })

  const handleContinue = () => {
    if (!selectedSchoolId) {
      toast.error(t.schoolSetup.step1.selectSchoolError)
      return
    }

    form.setValue('schoolId', selectedSchoolId)
    setCurrentStep(2)
  }

  const handleEdit = () => {
    setIsEditing(true)
    setCurrentStep(1)
  }

  const onSubmit = (data: TeacherSchoolFormData) => {
    createMutation.mutate(data)
  }

  if (isLoading) {
    return (
      <div className="flex flex-1 items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  // Show existing setup summary if data exists and not editing
  if (existingData && !isEditing) {
    return (
      <div className="flex flex-1 flex-col">
        {/* Page Header */}
        <div className="space-y-0.5">
          <h2 className="text-xl font-semibold tracking-tight">
            {t.schoolSetup.title}
          </h2>
          <p className="text-muted-foreground text-sm">{t.schoolSetup.subtitle}</p>
        </div>
        <Separator className="my-4" />

        {/* Current Setup Card */}
        <Card className="max-w-lg">
          <CardHeader>
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
                <School className="h-5 w-5 text-primary" />
              </div>
              <div>
                <CardTitle className="text-lg">{existingData.schoolName}</CardTitle>
                <CardDescription>
                  {t.schoolSetup.currentSetup?.description || 'Your current school setup'}
                </CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-3 text-sm">
              {existingData.provinceName && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">{t.schoolSetup.step1.province}</span>
                  <span className="font-medium">{existingData.provinceName}</span>
                </div>
              )}
              {existingData.districtName && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">{t.schoolSetup.step1.district}</span>
                  <span className="font-medium">{existingData.districtName}</span>
                </div>
              )}
              <div className="flex justify-between">
                <span className="text-muted-foreground">{t.schoolSetup.step2.principalName}</span>
                <span className="font-medium">{existingData.principalName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">{t.schoolSetup.step2.principalGender}</span>
                <span className="font-medium">
                  {existingData.principalGender === 'M'
                    ? t.schoolSetup.step2.genderMale
                    : t.schoolSetup.step2.genderFemale}
                </span>
              </div>
            </div>
            <Separator />
            <Button variant="outline" onClick={handleEdit} className="w-full">
              {t.schoolSetup.currentSetup?.editButton || 'Edit Setup'}
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col">
      {/* Warning Banner for Redirected Users */}
      {isRedirected && (
        <div className="mb-4 flex items-start gap-3 rounded-lg border border-amber-500/50 bg-amber-500/10 p-4">
          <AlertTriangle className="h-5 w-5 text-amber-500 shrink-0 mt-0.5" />
          <div className="text-sm">
            <p className="font-medium text-amber-500">
              {t.schoolSetup.warning?.title || 'School Setup Required'}
            </p>
            <p className="text-muted-foreground mt-1">
              {t.schoolSetup.warning?.description || 'You need to complete the school setup before accessing student and class management features.'}
            </p>
          </div>
        </div>
      )}

      {/* Page Header */}
      <div className="space-y-0.5">
        <h2 className="text-xl font-semibold tracking-tight">
          {t.schoolSetup.title}
        </h2>
        <p className="text-muted-foreground text-sm">{t.schoolSetup.subtitle}</p>
      </div>
      <Separator className="my-4" />

      {/* Step Indicator */}
      <nav aria-label="Progress" className="mb-8 max-w-md">
        <ol className="flex items-center">
          {steps.map((step, stepIdx) => (
            <li
              key={step.id}
              className={cn(
                stepIdx !== steps.length - 1 ? 'flex-1' : '',
                'relative'
              )}
            >
              <div className="flex items-center">
                <span
                  className={cn(
                    'flex h-8 w-8 shrink-0 items-center justify-center rounded-full border-2 text-sm font-semibold',
                    currentStep > step.id
                      ? 'border-primary bg-primary text-primary-foreground'
                      : currentStep === step.id
                        ? 'border-primary text-primary'
                        : 'border-muted-foreground/30 text-muted-foreground'
                  )}
                >
                  {currentStep > step.id ? (
                    <Check className="h-4 w-4" />
                  ) : (
                    step.id
                  )}
                </span>
                <span
                  className={cn(
                    'ml-2 text-sm font-medium',
                    currentStep >= step.id
                      ? 'text-foreground'
                      : 'text-muted-foreground'
                  )}
                >
                  {step.name}
                </span>
              </div>
              {stepIdx !== steps.length - 1 && (
                <div
                  className={cn(
                    'absolute left-4 top-4 -ml-px mt-0.5 h-0.5',
                    currentStep > step.id ? 'bg-primary' : 'bg-muted-foreground/30'
                  )}
                  style={{ width: 'calc(100% - 2rem)', marginLeft: '2rem' }}
                />
              )}
            </li>
          ))}
        </ol>
      </nav>

      {/* Step Content */}
      <div className="faded-bottom h-full w-full overflow-y-auto scroll-smooth pe-4 pb-12">
        {currentStep === 1 ? (
          <div className="space-y-6 max-w-3xl">
            <div className="grid gap-4 sm:grid-cols-2">
              <ProvinceSelector />
              <DistrictSelector />
            </div>
            <SchoolTable />
            <div className="flex justify-end gap-2 pt-4">
              {isEditing && (
                <Button
                  variant="outline"
                  onClick={() => {
                    setIsEditing(false)
                    // Restore original selections
                    if (existingData?.provinceId && existingData?.districtId) {
                      setAllSelections(
                        existingData.provinceId,
                        existingData.districtId,
                        existingData.schoolId
                      )
                    }
                  }}
                >
                  {t.common?.cancel || 'Cancel'}
                </Button>
              )}
              <Button
                onClick={handleContinue}
                disabled={!selectedSchoolId}
              >
                {t.schoolSetup.step1.continueButton}
              </Button>
            </div>
          </div>
        ) : (
          <div className="space-y-6">
            <Form {...form}>
              <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4 max-w-md">
                <FormField
                  control={form.control}
                  name="principalName"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>{t.schoolSetup.step2.principalName}</FormLabel>
                      <FormControl>
                        <Input
                          placeholder={t.schoolSetup.step2.principalNamePlaceholder}
                          {...field}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="principalGender"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>{t.schoolSetup.step2.principalGender}</FormLabel>
                      <Select
                        onValueChange={field.onChange}
                        value={field.value}
                      >
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue
                              placeholder={t.schoolSetup.step2.principalGenderPlaceholder}
                            />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="M">
                            {t.schoolSetup.step2.genderMale}
                          </SelectItem>
                          <SelectItem value="F">
                            {t.schoolSetup.step2.genderFemale}
                          </SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <div className="flex justify-between pt-4">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => setCurrentStep(1)}
                  >
                    {t.schoolSetup.step2.backButton}
                  </Button>
                  <Button
                    type="submit"
                    disabled={createMutation.isPending}
                  >
                    {createMutation.isPending && (
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    )}
                    {createMutation.isPending
                      ? t.schoolSetup.step2.completing
                      : isEditing
                        ? (t.schoolSetup.step2.updateButton || 'Update')
                        : t.schoolSetup.step2.completeButton}
                  </Button>
                </div>
              </form>
            </Form>
          </div>
        )}
      </div>
    </div>
  )
}
