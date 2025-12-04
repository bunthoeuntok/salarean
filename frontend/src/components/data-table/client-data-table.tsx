import { useState, useCallback } from 'react'
import { DataTable } from './data-table'
import type { DataTableProps } from './types'

/**
 * ClientDataTable - For fully client-side data operations
 *
 * Use this when:
 * - All data is loaded upfront (small datasets < 1000 rows)
 * - No pagination needed (or client-side pagination)
 * - Sorting/filtering done in frontend
 *
 * Features:
 * - Client-side search (no API calls)
 * - Client-side sorting (no API calls)
 * - No pagination (shows all rows)
 * - Optional URL persistence for search
 *
 * @example
 * ```tsx
 * <ClientDataTable
 *   data={allStudents}
 *   columns={columns}
 *   searchPlaceholder="Search students..."
 *   storageKey="students-table"
 * />
 * ```
 */
export function ClientDataTable<TData, TValue = unknown>({
  searchPlaceholder = 'Search...',
  enableSearch = true,
  initialSearchValue = '',
  ...props
}: Omit<
  DataTableProps<TData, TValue>,
  'pageIndex' | 'pageSize' | 'pageCount' | 'onPaginationChange' | 'sorting' | 'onSortingChange' | 'showPagination'
> & {
  /**
   * Enable search functionality (default: true)
   */
  enableSearch?: boolean
  /**
   * Initial search value
   */
  initialSearchValue?: string
}) {
  // Local state for search (no URL persistence by default)
  const [searchValue, setSearchValue] = useState(initialSearchValue)

  const handleSearchChange = useCallback((value: string) => {
    setSearchValue(value)
  }, [])

  return (
    <DataTable
      {...props}
      searchValue={enableSearch ? searchValue : undefined}
      onSearchChange={enableSearch ? handleSearchChange : undefined}
      searchPlaceholder={searchPlaceholder}
      showPagination={false}
      // Client-side: no pagination/sorting callbacks = TanStack Table handles it
    />
  )
}
