import { useDroppable } from '@dnd-kit/core'
import {
  SortableContext,
  verticalListSortingStrategy,
  useSortable,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { GripVertical } from 'lucide-react'
import { cn } from '@/lib/utils'
import type { ExamScheduleItem } from '@/types/semester-config'
import { useLanguage } from '@/context/language-provider'

interface SortableItemInZoneProps {
  item: ExamScheduleItem
}

function SortableItemInZone({ item }: SortableItemInZoneProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: item.assessmentCode,
    data: { assessmentCode: item.assessmentCode, title: item.title },
  })

  const style: React.CSSProperties = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  }

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={cn(
        'flex items-center gap-2 rounded-lg border bg-card p-2 cursor-grab active:cursor-grabbing',
        isDragging && 'shadow-lg ring-2 ring-primary'
      )}
      {...attributes}
      {...listeners}
    >
      <GripVertical className="h-4 w-4 text-muted-foreground" />
      <span className="flex-1 text-sm font-medium truncate">{item.title}</span>
    </div>
  )
}

interface SemesterDropZoneProps {
  id: string
  title: string
  items: ExamScheduleItem[]
}

export function SemesterDropZone({
  id,
  title,
  items,
}: SemesterDropZoneProps) {
  const { t } = useLanguage()
  const { setNodeRef, isOver } = useDroppable({
    id,
    data: { type: 'semester', semesterId: id },
  })

  return (
    <div
      ref={setNodeRef}
      className={cn(
        'flex flex-col rounded-lg border-2 border-dashed p-3 min-h-[200px] transition-colors',
        isOver ? 'border-primary bg-primary/5' : 'border-muted-foreground/25'
      )}
    >
      <h3 className="text-sm font-semibold mb-3 text-center">{title}</h3>
      <SortableContext
        items={items.map((i) => i.assessmentCode)}
        strategy={verticalListSortingStrategy}
      >
        <div className="flex-1 space-y-2">
          {items.length === 0 ? (
            <div className="flex items-center justify-center h-full text-xs text-muted-foreground">
              {t.semesterConfig?.dropHere || 'Drop exams here'}
            </div>
          ) : (
            items.map((item) => (
              <SortableItemInZone
                key={item.assessmentCode}
                item={item}
              />
            ))
          )}
        </div>
      </SortableContext>
    </div>
  )
}
