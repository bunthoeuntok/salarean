package com.sms.student.exception;

import com.sms.student.dto.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidPhotoFormatException extends RuntimeException {

    private final ErrorCode errorCode;

    public InvalidPhotoFormatException(String message) {
        super(message);
        this.errorCode = ErrorCode.INVALID_PHOTO_FORMAT;
    }

    public InvalidPhotoFormatException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.INVALID_PHOTO_FORMAT;
    }
}
