import type { Column, ColumnDef, Table } from '@tanstack/react-table'

export interface DataTableProps<TData, TValue> {
  columns: ColumnDef<TData, TValue>[]
  data: TData[]
  isLoading?: boolean
  // Storage key for persisting table state (column order, visibility, sizing)
  storageKey?: string
  // Pagination
  pageCount?: number
  pageIndex?: number
  pageSize?: number
  onPaginationChange?: (pageIndex: number, pageSize: number) => void
  // Sorting
  sorting?: { id: string; desc: boolean }[]
  onSortingChange?: (sorting: { id: string; desc: boolean }[]) => void
  // Search
  searchValue?: string
  onSearchChange?: (value: string) => void
  searchPlaceholder?: string
  // Filters
  filterableColumns?: {
    id: string
    title: string
    options: { label: string; value: string; icon?: React.ComponentType<{ className?: string }> }[]
  }[]
  // Row selection
  enableRowSelection?: boolean
  onRowSelectionChange?: (rows: TData[]) => void
  // Column features
  enableColumnResizing?: boolean
  enableColumnReordering?: boolean
  // Toolbar
  showToolbar?: boolean
  // Pagination display
  showPagination?: boolean
  columnLabels?: Record<string, string>
  // Toolbar actions
  toolbarActions?: React.ReactNode
  // Render prop for external toolbar integration
  renderViewOptions?: (table: Table<TData>) => React.ReactNode
  // Callback to get table instance for external use
  onTableReady?: (table: Table<TData>) => void
}

export interface DataTableColumnHeaderProps<TData, TValue> {
  column: Column<TData, TValue>
  title: string
  className?: string
}

export interface DataTablePaginationProps<TData> {
  table: Table<TData>
  pageSizeOptions?: number[]
}

export interface FilterableColumn {
  id: string
  title: string
  options: { label: string; value: string; icon?: React.ComponentType<{ className?: string }> }[]
  singleSelect?: boolean
  onFilterChange?: (values: string[]) => void
}

export interface DataTableToolbarProps<TData> {
  table: Table<TData>
  searchValue?: string
  onSearchChange?: (value: string) => void
  searchPlaceholder?: string
  filterableColumns?: FilterableColumn[]
  toolbarActions?: React.ReactNode
  columnLabels?: Record<string, string>
}

export interface DataTableFilterToolbarProps {
  // Initial values from URL params
  initialSearch?: string
  initialFilters?: Record<string, string[]>
  // Filterable columns configuration
  filterableColumns?: FilterableColumn[]
  // Callbacks
  onSubmit: (params: { search: string; filters: Record<string, string[]> }) => void
  onReset: () => void
  // UI customization
  searchPlaceholder?: string
  submitLabel?: string
  resetLabel?: string
  // Additional toolbar actions
  toolbarActions?: React.ReactNode
}

export interface DataTableFacetedFilterProps<TData, TValue> {
  column?: Column<TData, TValue>
  title?: string
  options: {
    label: string
    value: string
    icon?: React.ComponentType<{ className?: string }>
  }[]
}
