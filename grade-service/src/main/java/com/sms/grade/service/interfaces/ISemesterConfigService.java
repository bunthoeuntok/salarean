package com.sms.grade.service.interfaces;

import com.sms.grade.dto.SemesterConfigRequest;
import com.sms.grade.dto.SemesterConfigResponse;

import java.util.List;

/**
 * Service interface for semester configuration management.
 * Handles both default (admin) and teacher-specific configurations.
 */
public interface ISemesterConfigService {

    // =============================================
    // Teacher Configuration (with fallback to default)
    // =============================================

    /**
     * Get semester configuration for a teacher.
     * Falls back to default config if teacher has no custom config.
     *
     * @param academicYear     The academic year (e.g., "2024-2025")
     * @param semesterExamCode The semester exam code (e.g., "SEMESTER_1", "SEMESTER_2")
     * @return The configuration (teacher-specific or default)
     */
    SemesterConfigResponse getConfig(String academicYear, String semesterExamCode);

    /**
     * Get all semester configurations for a teacher for an academic year.
     * Falls back to defaults for semesters without custom config.
     *
     * @param academicYear The academic year
     * @return List of configurations for all semesters
     */
    List<SemesterConfigResponse> getConfigsByAcademicYear(String academicYear);

    /**
     * Save or update teacher-specific configuration.
     *
     * @param request The configuration request
     * @return The saved configuration
     */
    SemesterConfigResponse saveTeacherConfig(SemesterConfigRequest request);

    /**
     * Delete teacher-specific configuration.
     * Teacher will fall back to default config after deletion.
     *
     * @param academicYear     The academic year
     * @param semesterExamCode The semester exam code
     */
    void deleteTeacherConfig(String academicYear, String semesterExamCode);

    // =============================================
    // Admin Operations (Default Configurations)
    // =============================================

    /**
     * Get all default configurations (admin only).
     *
     * @return List of all default configurations
     */
    List<SemesterConfigResponse> getAllDefaultConfigs();

    /**
     * Get default configurations for a specific academic year.
     *
     * @param academicYear The academic year
     * @return List of default configurations
     */
    List<SemesterConfigResponse> getDefaultConfigsByAcademicYear(String academicYear);

    /**
     * Create or update a default configuration (admin only).
     *
     * @param request The configuration request
     * @return The saved configuration
     */
    SemesterConfigResponse saveDefaultConfig(SemesterConfigRequest request);

    /**
     * Delete a default configuration (admin only).
     *
     * @param academicYear     The academic year
     * @param semesterExamCode The semester exam code
     */
    void deleteDefaultConfig(String academicYear, String semesterExamCode);

    /**
     * Get list of academic years that have default configs.
     *
     * @return List of academic years
     */
    List<String> getAvailableAcademicYears();

    /**
     * Check if default config exists for an academic year and semester exam code.
     *
     * @param academicYear     The academic year
     * @param semesterExamCode The semester exam code
     * @return true if exists
     */
    boolean defaultConfigExists(String academicYear, String semesterExamCode);
}
