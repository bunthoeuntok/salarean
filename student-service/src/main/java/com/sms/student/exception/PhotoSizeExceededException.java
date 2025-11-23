package com.sms.student.exception;

import com.sms.student.dto.StudentErrorCode;
import lombok.Getter;

@Getter
public class PhotoSizeExceededException extends RuntimeException {

    private final Enum<?> errorCode;

    public PhotoSizeExceededException(String message) {
        super(message);
        this.errorCode = StudentErrorCode.PHOTO_SIZE_EXCEEDED;
    }

    public PhotoSizeExceededException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = StudentErrorCode.PHOTO_SIZE_EXCEEDED;
    }
}
