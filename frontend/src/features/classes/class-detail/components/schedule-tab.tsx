import { useState } from 'react'
import { Plus, Copy, Trash2, RefreshCw } from 'lucide-react'
import { useLanguage } from '@/context/language-provider'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import {
  useClassSchedule,
  useCreateClassSchedule,
  useDeleteClassSchedule,
  useClearScheduleEntries,
} from '@/hooks/use-schedule'
import { useQuery } from '@tanstack/react-query'
import { classService } from '@/services/class.service'
import { ScheduleGrid } from './schedule-grid'
import { CreateScheduleDialog } from './create-schedule-dialog'
import { CopyScheduleDialog } from './copy-schedule-dialog'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'

interface ScheduleTabProps {
  classId: string
}

export function ScheduleTab({ classId }: ScheduleTabProps) {
  const { t, language } = useLanguage()
  const [showCreateDialog, setShowCreateDialog] = useState(false)
  const [showCopyDialog, setShowCopyDialog] = useState(false)
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)
  const [showClearConfirm, setShowClearConfirm] = useState(false)

  // Fetch class details to get shift and academic year
  const { data: classData } = useQuery({
    queryKey: ['class', classId],
    queryFn: () => classService.getClass(classId),
    enabled: !!classId,
  })

  // Fetch class schedule
  const { data: schedule, isLoading, refetch } = useClassSchedule(classId)

  // Mutations
  const createSchedule = useCreateClassSchedule()
  const deleteSchedule = useDeleteClassSchedule(classId)
  const clearEntries = useClearScheduleEntries(classId)

  const handleCreateSchedule = async (templateId?: string) => {
    if (!classData) return

    await createSchedule.mutateAsync({
      classId,
      timeSlotTemplateId: templateId,
      academicYear: classData.academicYear,
    })
    setShowCreateDialog(false)
  }

  const handleDeleteSchedule = async () => {
    await deleteSchedule.mutateAsync()
    setShowDeleteConfirm(false)
  }

  const handleClearEntries = async () => {
    await clearEntries.mutateAsync()
    setShowClearConfirm(false)
  }

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <Skeleton className="h-8 w-48" />
          <Skeleton className="h-10 w-32" />
        </div>
        <Skeleton className="h-96 w-full" />
      </div>
    )
  }

  // No schedule yet - show create option
  if (!schedule) {
    return (
      <div className="flex flex-col items-center justify-center rounded-lg border-2 border-dashed p-12 text-center">
        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-muted">
          <Plus className="h-6 w-6 text-muted-foreground" />
        </div>
        <h3 className="mt-4 text-lg font-semibold">
          {language === 'km' ? 'មិនទាន់មានកាលវិភាគ' : 'No Schedule Yet'}
        </h3>
        <p className="mt-2 text-sm text-muted-foreground">
          {language === 'km'
            ? 'បង្កើតកាលវិភាគសម្រាប់ថ្នាក់នេះដើម្បីចាប់ផ្តើម'
            : 'Create a schedule for this class to get started'}
        </p>
        <div className="mt-6 flex gap-2">
          <Button onClick={() => setShowCreateDialog(true)}>
            <Plus className="mr-2 h-4 w-4" />
            {language === 'km' ? 'បង្កើតកាលវិភាគ' : 'Create Schedule'}
          </Button>
          <Button variant="outline" onClick={() => setShowCopyDialog(true)}>
            <Copy className="mr-2 h-4 w-4" />
            {language === 'km' ? 'ចម្លងពីថ្នាក់ផ្សេង' : 'Copy from Another Class'}
          </Button>
        </div>

        <CreateScheduleDialog
          open={showCreateDialog}
          onOpenChange={setShowCreateDialog}
          classShift={classData?.shift}
          onSelect={handleCreateSchedule}
          isLoading={createSchedule.isPending}
        />

        <CopyScheduleDialog
          open={showCopyDialog}
          onOpenChange={setShowCopyDialog}
          targetClassId={classId}
        />
      </div>
    )
  }

  // Schedule exists - show grid and management options
  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 className="text-lg font-semibold">
            {language === 'km' ? 'កាលវិភាគប្រចាំសប្តាហ៍' : 'Weekly Schedule'}
          </h3>
          {schedule.template && (
            <p className="text-sm text-muted-foreground">
              {language === 'km' ? 'គំរូ: ' : 'Template: '}
              {language === 'km' ? schedule.template.nameKm : schedule.template.name}
            </p>
          )}
        </div>
        <div className="flex flex-wrap gap-2">
          <Button variant="outline" size="sm" onClick={() => refetch()}>
            <RefreshCw className="mr-2 h-4 w-4" />
            {language === 'km' ? 'ធ្វើបច្ចុប្បន្នភាព' : 'Refresh'}
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() => setShowClearConfirm(true)}
            disabled={schedule.entries.length === 0}
          >
            <Trash2 className="mr-2 h-4 w-4" />
            {language === 'km' ? 'សម្អាតទាំងអស់' : 'Clear All'}
          </Button>
          <Button
            variant="destructive"
            size="sm"
            onClick={() => setShowDeleteConfirm(true)}
          >
            <Trash2 className="mr-2 h-4 w-4" />
            {language === 'km' ? 'លុបកាលវិភាគ' : 'Delete Schedule'}
          </Button>
        </div>
      </div>

      <ScheduleGrid
        classId={classId}
        schedule={schedule}
      />

      {/* Delete Schedule Confirmation */}
      <AlertDialog open={showDeleteConfirm} onOpenChange={setShowDeleteConfirm}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {language === 'km' ? 'លុបកាលវិភាគ?' : 'Delete Schedule?'}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {language === 'km'
                ? 'សកម្មភាពនេះមិនអាចត្រឡប់វិញបានទេ។ កាលវិភាគទាំងអស់នឹងត្រូវបានលុប។'
                : 'This action cannot be undone. All schedule entries will be deleted.'}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>{t.common?.cancel || 'Cancel'}</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteSchedule}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {deleteSchedule.isPending
                ? (language === 'km' ? 'កំពុងលុប...' : 'Deleting...')
                : (language === 'km' ? 'លុប' : 'Delete')}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Clear Entries Confirmation */}
      <AlertDialog open={showClearConfirm} onOpenChange={setShowClearConfirm}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {language === 'km' ? 'សម្អាតកាលវិភាគ?' : 'Clear Schedule?'}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {language === 'km'
                ? 'សកម្មភាពនេះនឹងលុបមុខវិជ្ជាទាំងអស់ពីកាលវិភាគ។ គំរូពេលវេលានឹងត្រូវរក្សាទុក។'
                : 'This will remove all subjects from the schedule. The time slot template will be preserved.'}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>{t.common?.cancel || 'Cancel'}</AlertDialogCancel>
            <AlertDialogAction onClick={handleClearEntries}>
              {clearEntries.isPending
                ? (language === 'km' ? 'កំពុងសម្អាត...' : 'Clearing...')
                : (language === 'km' ? 'សម្អាត' : 'Clear')}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
