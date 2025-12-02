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
  onGlobalFilterChange?: (value: string) => void
}

export function StudentList({
  students,
  className,
  globalFilter = '',
  onGlobalFilterChange,
}: StudentListProps) {
  const { t } = useLanguage()

  const columns = useMemo<ColumnDef<StudentEnrollmentItem>[]>(
    () => [
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
    data: students,
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

  if (filteredRows.length === 0 && globalFilter) {
    return (
      <div className="rounded-md border p-8 text-center">
        <p className="text-muted-foreground">{t.classes.detail?.noResults ?? 'No students found'}</p>
      </div>
    )
  }

  return (
    <div className="rounded-md border">
      <table className="w-full">
        <caption className="sr-only">Students enrolled in {className}</caption>
        <thead>
          <tr className="border-b bg-muted/50">
            {table.getHeaderGroups().map((headerGroup) =>
              headerGroup.headers.map((header) => (
                <th key={header.id} className="p-4 text-left font-medium">
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
  )
}

// Export the filtered count getter for use in parent component
export function getFilteredStudentCount(students: StudentEnrollmentItem[], search: string): number {
  if (!search) return students.length
  const searchLower = search.toLowerCase()
  return students.filter(
    (s) =>
      s.studentName.toLowerCase().includes(searchLower) ||
      s.studentCode.toLowerCase().includes(searchLower)
  ).length
}
