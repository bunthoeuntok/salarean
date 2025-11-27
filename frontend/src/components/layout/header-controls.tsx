import { ConfigDrawer } from '@/components/config-drawer'
import { LanguageSwitcher } from '@/components/language-switcher'
import { Search } from '@/components/search'

export function HeaderControls() {
  return (
    <div className='flex items-center justify-between w-full'>
      <Search />
      <div className='flex items-center gap-1'>
        <LanguageSwitcher />
        <ConfigDrawer />
      </div>
    </div>
  )
}
