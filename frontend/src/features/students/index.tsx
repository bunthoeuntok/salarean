import { useState, useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Plus, UserCheck, UserX, GraduationCap, ArrowRightLeft } from 'lucide-react'
import { useLanguage } from '@/context/language-provider'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { Button } from '@/components/ui/button'
import { DataTable, getStoredPageSize } from '@/components/data-table'
import { studentService } from '@/services/student.service'
import { createStudentColumns } from './columns'
import type { StudentStatus, Gender } from '@/types/student.types'

export function StudentsPage() {
  const { t } = useLanguage()
  const [pageIndex, setPageIndex] = useState(0)
  const [pageSize, setPageSize] = useState(() => getStoredPageSize('students-table'))
  const [searchValue, setSearchValue] = useState('')
  const [sorting, setSorting] = useState<{ id: string; desc: boolean }[]>([])

  // Fetch students data
  const { data, isLoading } = useQuery({
    queryKey: ['students', pageIndex, pageSize, searchValue, sorting],
    queryFn: () =>
      studentService.getStudents({
        page: pageIndex,
        size: pageSize,
        search: searchValue || undefined,
        sort: sorting.length > 0 ? `${sorting[0].id},${sorting[0].desc ? 'desc' : 'asc'}` : undefined,
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

  // Filter options for status and gender
  const filterableColumns = useMemo(
    () => [
      {
        id: 'status',
        title: t.students.columns.status,
        options: [
          { label: t.students.status.ACTIVE, value: 'ACTIVE' as StudentStatus, icon: UserCheck },
          { label: t.students.status.INACTIVE, value: 'INACTIVE' as StudentStatus, icon: UserX },
          { label: t.students.status.GRADUATED, value: 'GRADUATED' as StudentStatus, icon: GraduationCap },
          { label: t.students.status.TRANSFERRED, value: 'TRANSFERRED' as StudentStatus, icon: ArrowRightLeft },
        ],
      },
      {
        id: 'gender',
        title: t.students.columns.gender,
        options: [
          { label: t.students.gender.MALE, value: 'MALE' as Gender },
          { label: t.students.gender.FEMALE, value: 'FEMALE' as Gender },
        ],
      },
    ],
    [t]
  )

  const handlePaginationChange = (newPageIndex: number, newPageSize: number) => {
    setPageIndex(newPageIndex)
    setPageSize(newPageSize)
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
          searchValue={searchValue}
          onSearchChange={setSearchValue}
          searchPlaceholder={t.students.searchPlaceholder}
          filterableColumns={filterableColumns}
          enableRowSelection
          enableColumnReordering
          enableColumnResizing
        />
      </Main>
    </>
  )
}
