import { createFileRoute, redirect, Outlet } from '@tanstack/react-router'
import { useAuthStore } from '@/store/auth-store'
import { AuthenticatedLayout } from '@/components/layout/authenticated-layout'
import { fetchTeacherSchool } from '@/services/school'

export const Route = createFileRoute('/_authenticated')({
  beforeLoad: async ({ location }) => {
    const { accessToken } = useAuthStore.getState()

    // Check authentication
    if (!accessToken) {
      throw redirect({
        to: '/sign-in',
        search: { redirect: location.pathname },
      })
    }

    // Skip school setup check if already on school-setup page
    if (location.pathname === '/school-setup') {
      return
    }

    // Check if teacher has completed school setup
    try {
      const teacherSchool = await fetchTeacherSchool()

      if (!teacherSchool) {
        // No school association - redirect to school setup
        throw redirect({
          to: '/school-setup',
        })
      }
    } catch (error: any) {
      // If not a redirect error, allow access (server error shouldn't block user)
      if (error?.to !== '/school-setup') {
        console.error('Failed to check teacher-school association:', error)
      } else {
        throw error
      }
    }
  },
  component: () => (
    <AuthenticatedLayout>
      <Outlet />
    </AuthenticatedLayout>
  ),
})
