import { X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { DataTableFacetedFilter } from './data-table-faceted-filter'
import { DataTableViewOptions } from './data-table-view-options'
import type { DataTableToolbarProps } from './types'

export function DataTableToolbar<TData>({
  table,
  searchValue,
  onSearchChange,
  searchPlaceholder = 'Search...',
  filterableColumns = [],
  toolbarActions,
  columnLabels,
}: DataTableToolbarProps<TData>) {
  const isFiltered = table.getState().columnFilters.length > 0

  return (
    <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
      <div className='flex flex-1 flex-col gap-2 sm:flex-row sm:items-center sm:space-x-2'>
        {onSearchChange && (
          <Input
            placeholder={searchPlaceholder}
            value={searchValue ?? ''}
            onChange={(event) => onSearchChange(event.target.value)}
            className='h-8 w-full sm:w-[150px] lg:w-[250px]'
          />
        )}
        <div className='flex flex-wrap gap-2'>
          {filterableColumns.map((column) => {
            const tableColumn = table.getColumn(column.id)
            if (!tableColumn) return null
            return (
              <DataTableFacetedFilter
                key={column.id}
                column={tableColumn}
                title={column.title}
                options={column.options}
              />
            )
          })}
          {isFiltered && (
            <Button
              variant='ghost'
              onClick={() => table.resetColumnFilters()}
              className='h-8 px-2 lg:px-3'
            >
              Reset
              <X className='ml-2 h-4 w-4' />
            </Button>
          )}
        </div>
      </div>
      <div className='flex items-center gap-2'>
        {toolbarActions}
        <DataTableViewOptions table={table} columnLabels={columnLabels} />
      </div>
    </div>
  )
}
