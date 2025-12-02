import { useLanguage } from '@/context/language-provider'
import type { StudentEnrollmentItem } from '@/types/class.types'
import { StudentListItem } from './student-list-item'

interface StudentListProps {
  students: StudentEnrollmentItem[]
  className: string
}

export function StudentList({ students, className }: StudentListProps) {
  const { t } = useLanguage()

  return (
    <div className="rounded-md border">
      <table className="w-full">
        <caption className="sr-only">Students enrolled in {className}</caption>
        <thead>
          <tr className="border-b bg-muted/50">
            <th className="p-4 text-left font-medium">{t.students.columns.name}</th>
            <th className="p-4 text-left font-medium">
              {t.students.view.fields.enrollmentDate}
            </th>
            <th className="p-4 text-left font-medium">{t.students.columns.status}</th>
          </tr>
        </thead>
        <tbody>
          {students.map((student) => (
            <StudentListItem key={student.studentId} student={student} />
          ))}
        </tbody>
      </table>
    </div>
  )
}
