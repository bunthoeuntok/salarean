import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { scheduleService } from '@/services/schedule.service'
import type {
  ClassShift,
  CreateClassScheduleRequest,
  CreateTimeSlotTemplateRequest,
  ScheduleEntryDto,
  UpdateScheduleEntriesRequest,
} from '@/types/schedule.types'
import { useToast } from '@/hooks/use-toast'
import { useLanguage } from '@/context/language-provider'

/**
 * Hook for managing time slot templates
 */
export function useTimeSlotTemplates(shift?: ClassShift) {
  const queryKey = shift
    ? ['schedule-templates', shift]
    : ['schedule-templates']

  return useQuery({
    queryKey,
    queryFn: () => shift
      ? scheduleService.getTemplatesByShift(shift)
      : scheduleService.getTemplates(),
    staleTime: 10 * 60 * 1000, // 10 minutes - templates rarely change
  })
}

/**
 * Hook for getting a single template
 */
export function useTimeSlotTemplate(templateId: string | null) {
  return useQuery({
    queryKey: ['schedule-template', templateId],
    queryFn: () => scheduleService.getTemplate(templateId!),
    enabled: !!templateId,
  })
}

/**
 * Hook for creating custom templates
 */
export function useCreateTemplate() {
  const queryClient = useQueryClient()
  const { toast } = useToast()
  const { t } = useLanguage()

  return useMutation({
    mutationFn: (data: CreateTimeSlotTemplateRequest) => scheduleService.createTemplate(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['schedule-templates'] })
      toast({
        title: t.common?.success || 'Success',
        description: 'Template created successfully',
      })
    },
    onError: () => {
      toast({
        variant: 'destructive',
        title: t.common?.error || 'Error',
        description: 'Failed to create template',
      })
    },
  })
}

/**
 * Hook for deleting templates
 */
export function useDeleteTemplate() {
  const queryClient = useQueryClient()
  const { toast } = useToast()
  const { t } = useLanguage()

  return useMutation({
    mutationFn: (templateId: string) => scheduleService.deleteTemplate(templateId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['schedule-templates'] })
      toast({
        title: t.common?.success || 'Success',
        description: 'Template deleted successfully',
      })
    },
    onError: () => {
      toast({
        variant: 'destructive',
        title: t.common?.error || 'Error',
        description: 'Failed to delete template',
      })
    },
  })
}

/**
 * Hook for getting class schedule
 */
export function useClassSchedule(classId: string | null) {
  return useQuery({
    queryKey: ['class-schedule', classId],
    queryFn: () => scheduleService.getClassSchedule(classId!),
    enabled: !!classId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

/**
 * Hook for creating class schedule
 */
export function useCreateClassSchedule() {
  const queryClient = useQueryClient()
  const { toast } = useToast()
  const { t } = useLanguage()

  return useMutation({
    mutationFn: (data: CreateClassScheduleRequest) => scheduleService.createClassSchedule(data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['class-schedule', variables.classId] })
      toast({
        title: t.common?.success || 'Success',
        description: 'Schedule created successfully',
      })
    },
    onError: () => {
      toast({
        variant: 'destructive',
        title: t.common?.error || 'Error',
        description: 'Failed to create schedule',
      })
    },
  })
}

/**
 * Hook for updating schedule entries
 */
export function useUpdateScheduleEntries(classId: string) {
  const queryClient = useQueryClient()
  const { toast } = useToast()
  const { t } = useLanguage()

  return useMutation({
    mutationFn: (data: UpdateScheduleEntriesRequest) =>
      scheduleService.updateScheduleEntries(classId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['class-schedule', classId] })
      toast({
        title: t.common?.success || 'Success',
        description: 'Schedule updated successfully',
      })
    },
    onError: () => {
      toast({
        variant: 'destructive',
        title: t.common?.error || 'Error',
        description: 'Failed to update schedule',
      })
    },
  })
}

/**
 * Hook for adding/updating a single schedule entry
 */
export function useUpdateScheduleEntry(classId: string) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (entry: ScheduleEntryDto) =>
      scheduleService.updateScheduleEntries(classId, {
        entries: [entry],
        clearExisting: false,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['class-schedule', classId] })
    },
  })
}

/**
 * Hook for deleting class schedule
 */
export function useDeleteClassSchedule(classId: string) {
  const queryClient = useQueryClient()
  const { toast } = useToast()
  const { t } = useLanguage()

  return useMutation({
    mutationFn: () => scheduleService.deleteClassSchedule(classId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['class-schedule', classId] })
      toast({
        title: t.common?.success || 'Success',
        description: 'Schedule deleted successfully',
      })
    },
    onError: () => {
      toast({
        variant: 'destructive',
        title: t.common?.error || 'Error',
        description: 'Failed to delete schedule',
      })
    },
  })
}

/**
 * Hook for clearing schedule entries
 */
export function useClearScheduleEntries(classId: string) {
  const queryClient = useQueryClient()
  const { toast } = useToast()
  const { t } = useLanguage()

  return useMutation({
    mutationFn: () => scheduleService.clearScheduleEntries(classId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['class-schedule', classId] })
      toast({
        title: t.common?.success || 'Success',
        description: 'Schedule cleared successfully',
      })
    },
    onError: () => {
      toast({
        variant: 'destructive',
        title: t.common?.error || 'Error',
        description: 'Failed to clear schedule',
      })
    },
  })
}

/**
 * Hook for copying schedule from another class
 */
export function useCopySchedule(targetClassId: string) {
  const queryClient = useQueryClient()
  const { toast } = useToast()
  const { t } = useLanguage()

  return useMutation({
    mutationFn: (sourceClassId: string) =>
      scheduleService.copySchedule(targetClassId, sourceClassId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['class-schedule', targetClassId] })
      toast({
        title: t.common?.success || 'Success',
        description: 'Schedule copied successfully',
      })
    },
    onError: () => {
      toast({
        variant: 'destructive',
        title: t.common?.error || 'Error',
        description: 'Failed to copy schedule',
      })
    },
  })
}
