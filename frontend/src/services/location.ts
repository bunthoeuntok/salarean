import { api } from "@/lib/api";
import type { ApiResponse } from "@/types/api";
import type { SchoolCreationFormData } from "@/lib/validations/school-setup";

/**
 * Province response from API
 */
export interface ProvinceResponse {
  id: string;
  name: string;
  nameKhmer?: string;
  code?: string;
}

/**
 * District response from API
 */
export interface DistrictResponse {
  id: string;
  provinceId: string;
  name: string;
  nameKhmer?: string;
  code?: string;
}

/**
 * School response from API
 */
export interface SchoolResponse {
  id: string;
  name: string;
  nameKhmer?: string;
  address: string;
  provinceId?: string;
  districtId?: string;
  provinceName?: string;
  districtName?: string;
  type: "PRIMARY" | "SECONDARY" | "HIGH_SCHOOL" | "VOCATIONAL";
  createdAt: string;
  updatedAt: string;
}

/**
 * Fetch all provinces
 */
export const fetchProvinces = async (): Promise<ProvinceResponse[]> => {
  const response = await api.get<ApiResponse<ProvinceResponse[]>>(
    "/student-service/api/provinces"
  );
  return response.data.data;
};

/**
 * Fetch districts for a specific province
 */
export const fetchDistricts = async (
  provinceId: string
): Promise<DistrictResponse[]> => {
  const response = await api.get<ApiResponse<DistrictResponse[]>>(
    "/student-service/api/districts",
    {
      params: { provinceId },
    }
  );
  return response.data.data;
};

/**
 * Fetch schools for a specific district
 */
export const fetchSchools = async (
  districtId: string
): Promise<SchoolResponse[]> => {
  const response = await api.get<ApiResponse<SchoolResponse[]>>(
    "/student-service/api/schools",
    {
      params: { districtId },
    }
  );
  return response.data.data;
};

/**
 * Create a new school
 */
export const createSchool = async (
  data: SchoolCreationFormData
): Promise<SchoolResponse> => {
  const response = await api.post<ApiResponse<SchoolResponse>>(
    "/student-service/api/schools",
    data
  );
  return response.data.data;
};
