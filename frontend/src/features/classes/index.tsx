import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Plus, CheckCircle, XCircle, Clock, GraduationCap } from 'lucide-react'
import type { Table } from '@tanstack/react-table'
import { useLanguage } from '@/context/language-provider'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { Button } from '@/components/ui/button'
import {
  DataTable,
  DataTableFilterToolbar,
  DataTableViewOptions,
  useTableUrlParams,
  getStoredPageSize,
} from '@/components/data-table'
import { classService } from '@/services/class.service'
import { createClassColumns } from './columns'
import type { Class, ClassStatus } from '@/types/class.types'

// Grade options (1-12)
const GRADE_OPTIONS = Array.from({ length: 12 }, (_, i) => ({
  label: `Grade ${i + 1}`,
  value: String(i + 1),
  icon: GraduationCap,
}))

export function ClassesPage() {
  const { t } = useLanguage()
  const [tableInstance, setTableInstance] = useState<Table<Class> | null>(null)

  // Column labels for view options
  const columnLabels = useMemo(
    () => ({
      name: t.classes.columns.name,
      grade: t.classes.columns.grade,
      academicYear: t.classes.columns.academicYear,
      teacherName: t.classes.columns.teacher,
      enrollment: t.classes.columns.enrollment,
      status: t.classes.columns.status,
      actions: t.classes.columns.actions,
    }),
    [t]
  )

  // Use URL params for table state
  const {
    pageIndex,
    pageSize,
    searchValue,
    sorting,
    filters,
    setPage,
    setPageSize,
    setSorting,
    submitFilters,
    resetFilters,
  } = useTableUrlParams({
    defaultPageSize: getStoredPageSize('classes-table'),
  })

  // Fetch classes data
  const { data, isLoading } = useQuery({
    queryKey: ['classes', pageIndex, pageSize, searchValue, sorting, filters],
    queryFn: () =>
      classService.getClasses({
        page: pageIndex,
        size: pageSize,
        search: searchValue || undefined,
        sort: sorting.length > 0 ? `${sorting[0].id},${sorting[0].desc ? 'desc' : 'asc'}` : undefined,
        status: filters.status?.join(',') || undefined,
        grade: filters.grade?.[0] || undefined,
      }),
  })

  // Create columns with translations
  const columns = useMemo(
    () =>
      createClassColumns(
        t,
        (classItem) => console.log('Edit class:', classItem),
        (classItem) => console.log('Delete class:', classItem),
        (classItem) => console.log('View class:', classItem),
        (classItem) => console.log('Manage students:', classItem)
      ),
    [t]
  )

  // Filter options for status and grade
  const filterableColumns = useMemo(
    () => [
      {
        id: 'status',
        title: t.classes.columns.status,
        options: [
          { label: t.classes.status.ACTIVE, value: 'ACTIVE' as ClassStatus, icon: CheckCircle },
          { label: t.classes.status.INACTIVE, value: 'INACTIVE' as ClassStatus, icon: XCircle },
          { label: t.classes.status.COMPLETED, value: 'COMPLETED' as ClassStatus, icon: Clock },
        ],
      },
      {
        id: 'grade',
        title: t.classes.columns.grade,
        options: GRADE_OPTIONS,
      },
    ],
    [t]
  )

  const handlePaginationChange = (newPageIndex: number, newPageSize: number) => {
    if (newPageSize !== pageSize) {
      setPageSize(newPageSize)
    } else {
      setPage(newPageIndex)
    }
  }

  return (
    <>
      <Header fixed />
      <Main>
        <div className='mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
          <div>
            <h2 className='text-2xl font-bold tracking-tight'>{t.classes.title}</h2>
            <p className='text-muted-foreground'>{t.classes.description}</p>
          </div>
          <Button>
            <Plus className='mr-2 h-4 w-4' />
            {t.classes.addClass}
          </Button>
        </div>

        {/* Filter toolbar with submit button */}
        <DataTableFilterToolbar
          initialSearch={searchValue}
          initialFilters={filters}
          filterableColumns={filterableColumns}
          onSubmit={submitFilters}
          onReset={resetFilters}
          searchPlaceholder={t.classes.searchPlaceholder}
          submitLabel={t.filter.submit}
          resetLabel={t.filter.reset}
          toolbarActions={
            tableInstance && (
              <DataTableViewOptions
                table={tableInstance}
                columnLabels={columnLabels}
              />
            )
          }
        />

        <div className='mt-4'>
          <DataTable
            columns={columns}
            data={data?.content ?? []}
            isLoading={isLoading}
            storageKey='classes-table'
            pageCount={data?.totalPages ?? 0}
            pageIndex={pageIndex}
            pageSize={pageSize}
            onPaginationChange={handlePaginationChange}
            sorting={sorting}
            onSortingChange={setSorting}
            enableRowSelection
            enableColumnReordering
            enableColumnResizing
            showToolbar={false}
            onTableReady={setTableInstance}
          />
        </div>
      </Main>
    </>
  )
}
