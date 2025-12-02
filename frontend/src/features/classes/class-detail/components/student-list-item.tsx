import { format } from 'date-fns'
import { CheckCircle, XCircle, ArrowRightLeft, GraduationCap } from 'lucide-react'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { useLanguage } from '@/context/language-provider'
import type { StudentEnrollmentItem, EnrollmentStatus } from '@/types/class.types'

interface StudentListItemProps {
  student: StudentEnrollmentItem
}

const statusIconMap: Record<EnrollmentStatus, React.ComponentType<{ className?: string }>> = {
  ACTIVE: CheckCircle,
  COMPLETED: GraduationCap,
  TRANSFERRED: ArrowRightLeft,
  WITHDRAWN: XCircle,
}

export function StudentListItem({ student }: StudentListItemProps) {
  const { t } = useLanguage()

  const getStatusVariant = (status: EnrollmentStatus): 'default' | 'secondary' | 'outline' => {
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
      return format(new Date(dateString), 'dd/MM/yyyy')
    } catch {
      return dateString
    }
  }

  const StatusIcon = statusIconMap[student.enrollmentStatus]

  return (
    <tr className="border-b transition-colors hover:bg-muted/50">
      {/* Student Code */}
      <td className="p-4">
        <span className="font-medium">{student.studentCode}</span>
      </td>
      {/* Student Name with Avatar */}
      <td className="p-4">
        <div className="flex items-center gap-3">
          <Avatar className="h-10 w-10">
            <AvatarImage src={student.photoUrl ?? undefined} alt={student.studentName} />
            <AvatarFallback className="text-xs">{getInitials(student.studentName)}</AvatarFallback>
          </Avatar>
          <div className="flex flex-col">
            <span className="font-medium">{student.studentName}</span>
          </div>
        </div>
      </td>
      {/* Enrollment Date */}
      <td className="p-4 text-muted-foreground">
        {formatEnrollmentDate(student.enrollmentDate)}
      </td>
      {/* Status */}
      <td className="p-4">
        <div className="text-center">
          <Badge variant={getStatusVariant(student.enrollmentStatus)}>
            <StatusIcon className="mr-1 h-3 w-3" />
            {t.students.view.enrollmentStatus[student.enrollmentStatus]}
          </Badge>
        </div>
      </td>
    </tr>
  )
}
