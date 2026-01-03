package com.sms.grade.repository;

import com.sms.grade.model.SemesterConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SemesterConfig entity.
 * Provides methods to find configurations with fallback to defaults.
 */
@Repository
public interface SemesterConfigRepository extends JpaRepository<SemesterConfig, UUID> {

    /**
     * Find teacher-specific configuration.
     */
    Optional<SemesterConfig> findByTeacherIdAndAcademicYearAndSemesterExamCode(
            UUID teacherId, String academicYear, String semesterExamCode);

    /**
     * Find default (system) configuration where teacher_id is NULL.
     */
    @Query("SELECT sc FROM SemesterConfig sc WHERE sc.teacherId IS NULL " +
            "AND sc.academicYear = :academicYear AND sc.semesterExamCode = :semesterExamCode")
    Optional<SemesterConfig> findDefaultConfig(
            @Param("academicYear") String academicYear,
            @Param("semesterExamCode") String semesterExamCode);

    /**
     * Find all default configurations for an academic year.
     */
    @Query("SELECT sc FROM SemesterConfig sc WHERE sc.teacherId IS NULL " +
            "AND sc.academicYear = :academicYear ORDER BY sc.semesterExamCode")
    List<SemesterConfig> findDefaultConfigsByAcademicYear(@Param("academicYear") String academicYear);

    /**
     * Find all teacher-specific configurations.
     */
    List<SemesterConfig> findByTeacherIdAndAcademicYearOrderBySemesterExamCode(
            UUID teacherId, String academicYear);

    /**
     * Find all default configurations (for admin).
     */
    @Query("SELECT sc FROM SemesterConfig sc WHERE sc.teacherId IS NULL " +
            "ORDER BY sc.academicYear DESC, sc.semesterExamCode")
    List<SemesterConfig> findAllDefaultConfigs();

    /**
     * Check if default config exists for academic year and semester exam code.
     */
    @Query("SELECT COUNT(sc) > 0 FROM SemesterConfig sc WHERE sc.teacherId IS NULL " +
            "AND sc.academicYear = :academicYear AND sc.semesterExamCode = :semesterExamCode")
    boolean existsDefaultConfig(
            @Param("academicYear") String academicYear,
            @Param("semesterExamCode") String semesterExamCode);

    /**
     * Check if teacher config exists.
     */
    boolean existsByTeacherIdAndAcademicYearAndSemesterExamCode(
            UUID teacherId, String academicYear, String semesterExamCode);

    /**
     * Delete teacher-specific configuration.
     */
    void deleteByTeacherIdAndAcademicYearAndSemesterExamCode(
            UUID teacherId, String academicYear, String semesterExamCode);

    /**
     * Get distinct academic years that have default configs.
     */
    @Query("SELECT DISTINCT sc.academicYear FROM SemesterConfig sc " +
            "WHERE sc.teacherId IS NULL ORDER BY sc.academicYear DESC")
    List<String> findDistinctAcademicYears();

    /**
     * Get all semester exam codes for an academic year (default configs).
     */
    @Query("SELECT sc.semesterExamCode FROM SemesterConfig sc " +
            "WHERE sc.teacherId IS NULL AND sc.academicYear = :academicYear " +
            "ORDER BY sc.semesterExamCode")
    List<String> findSemesterExamCodesByAcademicYear(@Param("academicYear") String academicYear);
}
