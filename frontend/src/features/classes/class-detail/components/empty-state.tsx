import { Users } from 'lucide-react'

export function EmptyState() {
  return (
    <div className="flex flex-col items-center justify-center rounded-md border border-dashed p-12 text-center">
      <Users className="h-12 w-12 text-muted-foreground/50" />
      <h3 className="mt-4 text-lg font-semibold">No students enrolled</h3>
      <p className="mt-2 text-sm text-muted-foreground">
        This class doesn&apos;t have any students enrolled yet.
      </p>
    </div>
  )
}
