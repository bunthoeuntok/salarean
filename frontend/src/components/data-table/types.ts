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
  // Toolbar actions
  toolbarActions?: React.ReactNode
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

export interface DataTableToolbarProps<TData> {
  table: Table<TData>
  searchValue?: string
  onSearchChange?: (value: string) => void
  searchPlaceholder?: string
  filterableColumns?: {
    id: string
    title: string
    options: { label: string; value: string; icon?: React.ComponentType<{ className?: string }> }[]
  }[]
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
