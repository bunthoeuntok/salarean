import { Settings2 } from 'lucide-react'
import type { Table } from '@tanstack/react-table'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { Label } from '@/components/ui/label'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { useLanguage } from '@/context/language-provider'

interface DataTableViewOptionsProps<TData> {
  table: Table<TData>
}

export function DataTableViewOptions<TData>({
  table,
}: DataTableViewOptionsProps<TData>) {
  const { t } = useLanguage()
  const columns = table
    .getAllColumns()
    .filter(
      (column) =>
        typeof column.accessorFn !== 'undefined' && column.getCanHide()
    )

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button
          variant='outline'
          size='sm'
          className='ml-auto hidden h-8 lg:flex'
        >
          <Settings2 className='mr-2 h-4 w-4' />
          View
        </Button>
      </PopoverTrigger>
      <PopoverContent align='end' className='w-[180px] p-3'>
        <div className='mb-2 text-sm font-medium'>Toggle columns</div>
        <div className='space-y-2'>
          {columns.map((column) => (
            <div key={column.id} className='flex items-center space-x-2'>
              <Checkbox
                id={`column-${column.id}`}
                checked={column.getIsVisible()}
                onCheckedChange={(checked) =>
                  column.toggleVisibility(!!checked)
                }
              />
              <Label
                htmlFor={`column-${column.id}`}
                className='text-sm font-normal capitalize cursor-pointer'
              >
                {column.id}
              </Label>
            </div>
          ))}
        </div>
      </PopoverContent>
    </Popover>
  )
}
