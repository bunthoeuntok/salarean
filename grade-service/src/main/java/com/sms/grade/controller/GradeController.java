package com.sms.grade.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.grade.dto.*;
import com.sms.grade.service.interfaces.ICalculationService;
import com.sms.grade.service.interfaces.IGradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for grade management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@Tag(name = "Grades", description = "Grade management endpoints")
public class GradeController {

    private final IGradeService gradeService;
    private final ICalculationService calculationService;

    // =============================================
    // CRUD Operations
    // =============================================

    @PostMapping
    @Operation(summary = "Create a single grade entry")
    public ResponseEntity<ApiResponse<GradeResponse>> createGrade(
            @Valid @RequestBody GradeRequest request) {
        log.info("Creating grade for student {}", request.getStudentId());
        GradeResponse response = gradeService.createGrade(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create multiple grades in bulk")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> createBulkGrades(
            @Valid @RequestBody BulkGradeRequest request) {
        log.info("Creating bulk grades for class {}", request.getClassId());
        List<GradeResponse> responses = gradeService.createBulkGrades(request);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a grade by ID")
    public ResponseEntity<ApiResponse<GradeResponse>> getGrade(@PathVariable UUID id) {
        GradeResponse response = gradeService.getGrade(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a grade")
    public ResponseEntity<ApiResponse<GradeResponse>> updateGrade(
            @PathVariable UUID id,
            @Valid @RequestBody GradeRequest request) {
        log.info("Updating grade {}", id);
        GradeResponse response = gradeService.updateGrade(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a grade")
    public ResponseEntity<ApiResponse<Void>> deleteGrade(@PathVariable UUID id) {
        log.info("Deleting grade {}", id);
        gradeService.deleteGrade(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // =============================================
    // Monthly Exam Operations
    // =============================================

    @PostMapping("/monthly/{classId}/{subjectId}")
    @Operation(summary = "Enter monthly exam grades for a class")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> enterMonthlyGrades(
            @PathVariable UUID classId,
            @PathVariable UUID subjectId,
            @Valid @RequestBody MonthlyGradeEntryRequest request) {
        log.info("Entering monthly grades for class {} subject {}", classId, subjectId);
        // Ensure path variables match request body
        request.setClassId(classId);
        request.setSubjectId(subjectId);
        List<GradeResponse> responses = gradeService.enterMonthlyGrades(request);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/monthly/{studentId}/subject/{subjectId}/semester/{semester}")
    @Operation(summary = "Get student's monthly exam grades")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getStudentMonthlyGrades(
            @PathVariable UUID studentId,
            @PathVariable UUID subjectId,
            @PathVariable Integer semester,
            @RequestParam String academicYear) {
        List<GradeResponse> responses = gradeService.getStudentMonthlyGrades(
                studentId, subjectId, semester, academicYear);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // =============================================
    // Semester Exam Operations
    // =============================================

    @PostMapping("/semester/{classId}/{subjectId}")
    @Operation(summary = "Enter semester exam grades for a class")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> enterSemesterExamGrades(
            @PathVariable UUID classId,
            @PathVariable UUID subjectId,
            @Valid @RequestBody BulkGradeRequest request) {
        log.info("Entering semester exam grades for class {} subject {}", classId, subjectId);
        request.setClassId(classId);
        request.setSubjectId(subjectId);
        List<GradeResponse> responses = gradeService.enterSemesterExamGrades(request);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/semester/{studentId}/subject/{subjectId}/semester/{semester}")
    @Operation(summary = "Get student's semester exam grade")
    public ResponseEntity<ApiResponse<GradeResponse>> getStudentSemesterExam(
            @PathVariable UUID studentId,
            @PathVariable UUID subjectId,
            @PathVariable Integer semester,
            @RequestParam String academicYear) {
        GradeResponse response = gradeService.getStudentSemesterExam(
                studentId, subjectId, semester, academicYear);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // =============================================
    // Query Operations
    // =============================================

    @GetMapping("/student/{studentId}/semester/{semester}")
    @Operation(summary = "Get all grades for a student in a semester")
    public ResponseEntity<ApiResponse<StudentGradesSummary>> getStudentSemesterGrades(
            @PathVariable UUID studentId,
            @PathVariable Integer semester,
            @RequestParam String academicYear) {
        StudentGradesSummary summary = gradeService.getStudentSemesterGrades(
                studentId, semester, academicYear);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/class/{classId}/semester/{semester}")
    @Operation(summary = "Get all grades for a class in a semester")
    public ResponseEntity<ApiResponse<ClassGradesSummary>> getClassGrades(
            @PathVariable UUID classId,
            @PathVariable Integer semester,
            @RequestParam String academicYear) {
        ClassGradesSummary summary = gradeService.getClassGrades(classId, semester, academicYear);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/class/{classId}/subject/{subjectId}/semester/{semester}")
    @Operation(summary = "Get grades for a specific class/subject")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getClassSubjectGrades(
            @PathVariable UUID classId,
            @PathVariable UUID subjectId,
            @PathVariable Integer semester,
            @RequestParam String academicYear) {
        List<GradeResponse> responses = gradeService.getClassSubjectGrades(
                classId, subjectId, semester, academicYear);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // =============================================
    // Calculation Operations
    // =============================================

    @GetMapping("/calculate/{studentId}/subject/{subjectId}/semester/{semester}")
    @Operation(summary = "Calculate semester average for a student/subject")
    public ResponseEntity<ApiResponse<CalculationResult>> calculateSubjectSemesterAverage(
            @PathVariable UUID studentId,
            @PathVariable UUID subjectId,
            @PathVariable Integer semester,
            @RequestParam String academicYear) {
        CalculationResult result = calculationService.calculateSubjectSemesterAverage(
                studentId, subjectId, semester, academicYear);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/calculate/{studentId}/semester/{semester}")
    @Operation(summary = "Calculate overall semester average for a student")
    public ResponseEntity<ApiResponse<CalculationResult>> calculateOverallSemesterAverage(
            @PathVariable UUID studentId,
            @PathVariable Integer semester,
            @RequestParam String academicYear) {
        CalculationResult result = calculationService.calculateOverallSemesterAverage(
                studentId, semester, academicYear);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/calculate/{studentId}/annual")
    @Operation(summary = "Calculate annual average for a student")
    public ResponseEntity<ApiResponse<CalculationResult>> calculateAnnualAverage(
            @PathVariable UUID studentId,
            @RequestParam String academicYear) {
        CalculationResult result = calculationService.calculateOverallAnnualAverage(
                studentId, academicYear);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/calculate/class/{classId}/semester/{semester}")
    @Operation(summary = "Calculate all averages for a class")
    public ResponseEntity<ApiResponse<Void>> calculateClassAverages(
            @PathVariable UUID classId,
            @PathVariable Integer semester,
            @RequestParam String academicYear) {
        log.info("Calculating class averages for class {} semester {}", classId, semester);
        calculationService.calculateClassAverages(classId, semester, academicYear);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // =============================================
    // Ranking Operations
    // =============================================

    @GetMapping("/rankings/{classId}/semester/{semester}")
    @Operation(summary = "Get class rankings for a semester")
    public ResponseEntity<ApiResponse<RankingResponse>> getClassRankings(
            @PathVariable UUID classId,
            @PathVariable Integer semester,
            @RequestParam String academicYear) {
        RankingResponse rankings = calculationService.calculateClassRankings(
                classId, semester, academicYear);
        return ResponseEntity.ok(ApiResponse.success(rankings));
    }

    @GetMapping("/rankings/{classId}/subject/{subjectId}/semester/{semester}")
    @Operation(summary = "Get subject rankings for a class")
    public ResponseEntity<ApiResponse<RankingResponse>> getSubjectRankings(
            @PathVariable UUID classId,
            @PathVariable UUID subjectId,
            @PathVariable Integer semester,
            @RequestParam String academicYear) {
        RankingResponse rankings = calculationService.calculateSubjectRankings(
                classId, subjectId, semester, academicYear);
        return ResponseEntity.ok(ApiResponse.success(rankings));
    }
}
