import { useState, useMemo } from 'react'
import { useLanguage } from '@/context/language-provider'
import { cn } from '@/lib/utils'
import type { ClassSchedule, ScheduleEntry } from '@/types/schedule.types'
import { DAY_NAMES } from '@/types/schedule.types'
import { useQuery } from '@tanstack/react-query'
import { subjectService } from '@/services/subject.service'
import { EditEntryDialog } from './edit-entry-dialog'

interface ScheduleGridProps {
  classId: string
  schedule: ClassSchedule
}

interface CellProps {
  dayOfWeek: number
  periodNumber: number
  entry: ScheduleEntry | undefined
  subjectName: string | null
  onClick: () => void
}

function ScheduleCell({ dayOfWeek, periodNumber, entry, subjectName, onClick }: CellProps) {
  const { language } = useLanguage()

  return (
    <div
      className={cn(
        'flex min-h-[60px] cursor-pointer flex-col items-center justify-center rounded border p-2 text-center transition-colors hover:bg-muted/50',
        entry ? 'bg-primary/10 border-primary/30' : 'bg-muted/20 border-dashed'
      )}
      onClick={onClick}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault()
          onClick()
        }
      }}
      aria-label={`${DAY_NAMES[dayOfWeek][language === 'km' ? 'km' : 'en']} period ${periodNumber}${subjectName ? `: ${subjectName}` : ''}`}
    >
      {entry && subjectName ? (
        <>
          <span className="text-xs font-medium line-clamp-2">{subjectName}</span>
          {entry.room && (
            <span className="mt-1 text-[10px] text-muted-foreground">{entry.room}</span>
          )}
        </>
      ) : (
        <span className="text-xs text-muted-foreground">+</span>
      )}
    </div>
  )
}

export function ScheduleGrid({ classId, schedule }: ScheduleGridProps) {
  const { language } = useLanguage()
  const [editingCell, setEditingCell] = useState<{ dayOfWeek: number; periodNumber: number } | null>(null)

  // Fetch all subjects to display names
  const { data: subjects } = useQuery({
    queryKey: ['subjects'],
    queryFn: () => subjectService.getSubjects(),
    staleTime: 10 * 60 * 1000, // 10 minutes
  })

  // Create a map of subjectId -> subject name
  const subjectMap = useMemo(() => {
    if (!subjects) return {}
    const map: Record<string, string> = {}
    subjects.forEach((subject) => {
      map[subject.id] = language === 'km' ? subject.nameKhmer : subject.name
    })
    return map
  }, [subjects, language])

  // Create a map of entries by day and period for quick lookup
  const entryMap = useMemo(() => {
    const map: Record<string, ScheduleEntry> = {}
    schedule.entries.forEach((entry) => {
      const key = `${entry.dayOfWeek}-${entry.periodNumber}`
      map[key] = entry
    })
    return map
  }, [schedule.entries])

  // Get effective time slots (custom or from template) sorted by start time
  const timeSlots = useMemo(() => {
    const slots = schedule.effectiveSlots || []
    return [...slots].sort((a, b) => a.startTime.localeCompare(b.startTime))
  }, [schedule.effectiveSlots])

  // Days of the week (1-6 = Mon-Sat)
  const days = [1, 2, 3, 4, 5, 6]

  const handleCellClick = (dayOfWeek: number, periodNumber: number) => {
    setEditingCell({ dayOfWeek, periodNumber })
  }

  const handleCloseDialog = () => {
    setEditingCell(null)
  }

  const getExistingEntry = () => {
    if (!editingCell) return undefined
    return entryMap[`${editingCell.dayOfWeek}-${editingCell.periodNumber}`]
  }

  return (
    <>
      <div className="overflow-x-auto rounded-lg border">
        <table className="w-full min-w-[800px] border-collapse">
          <thead>
            <tr className="bg-muted/50">
              <th className="w-24 border-r p-2 text-left text-sm font-medium">
                {language === 'km' ? 'ម៉ោង' : 'Time'}
              </th>
              {days.map((day) => (
                <th key={day} className="border-r p-2 text-center text-sm font-medium last:border-r-0">
                  {DAY_NAMES[day][language === 'km' ? 'km' : 'en']}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {timeSlots.map((slot, index) => (
              slot.isBreak ? (
                // Break row - spans all columns
                <tr key={`slot-${index}`} className="border-t bg-amber-50/50 dark:bg-amber-950/20">
                  <td className="border-r p-2" colSpan={7}>
                    <div className="flex items-center justify-center gap-2 text-xs text-amber-700 dark:text-amber-400">
                      <span>{language === 'km' ? slot.labelKm : slot.label}</span>
                      <span className="text-muted-foreground">({slot.startTime} - {slot.endTime})</span>
                    </div>
                  </td>
                </tr>
              ) : (
                // Period row - clickable cells for each day
                <tr key={`slot-${index}`} className="border-t">
                  <td className="border-r bg-muted/30 p-2">
                    <div className="text-xs font-medium">
                      {language === 'km' ? slot.labelKm : slot.label}
                    </div>
                    <div className="text-[10px] text-muted-foreground">
                      {slot.startTime} - {slot.endTime}
                    </div>
                  </td>
                  {days.map((day) => {
                    const entry = entryMap[`${day}-${slot.periodNumber}`]
                    return (
                      <td key={day} className="border-r p-1 last:border-r-0">
                        <ScheduleCell
                          dayOfWeek={day}
                          periodNumber={slot.periodNumber!}
                          entry={entry}
                          subjectName={entry ? subjectMap[entry.subjectId] || null : null}
                          onClick={() => handleCellClick(day, slot.periodNumber!)}
                        />
                      </td>
                    )
                  })}
                </tr>
              )
            ))}
          </tbody>
        </table>
      </div>

      {/* Edit Entry Dialog */}
      {editingCell && (
        <EditEntryDialog
          open={!!editingCell}
          onOpenChange={(open) => !open && handleCloseDialog()}
          classId={classId}
          dayOfWeek={editingCell.dayOfWeek}
          periodNumber={editingCell.periodNumber}
          existingEntry={getExistingEntry()}
        />
      )}
    </>
  )
}
