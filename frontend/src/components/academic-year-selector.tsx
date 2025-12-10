import { Calendar, ChevronDown } from 'lucide-react'
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
        <Button variant='outline' size='sm' className='w-full justify-start gap-2 group-data-[collapsible=icon]:gap-0 group-data-[collapsible=icon]:px-0 group-data-[collapsible=icon]:justify-center'>
          <Calendar className='size-4 shrink-0' />
          <span className='flex-1 truncate text-left group-data-[collapsible=icon]:hidden'>{selectedAcademicYear}</span>
          <ChevronDown className='size-4 shrink-0 opacity-50 group-data-[collapsible=icon]:hidden' />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align='start' className='w-[200px]'>
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
