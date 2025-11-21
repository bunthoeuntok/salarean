package com.sms.student.exception;

import com.sms.student.dto.ApiResponse;
import com.sms.student.dto.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleStudentNotFound(StudentNotFoundException ex) {
        log.error("Student not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getErrorCode()));
    }

    @ExceptionHandler(InvalidStudentDataException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidStudentData(InvalidStudentDataException ex) {
        log.error("Invalid student data: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getErrorCode()));
    }

    @ExceptionHandler(DuplicateStudentCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateStudentCode(DuplicateStudentCodeException ex) {
        log.error("Duplicate student code: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getErrorCode()));
    }

    @ExceptionHandler(PhotoSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handlePhotoSizeExceeded(PhotoSizeExceededException ex) {
        log.error("Photo size exceeded: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getErrorCode()));
    }

    @ExceptionHandler(InvalidPhotoFormatException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPhotoFormat(InvalidPhotoFormatException ex) {
        log.error("Invalid photo format: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getErrorCode()));
    }

    @ExceptionHandler(PhotoProcessingException.class)
    public ResponseEntity<ApiResponse<Void>> handlePhotoProcessing(PhotoProcessingException ex) {
        log.error("Photo processing failed: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getErrorCode()));
    }

    @ExceptionHandler(ClassNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleClassNotFound(ClassNotFoundException ex) {
        log.error("Class not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getErrorCode()));
    }

    @ExceptionHandler(ClassCapacityExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleClassCapacityExceeded(ClassCapacityExceededException ex) {
        log.error("Class capacity exceeded: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("Validation failed: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.INVALID_STUDENT_DATA));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR));
    }
}
