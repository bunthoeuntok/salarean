import { useCallback } from 'react'
import { DataTable } from './data-table'
import { useTableUrlParams } from './use-table-url-params'
import { getStoredPageSize } from './use-table-state-storage'
import type { DataTableProps } from './types'

/**
 * ServerDataTable - For server-side paginated data operations
 *
 * Use this when:
 * - Large datasets (> 1000 rows)
 * - Paginated API endpoints
 * - Sorting/filtering done on backend
 *
 * Features:
 * - Server-side pagination (triggers API calls)
 * - Server-side sorting (triggers API calls)
 * - URL-persisted state (page, size, search, sort)
 * - Shareable/bookmarkable URLs
 *
 * @example
 * ```tsx
 * const { data, isLoading } = useQuery({
 *   queryKey: ['students', pageIndex, pageSize, searchValue, sorting],
 *   queryFn: () => api.getStudents({ page: pageIndex, size: pageSize, ... })
 * })
 *
 * <ServerDataTable
 *   data={data?.content ?? []}
 *   columns={columns}
 *   pageCount={data?.totalPages ?? 0}
 *   storageKey="students-table"
 *   isLoading={isLoading}
 * />
 * ```
 */
export function ServerDataTable<TData, TValue = unknown>({
  storageKey = 'table',
  searchPlaceholder = 'Search...',
  ...props
}: Omit<
  DataTableProps<TData, TValue>,
  'pageIndex' | 'pageSize' | 'onPaginationChange' | 'sorting' | 'onSortingChange' | 'searchValue' | 'onSearchChange'
> & {
  /**
   * Storage key for persisting table state (required for page size)
   */
  storageKey: string
}) {
  // URL params for server-side state
  const {
    pageIndex,
    pageSize,
    searchValue,
    sorting,
    setPage,
    setPageSize,
    setSorting,
    updateParams,
  } = useTableUrlParams({
    defaultPageSize: getStoredPageSize(storageKey),
  })

  // Handle pagination changes
  const handlePaginationChange = useCallback(
    (newPageIndex: number, newPageSize: number) => {
      if (newPageSize !== pageSize) {
        setPageSize(newPageSize)
      } else {
        setPage(newPageIndex)
      }
    },
    [pageSize, setPage, setPageSize]
  )

  // Handle search changes (debounced in DataTableToolbar)
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
      pageIndex={pageIndex}
      pageSize={pageSize}
      onPaginationChange={handlePaginationChange}
      sorting={sorting}
      onSortingChange={setSorting}
      manualSorting={true}
      searchValue={searchValue}
      onSearchChange={handleSearchChange}
      searchPlaceholder={searchPlaceholder}
      showPagination={true}
      // Server-side: callbacks trigger parent to refetch with new params
    />
  )
}
