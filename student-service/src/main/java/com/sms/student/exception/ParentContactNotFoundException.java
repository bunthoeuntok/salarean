package com.sms.student.exception;

import com.sms.student.dto.StudentErrorCode;
import lombok.Getter;

/**
 * Exception thrown when a parent contact is not found.
 */
@Getter
public class ParentContactNotFoundException extends RuntimeException {
    private final Enum<?> errorCode;

    public ParentContactNotFoundException(String message) {
        super(message);
        this.errorCode = StudentErrorCode.PARENT_CONTACT_NOT_FOUND;
    }
}
