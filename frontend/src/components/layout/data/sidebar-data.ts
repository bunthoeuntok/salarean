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
import { type SidebarData } from '../types'

export const sidebarData: SidebarData = {
  user: {
    name: 'Teacher',
    email: 'teacher@salarean.com',
    avatar: undefined,
  },
  navGroups: [
    {
      title: 'Overview',
      items: [
        {
          title: 'Dashboard',
          url: '/dashboard',
          icon: LayoutDashboard,
        },
      ],
    },
    {
      title: 'Management',
      items: [
        {
          title: 'Students',
          url: '/students',
          icon: GraduationCap,
        },
        {
          title: 'Classes',
          url: '/classes',
          icon: Users,
        },
        {
          title: 'Attendance',
          url: '/attendance',
          icon: ClipboardList,
        },
        {
          title: 'Schedule',
          url: '/schedule',
          icon: Calendar,
        },
      ],
    },
    {
      title: 'Settings',
      items: [
        {
          title: 'Settings',
          icon: Settings,
          items: [
            {
              title: 'Profile',
              url: '/settings/profile',
              icon: UserCog,
            },
            {
              title: 'Notifications',
              url: '/settings/notifications',
              icon: Bell,
            },
          ],
        },
        {
          title: 'Help',
          url: '/help',
          icon: HelpCircle,
        },
      ],
    },
  ],
}
