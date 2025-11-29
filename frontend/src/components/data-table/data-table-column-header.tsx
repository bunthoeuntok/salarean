import { ArrowDown, ArrowUp, ArrowUpDown } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import type { DataTableColumnHeaderProps } from './types'

export function DataTableColumnHeader<TData, TValue>({
  column,
  title,
  className,
}: DataTableColumnHeaderProps<TData, TValue>) {
  if (!column.getCanSort()) {
    return <div className={cn(className)}>{title}</div>
  }

  const handleClick = () => {
    // Cycle: none → asc → desc → none
    const currentSort = column.getIsSorted()
    if (currentSort === false) {
      column.toggleSorting(false) // Set to asc
    } else if (currentSort === 'asc') {
      column.toggleSorting(true) // Set to desc
    } else {
      column.clearSorting() // Clear sorting
    }
  }

  return (
    <div className={cn('flex items-center space-x-2 w-full', className)}>
      <Button
        variant='ghost'
        size='sm'
        className='-ml-3 h-8 flex w-full items-center justify-between rounded-md px-2 py-0 text-sm font-medium focus:outline-none'
        onClick={handleClick}
      >
        <span>{title}</span>
        {column.getIsSorted() === 'desc' ? (
          <ArrowDown className='ml-2 h-4 w-4' />
        ) : column.getIsSorted() === 'asc' ? (
          <ArrowUp className='ml-2 h-4 w-4' />
        ) : (
          <ArrowUpDown className='ml-2 h-4 w-4 text-muted-foreground' />
        )}
      </Button>
    </div>
  )
}
