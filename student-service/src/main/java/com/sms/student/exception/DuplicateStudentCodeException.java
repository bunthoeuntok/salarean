package com.sms.student.exception;

import com.sms.student.dto.StudentErrorCode;
import lombok.Getter;

@Getter
public class DuplicateStudentCodeException extends RuntimeException {

    private final Enum<?> errorCode;

    public DuplicateStudentCodeException(String message) {
        super(message);
        this.errorCode = StudentErrorCode.DUPLICATE_STUDENT_CODE;
    }

    public DuplicateStudentCodeException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = StudentErrorCode.DUPLICATE_STUDENT_CODE;
    }
}
