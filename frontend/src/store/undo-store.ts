import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { UndoState, UndoStore } from '@/types/transfer';

/**
 * Zustand store for managing undo transfer state.
 * Persists to sessionStorage with automatic expiration.
 */
export const useUndoStore = create<UndoStore>()(
  persist(
    (set, get) => ({
      undoState: null,

      /**
       * Set undo state after a successful transfer.
       * Calculates expiration timestamp (transferredAt + 5 minutes).
       */
      setUndoState: (state: UndoState) => {
        set({ undoState: state });
      },

      /**
       * Clear undo state after successful undo or expiration.
       */
      clearUndoState: () => {
        set({ undoState: null });
      },

      /**
       * Check if undo is currently available.
       * Returns false if:
       * - No undo state exists
       * - Undo window has expired (> 5 minutes)
       */
      isUndoAvailable: () => {
        const { undoState } = get();

        if (!undoState) {
          return false;
        }

        const now = Date.now();
        const isExpired = now > undoState.expiresAt;

        if (isExpired) {
          // Auto-clear expired undo state
          set({ undoState: null });
          return false;
        }

        return true;
      },
    }),
    {
      name: 'undo-transfer-storage', // sessionStorage key
      storage: {
        getItem: (name) => {
          const str = sessionStorage.getItem(name);
          return str ? JSON.parse(str) : null;
        },
        setItem: (name, value) => {
          sessionStorage.setItem(name, JSON.stringify(value));
        },
        removeItem: (name) => {
          sessionStorage.removeItem(name);
        },
      },
    }
  )
);
