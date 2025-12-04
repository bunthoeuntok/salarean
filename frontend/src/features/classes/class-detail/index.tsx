import { lazy, Suspense } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { ArrowLeft, Users, Calendar, ClipboardList, GraduationCap } from 'lucide-react'
import { Route, type ClassDetailTab } from '@/routes/_authenticated/classes.$id'
import { useLanguage } from '@/context/language-provider'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { classService } from '@/services/class.service'
import { ClassHeader } from './components/class-header'
import { ComingSoonTab } from '../components/coming-soon-tab'
import { ClassDetailErrorBoundary } from '../components/class-detail-error-boundary'
import { ErrorState } from '../components/error-state'
import { useClasses } from '@/hooks/use-classes'

const StudentsTab = lazy(() =>
  import('./components/students-tab').then((module) => ({
    default: module.StudentsTab,
  }))
)

function TabLoadingFallback() {
  return (
    <div className="space-y-2">
      {Array.from({ length: 5 }).map((_, i) => (
        <Skeleton key={i} className="h-16 w-full" />
      ))}
    </div>
  )
}

function ClassDetailContent() {
  const { t } = useLanguage()
  const navigate = useNavigate()
  const { id } = Route.useParams()
  const { tab } = Route.useSearch()

  // Fetch and populate global class store for batch transfer eligibility
  useClasses()

  // Fetch class details
  const {
    data: classData,
    isLoading: isClassLoading,
    error: classError,
    refetch,
  } = useQuery({
    queryKey: ['class', id],
    queryFn: () => classService.getClass(id),
    enabled: !!id,
    retry: (failureCount, error) => {
      // Don't retry on 404 (CLASS_NOT_FOUND)
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as { response?: { status?: number } }
        if (axiosError.response?.status === 404) {
          return false
        }
      }
      return failureCount < 2
    },
  })

  const handleBack = () => {
    navigate({ to: '/classes' })
  }

  const handleTabChange = (value: string) => {
    navigate({
      to: '/classes/$id',
      params: { id },
      search: { tab: value as ClassDetailTab },
      replace: true,
      resetScroll: false, // Keep scroll position when switching tabs
    })
  }

  // Handle CLASS_NOT_FOUND error
  if (classError) {
    const is404 = classError && typeof classError === 'object' && 'response' in classError
      && (classError as { response?: { status?: number } }).response?.status === 404

    return (
      <>
        <Header fixed />
        <Main>
          <div className="mb-4">
            <Button variant="ghost" size="sm" onClick={handleBack}>
              <ArrowLeft className="mr-2 h-4 w-4" />
              {t.common.back}
            </Button>
          </div>
          <ErrorState
            title={is404 ? 'Class Not Found' : t.common.error}
            message={is404
              ? 'The class you are looking for does not exist or has been removed.'
              : 'Failed to load class details. Please try again.'}
            onRetry={is404 ? undefined : () => refetch()}
          />
        </Main>
      </>
    )
  }

  return (
    <>
      <Header fixed />
      <Main>
        {/* Back Navigation */}
        <div className="mb-4">
          <Button variant="ghost" size="sm" onClick={handleBack}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            {t.common.back}
          </Button>
        </div>

        {/* Class Header */}
        {isClassLoading ? (
          <div className="mb-6 space-y-2">
            <Skeleton className="h-8 w-64" />
            <Skeleton className="h-4 w-48" />
          </div>
        ) : classData ? (
          <ClassHeader classData={classData} />
        ) : null}

        {/* Tabs Navigation - Mobile responsive with horizontal scroll */}
        <Tabs
          value={tab}
          onValueChange={handleTabChange}
          className="mt-6"
        >
          <div className="-mx-4 overflow-x-auto px-4 sm:mx-0 sm:px-0">
            <TabsList
              className="inline-flex w-auto min-w-full sm:grid sm:w-full sm:grid-cols-4"
              aria-label={t.classes.tabs?.ariaLabel ?? 'Class information tabs'}
            >
              <TabsTrigger
                value="students"
                className="flex shrink-0 items-center gap-2 px-4 sm:px-2"
                aria-controls="tabpanel-students"
              >
                <Users className="h-4 w-4" />
                <span className="hidden sm:inline">{t.classes.tabs?.students ?? 'Students'}</span>
              </TabsTrigger>
              <TabsTrigger
                value="schedule"
                className="flex shrink-0 items-center gap-2 px-4 sm:px-2"
                aria-controls="tabpanel-schedule"
              >
                <Calendar className="h-4 w-4" />
                <span className="hidden sm:inline">{t.classes.tabs?.schedule ?? 'Schedule'}</span>
              </TabsTrigger>
              <TabsTrigger
                value="attendance"
                className="flex shrink-0 items-center gap-2 px-4 sm:px-2"
                aria-controls="tabpanel-attendance"
              >
                <ClipboardList className="h-4 w-4" />
                <span className="hidden sm:inline">{t.classes.tabs?.attendance ?? 'Attendance'}</span>
              </TabsTrigger>
              <TabsTrigger
                value="grades"
                className="flex shrink-0 items-center gap-2 px-4 sm:px-2"
                aria-controls="tabpanel-grades"
              >
                <GraduationCap className="h-4 w-4" />
                <span className="hidden sm:inline">{t.classes.tabs?.grades ?? 'Grades'}</span>
              </TabsTrigger>
            </TabsList>
          </div>

          <TabsContent
            value="students"
            id="tabpanel-students"
            role="tabpanel"
            aria-labelledby="tab-students"
            className="mt-4"
          >
            <Suspense fallback={<TabLoadingFallback />}>
              <StudentsTab classId={id} />
            </Suspense>
          </TabsContent>

          <TabsContent
            value="schedule"
            id="tabpanel-schedule"
            role="tabpanel"
            aria-labelledby="tab-schedule"
            className="mt-4"
          >
            <ComingSoonTab featureName={t.classes.tabs?.schedule ?? 'Schedule'} />
          </TabsContent>

          <TabsContent
            value="attendance"
            id="tabpanel-attendance"
            role="tabpanel"
            aria-labelledby="tab-attendance"
            className="mt-4"
          >
            <ComingSoonTab featureName={t.classes.tabs?.attendance ?? 'Attendance'} />
          </TabsContent>

          <TabsContent
            value="grades"
            id="tabpanel-grades"
            role="tabpanel"
            aria-labelledby="tab-grades"
            className="mt-4"
          >
            <ComingSoonTab featureName={t.classes.tabs?.grades ?? 'Grades'} />
          </TabsContent>
        </Tabs>
      </Main>
    </>
  )
}

export function ClassDetailPage() {
  const navigate = useNavigate()

  const handleBack = () => {
    navigate({ to: '/classes' })
  }

  return (
    <ClassDetailErrorBoundary onBack={handleBack}>
      <ClassDetailContent />
    </ClassDetailErrorBoundary>
  )
}
