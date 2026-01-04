import { useDroppable } from '@dnd-kit/core'
import {
  SortableContext,
  verticalListSortingStrategy,
  useSortable,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { GripVertical, X } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import type { ExamScheduleItem } from '@/types/semester-config'
import { MONTH_NAMES, ASSESSMENT_NAMES } from '@/types/semester-config'
import { useLanguage } from '@/context/language-provider'

interface SortableItemInZoneProps {
  item: ExamScheduleItem
  onMonthChange: (assessmentCode: string, month: number) => void
  onRemove: (assessmentCode: string) => void
}

function SortableItemInZone({ item, onMonthChange, onRemove }: SortableItemInZoneProps) {
  const { language } = useLanguage()
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: item.assessmentCode,
    data: { assessmentCode: item.assessmentCode },
  })

  const style: React.CSSProperties = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  }

  const assessmentName = ASSESSMENT_NAMES[item.assessmentCode]
  const displayName = assessmentName
    ? (language === 'km' ? assessmentName.km : assessmentName.en)
    : item.assessmentCode

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={cn(
        'flex items-center gap-2 rounded-lg border bg-card p-2',
        isDragging && 'shadow-lg ring-2 ring-primary'
      )}
    >
      <button
        {...attributes}
        {...listeners}
        className="cursor-grab touch-none text-muted-foreground hover:text-foreground"
      >
        <GripVertical className="h-4 w-4" />
      </button>
      <span className="flex-1 text-sm font-medium truncate">{displayName}</span>
      <Select
        value={item.month.toString()}
        onValueChange={(value) => onMonthChange(item.assessmentCode, parseInt(value))}
      >
        <SelectTrigger className="w-[100px] h-8 text-xs">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {Object.entries(MONTH_NAMES).map(([monthNum, names]) => (
            <SelectItem key={monthNum} value={monthNum}>
              {language === 'km' ? names.km : names.en}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      <Button
        variant="ghost"
        size="icon"
        className="h-6 w-6 text-muted-foreground hover:text-destructive"
        onClick={() => onRemove(item.assessmentCode)}
      >
        <X className="h-3 w-3" />
      </Button>
    </div>
  )
}

interface SemesterDropZoneProps {
  id: string
  title: string
  items: ExamScheduleItem[]
  onMonthChange: (assessmentCode: string, month: number) => void
  onRemove: (assessmentCode: string) => void
}

export function SemesterDropZone({
  id,
  title,
  items,
  onMonthChange,
  onRemove,
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
                onMonthChange={onMonthChange}
                onRemove={onRemove}
              />
            ))
          )}
        </div>
      </SortableContext>
    </div>
  )
}
