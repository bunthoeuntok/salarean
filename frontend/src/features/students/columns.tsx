import type { ColumnDef } from '@tanstack/react-table'
import { MoreHorizontal, Mail, Phone } from 'lucide-react'
import { format } from 'date-fns'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { DataTableColumnHeader } from '@/components/data-table'
import type { Student, StudentStatus, Gender } from '@/types/student.types'

const statusVariantMap: Record<StudentStatus, 'default' | 'secondary'> = {
  ACTIVE: 'default',
  INACTIVE: 'secondary'
}

const genderLabels: Record<Gender, string> = {
  M: 'Male',
  F: 'Female',
}

export const createStudentColumns = (
  t: {
    students: {
      columns: {
        code: string
        name: string
        gender: string
        dateOfBirth: string
        contact: string
        class: string
        status: string
        actions: string
      }
      actions: {
        view: string
        edit: string
        delete: string
      }
      status: {
        ACTIVE: string
        INACTIVE: string
      }
      gender: {
        M: string
        F: string
      }
    }
  },
  onEdit?: (student: Student) => void,
  onDelete?: (student: Student) => void,
  onView?: (student: Student) => void
): ColumnDef<Student>[] => [
  {
    accessorKey: 'studentCode',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.students.columns.code} />
    ),
    cell: ({ row }) => (
      <span className='font-medium'>{row.getValue('studentCode')}</span>
    ),
    size: 120,
  },
  {
    accessorKey: 'firstName',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.students.columns.name} />
    ),
    cell: ({ row }) => {
      const student = row.original
      const fullName = `${student.firstName} ${student.lastName}`
      const initials = `${student.firstName[0]}${student.lastName[0]}`

      return (
        <div className='flex items-center gap-3'>
          <Avatar className='h-8 w-8'>
            <AvatarImage src={student.photoUrl} alt={fullName} />
            <AvatarFallback className='text-xs'>{initials}</AvatarFallback>
          </Avatar>
          <div className='flex flex-col'>
            <span className='font-medium'>{fullName}</span>
            {student.firstNameKhmer && (
              <span className='text-xs text-muted-foreground'>
                {student.firstNameKhmer} {student.lastNameKhmer}
              </span>
            )}
          </div>
        </div>
      )
    },
    size: 200,
  },
  {
    accessorKey: 'gender',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.students.columns.gender} />
    ),
    cell: ({ row }) => {
      const gender = row.getValue('gender') as Gender
      return t.students.gender[gender] || genderLabels[gender]
    },
    filterFn: (row, id, value) => {
      return value.includes(row.getValue(id))
    },
    size: 100,
  },
  {
    accessorKey: 'dateOfBirth',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.students.columns.dateOfBirth} />
    ),
    cell: ({ row }) => {
      const date = row.getValue('dateOfBirth') as string
      return date ? format(new Date(date), 'dd/MM/yyyy') : '-'
    },
    size: 120,
  },
  {
    id: 'contact',
    header: t.students.columns.contact,
    cell: ({ row }) => {
      const student = row.original
      return (
        <div className='flex flex-col gap-1'>
          {student.email && (
            <div className='flex items-center gap-1 text-xs text-muted-foreground'>
              <Mail className='h-3 w-3' />
              <span className='truncate max-w-[150px]'>{student.email}</span>
            </div>
          )}
          {student.primaryParentContact && (
            <div className='flex items-center gap-1 text-xs text-muted-foreground'>
              <Phone className='h-3 w-3' />
              <span>{student.primaryParentContact}</span>
            </div>
          )}
          {!student.email && !student.primaryParentContact && '-'}
        </div>
      )
    },
    size: 180,
  },
  {
    accessorKey: 'currentClassName',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.students.columns.class} />
    ),
    cell: ({ row }) => {
      const className = row.getValue('currentClassName') as string
      return className ? (
        <Badge variant='outline'>{className}</Badge>
      ) : (
        <span className='text-muted-foreground'>-</span>
      )
    },
    size: 120,
  },
  {
    accessorKey: 'status',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.students.columns.status} />
    ),
    cell: ({ row }) => {
      const status = row.getValue('status') as StudentStatus
      return (
        <Badge variant={statusVariantMap[status]}>
          {t.students.status[status] || status}
        </Badge>
      )
    },
    filterFn: (row, id, value) => {
      return value.includes(row.getValue(id))
    },
    size: 100,
  },
  {
    id: 'actions',
    header: t.students.columns.actions,
    cell: ({ row }) => {
      const student = row.original

      return (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant='ghost' className='h-8 w-8 p-0'>
              <span className='sr-only'>Open menu</span>
              <MoreHorizontal className='h-4 w-4' />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align='end'>
            <DropdownMenuLabel>{t.students.columns.actions}</DropdownMenuLabel>
            <DropdownMenuSeparator />
            {onView && (
              <DropdownMenuItem onClick={() => onView(student)}>
                {t.students.actions.view}
              </DropdownMenuItem>
            )}
            {onEdit && (
              <DropdownMenuItem onClick={() => onEdit(student)}>
                {t.students.actions.edit}
              </DropdownMenuItem>
            )}
            {onDelete && (
              <DropdownMenuItem
                onClick={() => onDelete(student)}
                className='text-destructive'
              >
                {t.students.actions.delete}
              </DropdownMenuItem>
            )}
          </DropdownMenuContent>
        </DropdownMenu>
      )
    },
    size: 70,
  },
]
