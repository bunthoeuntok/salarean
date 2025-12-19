package com.sms.grade.service.interfaces;

import com.sms.grade.dto.*;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for grade CRUD operations and queries.
 */
public interface IGradeService {

    // =============================================
    // CRUD Operations
    // =============================================

    /**
     * Create a new grade entry.
     */
    GradeResponse createGrade(GradeRequest request);

    /**
     * Create multiple grades in bulk.
     */
    List<GradeResponse> createBulkGrades(BulkGradeRequest request);

    /**
     * Update an existing grade.
     */
    GradeResponse updateGrade(UUID gradeId, GradeRequest request);

    /**
     * Delete a grade.
     */
    void deleteGrade(UUID gradeId);

    /**
     * Get a grade by ID.
     */
    GradeResponse getGrade(UUID gradeId);

    // =============================================
    // Monthly Exam Operations
    // =============================================

    /**
     * Enter monthly exam grades for a class.
     */
    List<GradeResponse> enterMonthlyGrades(MonthlyGradeEntryRequest request);

    /**
     * Get student's monthly exam grades for a subject/semester.
     */
    List<GradeResponse> getStudentMonthlyGrades(UUID studentId, UUID subjectId, Integer semester, String academicYear);

    // =============================================
    // Semester Exam Operations
    // =============================================

    /**
     * Enter semester exam grades for a class (bulk).
     */
    List<GradeResponse> enterSemesterExamGrades(BulkGradeRequest request);

    /**
     * Get student's semester exam grade for a subject.
     */
    GradeResponse getStudentSemesterExam(UUID studentId, UUID subjectId, Integer semester, String academicYear);

    // =============================================
    // Query Operations
    // =============================================

    /**
     * Get all grades for a student in a semester.
     */
    StudentGradesSummary getStudentSemesterGrades(UUID studentId, Integer semester, String academicYear);

    /**
     * Get all grades for a class in a semester.
     */
    ClassGradesSummary getClassGrades(UUID classId, Integer semester, String academicYear);

    /**
     * Get grades for a specific class/subject/semester.
     */
    List<GradeResponse> getClassSubjectGrades(UUID classId, UUID subjectId, Integer semester, String academicYear);
}
