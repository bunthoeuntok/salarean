package com.sms.student.exception;

import com.sms.student.dto.StudentErrorCode;
import lombok.Getter;

@Getter
public class StudentNotFoundException extends RuntimeException {

    private final Enum<?> errorCode;

    public StudentNotFoundException(String message) {
        super(message);
        this.errorCode = StudentErrorCode.STUDENT_NOT_FOUND;
    }

    public StudentNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = StudentErrorCode.STUDENT_NOT_FOUND;
    }
}
