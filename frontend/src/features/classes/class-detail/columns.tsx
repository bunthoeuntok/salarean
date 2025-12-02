import type { ColumnDef } from '@tanstack/react-table'
import { format } from 'date-fns'
import { CheckCircle, XCircle, ArrowRightLeft, GraduationCap } from 'lucide-react'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { DataTableColumnHeader } from '@/components/data-table'
import type { StudentEnrollmentItem, EnrollmentStatus } from '@/types/class.types'

const statusIconMap: Record<EnrollmentStatus, React.ComponentType<{ className?: string }>> = {
  ACTIVE: CheckCircle,
  COMPLETED: GraduationCap,
  TRANSFERRED: ArrowRightLeft,
  WITHDRAWN: XCircle,
}

const statusVariantMap: Record<EnrollmentStatus, 'default' | 'secondary' | 'outline'> = {
  ACTIVE: 'default',
  COMPLETED: 'secondary',
  TRANSFERRED: 'outline',
  WITHDRAWN: 'outline',
}

const getInitials = (name: string) => {
  return name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
}

const formatEnrollmentDate = (dateString: string) => {
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
        status: string
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
    accessorKey: 'studentName',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.students.columns.name} />
    ),
    cell: ({ row }) => {
      const student = row.original
      return (
        <div className="flex items-center gap-3">
          <Avatar className="h-10 w-10">
            <AvatarImage src={student.photoUrl ?? undefined} alt={student.studentName} />
            <AvatarFallback className="text-xs">{getInitials(student.studentName)}</AvatarFallback>
          </Avatar>
          <span className="font-medium">{student.studentName}</span>
        </div>
      )
    },
    size: 250,
  },
  {
    accessorKey: 'enrollmentDate',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.students.view.fields.enrollmentDate} />
    ),
    cell: ({ row }) => (
      <span className="text-muted-foreground">
        {formatEnrollmentDate(row.getValue('enrollmentDate'))}
      </span>
    ),
    size: 130,
  },
  {
    accessorKey: 'enrollmentStatus',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.students.columns.status} />
    ),
    cell: ({ row }) => {
      const status = row.getValue('enrollmentStatus') as EnrollmentStatus
      const StatusIcon = statusIconMap[status]
      return (
        <Badge variant={statusVariantMap[status]}>
          <StatusIcon className="mr-1 h-3 w-3" />
          {t.students.view.enrollmentStatus[status]}
        </Badge>
      )
    },
    filterFn: (row, id, value) => {
      return value.includes(row.getValue(id))
    },
    size: 130,
  },
]
