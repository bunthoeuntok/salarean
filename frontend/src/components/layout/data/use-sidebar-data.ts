import {
  LayoutDashboard,
  Users,
  GraduationCap,
  Calendar,
  ClipboardList,
  Settings,
  HelpCircle,
  UserCog,
  Bell,
} from 'lucide-react'
import { useLanguage } from '@/context/language-provider'
import { type SidebarData } from '../types'

export function useSidebarData(): SidebarData {
  const { t } = useLanguage()

  return {
    user: {
      name: 'Teacher',
      email: 'teacher@salarean.com',
      avatar: undefined,
    },
    navGroups: [
      {
        title: t.nav.overview,
        items: [
          {
            title: t.nav.dashboard,
            url: '/dashboard',
            icon: LayoutDashboard,
          },
        ],
      },
      {
        title: t.nav.management,
        items: [
          {
            title: t.nav.students,
            url: '/students',
            icon: GraduationCap,
          },
          {
            title: t.nav.classes,
            url: '/classes',
            icon: Users,
          },
          {
            title: t.nav.attendance,
            url: '/attendance',
            icon: ClipboardList,
          },
          {
            title: t.nav.schedule,
            url: '/schedule',
            icon: Calendar,
          },
        ],
      },
      {
        title: t.nav.settings,
        items: [
          {
            title: t.nav.settings,
            icon: Settings,
            items: [
              {
                title: t.nav.profile,
                url: '/settings/profile',
                icon: UserCog,
              },
              {
                title: t.nav.notifications,
                url: '/settings/notifications',
                icon: Bell,
              },
            ],
          },
          {
            title: t.nav.help,
            url: '/help',
            icon: HelpCircle,
          },
        ],
      },
    ],
  }
}
