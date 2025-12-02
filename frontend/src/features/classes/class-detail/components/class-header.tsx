import { Calendar, GraduationCap, Users } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { useLanguage } from '@/context/language-provider'
import type { Class, ClassStatus } from '@/types/class.types'

interface ClassHeaderProps {
  classData: Class
}

export function ClassHeader({ classData }: ClassHeaderProps) {
  const { t } = useLanguage()

  const className = classData.section
    ? `Grade ${classData.grade}${classData.section}`
    : `Grade ${classData.grade}`

  const getStatusVariant = (status: ClassStatus) => {
    switch (status) {
      case 'ACTIVE':
        return 'default'
      case 'COMPLETED':
        return 'secondary'
      case 'INACTIVE':
        return 'outline'
      default:
        return 'outline'
    }
  }

  return (
    <div className="mb-6">
      <div className="flex items-start justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">{className}</h2>
          <div className="mt-2 flex flex-wrap items-center gap-4 text-muted-foreground">
            <div className="flex items-center gap-1.5">
              <Calendar className="h-4 w-4" />
              <span>{classData.academicYear}</span>
            </div>
            <div className="flex items-center gap-1.5">
              <GraduationCap className="h-4 w-4" />
              <span>{t.classes.level[classData.level]}</span>
            </div>
            <div className="flex items-center gap-1.5">
              <Users className="h-4 w-4" />
              <span>
                {classData.studentCount}
                {classData.maxCapacity && ` / ${classData.maxCapacity}`}
              </span>
            </div>
          </div>
        </div>
        <Badge variant={getStatusVariant(classData.status)}>
          {t.classes.status[classData.status]}
        </Badge>
      </div>
    </div>
  )
}
