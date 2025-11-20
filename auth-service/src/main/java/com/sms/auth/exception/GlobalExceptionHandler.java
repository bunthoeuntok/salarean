package com.sms.auth.exception;

import com.sms.auth.dto.BaseResponse;
import com.sms.auth.dto.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for auth-service.
 * Returns error codes only - frontend handles translation to Khmer/English.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<BaseResponse<Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.badRequest()
            .body(BaseResponse.error(ErrorCode.DUPLICATE_EMAIL));
    }

    @ExceptionHandler(DuplicatePhoneException.class)
    public ResponseEntity<BaseResponse<Object>> handleDuplicatePhone(DuplicatePhoneException ex) {
        return ResponseEntity.badRequest()
            .body(BaseResponse.error(ErrorCode.DUPLICATE_PHONE));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<BaseResponse<Object>> handleInvalidPassword(InvalidPasswordException ex) {
        return ResponseEntity.badRequest()
            .body(BaseResponse.error(ErrorCode.INVALID_PASSWORD));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<BaseResponse<Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(BaseResponse.error(ErrorCode.INVALID_CREDENTIALS));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<BaseResponse<Object>> handleRateLimit(RateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(BaseResponse.error(ErrorCode.RATE_LIMIT_EXCEEDED));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        // Determine error code based on field
        ErrorCode errorCode = ErrorCode.INVALID_EMAIL_FORMAT; // default
        if (!ex.getBindingResult().getFieldErrors().isEmpty()) {
            String field = ex.getBindingResult().getFieldErrors().get(0).getField();
            if ("phoneNumber".equals(field)) {
                errorCode = ErrorCode.INVALID_PHONE_FORMAT;
            } else if ("password".equals(field)) {
                errorCode = ErrorCode.INVALID_PASSWORD;
            }
        }

        return ResponseEntity.badRequest()
            .body(BaseResponse.error(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleGeneric(Exception ex) {
        // Log full exception for debugging
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(BaseResponse.error(ErrorCode.INTERNAL_ERROR));
    }
}
