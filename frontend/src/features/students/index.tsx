import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Plus, UserCheck, UserX } from 'lucide-react'
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
import { studentService } from '@/services/student.service'
import { classService } from '@/services/class.service'
import { createStudentColumns } from './columns'
import { AddStudentModal } from './components/add-student-modal'
import { EditStudentModal } from './components/edit-student-modal'
import { EnrollStudentModal } from './components/enroll-student-modal'
import { TransferStudentModal } from './components/transfer-student-modal'
import { ViewStudentModal } from './components/view-student-modal'
import type { Student, StudentStatus, Gender } from '@/types/student.types'

export function StudentsPage() {
  const { t } = useLanguage()
  const [tableInstance, setTableInstance] = useState<Table<Student> | null>(null)
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)
  const [isEditModalOpen, setIsEditModalOpen] = useState(false)
  const [editStudentId, setEditStudentId] = useState<string | null>(null)
  const [isEnrollModalOpen, setIsEnrollModalOpen] = useState(false)
  const [enrollStudent, setEnrollStudent] = useState<Student | null>(null)
  const [isTransferModalOpen, setIsTransferModalOpen] = useState(false)
  const [transferStudent, setTransferStudent] = useState<Student | null>(null)
  const [isViewModalOpen, setIsViewModalOpen] = useState(false)
  const [viewStudent, setViewStudent] = useState<Student | null>(null)

  const handleEditStudent = (student: Student) => {
    setEditStudentId(student.id)
    setIsEditModalOpen(true)
  }

  const handleEditModalClose = (open: boolean) => {
    setIsEditModalOpen(open)
    if (!open) {
      setEditStudentId(null)
    }
  }

  const handleEnrollStudent = (student: Student) => {
    setEnrollStudent(student)
    setIsEnrollModalOpen(true)
  }

  const handleEnrollModalClose = (open: boolean) => {
    setIsEnrollModalOpen(open)
    if (!open) {
      setEnrollStudent(null)
    }
  }

  const handleTransferStudent = (student: Student) => {
    setTransferStudent(student)
    setIsTransferModalOpen(true)
  }

  const handleTransferModalClose = (open: boolean) => {
    setIsTransferModalOpen(open)
    if (!open) {
      setTransferStudent(null)
    }
  }

  const handleViewStudent = (student: Student) => {
    setViewStudent(student)
    setIsViewModalOpen(true)
  }

  const handleViewModalClose = (open: boolean) => {
    setIsViewModalOpen(open)
    if (!open) {
      setViewStudent(null)
    }
  }

  // Column labels for view options
  const columnLabels = useMemo(
    () => ({
      code: t.students.columns.code,
      name: t.students.columns.name,
      gender: t.students.columns.gender,
      dateOfBirth: t.students.columns.dateOfBirth,
      contact: t.students.columns.contact,
      className: t.students.columns.class,
      status: t.students.columns.status,
      actions: t.students.columns.actions,
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
        handleEditStudent,
        (student) => console.log('Delete student:', student),
        handleViewStudent,
        handleEnrollStudent,
        handleTransferStudent
      ),
    [t]
  )

  // Build class filter options from fetched data
  const classFilterOptions = useMemo(
    () => [
      { label: t.students.noClass, value: 'NONE' },
      ...(classesData?.content?.map((c) => ({
        label: `Grade ${c.grade}${c.section ? ` - ${c.section}` : ''}`,
        value: c.id,
      })) ?? []),
    ],
    [classesData, t]
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
        singleSelect: true,
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
          <Button onClick={() => setIsAddModalOpen(true)}>
            <Plus className='mr-2 h-4 w-4' />
            {t.students.addStudent}
          </Button>
        </div>

        <AddStudentModal
          open={isAddModalOpen}
          onOpenChange={setIsAddModalOpen}
        />

        <EditStudentModal
          open={isEditModalOpen}
          onOpenChange={handleEditModalClose}
          studentId={editStudentId}
        />

        <EnrollStudentModal
          open={isEnrollModalOpen}
          onOpenChange={handleEnrollModalClose}
          student={enrollStudent}
        />

        <TransferStudentModal
          open={isTransferModalOpen}
          onOpenChange={handleTransferModalClose}
          student={transferStudent}
        />

        <ViewStudentModal
          open={isViewModalOpen}
          onOpenChange={handleViewModalClose}
          student={viewStudent}
        />

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
            showToolbar={false}
            onTableReady={setTableInstance}
          />
        </div>
      </Main>
    </>
  )
}
