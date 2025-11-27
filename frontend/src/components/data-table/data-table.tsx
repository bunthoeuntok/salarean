import { useState, useCallback, useMemo } from 'react'
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
  type VisibilityState,
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
import type { DataTableProps } from './types'

export function DataTable<TData, TValue>({
  columns,
  data,
  isLoading = false,
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
  // Toolbar actions
  toolbarActions,
}: DataTableProps<TData, TValue>) {
  // Internal state
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([])
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({})
  const [rowSelection, setRowSelection] = useState<RowSelectionState>({})
  const [internalSorting, setInternalSorting] = useState<SortingState>([])
  const [columnOrder, setColumnOrder] = useState<string[]>(() =>
    columns.map((c) => (c as { accessorKey?: string }).accessorKey ?? c.id ?? '')
  )

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

  const table = useReactTable({
    data,
    columns: columnsWithSelection,
    state: {
      sorting,
      columnFilters,
      columnVisibility,
      rowSelection,
      columnOrder,
      pagination: {
        pageIndex: controlledPageIndex ?? 0,
        pageSize: controlledPageSize ?? 10,
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
      if (onPaginationChange) {
        const currentPagination = {
          pageIndex: controlledPageIndex ?? 0,
          pageSize: controlledPageSize ?? 10,
        }
        const newPagination =
          typeof updater === 'function' ? updater(currentPagination) : updater
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
    []
  )

  const headerGroups = table.getHeaderGroups()

  return (
    <div className='space-y-4'>
      <DataTableToolbar
        table={table}
        searchValue={searchValue}
        onSearchChange={onSearchChange}
        searchPlaceholder={searchPlaceholder}
        filterableColumns={filterableColumns}
        toolbarActions={toolbarActions}
      />
      <div className='rounded-md border'>
        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          modifiers={[restrictToHorizontalAxis]}
          onDragEnd={handleDragEnd}
        >
          <Table style={{ width: table.getCenterTotalSize() }}>
            <TableHeader>
              {headerGroups.map((headerGroup) => (
                <TableRow key={headerGroup.id}>
                  <SortableContext
                    items={columnOrder}
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
      <DataTablePagination table={table} />
    </div>
  )
}
