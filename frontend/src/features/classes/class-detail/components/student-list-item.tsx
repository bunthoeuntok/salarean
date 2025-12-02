import { format } from 'date-fns'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { useLanguage } from '@/context/language-provider'
import type { StudentEnrollmentItem, EnrollmentStatus } from '@/types/class.types'

interface StudentListItemProps {
  student: StudentEnrollmentItem
}

export function StudentListItem({ student }: StudentListItemProps) {
  const { t } = useLanguage()

  const getStatusVariant = (status: EnrollmentStatus) => {
    switch (status) {
      case 'ACTIVE':
        return 'default'
      case 'COMPLETED':
        return 'secondary'
      case 'TRANSFERRED':
      case 'WITHDRAWN':
        return 'outline'
      default:
        return 'outline'
    }
  }

  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2)
  }

  const formatEnrollmentDate = (dateString: string) => {
    try {
      return format(new Date(dateString), 'MMM d, yyyy')
    } catch {
      return dateString
    }
  }

  return (
    <tr className="border-b transition-colors hover:bg-muted/50">
      <td className="p-4">
        <div className="flex items-center gap-3">
          <Avatar className="h-10 w-10">
            <AvatarImage src={student.photoUrl ?? undefined} alt={student.studentName} />
            <AvatarFallback>{getInitials(student.studentName)}</AvatarFallback>
          </Avatar>
          <div>
            <p className="font-medium">{student.studentName}</p>
            <p className="text-sm text-muted-foreground">{student.studentCode}</p>
          </div>
        </div>
      </td>
      <td className="p-4 text-muted-foreground">
        {formatEnrollmentDate(student.enrollmentDate)}
      </td>
      <td className="p-4">
        <Badge variant={getStatusVariant(student.enrollmentStatus)}>
          {t.students.view.enrollmentStatus[student.enrollmentStatus]}
        </Badge>
      </td>
    </tr>
  )
}
