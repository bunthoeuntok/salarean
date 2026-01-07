import { useDraggable } from '@dnd-kit/core'
import { CSS } from '@dnd-kit/utilities'
import { GripVertical } from 'lucide-react'
import { cn } from '@/lib/utils'

interface DraggableExamItemProps {
  id: string
  assessmentCode: string
  title?: string
  isInDropZone?: boolean
}

export function DraggableExamItem({
  id,
  assessmentCode,
  title,
  isInDropZone = false,
}: DraggableExamItemProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    isDragging,
  } = useDraggable({
    id,
    data: { assessmentCode, title },
  })

  const style: React.CSSProperties = {
    transform: CSS.Translate.toString(transform),
    opacity: isDragging ? 0.5 : 1,
  }

  // Display title from database, fallback to assessmentCode
  const displayName = title || assessmentCode

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={cn(
        'flex items-center gap-2 rounded-lg border bg-card p-3 cursor-grab active:cursor-grabbing',
        isDragging && 'shadow-lg ring-2 ring-primary',
        isInDropZone && 'bg-muted/50'
      )}
      {...attributes}
      {...listeners}
    >
      <GripVertical className="h-4 w-4 text-muted-foreground shrink-0" />
      <span className="text-sm font-medium truncate">{displayName}</span>
    </div>
  )
}
