import type { ColumnDef } from '@tanstack/react-table'
import { MoreHorizontal, Phone, Eye, Pencil, UserPlus, ArrowRightLeft, Trash2 } from 'lucide-react'
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
        enroll: string
        transfer: string
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
  onView?: (student: Student) => void,
  onEnroll?: (student: Student) => void,
  onTransfer?: (student: Student) => void
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
    accessorKey: 'fullName',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.students.columns.name} />
    ),
    cell: ({ row }) => {
      const student = row.original
      const initials = `${student.firstName[0]}${student.lastName[0]}`

      return (
        <div className='flex items-center gap-3'>
          <Avatar className='h-10 w-10'>
            <AvatarImage src={student.photoUrl} alt={student.fullName} />
            <AvatarFallback className='text-xs'>{initials}</AvatarFallback>
          </Avatar>
          <div className='flex flex-col'>
            <span className='font-medium'>{student.fullNameKhmer}</span>
            {student.fullName && (
              <span className='text-xs text-muted-foreground'>
                {student.fullName}
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
          {student.primaryParentContact ? (
            <div className='flex items-center gap-1 text-xs text-muted-foreground'>
              <Phone className='h-3 w-3' />
              <span>{student.primaryParentContact}</span>
            </div>
          ) : (
            <span className='text-muted-foreground'>-</span>
          )}
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
        <div className='text-center'>
          <Badge variant={statusVariantMap[status]}>
            {t.students.status[status] || status}
          </Badge>
        </div>
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
        <div className='text-center'>
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
                  <Eye className='mr-2 h-4 w-4' />
                  {t.students.actions.view}
                </DropdownMenuItem>
              )}
              {onEdit && (
                <DropdownMenuItem onClick={() => onEdit(student)}>
                  <Pencil className='mr-2 h-4 w-4' />
                  {t.students.actions.edit}
                </DropdownMenuItem>
              )}
              {onEnroll && !student.currentClassId && (
                <DropdownMenuItem onClick={() => onEnroll(student)}>
                  <UserPlus className='mr-2 h-4 w-4' />
                  {t.students.actions.enroll}
                </DropdownMenuItem>
              )}
              {onTransfer && student.currentClassId && (
                <DropdownMenuItem onClick={() => onTransfer(student)}>
                  <ArrowRightLeft className='mr-2 h-4 w-4' />
                  {t.students.actions.transfer}
                </DropdownMenuItem>
              )}
              {onDelete && (
                <DropdownMenuItem
                  onClick={() => onDelete(student)}
                  className='text-destructive'
                >
                  <Trash2 className='mr-2 h-4 w-4' />
                  {t.students.actions.delete}
                </DropdownMenuItem>
              )}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      )
    },
    size: 70,
  },
]
