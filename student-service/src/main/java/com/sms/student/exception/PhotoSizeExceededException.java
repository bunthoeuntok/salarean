package com.sms.student.exception;

import com.sms.student.dto.ErrorCode;
import lombok.Getter;

@Getter
public class PhotoSizeExceededException extends RuntimeException {

    private final ErrorCode errorCode;

    public PhotoSizeExceededException(String message) {
        super(message);
        this.errorCode = ErrorCode.PHOTO_SIZE_EXCEEDED;
    }

    public PhotoSizeExceededException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.PHOTO_SIZE_EXCEEDED;
    }
}
