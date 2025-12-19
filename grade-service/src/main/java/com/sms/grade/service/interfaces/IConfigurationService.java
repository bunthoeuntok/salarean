package com.sms.grade.service.interfaces;

import com.sms.grade.dto.GradeConfigRequest;
import com.sms.grade.dto.TeacherAssessmentConfigResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for teacher assessment configuration management.
 */
public interface IConfigurationService {

    /**
     * Create or update assessment configuration for a class/subject.
     */
    TeacherAssessmentConfigResponse saveConfig(GradeConfigRequest request);

    /**
     * Get configuration for a class/subject/semester.
     */
    TeacherAssessmentConfigResponse getConfig(UUID classId, UUID subjectId, Integer semester, String academicYear);

    /**
     * Get all configurations for a class/semester.
     */
    List<TeacherAssessmentConfigResponse> getClassConfigs(UUID classId, Integer semester, String academicYear);

    /**
     * Delete configuration for a class/subject.
     */
    void deleteConfig(UUID configId);

    /**
     * Check if configuration exists for class/subject.
     */
    boolean configExists(UUID classId, UUID subjectId, Integer semester, String academicYear);

    /**
     * Get default configuration when teacher hasn't customized.
     * Defaults: 4 monthly exams, 50% monthly weight, 50% semester weight
     */
    TeacherAssessmentConfigResponse getDefaultConfig(UUID classId, UUID subjectId, Integer semester, String academicYear);
}
