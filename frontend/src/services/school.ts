import { api } from "@/lib/api";
import type { ApiResponse } from "@/types/api";
import type { TeacherSchoolFormData } from "@/lib/validations/school-setup";

/**
 * Teacher-school association response from API
 */
export interface TeacherSchoolResponse {
  id: string;
  userId: string;
  schoolId: string;
  schoolName: string;
  principalName: string;
  principalGender: "M" | "F";
  createdAt: string;
  updatedAt: string;
}

/**
 * Create or update teacher-school association
 */
export const createTeacherSchool = async (
  data: TeacherSchoolFormData
): Promise<TeacherSchoolResponse> => {
  const response = await api.post<ApiResponse<TeacherSchoolResponse>>(
    "/auth-service/api/teacher-school",
    data
  );
  return response.data.data;
};

/**
 * Fetch teacher-school association for authenticated user
 * Returns null if no association exists (teacher hasn't completed setup)
 */
export const fetchTeacherSchool =
  async (): Promise<TeacherSchoolResponse | null> => {
    try {
      const response = await api.get<ApiResponse<TeacherSchoolResponse>>(
        "/auth-service/api/teacher-school"
      );
      return response.data.data;
    } catch (error: any) {
      // Return null if 404 (no association exists)
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  };
