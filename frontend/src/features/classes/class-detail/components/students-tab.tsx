import { Skeleton } from '@/components/ui/skeleton'
import { useClassStudents } from '@/hooks/useClassStudents'
import type { Class } from '@/types/class.types'
import { StudentList } from './student-list'
import { EmptyState } from './empty-state'

interface StudentsTabProps {
  classId: string
  classData?: Class
}

export function StudentsTab({ classId, classData }: StudentsTabProps) {
  const {
    data: studentsData,
    isLoading,
    error,
  } = useClassStudents({
    classId,
    enabled: !!classId,
  })

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
        {Array.from({ length: 5 }).map((_, i) => (
          <Skeleton key={i} className="h-16 w-full" />
        ))}
      </div>
    )
  }

  if (!studentsData?.students || studentsData.students.length === 0) {
    return <EmptyState />
  }

  const className = classData?.grade
    ? `Grade ${classData.grade}${classData.section ? classData.section : ''}`
    : 'Class'

  return (
    <StudentList students={studentsData.students} className={className} />
  )
}
