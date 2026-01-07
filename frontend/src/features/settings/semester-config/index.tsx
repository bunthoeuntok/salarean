import { useState, useMemo } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  DndContext,
  DragOverlay,
  closestCorners,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  type DragStartEvent,
  type DragEndEvent,
  type DragOverEvent,
} from '@dnd-kit/core'
import { sortableKeyboardCoordinates, arrayMove } from '@dnd-kit/sortable'
import { Loader2, RotateCcw, Save } from 'lucide-react'
import { toast } from 'sonner'
import { useLanguage } from '@/context/language-provider'
import { Separator } from '@/components/ui/separator'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { semesterConfigService } from '@/services/semester-config.service'
import { assessmentTypeService } from '@/services/assessment-type.service'
import type { ExamScheduleItem } from '@/types/semester-config'
import { ASSESSMENT_NAMES } from '@/types/semester-config'
import { SemesterDropZone } from './components/semester-drop-zone'
import { AvailableExamsZone } from './components/available-exams-zone'
import { useAcademicYearStore } from '@/store/academic-year-store'

// Query key factory
const semesterConfigKeys = {
  all: ['semester-configs'] as const,
  byYear: (year: string) => [...semesterConfigKeys.all, year] as const,
}

export function SettingsSemesterConfig() {
  const { t, language } = useLanguage()
  const queryClient = useQueryClient()
  const { selectedAcademicYear: academicYear } = useAcademicYearStore()

  // State for each semester's exams
  const [semester1Items, setSemester1Items] = useState<ExamScheduleItem[]>([])
  const [semester2Items, setSemester2Items] = useState<ExamScheduleItem[]>([])
  const [hasChanges, setHasChanges] = useState(false)
  const [activeId, setActiveId] = useState<string | null>(null)

  // Fetch all assessment types to get monthly exams and their titles
  const { data: assessmentTypes, isLoading: loadingAssessmentTypes } = useQuery({
    queryKey: ['monthly-assessment-types'],
    queryFn: () => assessmentTypeService.getAssessmentTypesByCategory('MONTHLY_EXAM'),
  })

  // Get all monthly exam codes from API
  const allMonthlyExamCodes = useMemo(() => {
    if (!assessmentTypes) return []
    return assessmentTypes
      .sort((a, b) => a.displayOrder - b.displayOrder)
      .map((at) => at.code)
  }, [assessmentTypes])

  // Build title map from assessment types (using nameKhmer) - derived state with useMemo
  const titleMap = useMemo(() => {
    if (!assessmentTypes) return {}
    const map: Record<string, string> = {}
    assessmentTypes.forEach((at) => {
      map[at.code] = at.nameKhmer
    })
    return map
  }, [assessmentTypes])

  // Sensors for drag and drop
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  )

  // Fetch both semester configs
  const { data: semester1Config, isLoading: loading1 } = useQuery({
    queryKey: [...semesterConfigKeys.byYear(academicYear), 'SEMESTER_1'],
    queryFn: () => semesterConfigService.getConfig(academicYear, 'SEMESTER_1'),
    enabled: !!academicYear,
  })

  const { data: semester2Config, isLoading: loading2 } = useQuery({
    queryKey: [...semesterConfigKeys.byYear(academicYear), 'SEMESTER_2'],
    queryFn: () => semesterConfigService.getConfig(academicYear, 'SEMESTER_2'),
    enabled: !!academicYear,
  })

  const isLoading = loading1 || loading2 || loadingAssessmentTypes

  // Track previous config IDs to detect when server data changes
  const [prevConfigIds, setPrevConfigIds] = useState<{ s1?: string; s2?: string }>({})

  // Initialize state from configs using React's recommended pattern for updating state during render
  // See: https://react.dev/learn/you-might-not-need-an-effect#adjusting-some-state-when-a-prop-changes
  if (semester1Config?.id && semester1Config.id !== prevConfigIds.s1) {
    setPrevConfigIds((prev) => ({ ...prev, s1: semester1Config.id }))
    const monthlyExams = semester1Config.examSchedule?.filter(
      (item) => item.assessmentCode.startsWith('MONTHLY_')
    ) ?? []
    setSemester1Items(monthlyExams)
    setHasChanges(false)
  }

  if (semester2Config?.id && semester2Config.id !== prevConfigIds.s2) {
    setPrevConfigIds((prev) => ({ ...prev, s2: semester2Config.id }))
    const monthlyExams = semester2Config.examSchedule?.filter(
      (item) => item.assessmentCode.startsWith('MONTHLY_')
    ) ?? []
    setSemester2Items(monthlyExams)
    setHasChanges(false)
  }

  // Compute available exams (not assigned to any semester)
  const availableExams = useMemo(() => {
    const usedCodes = [
      ...semester1Items.map((i) => i.assessmentCode),
      ...semester2Items.map((i) => i.assessmentCode),
    ]
    return allMonthlyExamCodes.filter((code) => !usedCodes.includes(code))
  }, [semester1Items, semester2Items, allMonthlyExamCodes])

  // Save mutation for Semester 1
  const saveSemester1 = useMutation({
    mutationFn: () => {
      const examSchedule: ExamScheduleItem[] = [
        ...semester1Items.map((item, idx) => ({
          ...item,
          displayOrder: idx + 1,
        })),
        // Add semester exam at the end (use title from database)
        { assessmentCode: 'SEMESTER_1', title: titleMap['SEMESTER_1'] || 'SEMESTER_1', displayOrder: semester1Items.length + 1 },
      ]
      return semesterConfigService.saveTeacherConfig({
        academicYear,
        semesterExamCode: 'SEMESTER_1',
        examSchedule,
      })
    },
  })

  // Save mutation for Semester 2
  const saveSemester2 = useMutation({
    mutationFn: () => {
      const examSchedule: ExamScheduleItem[] = [
        ...semester2Items.map((item, idx) => ({
          ...item,
          displayOrder: idx + 1,
        })),
        // Add semester exam at the end (use title from database)
        { assessmentCode: 'SEMESTER_2', title: titleMap['SEMESTER_2'] || 'SEMESTER_2', displayOrder: semester2Items.length + 1 },
      ]
      return semesterConfigService.saveTeacherConfig({
        academicYear,
        semesterExamCode: 'SEMESTER_2',
        examSchedule,
      })
    },
  })

  // Combined save
  const handleSave = async () => {
    try {
      await Promise.all([saveSemester1.mutateAsync(), saveSemester2.mutateAsync()])
      queryClient.invalidateQueries({ queryKey: semesterConfigKeys.byYear(academicYear) })
      toast.success(t.semesterConfig?.saveSuccess || 'Configuration saved successfully')
      setHasChanges(false)
    } catch {
      toast.error(t.semesterConfig?.saveError || 'Failed to save configuration')
    }
  }

  // Reset mutation
  const handleReset = async () => {
    try {
      await Promise.all([
        semesterConfigService.deleteTeacherConfig(academicYear, 'SEMESTER_1'),
        semesterConfigService.deleteTeacherConfig(academicYear, 'SEMESTER_2'),
      ])
      queryClient.invalidateQueries({ queryKey: semesterConfigKeys.byYear(academicYear) })
      toast.success(t.semesterConfig?.resetSuccess || 'Configuration reset to default')
      setHasChanges(false)
    } catch {
      toast.error(t.semesterConfig?.resetError || 'Failed to reset configuration')
    }
  }

  // Handle drag start
  const handleDragStart = (event: DragStartEvent) => {
    setActiveId(event.active.id as string)
  }

  // Handle drag over (for visual feedback)
  const handleDragOver = (event: DragOverEvent) => {
    const { active, over } = event
    if (!over) return

    const activeCode = active.id as string
    const overId = over.id as string

    // Check if dragging from available to a semester
    if (availableExams.includes(activeCode)) {
      // Will be handled in dragEnd
      return
    }

    // Reordering within same semester
    const inSemester1 = semester1Items.find((i) => i.assessmentCode === activeCode)
    const inSemester2 = semester2Items.find((i) => i.assessmentCode === activeCode)

    if (inSemester1) {
      const overInSemester1 = semester1Items.find((i) => i.assessmentCode === overId)
      if (overInSemester1) {
        const oldIndex = semester1Items.findIndex((i) => i.assessmentCode === activeCode)
        const newIndex = semester1Items.findIndex((i) => i.assessmentCode === overId)
        if (oldIndex !== newIndex) {
          setSemester1Items(arrayMove(semester1Items, oldIndex, newIndex))
          setHasChanges(true)
        }
      }
    }

    if (inSemester2) {
      const overInSemester2 = semester2Items.find((i) => i.assessmentCode === overId)
      if (overInSemester2) {
        const oldIndex = semester2Items.findIndex((i) => i.assessmentCode === activeCode)
        const newIndex = semester2Items.findIndex((i) => i.assessmentCode === overId)
        if (oldIndex !== newIndex) {
          setSemester2Items(arrayMove(semester2Items, oldIndex, newIndex))
          setHasChanges(true)
        }
      }
    }
  }

  // Handle drag end
  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event
    setActiveId(null)

    if (!over) return

    const activeCode = active.id as string
    const overId = over.id as string

    // Determine source and destination
    const isFromAvailable = availableExams.includes(activeCode)
    const isFromSemester1 = semester1Items.some((i) => i.assessmentCode === activeCode)
    const isFromSemester2 = semester2Items.some((i) => i.assessmentCode === activeCode)

    const isToAvailable = overId === 'AVAILABLE'
    const isToSemester1 = overId === 'SEMESTER_1' || semester1Items.some((i) => i.assessmentCode === overId)
    const isToSemester2 = overId === 'SEMESTER_2' || semester2Items.some((i) => i.assessmentCode === overId)

    // Create new item with title from database (titleMap)
    const createItem = (code: string): ExamScheduleItem => ({
      assessmentCode: code,
      title: titleMap[code] || code,
      displayOrder: 1,
    })

    // Moving from available to semester
    if (isFromAvailable) {
      if (isToSemester1) {
        setSemester1Items([...semester1Items, createItem(activeCode)])
        setHasChanges(true)
      } else if (isToSemester2) {
        setSemester2Items([...semester2Items, createItem(activeCode)])
        setHasChanges(true)
      }
      return
    }

    // Moving from semester1 to available
    if (isFromSemester1 && isToAvailable) {
      setSemester1Items(semester1Items.filter((i) => i.assessmentCode !== activeCode))
      setHasChanges(true)
      return
    }

    // Moving from semester2 to available
    if (isFromSemester2 && isToAvailable) {
      setSemester2Items(semester2Items.filter((i) => i.assessmentCode !== activeCode))
      setHasChanges(true)
      return
    }

    // Moving from semester1 to semester2
    if (isFromSemester1 && isToSemester2) {
      const item = semester1Items.find((i) => i.assessmentCode === activeCode)
      if (item) {
        setSemester1Items(semester1Items.filter((i) => i.assessmentCode !== activeCode))
        setSemester2Items([...semester2Items, item])
        setHasChanges(true)
      }
      return
    }

    // Moving from semester2 to semester1
    if (isFromSemester2 && isToSemester1) {
      const item = semester2Items.find((i) => i.assessmentCode === activeCode)
      if (item) {
        setSemester2Items(semester2Items.filter((i) => i.assessmentCode !== activeCode))
        setSemester1Items([...semester1Items, item])
        setHasChanges(true)
      }
      return
    }
  }

  // Check if using custom config
  const isCustomConfig = semester1Config?.isDefault === false || semester2Config?.isDefault === false

  if (isLoading) {
    return (
      <div className="flex flex-1 items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  // Get active item for drag overlay
  const activeItem = activeId
    ? allMonthlyExamCodes.includes(activeId)
      ? activeId
      : semester1Items.find((i) => i.assessmentCode === activeId)?.assessmentCode ||
        semester2Items.find((i) => i.assessmentCode === activeId)?.assessmentCode
    : null

  return (
    <div className="flex flex-1 flex-col">
      {/* Page Header */}
      <div className="space-y-0.5">
        <h2 className="text-xl font-semibold tracking-tight">
          {t.semesterConfig?.title || 'Semester Configuration'}
        </h2>
        <p className="text-muted-foreground text-sm">
          {t.semesterConfig?.description || 'Configure exam schedule for each semester'}
        </p>
      </div>
      <Separator className="my-4" />

      {/* Academic Year & Status */}
      <div className="mb-4 flex items-center justify-between">
        <div className="text-sm text-muted-foreground">
          {t.semesterConfig?.academicYear || 'Academic Year'}:{' '}
          <span className="font-medium text-foreground">{academicYear}</span>
        </div>
        {isCustomConfig && (
          <span className="rounded-full bg-primary/10 px-2 py-1 text-xs font-medium text-primary">
            {t.semesterConfig?.customConfig || 'Custom'}
          </span>
        )}
      </div>

      {/* Main Content */}
      <DndContext
        sensors={sensors}
        collisionDetection={closestCorners}
        onDragStart={handleDragStart}
        onDragOver={handleDragOver}
        onDragEnd={handleDragEnd}
      >
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 flex-1">
          {/* Left: Available Exams */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-base">
                {t.semesterConfig?.availableExams || 'Available Exams'}
              </CardTitle>
              <CardDescription className="text-xs">
                {t.semesterConfig?.dragInstruction || 'Drag exams to assign to a semester'}
              </CardDescription>
            </CardHeader>
            <CardContent>
              <AvailableExamsZone availableExams={availableExams} titleMap={titleMap} />
            </CardContent>
          </Card>

          {/* Right: Semester Columns */}
          <div className="lg:col-span-2 grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Semester 1 */}
            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="text-base">
                  {language === 'km' ? ASSESSMENT_NAMES.SEMESTER_1.km : ASSESSMENT_NAMES.SEMESTER_1.en}
                </CardTitle>
                <CardDescription className="text-xs">
                  {semester1Items.length} {t.semesterConfig?.monthlyExams || 'monthly exams'}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <SemesterDropZone
                  id="SEMESTER_1"
                  title=""
                  items={semester1Items}
                />
              </CardContent>
            </Card>

            {/* Semester 2 */}
            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="text-base">
                  {language === 'km' ? ASSESSMENT_NAMES.SEMESTER_2.km : ASSESSMENT_NAMES.SEMESTER_2.en}
                </CardTitle>
                <CardDescription className="text-xs">
                  {semester2Items.length} {t.semesterConfig?.monthlyExams || 'monthly exams'}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <SemesterDropZone
                  id="SEMESTER_2"
                  title=""
                  items={semester2Items}
                />
              </CardContent>
            </Card>
          </div>
        </div>

        {/* Drag Overlay */}
        <DragOverlay>
          {activeItem && (
            <div className="rounded-lg border bg-card p-3 shadow-lg ring-2 ring-primary">
              <span className="text-sm font-medium">
                {titleMap[activeItem] || activeItem}
              </span>
            </div>
          )}
        </DragOverlay>
      </DndContext>

      {/* Action Buttons */}
      <div className="flex justify-between pt-6 border-t mt-6">
        <Button
          variant="outline"
          onClick={handleReset}
          disabled={!isCustomConfig}
        >
          <RotateCcw className="mr-2 h-4 w-4" />
          {t.semesterConfig?.resetToDefault || 'Reset to Default'}
        </Button>
        <Button
          onClick={handleSave}
          disabled={!hasChanges || saveSemester1.isPending || saveSemester2.isPending}
        >
          {(saveSemester1.isPending || saveSemester2.isPending) ? (
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
          ) : (
            <Save className="mr-2 h-4 w-4" />
          )}
          {t.semesterConfig?.saveChanges || 'Save Changes'}
        </Button>
      </div>
    </div>
  )
}
