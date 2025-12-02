import { useState } from 'react'
import { Skeleton } from '@/components/ui/skeleton'
import { useClassStudents } from '@/hooks/useClassStudents'
import { useDebouncedValue } from '@/hooks/use-debounce'
import type { Class, EnrollmentStatus } from '@/types/class.types'
import type { EnrollmentStatusFilter } from '@/lib/validations/class-filters'
import { StudentSearch } from '../../components/student-search'
import { StatusFilter } from '../../components/status-filter'
import { StudentList, getFilteredStudentCount } from './student-list'
import { EmptyState } from './empty-state'

interface StudentsTabProps {
  classId: string
  classData?: Class
}

export function StudentsTab({ classId, classData }: StudentsTabProps) {
  // Local search state (controlled input)
  const [search, setSearch] = useState('')
  // Debounced search for TanStack Table filtering
  const debouncedSearch = useDebouncedValue(search, 300)
  // Status filter for server-side filtering
  const [statusFilter, setStatusFilter] = useState<EnrollmentStatusFilter>('ALL')

  // Convert 'ALL' to undefined for API call
  const apiStatus = statusFilter === 'ALL' ? undefined : (statusFilter as EnrollmentStatus)

  const {
    data: studentsData,
    isLoading,
    error,
  } = useClassStudents({
    classId,
    status: apiStatus,
    enabled: !!classId,
  })

  const className = classData?.grade
    ? `Grade ${classData.grade}${classData.section ? classData.section : ''}`
    : 'Class'

  // Calculate filtered count for aria-live announcement
  const filteredCount = studentsData?.students
    ? getFilteredStudentCount(studentsData.students, debouncedSearch)
    : 0

  if (error) {
    return (
      <div className="flex items-center justify-center py-12">
        <p className="text-destructive">Failed to load students</p>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {/* Search and Filter Controls */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <StudentSearch
          value={search}
          onChange={setSearch}
          resultsCount={debouncedSearch ? filteredCount : undefined}
        />
        <StatusFilter value={statusFilter} onChange={setStatusFilter} />
      </div>

      {/* Student List */}
      {isLoading ? (
        <div className="space-y-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-16 w-full" />
          ))}
        </div>
      ) : !studentsData?.students || studentsData.students.length === 0 ? (
        <EmptyState />
      ) : (
        <StudentList
          students={studentsData.students}
          className={className}
          globalFilter={debouncedSearch}
          onGlobalFilterChange={setSearch}
        />
      )}
    </div>
  )
}
