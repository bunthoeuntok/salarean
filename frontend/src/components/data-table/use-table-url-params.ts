import { useCallback, useMemo } from 'react'
import { useNavigate, useSearch } from '@tanstack/react-router'

export interface TableUrlParams {
  page?: number
  size?: number
  search?: string
  sort?: string
  sortDir?: 'asc' | 'desc'
  filters?: Record<string, string[]>
}

export interface UseTableUrlParamsOptions {
  defaultPageSize?: number
}

/**
 * Hook to manage table state via URL search params
 * Enables page refresh persistence and shareable links
 */
export function useTableUrlParams(options: UseTableUrlParamsOptions = {}) {
  const { defaultPageSize = 10 } = options
  const navigate = useNavigate()

  // Get current search params from URL
  const searchParams = useSearch({ strict: false }) as TableUrlParams

  // Parse current state from URL
  const state = useMemo(() => {
    const page = searchParams.page ?? 0
    const size = searchParams.size ?? defaultPageSize
    const search = searchParams.search ?? ''
    const sort = searchParams.sort
    const sortDir = searchParams.sortDir ?? 'asc'
    const filters = searchParams.filters ?? {}

    return {
      pageIndex: page,
      pageSize: size,
      searchValue: search,
      sorting: sort ? [{ id: sort, desc: sortDir === 'desc' }] : [],
      filters,
    }
  }, [searchParams, defaultPageSize])

  // Helper to clean up params
  const cleanParams = useCallback(
    (params: TableUrlParams): TableUrlParams => {
      const cleaned: TableUrlParams = { ...params }

      // Remove default/empty values
      if (cleaned.page === 0) delete cleaned.page
      if (cleaned.size === defaultPageSize) delete cleaned.size
      if (!cleaned.search) delete cleaned.search
      if (!cleaned.sort) {
        delete cleaned.sort
        delete cleaned.sortDir
      }
      if (!cleaned.filters || Object.keys(cleaned.filters).length === 0) {
        delete cleaned.filters
      } else {
        // Clean up empty filter arrays
        const cleanFilters: Record<string, string[]> = {}
        for (const [key, values] of Object.entries(cleaned.filters)) {
          if (values && values.length > 0) {
            cleanFilters[key] = values
          }
        }
        if (Object.keys(cleanFilters).length === 0) {
          delete cleaned.filters
        } else {
          cleaned.filters = cleanFilters
        }
      }

      return cleaned
    },
    [defaultPageSize]
  )

  // Update URL params
  const updateParams = useCallback(
    (updates: Partial<TableUrlParams>) => {
      const currentParams = searchParams as TableUrlParams
      const newParams = cleanParams({ ...currentParams, ...updates })

      navigate({
        to: '.',
        search: newParams as Record<string, unknown>,
        replace: true,
      })
    },
    [navigate, searchParams, cleanParams]
  )

  // Convenience methods
  const setPage = useCallback(
    (pageIndex: number) => updateParams({ page: pageIndex }),
    [updateParams]
  )

  const setPageSize = useCallback(
    (size: number) => updateParams({ size, page: 0 }),
    [updateParams]
  )

  const setSorting = useCallback(
    (sorting: { id: string; desc: boolean }[]) => {
      if (sorting.length > 0) {
        updateParams({
          sort: sorting[0].id,
          sortDir: sorting[0].desc ? 'desc' : 'asc',
          page: 0,
        })
      } else {
        updateParams({ sort: undefined, sortDir: undefined, page: 0 })
      }
    },
    [updateParams]
  )

  // Submit all filters at once (search + column filters)
  const submitFilters = useCallback(
    (params: { search?: string; filters?: Record<string, string[]> }) => {
      updateParams({
        search: params.search,
        filters: params.filters,
        page: 0, // Reset to first page when filters change
      })
    },
    [updateParams]
  )

  // Reset all filters
  const resetFilters = useCallback(() => {
    updateParams({
      search: undefined,
      filters: undefined,
      page: 0,
    })
  }, [updateParams])

  // Reset everything
  const resetAll = useCallback(() => {
    navigate({
      to: '.',
      search: {},
      replace: true,
    })
  }, [navigate])

  return {
    ...state,
    setPage,
    setPageSize,
    setSorting,
    submitFilters,
    resetFilters,
    resetAll,
    updateParams,
  }
}
