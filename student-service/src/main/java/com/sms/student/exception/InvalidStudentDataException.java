package com.sms.student.exception;

import com.sms.student.dto.StudentErrorCode;
import lombok.Getter;

@Getter
public class InvalidStudentDataException extends RuntimeException {

    private final Enum<?> errorCode;

    public InvalidStudentDataException(String message) {
        super(message);
        this.errorCode = StudentErrorCode.INVALID_STUDENT_DATA;
    }

    public InvalidStudentDataException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = StudentErrorCode.INVALID_STUDENT_DATA;
    }
}
