import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'

interface AcademicYearState {
  selectedAcademicYear: string
  availableYears: string[]
}

interface AcademicYearActions {
  setAcademicYear: (year: string) => void
  initializeYears: () => void
}

type AcademicYearStore = AcademicYearState & AcademicYearActions

const getCurrentAcademicYear = (): string => {
  const now = new Date()
  const currentYear = now.getFullYear()
  const currentMonth = now.getMonth() + 1 // 0-indexed

  // Academic year typically starts in August/September
  // If current month is Aug-Dec, academic year is currentYear-nextYear
  // If current month is Jan-Jul, academic year is previousYear-currentYear
  if (currentMonth >= 8) {
    return `${currentYear}-${currentYear + 1}`
  } else {
    return `${currentYear - 1}-${currentYear}`
  }
}

const generateAcademicYears = (): string[] => {
  const currentYear = new Date().getFullYear()
  const years: string[] = []

  // Generate 5 years: 2 past, current, 2 future
  for (let i = -2; i <= 2; i++) {
    const startYear = currentYear + i
    years.push(`${startYear}-${startYear + 1}`)
  }

  return years
}

export const useAcademicYearStore = create<AcademicYearStore>()(
  persist(
    (set) => ({
      // State
      selectedAcademicYear: getCurrentAcademicYear(),
      availableYears: generateAcademicYears(),

      // Actions
      setAcademicYear: (year) => {
        set({ selectedAcademicYear: year })
      },

      initializeYears: () => {
        set({ availableYears: generateAcademicYears() })
      },
    }),
    {
      name: 'academic-year-storage',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        selectedAcademicYear: state.selectedAcademicYear,
      }),
    }
  )
)
