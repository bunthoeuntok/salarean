import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { useLanguage } from '@/context/language-provider'
import type { EnrollmentStatusFilter } from '@/lib/validations/class-filters'

interface StatusFilterProps {
  value: EnrollmentStatusFilter
  onChange: (value: EnrollmentStatusFilter) => void
}

export function StatusFilter({ value, onChange }: StatusFilterProps) {
  const { t } = useLanguage()

  return (
    <Select value={value} onValueChange={(v) => onChange(v as EnrollmentStatusFilter)}>
      <SelectTrigger className="w-[180px]" aria-label={t.classes.detail?.statusFilterLabel ?? 'Filter by enrollment status'}>
        <SelectValue placeholder={t.classes.detail?.statusFilterPlaceholder ?? 'Filter by status'} />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="ALL">{t.classes.detail?.statusOptions?.ALL ?? 'All Statuses'}</SelectItem>
        <SelectItem value="ACTIVE">{t.students.view.enrollmentStatus.ACTIVE}</SelectItem>
        <SelectItem value="COMPLETED">{t.students.view.enrollmentStatus.COMPLETED}</SelectItem>
        <SelectItem value="TRANSFERRED">{t.students.view.enrollmentStatus.TRANSFERRED}</SelectItem>
        <SelectItem value="WITHDRAWN">{t.students.view.enrollmentStatus.WITHDRAWN}</SelectItem>
      </SelectContent>
    </Select>
  )
}
