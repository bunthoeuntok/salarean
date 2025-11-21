package com.sms.student.exception;

import com.sms.student.dto.ErrorCode;
import lombok.Getter;

/**
 * Exception thrown when photo processing fails (resize, format conversion, etc.).
 */
@Getter
public class PhotoProcessingException extends RuntimeException {
    private final ErrorCode errorCode;

    public PhotoProcessingException(String message) {
        super(message);
        this.errorCode = ErrorCode.PHOTO_PROCESSING_ERROR;
    }

    public PhotoProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.PHOTO_PROCESSING_ERROR;
    }
}
