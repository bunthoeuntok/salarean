import { useState, useEffect, useCallback } from 'react'
import type { VisibilityState, ColumnSizingState } from '@tanstack/react-table'

interface TableState {
  columnOrder: string[]
  columnVisibility: VisibilityState
  columnSizing: ColumnSizingState
  pageSize: number
}

const STORAGE_PREFIX = 'data-table-state:'
const DEFAULT_PAGE_SIZE = 10

/**
 * Get stored page size for a table (for initializing parent component state)
 */
export function getStoredPageSize(storageKey: string, defaultSize: number = DEFAULT_PAGE_SIZE): number {
  try {
    const stored = localStorage.getItem(`${STORAGE_PREFIX}${storageKey}`)
    if (stored) {
      const state = JSON.parse(stored)
      return state.pageSize ?? defaultSize
    }
  } catch {
    // Ignore errors
  }
  return defaultSize
}

/**
 * Hook to persist and restore table state from localStorage
 */
export function useTableStateStorage(
  storageKey: string | undefined,
  defaultColumnOrder: string[],
  defaultPageSize: number = DEFAULT_PAGE_SIZE
) {
  const fullKey = storageKey ? `${STORAGE_PREFIX}${storageKey}` : null

  // Load initial state from localStorage
  const loadState = useCallback((): Partial<TableState> | null => {
    if (!fullKey) return null
    try {
      const stored = localStorage.getItem(fullKey)
      if (stored) {
        return JSON.parse(stored)
      }
    } catch (error) {
      console.warn('Failed to load table state from localStorage:', error)
    }
    return null
  }, [fullKey])

  // Initialize state with stored values or defaults
  const storedState = loadState()

  const [columnOrder, setColumnOrderState] = useState<string[]>(
    storedState?.columnOrder ?? defaultColumnOrder
  )
  const [columnVisibility, setColumnVisibilityState] = useState<VisibilityState>(
    storedState?.columnVisibility ?? {}
  )
  const [columnSizing, setColumnSizingState] = useState<ColumnSizingState>(
    storedState?.columnSizing ?? {}
  )
  const [pageSize, setPageSizeState] = useState<number>(
    storedState?.pageSize ?? defaultPageSize
  )

  // Save state to localStorage whenever it changes
  useEffect(() => {
    if (!fullKey) return

    const state: TableState = {
      columnOrder,
      columnVisibility,
      columnSizing,
      pageSize,
    }

    try {
      localStorage.setItem(fullKey, JSON.stringify(state))
    } catch (error) {
      console.warn('Failed to save table state to localStorage:', error)
    }
  }, [fullKey, columnOrder, columnVisibility, columnSizing, pageSize])

  // Update column order and sync with storage
  const setColumnOrder = useCallback((updater: string[] | ((prev: string[]) => string[])) => {
    setColumnOrderState(updater)
  }, [])

  // Update column visibility and sync with storage
  const setColumnVisibility = useCallback(
    (updater: VisibilityState | ((prev: VisibilityState) => VisibilityState)) => {
      setColumnVisibilityState(updater)
    },
    []
  )

  // Update column sizing and sync with storage
  const setColumnSizing = useCallback(
    (updater: ColumnSizingState | ((prev: ColumnSizingState) => ColumnSizingState)) => {
      setColumnSizingState(updater)
    },
    []
  )

  // Update page size and sync with storage
  const setPageSize = useCallback((size: number) => {
    setPageSizeState(size)
  }, [])

  // Reset state to defaults
  const resetState = useCallback(() => {
    setColumnOrderState(defaultColumnOrder)
    setColumnVisibilityState({})
    setColumnSizingState({})
    setPageSizeState(defaultPageSize)
    if (fullKey) {
      localStorage.removeItem(fullKey)
    }
  }, [defaultColumnOrder, defaultPageSize, fullKey])

  return {
    columnOrder,
    setColumnOrder,
    columnVisibility,
    setColumnVisibility,
    columnSizing,
    setColumnSizing,
    pageSize,
    setPageSize,
    resetState,
  }
}
