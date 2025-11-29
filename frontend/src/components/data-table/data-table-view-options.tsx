import { Columns3 } from 'lucide-react'
import type { Table } from '@tanstack/react-table'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { useLanguage } from '@/context/language-provider'

interface DataTableViewOptionsProps<TData> {
  table: Table<TData>
  columnLabels?: Record<string, string>
}

export function DataTableViewOptions<TData>({
  table,
  columnLabels = {},
}: DataTableViewOptionsProps<TData>) {
  const { t } = useLanguage()
  const columns = table
    .getAllColumns()
    .filter(
      (column) =>
        typeof column.accessorFn !== 'undefined' && column.getCanHide()
    )

  const visibleCount = columns.filter((col) => col.getIsVisible()).length

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button variant='outline' size='sm' className='h-9'>
          <Columns3 className='mr-2 h-4 w-4' />
          {t.table.view}
          <span className='ml-1.5 rounded bg-muted px-1.5 py-0.5 text-xs font-medium'>
            {visibleCount}/{columns.length}
          </span>
        </Button>
      </PopoverTrigger>
      <PopoverContent align='end' className='w-[200px] p-0'>
        <div className='p-3 pb-2'>
          <p className='text-sm font-medium'>{t.table.toggleColumns}</p>
          <p className='text-xs text-muted-foreground'>
            {visibleCount} of {columns.length} visible
          </p>
        </div>
        <Separator />
        <div className='max-h-[300px] overflow-y-auto p-2'>
          {columns.map((column) => (
            <div
              key={column.id}
              className='flex items-center space-x-2 rounded-sm px-2 py-1.5 hover:bg-accent'
            >
              <Checkbox
                id={`column-${column.id}`}
                checked={column.getIsVisible()}
                onCheckedChange={(checked) =>
                  column.toggleVisibility(!!checked)
                }
              />
              <Label
                htmlFor={`column-${column.id}`}
                className='flex-1 text-sm font-normal cursor-pointer'
              >
                {columnLabels[column.id] || column.id}
              </Label>
            </div>
          ))}
        </div>
      </PopoverContent>
    </Popover>
  )
}
