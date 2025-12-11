import { z } from "zod";

/**
 * Validation schema for teacher-school association
 */
export const teacherSchoolSchema = z.object({
  schoolId: z.string().uuid("Invalid school ID"),
  principalName: z
    .string()
    .min(1, "Principal name is required")
    .max(255, "Principal name must not exceed 255 characters"),
  principalGender: z.enum(["M", "F"], {
    required_error: "Principal gender is required",
    invalid_type_error: "Gender must be M or F",
  }),
});

export type TeacherSchoolFormData = z.infer<typeof teacherSchoolSchema>;

/**
 * Validation schema for school creation (for User Story 2)
 */
export const schoolCreationSchema = z.object({
  name: z
    .string()
    .min(1, "School name is required")
    .max(255, "School name must not exceed 255 characters"),
  nameKhmer: z
    .string()
    .max(255, "Khmer name must not exceed 255 characters")
    .optional(),
  address: z
    .string()
    .min(1, "Address is required")
    .max(500, "Address must not exceed 500 characters"),
  provinceId: z.string().uuid("Invalid province"),
  districtId: z.string().uuid("Invalid district"),
  type: z.enum(["PRIMARY", "SECONDARY", "HIGH_SCHOOL", "VOCATIONAL"], {
    required_error: "School type is required",
  }),
});

export type SchoolCreationFormData = z.infer<typeof schoolCreationSchema>;
