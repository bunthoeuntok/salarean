package com.sms.grade.exception;

import com.sms.common.dto.ApiResponse;
import com.sms.grade.dto.GradeErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for grade-service.
 * Maps exceptions to standardized API responses with error codes.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GradeNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleGradeNotFound(GradeNotFoundException ex) {
        log.warn("Grade not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(GradeErrorCode.GRADE_NOT_FOUND));
    }

    @ExceptionHandler(SubjectNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSubjectNotFound(SubjectNotFoundException ex) {
        log.warn("Subject not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(GradeErrorCode.SUBJECT_NOT_FOUND));
    }

    @ExceptionHandler(AssessmentTypeNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAssessmentTypeNotFound(AssessmentTypeNotFoundException ex) {
        log.warn("Assessment type not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(GradeErrorCode.ASSESSMENT_TYPE_NOT_FOUND));
    }

    @ExceptionHandler(DuplicateGradeException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateGrade(DuplicateGradeException ex) {
        log.warn("Duplicate grade: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(GradeErrorCode.DUPLICATE_GRADE_ENTRY));
    }

    @ExceptionHandler(InvalidGradeDataException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidGradeData(InvalidGradeDataException ex) {
        log.warn("Invalid grade data: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(GradeErrorCode.SCORE_OUT_OF_RANGE));
    }

    @ExceptionHandler(InsufficientGradesException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientGrades(InsufficientGradesException ex) {
        log.warn("Insufficient grades: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(GradeErrorCode.INSUFFICIENT_GRADES_FOR_CALCULATION));
    }

    @ExceptionHandler(CalculationException.class)
    public ResponseEntity<ApiResponse<Void>> handleCalculationError(CalculationException ex) {
        log.error("Calculation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(GradeErrorCode.CALCULATION_ERROR));
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(GradeErrorCode.UNAUTHORIZED));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation errors: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(GradeErrorCode.VALIDATION_ERROR));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(GradeErrorCode.VALIDATION_ERROR));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(GradeErrorCode.INTERNAL_ERROR));
    }
}
