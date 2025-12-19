package com.sms.grade.repository;

import com.sms.grade.enums.AverageType;
import com.sms.grade.model.GradeAverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for GradeAverage entity.
 * Includes teacher-based data isolation on all queries.
 */
@Repository
public interface GradeAverageRepository extends JpaRepository<GradeAverage, UUID> {

    // =============================================
    // Teacher Isolation Queries
    // =============================================

    /**
     * Find average by ID and teacher ID (teacher isolation).
     */
    Optional<GradeAverage> findByIdAndTeacherId(UUID id, UUID teacherId);

    // =============================================
    // Student Average Queries
    // =============================================

    /**
     * Find all averages for a student in an academic year.
     */
    List<GradeAverage> findByTeacherIdAndStudentIdAndAcademicYear(
            UUID teacherId, UUID studentId, String academicYear);

    /**
     * Find semester averages for a student.
     */
    List<GradeAverage> findByTeacherIdAndStudentIdAndSemesterAndAcademicYear(
            UUID teacherId, UUID studentId, Integer semester, String academicYear);

    /**
     * Find specific average for a student/subject/semester.
     */
    Optional<GradeAverage> findByTeacherIdAndStudentIdAndSubjectIdAndSemesterAndAcademicYearAndAverageType(
            UUID teacherId, UUID studentId, UUID subjectId, Integer semester, String academicYear, AverageType averageType);

    /**
     * Find overall semester average for a student.
     */
    Optional<GradeAverage> findByTeacherIdAndStudentIdAndSemesterAndAcademicYearAndAverageType(
            UUID teacherId, UUID studentId, Integer semester, String academicYear, AverageType averageType);

    /**
     * Find overall annual average for a student.
     */
    @Query("SELECT ga FROM GradeAverage ga WHERE ga.teacherId = :teacherId AND ga.studentId = :studentId " +
            "AND ga.academicYear = :academicYear AND ga.averageType = :averageType AND ga.semester IS NULL")
    Optional<GradeAverage> findAnnualAverage(
            @Param("teacherId") UUID teacherId,
            @Param("studentId") UUID studentId,
            @Param("academicYear") String academicYear,
            @Param("averageType") AverageType averageType);

    // =============================================
    // Class Ranking Queries
    // =============================================

    /**
     * Get class rankings for a semester (ordered by score descending).
     */
    @Query("SELECT ga FROM GradeAverage ga WHERE ga.teacherId = :teacherId AND ga.classId = :classId " +
            "AND ga.semester = :semester AND ga.academicYear = :academicYear " +
            "AND ga.averageType = :averageType AND ga.subject IS NULL " +
            "ORDER BY ga.averageScore DESC")
    List<GradeAverage> findClassRankings(
            @Param("teacherId") UUID teacherId,
            @Param("classId") UUID classId,
            @Param("semester") Integer semester,
            @Param("academicYear") String academicYear,
            @Param("averageType") AverageType averageType);

    /**
     * Get subject rankings for a class (ordered by score descending).
     */
    @Query("SELECT ga FROM GradeAverage ga WHERE ga.teacherId = :teacherId AND ga.classId = :classId " +
            "AND ga.subject.id = :subjectId AND ga.semester = :semester AND ga.academicYear = :academicYear " +
            "AND ga.averageType = :averageType " +
            "ORDER BY ga.averageScore DESC")
    List<GradeAverage> findSubjectRankings(
            @Param("teacherId") UUID teacherId,
            @Param("classId") UUID classId,
            @Param("subjectId") UUID subjectId,
            @Param("semester") Integer semester,
            @Param("academicYear") String academicYear,
            @Param("averageType") AverageType averageType);

    // =============================================
    // Delete Operations
    // =============================================

    /**
     * Delete all averages for a student in a class.
     */
    void deleteByTeacherIdAndStudentIdAndClassId(UUID teacherId, UUID studentId, UUID classId);

    /**
     * Delete all averages for a class/semester.
     */
    void deleteByTeacherIdAndClassIdAndSemesterAndAcademicYear(
            UUID teacherId, UUID classId, Integer semester, String academicYear);
}
