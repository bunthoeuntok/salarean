import { useState, useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Plus, CheckCircle, XCircle, Clock } from 'lucide-react'
import { useLanguage } from '@/context/language-provider'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { Button } from '@/components/ui/button'
import { DataTable } from '@/components/data-table'
import { classService } from '@/services/class.service'
import { createClassColumns } from './columns'
import type { ClassStatus } from '@/types/class.types'

export function ClassesPage() {
  const { t } = useLanguage()
  const [pageIndex, setPageIndex] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [searchValue, setSearchValue] = useState('')
  const [sorting, setSorting] = useState<{ id: string; desc: boolean }[]>([])

  // Fetch classes data
  const { data, isLoading } = useQuery({
    queryKey: ['classes', pageIndex, pageSize, searchValue, sorting],
    queryFn: () =>
      classService.getClasses({
        page: pageIndex,
        size: pageSize,
        search: searchValue || undefined,
        sort: sorting.length > 0 ? `${sorting[0].id},${sorting[0].desc ? 'desc' : 'asc'}` : undefined,
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

  // Filter options for status
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
            <h2 className='text-2xl font-bold tracking-tight'>{t.classes.title}</h2>
            <p className='text-muted-foreground'>{t.classes.description}</p>
          </div>
          <Button>
            <Plus className='mr-2 h-4 w-4' />
            {t.classes.addClass}
          </Button>
        </div>

        <DataTable
          columns={columns}
          data={data?.content ?? []}
          isLoading={isLoading}
          pageCount={data?.totalPages ?? 0}
          pageIndex={pageIndex}
          pageSize={pageSize}
          onPaginationChange={handlePaginationChange}
          sorting={sorting}
          onSortingChange={setSorting}
          searchValue={searchValue}
          onSearchChange={setSearchValue}
          searchPlaceholder={t.classes.searchPlaceholder}
          filterableColumns={filterableColumns}
          enableRowSelection
          enableColumnReordering
          enableColumnResizing
        />
      </Main>
    </>
  )
}
