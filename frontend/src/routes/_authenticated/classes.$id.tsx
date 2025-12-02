import { createFileRoute } from '@tanstack/react-router'
import { ClassDetailPage } from '@/features/classes/class-detail'

export const Route = createFileRoute('/_authenticated/classes/$id')({
  component: ClassDetailPage,
})
