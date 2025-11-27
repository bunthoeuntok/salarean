import { createFileRoute } from '@tanstack/react-router'
import { StudentsPage } from '@/features/students'

export const Route = createFileRoute('/_authenticated/students')({
  component: StudentsPage,
})
