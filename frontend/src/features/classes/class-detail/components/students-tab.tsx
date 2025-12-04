import { useMemo, useState, useCallback, useEffect } from 'react'
import { useLanguage } from '@/context/language-provider'
import { Skeleton } from '@/components/ui/skeleton'
import { ClientDataTableWithUrl } from '@/components/data-table'
import { useClassStudents } from '@/hooks/useClassStudents'
import { createStudentEnrollmentColumns } from '../columns'
import { useStudentSelectionStore } from '../../store/selection-store'
import { FloatingActionButton } from '../../components/floating-action-button'
import { BatchTransferDialog } from '../../components/batch-transfer-dialog'
import { EmptyState } from './empty-state'
import type { StudentEnrollmentItem } from '@/types/class.types'

interface StudentsTabProps {
  classId: string
}

export function StudentsTab({ classId }: StudentsTabProps) {
  const { t } = useLanguage()

  // Fetch only ACTIVE students from backend
  // No sort parameter - we'll sort on frontend since all data is loaded
  const {
    data: studentsData,
    isLoading,
    error,
  } = useClassStudents({
    classId,
    status: 'ACTIVE',
    enabled: !!classId,
  })

  // Selection state from Zustand store
  const {
    selectedStudentsByClass,
    toggleStudent,
    toggleAll,
    getSelectedCount,
    getSelectedStudents,
    clearSelection,
  } = useStudentSelectionStore()

  const selectedIds = useMemo(
    () => selectedStudentsByClass.get(classId) || new Set<string>(),
    [selectedStudentsByClass, classId]
  )

  const selectedCount = getSelectedCount(classId)
  const selectedStudents = useMemo(
    () => getSelectedStudents(classId),
    [getSelectedStudents, classId]
  )

  const [isTransferDialogOpen, setIsTransferDialogOpen] = useState(false)

  // Clear selection when navigating away from this class
  useEffect(() => {
    return () => {
      clearSelection(classId)
    }
  }, [classId, clearSelection])

  const handleOpenTransferDialog = () => {
    setIsTransferDialogOpen(true)
  }

  const handleTransferSuccess = () => {
    // Clear selection after successful transfer
    clearSelection(classId)
  }

  const handleToggleStudent = useCallback((studentId: string) => {
    const student = studentsData?.students.find((s) => s.studentId === studentId)
    if (student) {
      toggleStudent(classId, student)
    }
  }, [studentsData?.students, classId, toggleStudent])

  const handleToggleAll = useCallback((students: StudentEnrollmentItem[]) => {
    toggleAll(classId, students)
  }, [classId, toggleAll])

  // Create columns with translations and selection handlers
  const columns = useMemo(
    () =>
      createStudentEnrollmentColumns(t, {
        enableSelection: true,
        selectedStudentIds: selectedIds,
        onToggleStudent: handleToggleStudent,
        onToggleAll: handleToggleAll,
      }),
    [t, selectedIds, handleToggleStudent, handleToggleAll]
  )

  if (error) {
    return (
      <div className="flex items-center justify-center py-12">
        <p className="text-destructive">Failed to load students</p>
      </div>
    )
  }

  if (isLoading) {
    return (
      <div className="space-y-2">
        {Array.from({ length: 10 }).map((_, i) => (
          <Skeleton key={i} className="h-16 w-full" />
        ))}
      </div>
    )
  }

  if (!studentsData?.students || studentsData.students.length === 0) {
    return <EmptyState />
  }

  return (
    <>
      <ClientDataTableWithUrl
        columns={columns}
        data={studentsData.students}
        storageKey={`class-students-${classId}`}
        searchPlaceholder={t.classes.detail?.searchPlaceholder ?? 'Search students...'}
        enableColumnResizing
        enableColumnReordering
        showToolbar
        columnLabels={{
          studentCode: t.students.columns.code,
          fullName: t.students.columns.name,
          fullNameKhmer: t.students.columns.fullNameKhmer,
          gender: t.students.columns.gender,
          dateOfBirth: t.students.columns.dateOfBirth,
          enrollmentDate: t.students.view.fields.enrollmentDate,
          enrollmentStatus: t.students.columns.status,
        }}
      />

      <FloatingActionButton
        selectedCount={selectedCount}
        onClick={handleOpenTransferDialog}
        label={t.classes.transfer?.transferButton ?? 'Transfer Students'}
      />

      <BatchTransferDialog
        open={isTransferDialogOpen}
        onOpenChange={setIsTransferDialogOpen}
        sourceClassId={classId}
        selectedStudents={selectedStudents}
        onSuccess={handleTransferSuccess}
      />
    </>
  )
}
