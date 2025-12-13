import { Outlet } from '@tanstack/react-router'
import { User, KeyRound, Monitor } from 'lucide-react'
import { useLanguage } from '@/context/language-provider'
import { Separator } from '@/components/ui/separator'
import { SidebarNav } from '../components/sidebar-nav'

export function SettingsAccounts() {
  const { t } = useLanguage()

  const sidebarNavItems = [
    {
      title: t.settings.profile.title,
      href: '/settings/accounts/profile',
      icon: <User size={18} />,
    },
    {
      title: t.settings.account.title,
      href: '/settings/accounts/account',
      icon: <KeyRound size={18} />,
    },
    {
      title: t.settings.display.title,
      href: '/settings/accounts/display',
      icon: <Monitor size={18} />,
    },
  ]

  return (
    <div className="flex flex-1 flex-col">
      <div className="space-y-0.5">
        <h2 className="text-xl font-semibold tracking-tight">
          {t.settings.title}
        </h2>
        <p className="text-muted-foreground text-sm">{t.settings.description}</p>
      </div>
      <Separator className="my-4" />
      <div className="flex flex-1 flex-col space-y-4 overflow-hidden md:space-y-2 lg:flex-row lg:space-y-0 lg:space-x-12">
        <aside className="top-0 lg:sticky lg:w-1/5">
          <SidebarNav items={sidebarNavItems} />
        </aside>
        <div className="flex w-full overflow-y-hidden p-1">
          <Outlet />
        </div>
      </div>
    </div>
  )
}
