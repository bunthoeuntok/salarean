import { create } from "zustand";

interface SchoolSetupState {
  // Selected IDs
  selectedProvinceId: string | null;
  selectedDistrictId: string | null;
  selectedSchoolId: string | null;

  // Actions
  setProvinceId: (id: string | null) => void;
  setDistrictId: (id: string | null) => void;
  setSchoolId: (id: string | null) => void;
  reset: () => void;
}

/**
 * Zustand store for school setup UI state
 * Manages the hierarchical selection flow: Province → District → School
 */
export const useSchoolSetupStore = create<SchoolSetupState>((set) => ({
  selectedProvinceId: null,
  selectedDistrictId: null,
  selectedSchoolId: null,

  setProvinceId: (id) =>
    set({
      selectedProvinceId: id,
      // Reset dependent selections when province changes
      selectedDistrictId: null,
      selectedSchoolId: null,
    }),

  setDistrictId: (id) =>
    set({
      selectedDistrictId: id,
      // Reset school selection when district changes
      selectedSchoolId: null,
    }),

  setSchoolId: (id) =>
    set({
      selectedSchoolId: id,
    }),

  reset: () =>
    set({
      selectedProvinceId: null,
      selectedDistrictId: null,
      selectedSchoolId: null,
    }),
}));
