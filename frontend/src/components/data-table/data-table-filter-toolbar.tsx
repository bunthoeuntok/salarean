import { useState, useCallback, useEffect } from 'react'
import { Search, X, Filter } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { FilterSelect } from './filter-select'
import type { DataTableFilterToolbarProps } from './types'

export function DataTableFilterToolbar({
  initialSearch = '',
  initialFilters = {},
  filterableColumns = [],
  onSubmit,
  onReset,
  searchPlaceholder = 'Search...',
  submitLabel = 'Search',
  resetLabel = 'Reset',
  toolbarActions,
}: DataTableFilterToolbarProps) {
  // Local state for pending changes
  const [search, setSearch] = useState(initialSearch)
  const [filters, setFilters] = useState<Record<string, string[]>>(initialFilters)

  // Sync local state when URL params change (e.g., browser back/forward)
  useEffect(() => {
    setSearch(initialSearch)
    setFilters(initialFilters)
  }, [initialSearch, initialFilters])

  // Check if there are any active filters
  const hasActiveFilters =
    search.length > 0 ||
    Object.values(filters).some((values) => values.length > 0)

  // Check if local state differs from initial (URL) state
  const hasChanges =
    search !== initialSearch ||
    JSON.stringify(filters) !== JSON.stringify(initialFilters)

  const handleFilterChange = useCallback((columnId: string, values: string[]) => {
    setFilters((prev) => ({
      ...prev,
      [columnId]: values,
    }))
  }, [])

  const handleSubmit = useCallback(() => {
    onSubmit({ search, filters })
  }, [search, filters, onSubmit])

  const handleReset = useCallback(() => {
    setSearch('')
    setFilters({})
    onReset()
  }, [onReset])

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === 'Enter') {
        handleSubmit()
      }
    },
    [handleSubmit]
  )

  return (
    <div className='flex flex-col gap-4'>
      <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
        {/* Search and filters */}
        <div className='flex flex-1 flex-col gap-2 sm:flex-row sm:items-center sm:gap-2'>
          {/* Search input */}
          <div className='relative w-full sm:w-[200px] lg:w-[300px]'>
            <Search className='absolute left-2.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              placeholder={searchPlaceholder}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={handleKeyDown}
              className='h-9 pl-8'
            />
          </div>

          {/* Faceted filters */}
          {filterableColumns.length > 0 && (
            <div className='flex flex-wrap gap-2'>
              {filterableColumns.map((column) => (
                <FilterSelect
                  key={column.id}
                  title={column.title}
                  options={column.options}
                  selectedValues={filters[column.id] ?? []}
                  onChange={(values) => handleFilterChange(column.id, values)}
                  singleSelect={column.singleSelect}
                />
              ))}
            </div>
          )}
        </div>

        {/* Actions */}
        <div className='flex items-center gap-2'>
          {/* Submit button */}
          <Button
            onClick={handleSubmit}
            size='sm'
            className='h-9'
            disabled={!hasChanges && !hasActiveFilters}
          >
            <Filter className='mr-2 h-4 w-4' />
            {submitLabel}
          </Button>

          {/* Reset button */}
          {hasActiveFilters && (
            <Button
              variant='outline'
              onClick={handleReset}
              size='sm'
              className='h-9'
            >
              <X className='mr-2 h-4 w-4' />
              {resetLabel}
            </Button>
          )}

          {/* Additional toolbar actions */}
          {toolbarActions}
        </div>
      </div>
    </div>
  )
}
