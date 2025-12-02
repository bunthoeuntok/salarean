import { useQuery } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { ArrowLeft } from 'lucide-react'
import { Route } from '@/routes/_authenticated/classes.$id'
import { useLanguage } from '@/context/language-provider'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { classService } from '@/services/class.service'
import { useClassStudents } from '@/hooks/useClassStudents'
import { ClassHeader } from './components/class-header'
import { StudentList } from './components/student-list'
import { EmptyState } from './components/empty-state'

export function ClassDetailPage() {
  const { t } = useLanguage()
  const navigate = useNavigate()
  const { id } = Route.useParams()

  // Fetch class details
  const {
    data: classData,
    isLoading: isClassLoading,
    error: classError,
  } = useQuery({
    queryKey: ['class', id],
    queryFn: () => classService.getClass(id),
    enabled: !!id,
  })

  // Fetch students in class
  const {
    data: studentsData,
    isLoading: isStudentsLoading,
    error: studentsError,
  } = useClassStudents({
    classId: id,
    enabled: !!id,
  })

  const handleBack = () => {
    navigate({ to: '/classes' })
  }

  const error = classError || studentsError

  if (error) {
    return (
      <>
        <Header fixed />
        <Main>
          <div className="flex flex-col items-center justify-center py-12">
            <p className="text-destructive">
              {t.common.error}: Failed to load class details
            </p>
            <Button variant="outline" onClick={handleBack} className="mt-4">
              <ArrowLeft className="mr-2 h-4 w-4" />
              {t.common.back}
            </Button>
          </div>
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

        {/* Students List */}
        <div className="mt-6">
          <h3 className="mb-4 text-lg font-semibold">
            Students ({studentsData?.totalCount ?? 0})
          </h3>

          {isStudentsLoading ? (
            <div className="space-y-2">
              {Array.from({ length: 5 }).map((_, i) => (
                <Skeleton key={i} className="h-16 w-full" />
              ))}
            </div>
          ) : studentsData?.students && studentsData.students.length > 0 ? (
            <StudentList
              students={studentsData.students}
              className={classData?.grade ? `Grade ${classData.grade}${classData.section ? classData.section : ''}` : 'Class'}
            />
          ) : (
            <EmptyState />
          )}
        </div>
      </Main>
    </>
  )
}
