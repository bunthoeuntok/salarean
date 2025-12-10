import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type TextSize = 'small' | 'medium' | 'large'

interface DisplayStore {
  textSize: TextSize
  setTextSize: (size: TextSize) => void
}

// Apply saved text size immediately on page load (before React hydrates)
const applyTextSizeFromStorage = () => {
  try {
    const stored = localStorage.getItem('sms-display-settings')
    if (stored) {
      const data = JSON.parse(stored)
      const textSize = data.state?.textSize || 'medium'
      document.documentElement.setAttribute('data-text-size', textSize)
    } else {
      // Apply default if no stored value
      document.documentElement.setAttribute('data-text-size', 'medium')
    }
  } catch (error) {
    // Apply default on error
    document.documentElement.setAttribute('data-text-size', 'medium')
  }
}

// Apply immediately when this module loads
if (typeof window !== 'undefined') {
  applyTextSizeFromStorage()
}

export const useDisplayStore = create<DisplayStore>()(
  persist(
    (set) => ({
      textSize: 'medium',
      setTextSize: (size) => {
        set({ textSize: size })
        // Apply text size to document root
        document.documentElement.setAttribute('data-text-size', size)
      },
    }),
    {
      name: 'sms-display-settings',
      onRehydrateStorage: () => (state) => {
        // Apply text size on page load (backup)
        if (state?.textSize) {
          document.documentElement.setAttribute('data-text-size', state.textSize)
        }
      },
    }
  )
)
