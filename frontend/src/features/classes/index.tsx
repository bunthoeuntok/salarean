import { useMemo, useState, useEffect, useCallback } from 'react'
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
import { AddClassModal } from './components/add-class-modal'
import { EditClassModal } from './components/edit-class-modal'
import type { Class, ClassStatus, ClassLevel, ClassType } from '@/types/class.types'

// Grade options (1-12)
const GRADE_OPTIONS = Array.from({ length: 12 }, (_, i) => ({
  label: `Grade ${i + 1}`,
  value: String(i + 1),
  icon: GraduationCap,
}))

export function ClassesPage() {
  const { t } = useLanguage()
  const [tableInstance, setTableInstance] = useState<Table<Class> | null>(null)
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)
  const [isEditModalOpen, setIsEditModalOpen] = useState(false)
  const [editClassId, setEditClassId] = useState<string | null>(null)

  // Track current level selection for dynamic grade filtering
  const [currentLevelSelection, setCurrentLevelSelection] = useState<ClassLevel | undefined>(undefined)

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
        grade: filters.grade?.[0] ? Number(filters.grade[0]) : undefined,
        level: filters.level?.[0] as ClassLevel | undefined,
        type: filters.type?.[0] as ClassType | undefined,
      }),
  })

  // Create columns with translations
  const columns = useMemo(
    () =>
      createClassColumns(
        t,
        handleEditClass,
        (classItem) => console.log('Delete class:', classItem),
        (classItem) => console.log('View class:', classItem),
        (classItem) => console.log('Manage students:', classItem)
      ),
    [t]
  )

  // Sync currentLevelSelection with URL params on mount/change
  useEffect(() => {
    setCurrentLevelSelection(filters.level?.[0] as ClassLevel | undefined)
  }, [filters.level])

  // Filter grade options based on current level selection
  const filteredGradeOptions = useMemo(() => {
    if (!currentLevelSelection) {
      return GRADE_OPTIONS
    }

    // PRIMARY: grades 1-6, SECONDARY: grades 7-9, HIGH_SCHOOL: grades 10-12
    const gradeRanges: Record<ClassLevel, { min: number; max: number }> = {
      PRIMARY: { min: 1, max: 6 },
      SECONDARY: { min: 7, max: 9 },
      HIGH_SCHOOL: { min: 10, max: 12 },
    }

    const range = gradeRanges[currentLevelSelection]
    return GRADE_OPTIONS.filter((option) => {
      const grade = Number(option.value)
      return grade >= range.min && grade <= range.max
    })
  }, [currentLevelSelection])

  // Custom handler for level filter to update grade options in real-time
  const handleLevelChange = useCallback((values: string[]) => {
    const newLevel = values.length > 0 ? (values[0] as ClassLevel) : undefined
    setCurrentLevelSelection(newLevel)
  }, [])

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
        ],
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
    [t, filteredGradeOptions, handleLevelChange]
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
