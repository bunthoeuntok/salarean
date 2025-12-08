import { Calendar } from 'lucide-react'
import { useLanguage } from '@/context/language-provider'
import { useAcademicYearStore } from '@/store/academic-year-store'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

export function AcademicYearSelector() {
  const { t } = useLanguage()
  const { selectedAcademicYear, availableYears, setAcademicYear } =
    useAcademicYearStore()

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant='ghost' size='sm' className='gap-2 px-2'>
          <Calendar className='size-4 opacity-50' />
          <span className='hidden sm:inline'>{selectedAcademicYear}</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align='end'>
        {availableYears.map((year) => (
          <DropdownMenuItem
            key={year}
            onClick={() => setAcademicYear(year)}
            className='gap-2'
          >
            <span>{year}</span>
            {selectedAcademicYear === year && (
              <span className='ml-auto text-xs text-muted-foreground'>
                {t.common.active}
              </span>
            )}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
