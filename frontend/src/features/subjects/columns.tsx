import type { ColumnDef } from '@tanstack/react-table'
import { GraduationCap, Pencil, MoreHorizontal } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { DataTableColumnHeader } from '@/components/data-table'
import type { Subject } from '@/types/subject.types'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

interface SubjectColumnsProps {
  t: {
    subjects: {
      columns: {
        name: string
        nameKhmer: string
        code: string
        gradeLevels: string
        isCore: string
        actions: string
      }
      actions: {
        edit: string
      }
    }
    common: {
      grade: string
    }
  }
  gradeLabel: string
  availableGrades: number[]
  onEdit: (subject: Subject) => void
}

export const createSubjectColumns = ({
  t,
  gradeLabel,
  availableGrades,
  onEdit,
}: SubjectColumnsProps): ColumnDef<Subject>[] => [
  {
    accessorKey: 'displayOrder',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title="#" />
    ),
    cell: ({ row }) => (
      <span className="text-muted-foreground">{row.original.displayOrder}</span>
    ),
    size: 60,
  },
  {
    accessorKey: 'code',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.subjects.columns.code} />
    ),
    cell: ({ row }) => (
      <Badge variant="outline" className="font-mono">
        {row.original.code}
      </Badge>
    ),
    size: 100,
  },
  {
    accessorKey: 'name',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.subjects.columns.name} />
    ),
    cell: ({ row }) => (
      <span className="font-medium">{row.original.name}</span>
    ),
    size: 200,
  },
  {
    accessorKey: 'nameKhmer',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.subjects.columns.nameKhmer} />
    ),
    cell: ({ row }) => (
      <span className="font-medium">{row.original.nameKhmer}</span>
    ),
    size: 200,
  },
  {
    accessorKey: 'gradeLevels',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.subjects.columns.gradeLevels} />
    ),
    cell: ({ row }) => {
      const levels = row.original.gradeLevels
      if (!levels || levels.length === 0) {
        return <span className="text-muted-foreground">-</span>
      }
      // Filter to only show grades within the teacher's school level
      const filteredLevels = levels.filter((level) => availableGrades.includes(level))
      if (filteredLevels.length === 0) {
        return <span className="text-muted-foreground">-</span>
      }
      return (
        <div className="flex flex-wrap gap-1">
          {filteredLevels.map((level) => (
            <Badge key={level} variant="secondary" className="text-xs">
              <GraduationCap className="mr-1 h-3 w-3" />
              {gradeLabel} {level}
            </Badge>
          ))}
        </div>
      )
    },
    size: 300,
  },
  {
    id: 'actions',
    header: t.subjects.columns.actions,
    cell: ({ row }) => {
      const subject = row.original
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
              <DropdownMenuLabel>{t.subjects.columns.actions}</DropdownMenuLabel>
              <DropdownMenuSeparator />
              
              {onEdit && (
                <DropdownMenuItem onClick={() => onEdit(subject)}>
                  <Pencil className='mr-2 h-4 w-4' />
                  {t.subjects.actions.edit}
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
