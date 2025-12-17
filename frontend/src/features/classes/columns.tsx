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
import type { Class, ClassStatus, ClassLevel, ClassType } from '@/types/class.types'

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
        className: string
        grade: string
        academicYear: string
        teacher: string
        enrollment: string
        level: string
        type: string
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
      level: {
        PRIMARY: string
        SECONDARY: string
        HIGH_SCHOOL: string
      }
      type: {
        NORMAL: string
        SCIENCE: string
        SOCIAL_SCIENCE: string
      }
    }
  },
  onEdit?: (classItem: Class) => void,
  onDelete?: (classItem: Class) => void,
  onView?: (classItem: Class) => void,
  onManageStudents?: (classItem: Class) => void
): ColumnDef<Class>[] => [
  {
    accessorKey: 'level',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.classes.columns.level} />
    ),
    cell: ({ row }) => {
      const level = row.getValue('level') as ClassLevel
      return (
        <Badge variant='outline'>
          {t.classes.level[level] || level}
        </Badge>
      )
    },
    filterFn: (row, id, value) => {
      return value.includes(row.getValue(id))
    },
    size: 120,
  },
  {
    accessorKey: 'grade',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.classes.columns.grade} />
    ),
    cell: ({ row }) => {
      const classItem = row.original
      return (
        <div className=''>
          <Badge variant='outline'>{classItem.grade}</Badge>
        </div>
      )
    },
    filterFn: (row, id, value) => {
      return value.includes(String(row.getValue(id)))
    },
    size: 150,
  },
  {
    id: 'className',
    accessorFn: (row) => row.name,
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.classes.columns.className} />
    ),
    cell: ({ row }) => {
      const classItem = row.original
      return (
        <span className='font-medium'>
          {classItem.name}
        </span>
      )
    },
    size: 200,
  },
  {
    accessorKey: 'type',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.classes.columns.type} />
    ),
    cell: ({ row }) => {
      const type = row.getValue('type') as ClassType
      return (
        <Badge variant='secondary'>
          {t.classes.type[type] || type}
        </Badge>
      )
    },
    filterFn: (row, id, value) => {
      return value.includes(row.getValue(id))
    },
    size: 130,
  },
  {
    id: 'enrollment',
    header: t.classes.columns.enrollment,
    cell: ({ row }) => {
      const classItem = row.original
      const percentage = classItem.maxCapacity > 0
        ? Math.round((classItem.studentCount / classItem.maxCapacity) * 100)
        : 0

      return (
        <div className='flex items-center gap-2 min-w-[120px]'>
          <Users className='h-4 w-4 text-muted-foreground' />
          <div className='flex-1'>
            <div className='flex items-center justify-between text-xs mb-1'>
              <span>{classItem.studentCount}/{classItem.maxCapacity}</span>
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
        <div className='flex justify-center'>
          <Badge variant={statusVariantMap[status]} className='flex items-center gap-1 w-fit'>
            <StatusIcon className='h-3 w-3' />
            {t.classes.status[status] || status}
          </Badge>
        </div>
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
        <div className='text-center'>
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
        </div>
      )
    },
    size: 70,
  },
]
