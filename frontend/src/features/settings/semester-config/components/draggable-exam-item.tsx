import { useDraggable } from '@dnd-kit/core'
import { CSS } from '@dnd-kit/utilities'
import { GripVertical } from 'lucide-react'
import { cn } from '@/lib/utils'
import { ASSESSMENT_NAMES } from '@/types/semester-config'
import { useLanguage } from '@/context/language-provider'

interface DraggableExamItemProps {
  id: string
  assessmentCode: string
  month?: number
  isInDropZone?: boolean
}

export function DraggableExamItem({
  id,
  assessmentCode,
  isInDropZone = false,
}: DraggableExamItemProps) {
  const { language } = useLanguage()
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    isDragging,
  } = useDraggable({
    id,
    data: { assessmentCode },
  })

  const style: React.CSSProperties = {
    transform: CSS.Translate.toString(transform),
    opacity: isDragging ? 0.5 : 1,
  }

  const assessmentName = ASSESSMENT_NAMES[assessmentCode]
  const displayName = assessmentName
    ? (language === 'km' ? assessmentName.km : assessmentName.en)
    : assessmentCode

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
