import api, { apiRequest } from '@/lib/api'
import type { SchoolCreationFormData } from '@/lib/validations/school-setup'

/**
 * Province response from API
 */
export interface ProvinceResponse {
  id: string
  name: string
  nameKhmer?: string
  code?: string
}

/**
 * District response from API
 */
export interface DistrictResponse {
  id: string
  provinceId: string
  name: string
  nameKhmer?: string
  code?: string
}

/**
 * School response from API
 */
export interface SchoolResponse {
  id: string
  name: string
  nameKhmer?: string
  address: string
  provinceId?: string
  districtId?: string
  provinceName?: string
  districtName?: string
  type: 'PRIMARY' | 'SECONDARY' | 'HIGH_SCHOOL' | 'VOCATIONAL'
  createdAt: string
  updatedAt: string
}

/**
 * Location service for province, district, and school API calls
 */
export const locationService = {
  /**
   * Fetch all provinces
   */
  async getProvinces(): Promise<ProvinceResponse[]> {
    return apiRequest<ProvinceResponse[]>(
      api.get('/student-service/api/provinces')
    )
  },

  /**
   * Fetch districts for a specific province
   */
  async getDistricts(provinceId: string): Promise<DistrictResponse[]> {
    return apiRequest<DistrictResponse[]>(
      api.get('/student-service/api/districts', {
        params: { provinceId },
      })
    )
  },

  /**
   * Fetch schools for a specific district
   */
  async getSchools(districtId: string): Promise<SchoolResponse[]> {
    return apiRequest<SchoolResponse[]>(
      api.get('/student-service/api/schools', {
        params: { districtId },
      })
    )
  },

  /**
   * Create a new school
   */
  async createSchool(data: SchoolCreationFormData): Promise<SchoolResponse> {
    return apiRequest<SchoolResponse>(
      api.post('/student-service/api/schools', data)
    )
  },
}

// Named exports for backward compatibility
export const fetchProvinces = locationService.getProvinces
export const fetchDistricts = locationService.getDistricts
export const fetchSchools = locationService.getSchools
export const createSchool = locationService.createSchool
