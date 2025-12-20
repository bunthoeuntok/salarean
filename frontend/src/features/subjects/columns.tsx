import type { ColumnDef } from '@tanstack/react-table'
import { Check, X, GraduationCap } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataTableColumnHeader } from '@/components/data-table'
import type { Subject } from '@/types/subject.types'

export const createSubjectColumns = (
  t: {
    subjects: {
      columns: {
        name: string
        nameKhmer: string
        code: string
        gradeLevels: string
        isCore: string
      }
    }
    common: {
      grade: string
    }
  },
  gradeLabel: string
): ColumnDef<Subject>[] => [
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
      return (
        <div className="flex flex-wrap gap-1">
          {levels.map((level) => (
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
    accessorKey: 'isCore',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.subjects.columns.isCore} />
    ),
    cell: ({ row }) => {
      const isCore = row.original.isCore
      return (
        <div className="flex justify-center">
          {isCore ? (
            <Check className="h-5 w-5 text-green-600" />
          ) : (
            <X className="h-5 w-5 text-muted-foreground" />
          )}
        </div>
      )
    },
    size: 100,
  },
]
