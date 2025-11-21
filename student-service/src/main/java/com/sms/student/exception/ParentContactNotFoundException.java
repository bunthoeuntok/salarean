package com.sms.student.exception;

import com.sms.student.dto.ErrorCode;
import lombok.Getter;

/**
 * Exception thrown when a parent contact is not found.
 */
@Getter
public class ParentContactNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public ParentContactNotFoundException(String message) {
        super(message);
        this.errorCode = ErrorCode.PARENT_CONTACT_NOT_FOUND;
    }
}
