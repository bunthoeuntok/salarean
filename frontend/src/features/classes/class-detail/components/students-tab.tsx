import { useMemo, useCallback } from 'react'
import { useLanguage } from '@/context/language-provider'
import { Skeleton } from '@/components/ui/skeleton'
import { DataTable, useTableUrlParams } from '@/components/data-table'
import { useClassStudents } from '@/hooks/useClassStudents'
import { createStudentEnrollmentColumns } from '../columns'
import { EmptyState } from './empty-state'

interface StudentsTabProps {
  classId: string
}

export function StudentsTab({ classId }: StudentsTabProps) {
  const { t } = useLanguage()

  // Use URL params for search and sorting (no pagination)
  const { searchValue, sorting, setSorting, updateParams } = useTableUrlParams({
    defaultPageSize: 10, // Not used since pagination is disabled
  })

  // Construct sort parameter for backend
  const sortParam = useMemo(() => {
    if (sorting.length > 0) {
      return `${sorting[0].id},${sorting[0].desc ? 'desc' : 'asc'}`
    }
    return 'fullName,asc' // Default sort by name
  }, [sorting])

  // Handle search change with URL persistence
  const handleSearchChange = useCallback(
    (value: string) => {
      updateParams({ search: value || undefined })
    },
    [updateParams]
  )

  // Fetch only ACTIVE students from backend
  const {
    data: studentsData,
    isLoading,
    error,
  } = useClassStudents({
    classId,
    status: 'ACTIVE',
    sort: sortParam,
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
    <DataTable
      columns={columns}
      data={studentsData.students}
      storageKey={`class-students-${classId}`}
      searchValue={searchValue}
      onSearchChange={handleSearchChange}
      searchPlaceholder={t.classes.detail?.searchPlaceholder ?? 'Search students...'}
      sorting={sorting}
      onSortingChange={setSorting}
      enableColumnResizing
      enableColumnReordering
      showToolbar
      showPagination={false}
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
