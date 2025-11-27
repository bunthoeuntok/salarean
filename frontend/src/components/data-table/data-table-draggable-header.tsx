import { useSortable } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { GripVertical } from 'lucide-react'
import { flexRender, type Header } from '@tanstack/react-table'
import { cn } from '@/lib/utils'
import { TableHead } from '@/components/ui/table'

interface DraggableTableHeaderProps<TData> {
  header: Header<TData, unknown>
  enableReordering?: boolean
  enableResizing?: boolean
}

export function DraggableTableHeader<TData>({
  header,
  enableReordering = false,
  enableResizing = false,
}: DraggableTableHeaderProps<TData>) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: header.id,
    disabled: !enableReordering,
  })

  const style: React.CSSProperties = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.8 : 1,
    position: 'relative',
    width: header.getSize(),
    zIndex: isDragging ? 1 : 0,
  }

  return (
    <TableHead
      ref={setNodeRef}
      style={style}
      className={cn(
        'relative whitespace-nowrap',
        isDragging && 'bg-muted'
      )}
      colSpan={header.colSpan}
    >
      <div className='flex items-center'>
        {enableReordering && (
          <button
            {...attributes}
            {...listeners}
            className='mr-2 cursor-grab touch-none text-muted-foreground hover:text-foreground'
          >
            <GripVertical className='h-4 w-4' />
          </button>
        )}
        {header.isPlaceholder
          ? null
          : flexRender(header.column.columnDef.header, header.getContext())}
      </div>
      {enableResizing && (
        <div
          onMouseDown={header.getResizeHandler()}
          onTouchStart={header.getResizeHandler()}
          className={cn(
            'absolute right-0 top-0 h-full w-1 cursor-col-resize select-none touch-none bg-transparent hover:bg-primary/50',
            header.column.getIsResizing() && 'bg-primary'
          )}
        />
      )}
    </TableHead>
  )
}
