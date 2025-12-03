import type { ColumnDef } from '@tanstack/react-table'
import { format } from 'date-fns'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { DataTableColumnHeader } from '@/components/data-table'
import type { StudentEnrollmentItem, EnrollmentStatus } from '@/types/class.types'


const getInitials = (name: string) => {
  return name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
}

const formatDate = (dateString: string) => {
  try {
    return format(new Date(dateString), 'dd/MM/yyyy')
  } catch {
    return dateString
  }
}

export const createStudentEnrollmentColumns = (
  t: {
    students: {
      columns: {
        code: string
        name: string
        fullNameKhmer: string
        gender: string
        dateOfBirth: string
      }
      gender: {
        M: string
        F: string
      }
      view: {
        fields: {
          enrollmentDate: string
        }
        enrollmentStatus: Record<EnrollmentStatus, string>
      }
    }
  }
): ColumnDef<StudentEnrollmentItem>[] => [
  {
    accessorKey: 'studentCode',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.students.columns.code} />
    ),
    cell: ({ row }) => (
      <span className="font-medium">{row.getValue('studentCode')}</span>
    ),
    size: 120,
  },
  {
    accessorKey: 'fullName',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.students.columns.name} />
    ),
    cell: ({ row }) => {
      const student = row.original
      return (
        <div className="flex items-center gap-3">
          <Avatar className="h-10 w-10">
            <AvatarImage src={student.photoUrl ?? undefined} alt={student.fullName} />
            <AvatarFallback className="text-xs">{getInitials(student.fullName)}</AvatarFallback>
          </Avatar>
          <span className="font-medium">{student.fullName}</span>
        </div>
      )
    },
    size: 250,
  },
  {
    accessorKey: 'fullNameKhmer',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.students.columns.fullNameKhmer} />
    ),
    cell: ({ row }) => (
      <span className="font-medium">{row.getValue('fullNameKhmer') || '-'}</span>
    ),
    size: 200,
  },
  {
    accessorKey: 'gender',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.students.columns.gender} />
    ),
    cell: ({ row }) => {
      const gender = row.getValue('gender') as 'M' | 'F'
      return <span className="text-muted-foreground">{gender === 'M' ? t.students.gender.M : t.students.gender.F}</span>
    },
    size: 100,
  },
  {
    accessorKey: 'dateOfBirth',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.students.columns.dateOfBirth} />
    ),
    cell: ({ row }) => (
      <span className="text-muted-foreground">
        {formatDate(row.getValue('dateOfBirth'))}
      </span>
    ),
    size: 130,
  },
  {
    accessorKey: 'enrollmentDate',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.students.view.fields.enrollmentDate} />
    ),
    cell: ({ row }) => (
      <span className="text-muted-foreground">
        {formatDate(row.getValue('enrollmentDate'))}
      </span>
    ),
    size: 130,
  }
]
