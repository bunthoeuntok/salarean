package com.sms.auth.exception;

import com.sms.auth.dto.ApiResponse;
import com.sms.auth.dto.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Global exception handler for auth-service.
 * Returns error codes only - frontend handles translation to Khmer/English.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ErrorCode.DUPLICATE_EMAIL));
    }

    @ExceptionHandler(DuplicatePhoneException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicatePhone(DuplicatePhoneException ex) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ErrorCode.DUPLICATE_PHONE));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidPassword(InvalidPasswordException ex) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ErrorCode.INVALID_PASSWORD));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(ErrorCode.INVALID_CREDENTIALS));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleRateLimit(RateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(ApiResponse.error(ErrorCode.RATE_LIMIT_EXCEEDED));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        logger.warn("Validation exception: {}", ex.getMessage());

        // Determine error code based on field
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        if (!ex.getBindingResult().getFieldErrors().isEmpty()) {
            String field = ex.getBindingResult().getFieldErrors().get(0).getField();
            if ("phoneNumber".equals(field)) {
                errorCode = ErrorCode.INVALID_PHONE_FORMAT;
            } else if ("password".equals(field)) {
                errorCode = ErrorCode.WEAK_PASSWORD;
            } else if ("email".equals(field)) {
                errorCode = ErrorCode.VALIDATION_ERROR;
            }
        }

        return ResponseEntity.badRequest()
            .body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTokenException(InvalidTokenException ex) {
        logger.warn("Invalid token exception: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.INVALID_TOKEN));
    }

    @ExceptionHandler(ProfileUpdateException.class)
    public ResponseEntity<ApiResponse<Void>> handleProfileUpdateException(ProfileUpdateException ex) {
        logger.warn("Profile update exception: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.PROFILE_UPDATE_FAILED));
    }

    @ExceptionHandler(PhotoUploadException.class)
    public ResponseEntity<ApiResponse<Void>> handlePhotoUploadException(PhotoUploadException ex) {
        logger.warn("Photo upload exception: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.INVALID_PHOTO_FORMAT));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        logger.warn("Constraint violation exception: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        logger.warn("File size exceeded: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.PHOTO_SIZE_EXCEEDED));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException ex) {
        logger.warn("User not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getErrorCode()));
    }

    @ExceptionHandler(ResetTokenInvalidException.class)
    public ResponseEntity<ApiResponse<Void>> handleResetTokenInvalidException(ResetTokenInvalidException ex) {
        logger.warn("Reset token invalid: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getErrorCode()));
    }

    @ExceptionHandler(ResetTokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleResetTokenExpiredException(ResetTokenExpiredException ex) {
        logger.warn("Reset token expired: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex) {
        // Log full exception for debugging
        logger.error("Unexpected exception", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR));
    }
}
