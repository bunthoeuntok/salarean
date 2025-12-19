import { useState, useEffect } from 'react'
import { X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { DataTableFacetedFilter } from './data-table-faceted-filter'
import { DataTableViewOptions } from './data-table-view-options'
import { useDebouncedValue } from '@/hooks/use-debounce'
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

  // Local state for immediate UI updates + tracking previous prop for sync
  const [localSearchValue, setLocalSearchValue] = useState(searchValue ?? '')
  const [prevSearchValue, setPrevSearchValue] = useState(searchValue)

  // Sync local state when prop changes (e.g., from URL navigation)
  if (prevSearchValue !== searchValue) {
    setPrevSearchValue(searchValue)
    setLocalSearchValue(searchValue ?? '')
  }

  // Debounce the search value before updating URL (300ms delay)
  const debouncedSearchValue = useDebouncedValue(localSearchValue, 300)

  // Update URL when debounced value changes
  useEffect(() => {
    if (onSearchChange && debouncedSearchValue !== searchValue) {
      onSearchChange(debouncedSearchValue)
    }
  }, [debouncedSearchValue, onSearchChange, searchValue])

  return (
    <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
      <div className='flex flex-1 flex-col gap-2 sm:flex-row sm:items-center sm:space-x-2'>
        {onSearchChange && (
          <Input
            placeholder={searchPlaceholder}
            value={localSearchValue}
            onChange={(event) => setLocalSearchValue(event.target.value)}
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
