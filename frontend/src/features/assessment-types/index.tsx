import { useMemo, useState, useCallback } from 'react'
import { useQuery } from '@tanstack/react-query'
import type { Table } from '@tanstack/react-table'
import { ClipboardList, Plus } from 'lucide-react'
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
import { assessmentTypeService } from '@/services/assessment-type.service'
import { createAssessmentTypeColumns } from './columns'
import { EditAssessmentTypeModal } from './components/edit-assessment-type-modal'
import { AddAssessmentTypeModal } from './components/add-assessment-type-modal'
import type { AssessmentType } from '@/types/assessment-type.types'

export function AssessmentTypesPage() {
  const { t } = useLanguage()
  const [tableInstance, setTableInstance] = useState<Table<AssessmentType> | null>(null)
  const [isEditModalOpen, setIsEditModalOpen] = useState(false)
  const [editAssessmentTypeId, setEditAssessmentTypeId] = useState<string | null>(null)
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)

  const columnLabels = useMemo(
    () => ({
      displayOrder: '#',
      code: t.assessmentTypes.columns.code,
      name: t.assessmentTypes.columns.name,
      nameKhmer: t.assessmentTypes.columns.nameKhmer,
      category: t.assessmentTypes.columns.category,
      defaultWeight: t.assessmentTypes.columns.defaultWeight,
      maxScore: t.assessmentTypes.columns.maxScore,
      actions: t.assessmentTypes.columns.actions,
    }),
    [t]
  )

  const handleEditAssessmentType = useCallback((assessmentType: AssessmentType) => {
    setEditAssessmentTypeId(assessmentType.id)
    setIsEditModalOpen(true)
  }, [])

  const handleEditModalClose = useCallback((open: boolean) => {
    setIsEditModalOpen(open)
    if (!open) setEditAssessmentTypeId(null)
  }, [])

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
    defaultPageSize: getStoredPageSize('assessment-types-table'),
  })

  const selectedCategory = filters.category?.[0] || undefined

  const { data: allAssessmentTypes = [], isLoading } = useQuery({
    queryKey: ['assessmentTypes'],
    queryFn: () => assessmentTypeService.getAssessmentTypes(),
  })

  const filteredAssessmentTypes = useMemo(() => {
    if (!selectedCategory) return allAssessmentTypes
    return allAssessmentTypes.filter(
      (type) => type.category === selectedCategory
    )
  }, [allAssessmentTypes, selectedCategory])

  const searchedAssessmentTypes = useMemo(() => {
    if (!searchValue) return filteredAssessmentTypes
    const search = searchValue.toLowerCase()
    return filteredAssessmentTypes.filter(
      (type) =>
        type.name.toLowerCase().includes(search) ||
        type.nameKhmer.toLowerCase().includes(search) ||
        type.code.toLowerCase().includes(search)
    )
  }, [filteredAssessmentTypes, searchValue])

  const paginatedAssessmentTypes = useMemo(() => {
    const start = pageIndex * pageSize
    return searchedAssessmentTypes.slice(start, start + pageSize)
  }, [searchedAssessmentTypes, pageIndex, pageSize])

  const totalPages = Math.ceil(searchedAssessmentTypes.length / pageSize)

  const columns = useMemo(
    () => createAssessmentTypeColumns({ t, onEdit: handleEditAssessmentType }),
    [t, handleEditAssessmentType]
  )

  const filterableColumns = useMemo(
    () => [
      {
        id: 'category',
        title: t.assessmentTypes.columns.category,
        options: [
          {
            label: t.assessmentTypes.categories.MONTHLY_EXAM,
            value: 'MONTHLY_EXAM',
            icon: ClipboardList,
          },
          {
            label: t.assessmentTypes.categories.SEMESTER_EXAM,
            value: 'SEMESTER_EXAM',
            icon: ClipboardList,
          },
        ],
        singleSelect: true,
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
        <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">{t.assessmentTypes.title}</h2>
            <p className="text-muted-foreground">{t.assessmentTypes.description}</p>
          </div>
          <Button onClick={() => setIsAddModalOpen(true)}>
            <Plus className="mr-2 h-4 w-4" />
            {t.assessmentTypes.addAssessmentType}
          </Button>
        </div>

        <DataTableFilterToolbar
          initialSearch={searchValue}
          initialFilters={filters}
          filterableColumns={filterableColumns}
          onSubmit={submitFilters}
          onReset={resetFilters}
          searchPlaceholder={t.assessmentTypes.searchPlaceholder}
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
            data={paginatedAssessmentTypes}
            isLoading={isLoading}
            storageKey="assessment-types-table"
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

      <EditAssessmentTypeModal
        open={isEditModalOpen}
        onOpenChange={handleEditModalClose}
        assessmentTypeId={editAssessmentTypeId}
      />

      <AddAssessmentTypeModal
        open={isAddModalOpen}
        onOpenChange={setIsAddModalOpen}
      />
    </>
  )
}
