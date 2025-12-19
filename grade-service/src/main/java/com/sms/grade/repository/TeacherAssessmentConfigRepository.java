package com.sms.grade.repository;

import com.sms.grade.model.TeacherAssessmentConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for TeacherAssessmentConfig entity.
 * Includes teacher-based data isolation.
 */
@Repository
public interface TeacherAssessmentConfigRepository extends JpaRepository<TeacherAssessmentConfig, UUID> {

    /**
     * Find config by ID and teacher ID (teacher isolation).
     */
    Optional<TeacherAssessmentConfig> findByIdAndTeacherId(UUID id, UUID teacherId);

    /**
     * Find all configs for a teacher.
     */
    List<TeacherAssessmentConfig> findByTeacherId(UUID teacherId);

    /**
     * Find config for specific class/subject/semester.
     */
    Optional<TeacherAssessmentConfig> findByTeacherIdAndClassIdAndSubjectIdAndSemesterAndAcademicYear(
            UUID teacherId, UUID classId, UUID subjectId, Integer semester, String academicYear);

    /**
     * Find all configs for a class.
     */
    List<TeacherAssessmentConfig> findByTeacherIdAndClassIdAndSemesterAndAcademicYear(
            UUID teacherId, UUID classId, Integer semester, String academicYear);

    /**
     * Check if config exists for class/subject/semester.
     */
    boolean existsByTeacherIdAndClassIdAndSubjectIdAndSemesterAndAcademicYear(
            UUID teacherId, UUID classId, UUID subjectId, Integer semester, String academicYear);

    /**
     * Delete all configs for a class.
     */
    void deleteByTeacherIdAndClassIdAndSemesterAndAcademicYear(
            UUID teacherId, UUID classId, Integer semester, String academicYear);
}
