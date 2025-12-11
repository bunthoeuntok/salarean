package com.sms.student.exception;

public class DistrictNotFoundException extends RuntimeException {
    public DistrictNotFoundException(String message) {
        super(message);
    }
}
