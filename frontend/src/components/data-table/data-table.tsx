import { useState, useCallback, useMemo, useEffect } from 'react'
import {
  DndContext,
  KeyboardSensor,
  MouseSensor,
  TouchSensor,
  closestCenter,
  useSensor,
  useSensors,
  type DragEndEvent,
} from '@dnd-kit/core'
import {
  arrayMove,
  SortableContext,
  horizontalListSortingStrategy,
} from '@dnd-kit/sortable'
import { restrictToHorizontalAxis } from '@dnd-kit/modifiers'
import {
  flexRender,
  getCoreRowModel,
  getFacetedRowModel,
  getFacetedUniqueValues,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  useReactTable,
  type ColumnFiltersState,
  type SortingState,
  type RowSelectionState,
} from '@tanstack/react-table'
import { Loader2 } from 'lucide-react'
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Checkbox } from '@/components/ui/checkbox'
import { DataTablePagination } from './data-table-pagination'
import { DataTableToolbar } from './data-table-toolbar'
import { DraggableTableHeader } from './data-table-draggable-header'
import { useTableStateStorage } from './use-table-state-storage'
import type { DataTableProps } from './types'

export function DataTable<TData, TValue>({
  columns,
  data,
  isLoading = false,
  storageKey,
  // Pagination
  pageCount,
  pageIndex: controlledPageIndex,
  pageSize: controlledPageSize,
  onPaginationChange,
  // Sorting
  sorting: controlledSorting,
  onSortingChange,
  // Search
  searchValue,
  onSearchChange,
  searchPlaceholder,
  // Filters
  filterableColumns,
  // Row selection
  enableRowSelection = false,
  onRowSelectionChange,
  // Column features
  enableColumnResizing = false,
  enableColumnReordering = false,
  // Toolbar
  showToolbar = true,
  // Pagination
  showPagination = true,
  columnLabels,
  // Toolbar actions
  toolbarActions,
  // Render prop for external toolbar integration
  renderViewOptions,
  // Callback to get table instance
  onTableReady,
}: DataTableProps<TData, TValue>) {
  // Get default column order from columns
  const defaultColumnOrder = useMemo(
    () => columns.map((c) => (c as { accessorKey?: string }).accessorKey ?? c.id ?? ''),
    [columns]
  )

  // Use localStorage persistence for column state and page size
  const {
    columnOrder,
    setColumnOrder,
    columnVisibility,
    setColumnVisibility,
    columnSizing,
    setColumnSizing,
    pageSize: storedPageSize,
    setPageSize: setStoredPageSize,
  } = useTableStateStorage(storageKey, defaultColumnOrder, controlledPageSize ?? 10)

  // Internal state
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([])
  const [rowSelection, setRowSelection] = useState<RowSelectionState>({})
  const [internalSorting, setInternalSorting] = useState<SortingState>([])

  // Use controlled or internal sorting
  const sorting = controlledSorting
    ? controlledSorting.map((s) => ({ id: s.id, desc: s.desc }))
    : internalSorting

  const handleSortingChange = useCallback(
    (updater: SortingState | ((old: SortingState) => SortingState)) => {
      const newSorting = typeof updater === 'function' ? updater(sorting) : updater
      if (onSortingChange) {
        onSortingChange(newSorting.map((s) => ({ id: s.id, desc: s.desc })))
      } else {
        setInternalSorting(newSorting)
      }
    },
    [sorting, onSortingChange]
  )

  // Add selection column if enabled
  const columnsWithSelection = useMemo(() => {
    if (!enableRowSelection) return columns

    return [
      {
        id: 'select',
        header: ({ table }: { table: ReturnType<typeof useReactTable<TData>> }) => (
          <Checkbox
            checked={
              table.getIsAllPageRowsSelected() ||
              (table.getIsSomePageRowsSelected() && 'indeterminate')
            }
            onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
            aria-label='Select all'
            className='translate-y-[2px]'
          />
        ),
        cell: ({ row }: { row: { getIsSelected: () => boolean; toggleSelected: (value: boolean) => void } }) => (
          <Checkbox
            checked={row.getIsSelected()}
            onCheckedChange={(value) => row.toggleSelected(!!value)}
            aria-label='Select row'
            className='translate-y-[2px]'
          />
        ),
        enableSorting: false,
        enableHiding: false,
        size: 40,
      },
      ...columns,
    ]
  }, [columns, enableRowSelection])

  // Ensure column order includes selection column if enabled
  const effectiveColumnOrder = useMemo(() => {
    if (enableRowSelection && !columnOrder.includes('select')) {
      return ['select', ...columnOrder]
    }
    return columnOrder
  }, [columnOrder, enableRowSelection])

  const table = useReactTable({
    data,
    columns: columnsWithSelection,
    state: {
      sorting,
      columnFilters,
      columnVisibility,
      rowSelection,
      columnOrder: effectiveColumnOrder,
      columnSizing,
      pagination: {
        pageIndex: controlledPageIndex ?? 0,
        pageSize: controlledPageSize ?? storedPageSize,
      },
    },
    pageCount: pageCount ?? -1,
    manualPagination: !!onPaginationChange,
    manualSorting: !!onSortingChange,
    enableColumnResizing,
    columnResizeMode: 'onChange',
    onSortingChange: handleSortingChange,
    onColumnFiltersChange: setColumnFilters,
    onColumnVisibilityChange: setColumnVisibility,
    onColumnSizingChange: setColumnSizing,
    onRowSelectionChange: (updater) => {
      const newSelection = typeof updater === 'function' ? updater(rowSelection) : updater
      setRowSelection(newSelection)
      if (onRowSelectionChange) {
        const selectedRows = Object.keys(newSelection)
          .filter((key) => newSelection[key])
          .map((key) => data[parseInt(key)])
        onRowSelectionChange(selectedRows)
      }
    },
    onColumnOrderChange: setColumnOrder,
    onPaginationChange: (updater) => {
      const currentPagination = {
        pageIndex: controlledPageIndex ?? 0,
        pageSize: controlledPageSize ?? storedPageSize,
      }
      const newPagination =
        typeof updater === 'function' ? updater(currentPagination) : updater

      // Persist page size to localStorage
      if (newPagination.pageSize !== currentPagination.pageSize) {
        setStoredPageSize(newPagination.pageSize)
      }

      // Notify parent component
      if (onPaginationChange) {
        onPaginationChange(newPagination.pageIndex, newPagination.pageSize)
      }
    },
    getCoreRowModel: getCoreRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFacetedRowModel: getFacetedRowModel(),
    getFacetedUniqueValues: getFacetedUniqueValues(),
  })

  // Call onTableReady when table is ready
  useEffect(() => {
    onTableReady?.(table)
  }, [table, onTableReady])

  // DnD sensors
  const sensors = useSensors(
    useSensor(MouseSensor, {}),
    useSensor(TouchSensor, {}),
    useSensor(KeyboardSensor, {})
  )

  const handleDragEnd = useCallback(
    (event: DragEndEvent) => {
      const { active, over } = event
      if (active && over && active.id !== over.id) {
        setColumnOrder((prev) => {
          const oldIndex = prev.indexOf(active.id as string)
          const newIndex = prev.indexOf(over.id as string)
          return arrayMove(prev, oldIndex, newIndex)
        })
      }
    },
    [setColumnOrder]
  )

  const headerGroups = table.getHeaderGroups()

  // Expose table to render prop for external toolbar integration
  const viewOptionsElement = renderViewOptions?.(table)

  return (
    <div className='space-y-4'>
      {showToolbar && (
        <DataTableToolbar
          table={table}
          searchValue={searchValue}
          onSearchChange={onSearchChange}
          searchPlaceholder={searchPlaceholder}
          filterableColumns={filterableColumns}
          toolbarActions={
            <>
              {toolbarActions}
              {viewOptionsElement}
            </>
          }
          columnLabels={columnLabels}
        />
      )}
      <div className='rounded-md border overflow-auto'>
        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          modifiers={[restrictToHorizontalAxis]}
          onDragEnd={handleDragEnd}
        >
          <Table className='w-full'>
            <TableHeader>
              {headerGroups.map((headerGroup) => (
                <TableRow key={headerGroup.id}>
                  <SortableContext
                    items={effectiveColumnOrder}
                    strategy={horizontalListSortingStrategy}
                  >
                    {headerGroup.headers.map((header) => (
                      <DraggableTableHeader
                        key={header.id}
                        header={header}
                        enableReordering={enableColumnReordering && header.id !== 'select'}
                        enableResizing={enableColumnResizing && header.id !== 'select'}
                      />
                    ))}
                  </SortableContext>
                </TableRow>
              ))}
            </TableHeader>
            <TableBody>
              {isLoading ? (
                <TableRow>
                  <TableCell
                    colSpan={columnsWithSelection.length}
                    className='h-24 text-center'
                  >
                    <div className='flex items-center justify-center'>
                      <Loader2 className='h-6 w-6 animate-spin text-muted-foreground' />
                    </div>
                  </TableCell>
                </TableRow>
              ) : table.getRowModel().rows?.length ? (
                table.getRowModel().rows.map((row) => (
                  <TableRow
                    key={row.id}
                    data-state={row.getIsSelected() && 'selected'}
                  >
                    {row.getVisibleCells().map((cell) => (
                      <TableCell
                        key={cell.id}
                        style={{ width: cell.column.getSize() }}
                      >
                        {flexRender(
                          cell.column.columnDef.cell,
                          cell.getContext()
                        )}
                      </TableCell>
                    ))}
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell
                    colSpan={columnsWithSelection.length}
                    className='h-24 text-center'
                  >
                    No results.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </DndContext>
      </div>
      {/* Fixed pagination for tablet and desktop */}
      {showPagination && (
        <div className='md:sticky md:bottom-0 md:bg-background md:pt-4 md:pb-2 md:border-t md:-mx-4 md:px-4'>
          <DataTablePagination table={table} />
        </div>
      )}
    </div>
  )
}
