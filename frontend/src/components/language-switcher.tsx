import { Languages } from 'lucide-react'
import { useLanguage } from '@/context/language-provider'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

const languages = [
  {
    code: 'en' as const,
    label: 'English',
    flag: '🇺🇸',
  },
  {
    code: 'km' as const,
    label: 'ខ្មែរ',
    flag: '🇰🇭',
  },
]

export function LanguageSwitcher() {
  const { language, setLanguage } = useLanguage()
  const currentLang = languages.find((l) => l.code === language) || languages[0]

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant='ghost' size='sm' className='gap-2 px-2'>
          <span className='text-base'>{currentLang.flag}</span>
          <span className='hidden sm:inline'>{currentLang.label}</span>
          <Languages className='size-4 opacity-50' />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align='end'>
        {languages.map((lang) => (
          <DropdownMenuItem
            key={lang.code}
            onClick={() => setLanguage(lang.code)}
            className='gap-2'
          >
            <span className='text-base'>{lang.flag}</span>
            <span>{lang.label}</span>
            {language === lang.code && (
              <span className='ml-auto text-xs text-muted-foreground'>
                Active
              </span>
            )}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
