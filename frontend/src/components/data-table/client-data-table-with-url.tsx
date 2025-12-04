import { useCallback } from 'react'
import { DataTable } from './data-table'
import { useTableUrlParams } from './use-table-url-params'
import type { DataTableProps } from './types'

/**
 * ClientDataTableWithUrl - Client-side operations with URL-persisted search & sort
 *
 * Use this when:
 * - All data is loaded upfront (small datasets)
 * - No pagination needed
 * - Want URL persistence for search & sorting (shareable links)
 * - Sorting/filtering done in frontend
 *
 * Features:
 * - Client-side search with URL persistence
 * - Client-side sorting with URL persistence
 * - No pagination
 * - Fully shareable URLs with search & sort state
 *
 * @example
 * ```tsx
 * <ClientDataTableWithUrl
 *   data={classStudents}
 *   columns={columns}
 *   searchPlaceholder="Search students..."
 *   storageKey="class-students"
 * />
 * ```
 *
 * URL Example: `/classes/123?search=john&sort=fullName&sortDir=asc`
 */
export function ClientDataTableWithUrl<TData, TValue = unknown>({
  storageKey = 'table',
  searchPlaceholder = 'Search...',
  ...props
}: Omit<
  DataTableProps<TData, TValue>,
  'pageIndex' | 'pageSize' | 'pageCount' | 'onPaginationChange' | 'sorting' | 'onSortingChange' | 'searchValue' | 'onSearchChange' | 'showPagination'
> & {
  /**
   * Storage key for persisting table state
   */
  storageKey?: string
}) {
  // URL params for search AND sorting (no pagination)
  const { searchValue, sorting, setSorting, updateParams } = useTableUrlParams({
    defaultPageSize: 10, // Not used
  })

  // Handle search changes with URL persistence
  const handleSearchChange = useCallback(
    (value: string) => {
      updateParams({ search: value || undefined })
    },
    [updateParams]
  )

  return (
    <DataTable
      {...props}
      storageKey={storageKey}
      searchValue={searchValue}
      onSearchChange={handleSearchChange}
      sorting={sorting}
      onSortingChange={setSorting}
      manualSorting={false}
      searchPlaceholder={searchPlaceholder}
      showPagination={false}
      // Client-side sorting: TanStack Table does the sorting, we just persist to URL
    />
  )
}
