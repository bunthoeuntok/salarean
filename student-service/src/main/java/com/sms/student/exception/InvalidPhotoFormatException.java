package com.sms.student.exception;

import com.sms.student.dto.StudentErrorCode;
import lombok.Getter;

@Getter
public class InvalidPhotoFormatException extends RuntimeException {

    private final Enum<?> errorCode;

    public InvalidPhotoFormatException(String message) {
        super(message);
        this.errorCode = StudentErrorCode.INVALID_PHOTO_FORMAT;
    }

    public InvalidPhotoFormatException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = StudentErrorCode.INVALID_PHOTO_FORMAT;
    }
}
