package com.sms.grade.repository;

import com.sms.grade.enums.AssessmentCategory;
import com.sms.grade.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Grade entity.
 * Includes teacher-based data isolation on all queries.
 */
@Repository
public interface GradeRepository extends JpaRepository<Grade, UUID> {

    // =============================================
    // Teacher Isolation Queries
    // =============================================

    /**
     * Find grade by ID and teacher ID (teacher isolation).
     */
    Optional<Grade> findByIdAndTeacherId(UUID id, UUID teacherId);

    /**
     * Find all grades for a teacher.
     */
    List<Grade> findByTeacherId(UUID teacherId);

    // =============================================
    // Student Grade Queries
    // =============================================

    /**
     * Find all grades for a student in a semester.
     */
    List<Grade> findByTeacherIdAndStudentIdAndSemesterAndAcademicYear(
            UUID teacherId, UUID studentId, Integer semester, String academicYear);

    /**
     * Find all grades for a student in a specific subject.
     */
    List<Grade> findByTeacherIdAndStudentIdAndSubjectIdAndAcademicYear(
            UUID teacherId, UUID studentId, UUID subjectId, String academicYear);

    /**
     * Find all grades for a student in an academic year.
     */
    List<Grade> findByTeacherIdAndStudentIdAndAcademicYear(
            UUID teacherId, UUID studentId, String academicYear);

    // =============================================
    // Class Grade Queries
    // =============================================

    /**
     * Find all grades for a class in a semester.
     */
    List<Grade> findByTeacherIdAndClassIdAndSemesterAndAcademicYear(
            UUID teacherId, UUID classId, Integer semester, String academicYear);

    /**
     * Find all grades for a class and subject.
     */
    List<Grade> findByTeacherIdAndClassIdAndSubjectIdAndSemesterAndAcademicYear(
            UUID teacherId, UUID classId, UUID subjectId, Integer semester, String academicYear);

    /**
     * Find all grades for a class, subject, and assessment type.
     */
    List<Grade> findByTeacherIdAndClassIdAndSubjectIdAndAssessmentTypeIdAndSemesterAndAcademicYear(
            UUID teacherId, UUID classId, UUID subjectId, UUID assessmentTypeId, Integer semester, String academicYear);

    // =============================================
    // Monthly Exam Queries
    // =============================================

    /**
     * Find monthly exam grades for a class and subject.
     */
    @Query("SELECT g FROM Grade g WHERE g.teacherId = :teacherId AND g.classId = :classId " +
            "AND g.subject.id = :subjectId AND g.assessmentType.category = :category " +
            "AND g.semester = :semester AND g.academicYear = :academicYear " +
            "ORDER BY g.studentId, g.assessmentType.displayOrder")
    List<Grade> findByClassAndSubjectAndCategory(
            @Param("teacherId") UUID teacherId,
            @Param("classId") UUID classId,
            @Param("subjectId") UUID subjectId,
            @Param("category") AssessmentCategory category,
            @Param("semester") Integer semester,
            @Param("academicYear") String academicYear);

    /**
     * Find monthly exam grades for a student and subject.
     */
    @Query("SELECT g FROM Grade g WHERE g.teacherId = :teacherId AND g.studentId = :studentId " +
            "AND g.subject.id = :subjectId AND g.assessmentType.category = 'MONTHLY_EXAM' " +
            "AND g.semester = :semester AND g.academicYear = :academicYear " +
            "ORDER BY g.assessmentType.displayOrder")
    List<Grade> findStudentMonthlyExams(
            @Param("teacherId") UUID teacherId,
            @Param("studentId") UUID studentId,
            @Param("subjectId") UUID subjectId,
            @Param("semester") Integer semester,
            @Param("academicYear") String academicYear);

    // =============================================
    // Semester Exam Queries
    // =============================================

    /**
     * Find semester exam grade for a student and subject.
     */
    @Query("SELECT g FROM Grade g WHERE g.teacherId = :teacherId AND g.studentId = :studentId " +
            "AND g.subject.id = :subjectId AND g.assessmentType.category = 'SEMESTER_EXAM' " +
            "AND g.semester = :semester AND g.academicYear = :academicYear")
    Optional<Grade> findStudentSemesterExam(
            @Param("teacherId") UUID teacherId,
            @Param("studentId") UUID studentId,
            @Param("subjectId") UUID subjectId,
            @Param("semester") Integer semester,
            @Param("academicYear") String academicYear);

    // =============================================
    // Existence Checks
    // =============================================

    /**
     * Check if a grade entry exists.
     */
    boolean existsByStudentIdAndClassIdAndSubjectIdAndAssessmentTypeIdAndSemesterAndAcademicYear(
            UUID studentId, UUID classId, UUID subjectId, UUID assessmentTypeId, Integer semester, String academicYear);

    // =============================================
    // Delete Operations
    // =============================================

    /**
     * Delete grade by ID and teacher ID (teacher isolation).
     */
    void deleteByIdAndTeacherId(UUID id, UUID teacherId);

    /**
     * Delete all grades for a student in a class.
     */
    void deleteByTeacherIdAndStudentIdAndClassId(UUID teacherId, UUID studentId, UUID classId);
}
