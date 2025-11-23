package com.sms.student.exception;

import com.sms.student.dto.StudentErrorCode;
import lombok.Getter;

/**
 * Exception thrown when photo processing fails (resize, format conversion, etc.).
 */
@Getter
public class PhotoProcessingException extends RuntimeException {
    private final Enum<?> errorCode;

    public PhotoProcessingException(String message) {
        super(message);
        this.errorCode = StudentErrorCode.PHOTO_PROCESSING_ERROR;
    }

    public PhotoProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = StudentErrorCode.PHOTO_PROCESSING_ERROR;
    }
}
