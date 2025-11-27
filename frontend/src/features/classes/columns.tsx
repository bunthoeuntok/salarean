import type { ColumnDef } from '@tanstack/react-table'
import { MoreHorizontal, Users, CheckCircle, XCircle, Clock } from 'lucide-react'
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
import { Progress } from '@/components/ui/progress'
import { DataTableColumnHeader } from '@/components/data-table'
import type { Class, ClassStatus } from '@/types/class.types'

const statusVariantMap: Record<ClassStatus, 'default' | 'secondary' | 'destructive'> = {
  ACTIVE: 'default',
  INACTIVE: 'secondary',
  COMPLETED: 'destructive',
}

const statusIconMap: Record<ClassStatus, React.ComponentType<{ className?: string }>> = {
  ACTIVE: CheckCircle,
  INACTIVE: XCircle,
  COMPLETED: Clock,
}

export const createClassColumns = (
  t: {
    classes: {
      columns: {
        name: string
        grade: string
        academicYear: string
        teacher: string
        enrollment: string
        status: string
        actions: string
      }
      actions: {
        view: string
        edit: string
        delete: string
        manageStudents: string
      }
      status: {
        ACTIVE: string
        INACTIVE: string
        COMPLETED: string
      }
    }
  },
  onEdit?: (classItem: Class) => void,
  onDelete?: (classItem: Class) => void,
  onView?: (classItem: Class) => void,
  onManageStudents?: (classItem: Class) => void
): ColumnDef<Class>[] => [
  {
    accessorKey: 'name',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.classes.columns.name} />
    ),
    cell: ({ row }) => {
      const classItem = row.original
      return (
        <div className='flex flex-col'>
          <span className='font-medium'>{classItem.name}</span>
          {classItem.description && (
            <span className='text-xs text-muted-foreground truncate max-w-[200px]'>
              {classItem.description}
            </span>
          )}
        </div>
      )
    },
    size: 200,
  },
  {
    accessorKey: 'grade',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.classes.columns.grade} />
    ),
    cell: ({ row }) => {
      const classItem = row.original
      return (
        <div className='flex items-center gap-2'>
          <Badge variant='outline'>{classItem.grade}</Badge>
          {classItem.section && (
            <span className='text-muted-foreground'>- {classItem.section}</span>
          )}
        </div>
      )
    },
    filterFn: (row, id, value) => {
      return value.includes(row.getValue(id))
    },
    size: 150,
  },
  {
    accessorKey: 'academicYear',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.classes.columns.academicYear} />
    ),
    cell: ({ row }) => (
      <span>{row.getValue('academicYear')}</span>
    ),
    filterFn: (row, id, value) => {
      return value.includes(row.getValue(id))
    },
    size: 120,
  },
  {
    accessorKey: 'teacherName',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.classes.columns.teacher} />
    ),
    cell: ({ row }) => {
      const teacherName = row.getValue('teacherName') as string
      return teacherName || <span className='text-muted-foreground'>-</span>
    },
    size: 150,
  },
  {
    id: 'enrollment',
    header: t.classes.columns.enrollment,
    cell: ({ row }) => {
      const classItem = row.original
      const percentage = classItem.capacity > 0
        ? Math.round((classItem.currentEnrollment / classItem.capacity) * 100)
        : 0

      return (
        <div className='flex items-center gap-2 min-w-[120px]'>
          <Users className='h-4 w-4 text-muted-foreground' />
          <div className='flex-1'>
            <div className='flex items-center justify-between text-xs mb-1'>
              <span>{classItem.currentEnrollment}/{classItem.capacity}</span>
              <span className='text-muted-foreground'>{percentage}%</span>
            </div>
            <Progress value={percentage} className='h-1.5' />
          </div>
        </div>
      )
    },
    size: 160,
  },
  {
    accessorKey: 'status',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.classes.columns.status} />
    ),
    cell: ({ row }) => {
      const status = row.getValue('status') as ClassStatus
      const StatusIcon = statusIconMap[status]
      return (
        <Badge variant={statusVariantMap[status]} className='flex items-center gap-1 w-fit'>
          <StatusIcon className='h-3 w-3' />
          {t.classes.status[status] || status}
        </Badge>
      )
    },
    filterFn: (row, id, value) => {
      return value.includes(row.getValue(id))
    },
    size: 120,
  },
  {
    id: 'actions',
    header: t.classes.columns.actions,
    cell: ({ row }) => {
      const classItem = row.original

      return (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant='ghost' className='h-8 w-8 p-0'>
              <span className='sr-only'>Open menu</span>
              <MoreHorizontal className='h-4 w-4' />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align='end'>
            <DropdownMenuLabel>{t.classes.columns.actions}</DropdownMenuLabel>
            <DropdownMenuSeparator />
            {onView && (
              <DropdownMenuItem onClick={() => onView(classItem)}>
                {t.classes.actions.view}
              </DropdownMenuItem>
            )}
            {onManageStudents && (
              <DropdownMenuItem onClick={() => onManageStudents(classItem)}>
                {t.classes.actions.manageStudents}
              </DropdownMenuItem>
            )}
            {onEdit && (
              <DropdownMenuItem onClick={() => onEdit(classItem)}>
                {t.classes.actions.edit}
              </DropdownMenuItem>
            )}
            {onDelete && (
              <DropdownMenuItem
                onClick={() => onDelete(classItem)}
                className='text-destructive'
              >
                {t.classes.actions.delete}
              </DropdownMenuItem>
            )}
          </DropdownMenuContent>
        </DropdownMenu>
      )
    },
    size: 70,
  },
]
