import { useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Plus, UserCheck, UserX, GraduationCap } from 'lucide-react'
import { useLanguage } from '@/context/language-provider'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { Button } from '@/components/ui/button'
import {
  DataTable,
  DataTableFilterToolbar,
  useTableUrlParams,
  getStoredPageSize,
} from '@/components/data-table'
import { studentService } from '@/services/student.service'
import { classService } from '@/services/class.service'
import { createStudentColumns } from './columns'
import type { StudentStatus, Gender } from '@/types/student.types'

export function StudentsPage() {
  const { t } = useLanguage()

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
    defaultPageSize: getStoredPageSize('students-table'),
  })

  // Fetch classes for filter dropdown
  const { data: classesData } = useQuery({
    queryKey: ['classes-for-filter'],
    queryFn: () => classService.getClasses({ size: 100 }),
    staleTime: 5 * 60 * 1000, // Cache for 5 minutes
  })

  // Fetch students data
  const { data, isLoading } = useQuery({
    queryKey: ['students', pageIndex, pageSize, searchValue, sorting, filters],
    queryFn: () =>
      studentService.getStudents({
        page: pageIndex,
        size: pageSize,
        search: searchValue || undefined,
        sort: sorting.length > 0 ? `${sorting[0].id},${sorting[0].desc ? 'desc' : 'asc'}` : undefined,
        status: filters.status?.join(',') || undefined,
        gender: filters.gender?.join(',') || undefined,
        classId: filters.classId?.[0] || undefined,
      }),
  })

  // Create columns with translations
  const columns = useMemo(
    () =>
      createStudentColumns(
        t,
        (student) => console.log('Edit student:', student),
        (student) => console.log('Delete student:', student),
        (student) => console.log('View student:', student)
      ),
    [t]
  )

  // Build class filter options from fetched data
  const classFilterOptions = useMemo(
    () =>
      classesData?.content?.map((c) => ({
        label: c.name,
        value: c.id,
        icon: GraduationCap,
      })) ?? [],
    [classesData]
  )

  // Filter options for status, gender, and class
  const filterableColumns = useMemo(
    () => [
      {
        id: 'status',
        title: t.students.columns.status,
        options: [
          { label: t.students.status.ACTIVE, value: 'ACTIVE' as StudentStatus, icon: UserCheck },
          { label: t.students.status.INACTIVE, value: 'INACTIVE' as StudentStatus, icon: UserX }
        ],
      },
      {
        id: 'gender',
        title: t.students.columns.gender,
        options: [
          { label: t.students.gender.M, value: 'M' as Gender },
          { label: t.students.gender.F, value: 'F' as Gender },
        ],
      },
      {
        id: 'classId',
        title: t.students.columns.class,
        options: classFilterOptions,
      },
    ],
    [t, classFilterOptions]
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
            <h2 className='text-2xl font-bold tracking-tight'>{t.students.title}</h2>
            <p className='text-muted-foreground'>{t.students.description}</p>
          </div>
          <Button>
            <Plus className='mr-2 h-4 w-4' />
            {t.students.addStudent}
          </Button>
        </div>

        {/* Filter toolbar with submit button */}
        <DataTableFilterToolbar
          initialSearch={searchValue}
          initialFilters={filters}
          filterableColumns={filterableColumns}
          onSubmit={submitFilters}
          onReset={resetFilters}
          searchPlaceholder={t.students.searchPlaceholder}
          submitLabel={t.filter.submit}
          resetLabel={t.filter.reset}
        />

        <div className='mt-4'>
          <DataTable
            columns={columns}
            data={data?.content ?? []}
            isLoading={isLoading}
            storageKey='students-table'
            pageCount={data?.totalPages ?? 0}
            pageIndex={pageIndex}
            pageSize={pageSize}
            onPaginationChange={handlePaginationChange}
            sorting={sorting}
            onSortingChange={setSorting}
            enableRowSelection
            enableColumnReordering
            enableColumnResizing
          />
        </div>
      </Main>
    </>
  )
}
