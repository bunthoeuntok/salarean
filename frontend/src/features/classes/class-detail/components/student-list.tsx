import { useMemo } from 'react'
import {
  useReactTable,
  getCoreRowModel,
  getFilteredRowModel,
  flexRender,
  type ColumnDef,
  type FilterFn,
} from '@tanstack/react-table'
import { useLanguage } from '@/context/language-provider'
import type { StudentEnrollmentItem } from '@/types/class.types'
import type { EnrollmentStatusFilter } from '@/lib/validations/class-filters'
import { StudentListItem } from './student-list-item'

// Custom global filter function for searching by name or code
const globalFilterFn: FilterFn<StudentEnrollmentItem> = (row, _columnId, filterValue: string) => {
  const search = filterValue.toLowerCase()
  const name = row.original.studentName.toLowerCase()
  const code = row.original.studentCode.toLowerCase()
  return name.includes(search) || code.includes(search)
}

interface StudentListProps {
  students: StudentEnrollmentItem[]
  className: string
  globalFilter?: string
  statusFilter?: EnrollmentStatusFilter
  onGlobalFilterChange?: (value: string) => void
}

export function StudentList({
  students,
  className,
  globalFilter = '',
  statusFilter = 'ALL',
  onGlobalFilterChange,
}: StudentListProps) {
  const { t } = useLanguage()

  // Client-side status filtering
  const filteredByStatus = useMemo(() => {
    if (statusFilter === 'ALL') return students
    return students.filter((s) => s.enrollmentStatus === statusFilter)
  }, [students, statusFilter])

  const columns = useMemo<ColumnDef<StudentEnrollmentItem>[]>(
    () => [
      {
        accessorKey: 'studentCode',
        header: t.students.columns.code,
      },
      {
        accessorKey: 'studentName',
        header: t.students.columns.name,
      },
      {
        accessorKey: 'enrollmentDate',
        header: t.students.view.fields.enrollmentDate,
      },
      {
        accessorKey: 'enrollmentStatus',
        header: t.students.columns.status,
      },
    ],
    [t]
  )

  const table = useReactTable({
    data: filteredByStatus,
    columns,
    state: {
      globalFilter,
    },
    onGlobalFilterChange,
    globalFilterFn,
    getCoreRowModel: getCoreRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
  })

  const filteredRows = table.getRowModel().rows

  if (filteredRows.length === 0) {
    return (
      <div className="rounded-md border p-8 text-center">
        <p className="text-muted-foreground">{t.classes.detail?.noResults ?? 'No students found'}</p>
      </div>
    )
  }

  return (
    <div className="rounded-md border">
      {/* Mobile-responsive table with horizontal scroll */}
      <div className="-mx-4 overflow-x-auto sm:mx-0">
        <div className="inline-block min-w-full align-middle">
          <table className="min-w-full">
            <caption className="sr-only">Students enrolled in {className}</caption>
            <thead>
              <tr className="border-b bg-muted/50">
                {table.getHeaderGroups().map((headerGroup) =>
                  headerGroup.headers.map((header) => (
                    <th key={header.id} className="whitespace-nowrap p-4 text-left font-medium">
                      {flexRender(header.column.columnDef.header, header.getContext())}
                    </th>
                  ))
                )}
              </tr>
            </thead>
            <tbody>
              {filteredRows.map((row) => (
                <StudentListItem key={row.original.studentId} student={row.original} />
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

// Export the filtered count getter for use in parent component
export function getFilteredStudentCount(
  students: StudentEnrollmentItem[],
  search: string,
  statusFilter: EnrollmentStatusFilter = 'ALL'
): number {
  let filtered = students

  // Apply status filter first
  if (statusFilter !== 'ALL') {
    filtered = filtered.filter((s) => s.enrollmentStatus === statusFilter)
  }

  // Then apply search filter
  if (search) {
    const searchLower = search.toLowerCase()
    filtered = filtered.filter(
      (s) =>
        s.studentName.toLowerCase().includes(searchLower) ||
        s.studentCode.toLowerCase().includes(searchLower)
    )
  }

  return filtered.length
}
