import { createFileRoute, redirect, isRedirect, Outlet } from '@tanstack/react-router'
import { useAuthStore } from '@/store/auth-store'
import { AuthenticatedLayout } from '@/components/layout/authenticated-layout'
import { teacherSchoolQueryOptions } from '@/services/school.service'

// Pages that require school setup to be completed
const SCHOOL_SETUP_REQUIRED_PATHS = ['/students', '/classes']

export const Route = createFileRoute('/_authenticated')({
  beforeLoad: async ({ location, context }) => {
    const { accessToken } = useAuthStore.getState()

    // Check authentication
    if (!accessToken) {
      throw redirect({
        to: '/sign-in',
        search: { redirect: location.pathname },
      })
    }

    // Only check school setup for specific pages (students, classes)
    const requiresSchoolSetup = SCHOOL_SETUP_REQUIRED_PATHS.some(
      (path) => location.pathname === path || location.pathname.startsWith(`${path}/`)
    )

    if (!requiresSchoolSetup) {
      return
    }

    // Check if teacher has completed school setup using TanStack Query cache
    try {
      const teacherSchool = await context.queryClient.ensureQueryData(
        teacherSchoolQueryOptions
      )

      if (!teacherSchool) {
        // No school association - redirect to school setup with reason
        throw redirect({
          to: '/settings/school-setup',
          search: { reason: 'required' },
        })
      }
    } catch (error) {
      // Re-throw redirect errors (they should be handled by the router)
      if (isRedirect(error)) {
        throw error
      }
      // Log other errors but allow access (server error shouldn't block user)
      console.error('Failed to check teacher-school association:', error)
    }
  },
  component: () => (
    <AuthenticatedLayout>
      <Outlet />
    </AuthenticatedLayout>
  ),
})
