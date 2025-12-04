import { useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { classService } from '@/services/class.service'
import { useClassStore } from '@/store/class-store'

/**
 * Global hook to fetch and maintain all classes in the class store.
 * This ensures the class store is populated regardless of which page loads first.
 *
 * Usage: Call this hook in pages that need access to all classes
 * for operations like batch transfer eligibility.
 */
export function useClasses() {
  const { setClasses, getClassById, getEligibleDestinations } = useClassStore()

  // Fetch all classes (with a large page size to get all records)
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['classes', 'all'],
    queryFn: () =>
      classService.getClasses({
        page: 0,
        size: 1000, // Fetch all classes for global access
      }),
    staleTime: 5 * 60 * 1000, // Consider data fresh for 5 minutes
    gcTime: 10 * 60 * 1000, // Keep in cache for 10 minutes
  })

  // Update class store whenever data changes
  useEffect(() => {
    if (data?.content) {
      setClasses(data.content)
    }
  }, [data?.content, setClasses])

  return {
    classes: data?.content ?? [],
    isLoading,
    error,
    refetch,
    getClassById,
    getEligibleDestinations,
  }
}
