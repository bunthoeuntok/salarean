import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { Plus, CheckCircle, XCircle, Clock } from 'lucide-react'
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
import { AddClassModal } from './components/add-class-modal'
import { EditClassModal } from './components/edit-class-modal'
import { useClassFiltering } from '@/hooks/use-class-filtering'
import { useClasses } from '@/hooks/use-classes'
import { useAvailableLevels } from '@/hooks/use-available-levels'
import { useAcademicYearStore } from '@/store/academic-year-store'
import type { Class, ClassStatus, ClassLevel, ClassType } from '@/types/class.types'

export function ClassesPage() {
  const { t } = useLanguage()
  const navigate = useNavigate()
  const { selectedAcademicYear } = useAcademicYearStore()
  const [tableInstance, setTableInstance] = useState<Table<Class> | null>(null)
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)
  const [isEditModalOpen, setIsEditModalOpen] = useState(false)
  const [editClassId, setEditClassId] = useState<string | null>(null)

  // Fetch and populate global class store for batch transfer eligibility
  // This automatically filters by selected academic year
  useClasses()

  const handleEditClass = (classItem: Class) => {
    setEditClassId(classItem.id)
    setIsEditModalOpen(true)
  }

  const handleEditModalClose = (open: boolean) => {
    setIsEditModalOpen(open)
    if (!open) {
      setEditClassId(null)
    }
  }

  // Column labels for view options
  const columnLabels = useMemo(
    () => ({
      name: t.classes.columns.name,
      grade: t.classes.columns.grade,
      academicYear: t.classes.columns.academicYear,
      level: t.classes.columns.level,
      type: t.classes.columns.type,
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

  // Get available levels based on teacher's school type
  const { availableLevels } = useAvailableLevels()

  // Use class filtering hook for level → grade filtering
  const { filteredGradeOptions, handleLevelChange } = useClassFiltering({
    initialLevel: filters.level?.[0] as ClassLevel | undefined,
    availableLevels,
  })

  // Fetch classes data (automatically filtered by selected academic year)
  const { data, isLoading } = useQuery({
    queryKey: ['classes', pageIndex, pageSize, searchValue, sorting, filters, selectedAcademicYear],
    queryFn: () =>
      classService.getClasses({
        page: pageIndex,
        size: pageSize,
        search: searchValue || undefined,
        sort: sorting.length > 0 ? `${sorting[0].id},${sorting[0].desc ? 'desc' : 'asc'}` : undefined,
        status: filters.status?.join(',') || undefined,
        grade: filters.grade?.[0] ? Number(filters.grade[0]) : undefined,
        level: filters.level?.[0] as ClassLevel | undefined,
        type: filters.type?.[0] as ClassType | undefined,
        academicYear: selectedAcademicYear,
      }),
  })

  const handleViewClass = (classItem: Class) => {
    navigate({ to: '/classes/$id', params: { id: classItem.id } })
  }

  // Create columns with translations
  const columns = useMemo(
    () =>
      createClassColumns(
        t,
        handleEditClass,
        (classItem) => console.log('Delete class:', classItem),
        handleViewClass,
        (classItem) => console.log('Manage students:', classItem)
      ),
    [t, navigate]
  )

  // Filter options for status, grade, level, and type
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
        id: 'level',
        title: t.classes.columns.level,
        options: [
          { label: t.classes.level.PRIMARY, value: 'PRIMARY' as ClassLevel },
          { label: t.classes.level.SECONDARY, value: 'SECONDARY' as ClassLevel },
          { label: t.classes.level.HIGH_SCHOOL, value: 'HIGH_SCHOOL' as ClassLevel },
        ].filter((option) => availableLevels.includes(option.value)),
        singleSelect: true,
        onFilterChange: handleLevelChange,
      },
      {
        id: 'grade',
        title: t.classes.columns.grade,
        options: filteredGradeOptions,
      },
      {
        id: 'type',
        title: t.classes.columns.type,
        options: [
          { label: t.classes.type.NORMAL, value: 'NORMAL' as ClassType },
          { label: t.classes.type.SCIENCE, value: 'SCIENCE' as ClassType },
          { label: t.classes.type.SOCIAL_SCIENCE, value: 'SOCIAL_SCIENCE' as ClassType },
        ],
        singleSelect: true,
      },
    ],
    [t, filteredGradeOptions, handleLevelChange, availableLevels]
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
          <Button onClick={() => setIsAddModalOpen(true)}>
            <Plus className='mr-2 h-4 w-4' />
            {t.classes.addClass}
          </Button>
        </div>

        <AddClassModal
          open={isAddModalOpen}
          onOpenChange={setIsAddModalOpen}
        />

        <EditClassModal
          open={isEditModalOpen}
          onOpenChange={handleEditModalClose}
          classId={editClassId}
        />

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
