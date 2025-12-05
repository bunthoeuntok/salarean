import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type TextSize = 'small' | 'medium' | 'large'

interface DisplayStore {
  textSize: TextSize
  setTextSize: (size: TextSize) => void
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
        // Apply text size on page load
        if (state?.textSize) {
          document.documentElement.setAttribute('data-text-size', state.textSize)
        }
      },
    }
  )
)
