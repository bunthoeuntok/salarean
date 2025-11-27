import { Outlet } from '@tanstack/react-router'
import { User, KeyRound } from 'lucide-react'
import { useLanguage } from '@/context/language-provider'
import { Separator } from '@/components/ui/separator'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { SidebarNav } from './components/sidebar-nav'

export function Settings() {
  const { t } = useLanguage()

  const sidebarNavItems = [
    {
      title: t.settings.profile.title,
      href: '/settings/profile',
      icon: <User size={18} />,
    },
    {
      title: t.settings.account.title,
      href: '/settings/account',
      icon: <KeyRound size={18} />,
    },
  ]

  return (
    <>
      <Header fixed />

      <Main fixed>
        <div className='space-y-0.5'>
          <h1 className='text-2xl font-bold tracking-tight md:text-3xl'>
            {t.settings.title}
          </h1>
          <p className='text-muted-foreground'>{t.settings.description}</p>
        </div>
        <Separator className='my-4 lg:my-6' />
        <div className='flex flex-1 flex-col space-y-2 overflow-hidden md:space-y-2 lg:flex-row lg:space-y-0 lg:space-x-12'>
          <aside className='top-0 lg:sticky lg:w-1/5'>
            <SidebarNav items={sidebarNavItems} />
          </aside>
          <div className='flex w-full overflow-y-hidden p-1'>
            <Outlet />
          </div>
        </div>
      </Main>
    </>
  )
}
