import { useMemo } from 'react'
import { useLanguage } from '@/context/language-provider'
import { Skeleton } from '@/components/ui/skeleton'
import { ClientDataTableWithUrl } from '@/components/data-table'
import { useClassStudents } from '@/hooks/useClassStudents'
import { createStudentEnrollmentColumns } from '../columns'
import { EmptyState } from './empty-state'

interface StudentsTabProps {
  classId: string
}

export function StudentsTab({ classId }: StudentsTabProps) {
  const { t } = useLanguage()

  // Fetch only ACTIVE students from backend
  // No sort parameter - we'll sort on frontend since all data is loaded
  const {
    data: studentsData,
    isLoading,
    error,
  } = useClassStudents({
    classId,
    status: 'ACTIVE',
    enabled: !!classId,
  })

  // Create columns with translations
  const columns = useMemo(() => createStudentEnrollmentColumns(t), [t])

  if (error) {
    return (
      <div className="flex items-center justify-center py-12">
        <p className="text-destructive">Failed to load students</p>
      </div>
    )
  }

  if (isLoading) {
    return (
      <div className="space-y-2">
        {Array.from({ length: 10 }).map((_, i) => (
          <Skeleton key={i} className="h-16 w-full" />
        ))}
      </div>
    )
  }

  if (!studentsData?.students || studentsData.students.length === 0) {
    return <EmptyState />
  }

  return (
    <ClientDataTableWithUrl
      columns={columns}
      data={studentsData.students}
      storageKey={`class-students-${classId}`}
      searchPlaceholder={t.classes.detail?.searchPlaceholder ?? 'Search students...'}
      enableColumnResizing
      enableColumnReordering
      showToolbar
      columnLabels={{
        studentCode: t.students.columns.code,
        fullName: t.students.columns.name,
        fullNameKhmer: t.students.columns.fullNameKhmer,
        gender: t.students.columns.gender,
        dateOfBirth: t.students.columns.dateOfBirth,
        enrollmentDate: t.students.view.fields.enrollmentDate,
        enrollmentStatus: t.students.columns.status,
      }}
    />
  )
}
