import type { ColumnDef } from '@tanstack/react-table'
import { Pencil, MoreHorizontal } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { DataTableColumnHeader } from '@/components/data-table'
import type { AssessmentType, AssessmentCategory } from '@/types/assessment-type.types'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

interface AssessmentTypeColumnsProps {
  t: {
    assessmentTypes: {
      columns: {
        name: string
        nameKhmer: string
        code: string
        category: string
        defaultWeight: string
        maxScore: string
        actions: string
      }
      actions: {
        edit: string
      }
      categories: {
        MONTHLY_EXAM: string
        SEMESTER_EXAM: string
      }
    }
  }
  onEdit: (assessmentType: AssessmentType) => void
}

const getCategoryVariant = (category: AssessmentCategory): 'default' | 'secondary' => {
  return category === 'SEMESTER_EXAM' ? 'default' : 'secondary'
}

export const createAssessmentTypeColumns = ({
  t,
  onEdit,
}: AssessmentTypeColumnsProps): ColumnDef<AssessmentType>[] => [
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
      <DataTableColumnHeader column={column} title={t.assessmentTypes.columns.code} />
    ),
    cell: ({ row }) => (
      <Badge variant="outline" className="font-mono">
        {row.original.code}
      </Badge>
    ),
    size: 120,
  },
  {
    accessorKey: 'name',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.assessmentTypes.columns.name} />
    ),
    cell: ({ row }) => (
      <span className="font-medium">{row.original.name}</span>
    ),
    size: 200,
  },
  {
    accessorKey: 'nameKhmer',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.assessmentTypes.columns.nameKhmer} />
    ),
    cell: ({ row }) => (
      <span className="font-medium">{row.original.nameKhmer}</span>
    ),
    size: 200,
  },
  {
    accessorKey: 'category',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.assessmentTypes.columns.category} />
    ),
    cell: ({ row }) => {
      const category = row.original.category
      return (
        <Badge variant={getCategoryVariant(category)}>
          {t.assessmentTypes.categories[category]}
        </Badge>
      )
    },
    size: 150,
  },
  {
    accessorKey: 'defaultWeight',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.assessmentTypes.columns.defaultWeight} />
    ),
    cell: ({ row }) => (
      <span className="text-muted-foreground">{row.original.defaultWeight}%</span>
    ),
    size: 120,
  },
  {
    accessorKey: 'maxScore',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title={t.assessmentTypes.columns.maxScore} />
    ),
    cell: ({ row }) => (
      <span className="text-muted-foreground">{row.original.maxScore}</span>
    ),
    size: 100,
  },
  {
    id: 'actions',
    header: t.assessmentTypes.columns.actions,
    cell: ({ row }) => {
      const assessmentType = row.original
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
              <DropdownMenuLabel>{t.assessmentTypes.columns.actions}</DropdownMenuLabel>
              <DropdownMenuSeparator />

              {onEdit && (
                <DropdownMenuItem onClick={() => onEdit(assessmentType)}>
                  <Pencil className='mr-2 h-4 w-4' />
                  {t.assessmentTypes.actions.edit}
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
