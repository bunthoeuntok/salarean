import { useState, useEffect, useCallback } from 'react'
import type { VisibilityState, ColumnSizingState } from '@tanstack/react-table'

interface TableState {
  columnOrder: string[]
  columnVisibility: VisibilityState
  columnSizing: ColumnSizingState
}

const STORAGE_PREFIX = 'data-table-state:'

/**
 * Hook to persist and restore table state from localStorage
 */
export function useTableStateStorage(
  storageKey: string | undefined,
  defaultColumnOrder: string[]
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

  // Save state to localStorage whenever it changes
  useEffect(() => {
    if (!fullKey) return

    const state: TableState = {
      columnOrder,
      columnVisibility,
      columnSizing,
    }

    try {
      localStorage.setItem(fullKey, JSON.stringify(state))
    } catch (error) {
      console.warn('Failed to save table state to localStorage:', error)
    }
  }, [fullKey, columnOrder, columnVisibility, columnSizing])

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

  // Reset state to defaults
  const resetState = useCallback(() => {
    setColumnOrderState(defaultColumnOrder)
    setColumnVisibilityState({})
    setColumnSizingState({})
    if (fullKey) {
      localStorage.removeItem(fullKey)
    }
  }, [defaultColumnOrder, fullKey])

  return {
    columnOrder,
    setColumnOrder,
    columnVisibility,
    setColumnVisibility,
    columnSizing,
    setColumnSizing,
    resetState,
  }
}
