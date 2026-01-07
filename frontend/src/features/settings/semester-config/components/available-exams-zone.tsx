import { useDroppable } from '@dnd-kit/core'
import { cn } from '@/lib/utils'
import { useLanguage } from '@/context/language-provider'
import { DraggableExamItem } from './draggable-exam-item'

interface AvailableExamsZoneProps {
  availableExams: string[]
  titleMap: Record<string, string>
}

export function AvailableExamsZone({ availableExams, titleMap }: AvailableExamsZoneProps) {
  const { t } = useLanguage()
  const { setNodeRef, isOver } = useDroppable({
    id: 'AVAILABLE',
    data: { type: 'available' },
  })

  return (
    <div
      ref={setNodeRef}
      className={cn(
        'space-y-2 min-h-[200px] rounded-lg border-2 border-dashed p-3 transition-colors',
        isOver ? 'border-primary bg-primary/5' : 'border-transparent'
      )}
    >
      {availableExams.length === 0 ? (
        <p className="text-sm text-muted-foreground text-center py-4">
          {t.semesterConfig?.allAssigned || 'All exams assigned'}
        </p>
      ) : (
        availableExams.map((code) => (
          <DraggableExamItem
            key={code}
            id={code}
            assessmentCode={code}
            title={titleMap[code]}
          />
        ))
      )}
    </div>
  )
}
