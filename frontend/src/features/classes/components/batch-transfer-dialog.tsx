import { useMemo } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Loader2, ArrowRight } from 'lucide-react'
import { CountdownToast } from '@/components/ui/countdown-toast'

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogDescription,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Badge } from '@/components/ui/badge'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Separator } from '@/components/ui/separator'
import { useLanguage } from '@/context/language-provider'
import { classService } from '@/services/class.service'
import { useClasses } from '@/hooks/use-classes'
import type { StudentEnrollmentItem } from '@/types/class.types'

const baseTransferSchema = z.object({
  destinationClassId: z.string(),
})

type FormData = z.infer<typeof baseTransferSchema>

interface BatchTransferDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  sourceClassId: string
  selectedStudents: StudentEnrollmentItem[]
  onSuccess?: () => void
}

export function BatchTransferDialog({
  open,
  onOpenChange,
  sourceClassId,
  selectedStudents,
  onSuccess,
}: BatchTransferDialogProps) {
  const { t, translateError } = useLanguage()
  const queryClient = useQueryClient()
  const { getEligibleDestinations } = useClasses()

  // Create schema with translated messages
  const transferSchema = useMemo(() => {
    return z.object({
      destinationClassId: z.string().min(1, t.validation.required),
    })
  }, [t])

  // Get eligible destination classes from global hook
  const eligibleClasses = useMemo(() => {
    if (!open || !sourceClassId) return []
    return getEligibleDestinations(sourceClassId)
  }, [open, sourceClassId, getEligibleDestinations])

  const form = useForm<FormData>({
    resolver: zodResolver(transferSchema),
    defaultValues: {
      destinationClassId: '',
    },
  })

  // Batch transfer mutation
  const transferMutation = useMutation({
    mutationFn: (destinationClassId: string) =>
      classService.batchTransfer(sourceClassId, {
        destinationClassId,
        studentIds: selectedStudents.map((s) => s.studentId),
      }),
    onSuccess: (response) => {
      // Invalidate queries to refresh data
      queryClient.invalidateQueries({ queryKey: ['classStudents', sourceClassId] })
      queryClient.invalidateQueries({ queryKey: ['classStudents', response.destinationClassId] })
      queryClient.invalidateQueries({ queryKey: ['classes'] })

      // Store transfer info in session storage for undo capability (5 minute window)
      // Calculate expiration time from current client time (not server time)
      const expiresAt = new Date(Date.now() + 5 * 60 * 1000).toISOString()

      const undoInfo = {
        transferId: response.transferId,
        sourceClassId: response.sourceClassId,
        destinationClassId: response.destinationClassId,
        transferredAt: response.transferredAt,
        expiresAt,
        studentCount: response.successfulTransfers,
      }
      sessionStorage.setItem(`transfer_${response.transferId}`, JSON.stringify(undoInfo))

      // Show success toast with undo action and countdown
      if (response.failedTransfers.length === 0) {
        toast.custom(
          (toastId) => (
            <div className="w-full rounded-lg border bg-background p-4 shadow-lg">
              <CountdownToast
                message={
                  t.classes.transfer?.dialog?.successMessage?.replace(
                    '{count}',
                    response.successfulTransfers.toString()
                  ) || `Successfully transferred ${response.successfulTransfers} student(s)`
                }
                expiresAt={expiresAt}
                actionLabel={t.classes.transfer?.dialog?.undoButton || 'Undo'}
                onAction={() => {
                  handleUndo(response.transferId)
                  toast.dismiss(toastId)
                }}
                toastId={toastId}
              />
            </div>
          ),
          {
            duration: 300000, // 5 minutes
          }
        )
      } else {
        // Partial success - show warning with details
        const failureDetails = response.failedTransfers
          .map((f) => `• ${f.studentName}: ${translateError(f.reason as Parameters<typeof translateError>[0])}`)
          .join('\n')

        toast.warning(
          t.classes.transfer?.dialog?.partialSuccessMessage
            ?.replace('{successCount}', response.successfulTransfers.toString())
            ?.replace('{failCount}', response.failedTransfers.length.toString()) ||
          `Transferred ${response.successfulTransfers} student(s). ${response.failedTransfers.length} failed.`,
          {
            description: failureDetails,
            duration: 10000, // 10 seconds for user to read failure details
          }
        )
      }

      // Call success callback
      onSuccess?.()

      // Close dialog and reset form
      onOpenChange(false)
      form.reset()
    },
    onError: (error: Error) => {
      toast.error(translateError(error.message as Parameters<typeof translateError>[0]))
    },
  })

  // Undo mutation
  const undoMutation = useMutation({
    mutationFn: (transferId: string) => classService.undoTransfer(transferId),
    onSuccess: (response) => {
      // Invalidate queries to refresh data
      queryClient.invalidateQueries({ queryKey: ['classStudents', sourceClassId] })
      queryClient.invalidateQueries({ queryKey: ['classStudents', response.sourceClassId] })
      queryClient.invalidateQueries({ queryKey: ['classes'] })

      // Remove from session storage
      sessionStorage.removeItem(`transfer_${response.transferId}`)

      // Show success toast
      toast.success(
        t.classes.transfer?.dialog?.undoSuccess?.replace(
          '{count}',
          response.undoneStudents.toString()
        ) || `Successfully undone transfer - ${response.undoneStudents} student(s) returned`
      )
    },
    onError: (error: Error) => {
      toast.error(translateError(error.message as Parameters<typeof translateError>[0]))
    },
  })

  const handleUndo = (transferId: string) => {
    undoMutation.mutate(transferId)
  }

  const onSubmit = (data: FormData) => {
    transferMutation.mutate(data.destinationClassId)
  }

  const handleClose = () => {
    onOpenChange(false)
    form.reset()
  }

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-[600px] flex flex-col p-0 max-h-[90vh]">
        <DialogHeader className="shrink-0 bg-muted/50 border-b px-6 py-6">
          <DialogTitle>{t.classes.transfer?.dialog?.title}</DialogTitle>
          <DialogDescription>{t.classes.transfer?.dialog?.description}</DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit)}
            className="flex flex-col flex-1 overflow-hidden"
          >
            <div className="flex-1 overflow-y-auto px-6 py-4 space-y-6">
              {/* Selected Students Section */}
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <h3 className="text-sm font-medium">
                    {t.classes.transfer?.dialog?.selectedStudents}
                  </h3>
                  <Badge variant="secondary">
                    {selectedStudents.length} {t.classes.transfer?.dialog?.studentsSelected}
                  </Badge>
                </div>

                <ScrollArea className="h-[200px] w-full rounded-md border p-4">
                  <div className="space-y-2">
                    {selectedStudents.map((student, index) => (
                      <div key={student.studentId}>
                        <div className="flex items-center justify-between py-2">
                          <div className="flex flex-col">
                            <span className="text-sm font-medium">{student.fullName}</span>
                            <span className="text-xs text-muted-foreground">
                              {student.studentCode}
                            </span>
                          </div>
                          <Badge variant="outline" className="text-xs">
                            {student.enrollmentStatus}
                          </Badge>
                        </div>
                        {index < selectedStudents.length - 1 && <Separator />}
                      </div>
                    ))}
                  </div>
                </ScrollArea>
              </div>

              {/* Destination Class Selection */}
              <FormField
                control={form.control}
                name="destinationClassId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      {t.classes.transfer?.dialog?.destinationClass}{' '}
                      <span className="text-destructive">*</span>
                    </FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      value={field.value}
                    >
                      <FormControl>
                        <SelectTrigger className="w-full">
                          <SelectValue
                            placeholder={t.classes.transfer?.dialog?.destinationClassPlaceholder}
                          />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {eligibleClasses.map((cls) => (
                          <SelectItem key={cls.id} value={cls.id}>
                            <div className="flex items-center justify-between w-full gap-4">
                              <span className="font-medium">
                                Grade {cls.grade}{cls.section ? ` - ${cls.section}` : ''}
                              </span>
                              <div className="flex items-center gap-2 text-xs text-muted-foreground">
                                <span>
                                  {cls.studentCount}/{cls.maxCapacity}
                                </span>
                              </div>
                            </div>
                          </SelectItem>
                        ))}
                        {eligibleClasses.length === 0 && (
                          <div className="px-2 py-6 text-center text-sm text-muted-foreground">
                            {t.classes.transfer?.dialog?.noEligibleClasses}
                          </div>
                        )}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {/* Warning Message */}
              <div className="rounded-md bg-amber-50 dark:bg-amber-950/30 border border-amber-200 dark:border-amber-900 p-4">
                <p className="text-sm text-amber-800 dark:text-amber-200">
                  {t.classes.transfer?.dialog?.warning}
                </p>
              </div>
            </div>

            <DialogFooter className="shrink-0 gap-2 border-t bg-muted/50 px-6 py-4">
              <Button
                type="button"
                variant="outline"
                onClick={handleClose}
                disabled={transferMutation.isPending}
              >
                {t.common.cancel}
              </Button>
              <Button
                type="submit"
                disabled={
                  transferMutation.isPending ||
                  eligibleClasses.length === 0
                }
              >
                {transferMutation.isPending ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    {t.classes.transfer?.dialog?.transferring}
                  </>
                ) : (
                  <>
                    <ArrowRight className="mr-2 h-4 w-4" />
                    {t.classes.transfer?.dialog?.confirmButton}
                  </>
                )}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
