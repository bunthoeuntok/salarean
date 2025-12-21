import { useMemo, useState, useCallback } from 'react'
import { useQuery } from '@tanstack/react-query'
import type { Table } from '@tanstack/react-table'
import { GraduationCap } from 'lucide-react'
import { useLanguage } from '@/context/language-provider'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import {
  DataTable,
  DataTableFilterToolbar,
  DataTableViewOptions,
  useTableUrlParams,
  getStoredPageSize,
} from '@/components/data-table'
import { subjectService } from '@/services/subject.service'
import { createSubjectColumns } from './columns'
import { EditSubjectModal } from './components/edit-subject-modal'
import { useAvailableLevels } from '@/hooks/use-available-levels'
import { GRADE_RANGES, getFilteredGradeOptions } from '@/lib/utils/class-filters'
import type { Subject } from '@/types/subject.types'

export function SubjectsPage() {
  const { t } = useLanguage()
  const [tableInstance, setTableInstance] = useState<Table<Subject> | null>(null)
  const [isEditModalOpen, setIsEditModalOpen] = useState(false)
  const [editSubjectId, setEditSubjectId] = useState<string | null>(null)

  // Get available levels based on teacher's school type
  const { availableLevels } = useAvailableLevels()

  // Grade label for display
  const gradeLabel = t.common.grade

  // Column labels for view options
  const columnLabels = useMemo(
    () => ({
      displayOrder: '#',
      code: t.subjects.columns.code,
      name: t.subjects.columns.name,
      nameKhmer: t.subjects.columns.nameKhmer,
      gradeLevels: t.subjects.columns.gradeLevels,
      isCore: t.subjects.columns.isCore,
      actions: t.subjects.columns.actions,
    }),
    [t]
  )

  // Edit handlers
  const handleEditSubject = useCallback((subject: Subject) => {
    setEditSubjectId(subject.id)
    setIsEditModalOpen(true)
  }, [])

  const handleEditModalClose = useCallback((open: boolean) => {
    setIsEditModalOpen(open)
    if (!open) setEditSubjectId(null)
  }, [])

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
    defaultPageSize: getStoredPageSize('subjects-table'),
  })

  // Get the selected grade filter (only one can be selected)
  const selectedGrade = filters.grade?.[0] ? Number(filters.grade[0]) : undefined

  // Fetch all subjects
  const { data: allSubjects = [], isLoading } = useQuery({
    queryKey: ['subjects'],
    queryFn: () => subjectService.getSubjects(),
  })

  // Filter subjects by selected grade level (client-side filtering since data is small)
  const filteredSubjects = useMemo(() => {
    if (!selectedGrade) {
      // If no grade selected, show subjects that apply to any available grade
      const availableGrades = availableLevels.flatMap((level) => {
        const range = GRADE_RANGES[level]
        return Array.from({ length: range.max - range.min + 1 }, (_, i) => range.min + i)
      })
      return allSubjects.filter(
        (subject) =>
          subject.gradeLevels?.some((g) => availableGrades.includes(g)) ?? false
      )
    }
    return allSubjects.filter(
      (subject) => subject.gradeLevels?.includes(selectedGrade) ?? false
    )
  }, [allSubjects, selectedGrade, availableLevels])

  // Further filter by search
  const searchedSubjects = useMemo(() => {
    if (!searchValue) return filteredSubjects
    const search = searchValue.toLowerCase()
    return filteredSubjects.filter(
      (subject) =>
        subject.name.toLowerCase().includes(search) ||
        subject.nameKhmer.toLowerCase().includes(search) ||
        subject.code.toLowerCase().includes(search)
    )
  }, [filteredSubjects, searchValue])

  // Client-side pagination
  const paginatedSubjects = useMemo(() => {
    const start = pageIndex * pageSize
    return searchedSubjects.slice(start, start + pageSize)
  }, [searchedSubjects, pageIndex, pageSize])

  const totalPages = Math.ceil(searchedSubjects.length / pageSize)

  // Create columns with translations
  const columns = useMemo(
    () => createSubjectColumns({ t, gradeLabel, onEdit: handleEditSubject }),
    [t, gradeLabel, handleEditSubject]
  )

  // Grade filter options based on school type
  const gradeOptions = useMemo(
    () => getFilteredGradeOptions(gradeLabel, undefined, availableLevels),
    [gradeLabel, availableLevels]
  )

  // Filter options
  const filterableColumns = useMemo(
    () => [
      {
        id: 'grade',
        title: t.subjects.columns.gradeLevels,
        options: gradeOptions.map((opt) => ({
          label: opt.label,
          value: opt.value,
          icon: GraduationCap,
        })),
        singleSelect: true,
      },
    ],
    [t, gradeOptions]
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
        <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">{t.subjects.title}</h2>
            <p className="text-muted-foreground">{t.subjects.description}</p>
          </div>
        </div>

        {/* Filter toolbar */}
        <DataTableFilterToolbar
          initialSearch={searchValue}
          initialFilters={filters}
          filterableColumns={filterableColumns}
          onSubmit={submitFilters}
          onReset={resetFilters}
          searchPlaceholder={t.subjects.searchPlaceholder}
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

        <div className="mt-4">
          <DataTable
            columns={columns}
            data={paginatedSubjects}
            isLoading={isLoading}
            storageKey="subjects-table"
            pageCount={totalPages}
            pageIndex={pageIndex}
            pageSize={pageSize}
            onPaginationChange={handlePaginationChange}
            sorting={sorting}
            onSortingChange={setSorting}
            enableColumnReordering
            enableColumnResizing
            showToolbar={false}
            onTableReady={setTableInstance}
          />
        </div>
      </Main>

      {/* Edit Subject Modal */}
      <EditSubjectModal
        open={isEditModalOpen}
        onOpenChange={handleEditModalClose}
        subjectId={editSubjectId}
      />
    </>
  )
}
