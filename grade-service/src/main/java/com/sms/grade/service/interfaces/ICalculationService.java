package com.sms.grade.service.interfaces;

import com.sms.grade.dto.CalculationResult;
import com.sms.grade.dto.RankingResponse;

import java.util.UUID;

/**
 * Service interface for grade calculations following MoEYS standards.
 */
public interface ICalculationService {

    // =============================================
    // Student Calculations
    // =============================================

    /**
     * Calculate monthly average for a student/subject/semester.
     * Formula: (Exam1 + Exam2 + Exam3 + Exam4) / 4
     */
    CalculationResult calculateMonthlyAverage(UUID studentId, UUID subjectId, Integer semester, String academicYear);

    /**
     * Calculate semester average for a student/subject.
     * Formula: (Monthly Average * monthlyWeight + Semester Exam * semesterWeight) / 100
     * Default weights: 50% monthly, 50% semester
     */
    CalculationResult calculateSubjectSemesterAverage(UUID studentId, UUID subjectId, Integer semester, String academicYear);

    /**
     * Calculate overall semester average for a student (all subjects).
     * Formula: Sum of all subject semester averages / number of subjects
     */
    CalculationResult calculateOverallSemesterAverage(UUID studentId, Integer semester, String academicYear);

    /**
     * Calculate annual average for a student/subject.
     * Formula: (Semester1 Average + Semester2 Average) / 2
     */
    CalculationResult calculateSubjectAnnualAverage(UUID studentId, UUID subjectId, String academicYear);

    /**
     * Calculate overall annual average for a student.
     * Formula: (Semester1 Overall + Semester2 Overall) / 2
     */
    CalculationResult calculateOverallAnnualAverage(UUID studentId, String academicYear);

    // =============================================
    // Class Calculations
    // =============================================

    /**
     * Calculate and cache all averages for a class/semester.
     */
    void calculateClassAverages(UUID classId, Integer semester, String academicYear);

    /**
     * Calculate rankings for a class/semester.
     */
    RankingResponse calculateClassRankings(UUID classId, Integer semester, String academicYear);

    /**
     * Calculate subject rankings for a class.
     */
    RankingResponse calculateSubjectRankings(UUID classId, UUID subjectId, Integer semester, String academicYear);

    // =============================================
    // Utility Methods
    // =============================================

    /**
     * Determine letter grade based on percentage.
     * MoEYS standards:
     * A: 85-100%, B: 70-84%, C: 55-69%, D: 40-54%, E: 25-39%, F: 0-24%
     */
    String getLetterGrade(double percentage);

    /**
     * Check if a student has passed (grade D or above, >= 40%).
     */
    boolean hasPassed(double percentage);

    /**
     * Recalculate all averages when a grade is updated.
     */
    void recalculateOnGradeChange(UUID studentId, UUID classId, UUID subjectId, Integer semester, String academicYear);
}
