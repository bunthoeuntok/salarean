package com.sms.student.dto;

public class ScheduleErrorCode {
    public static final String TEMPLATE_NOT_FOUND = "TEMPLATE_NOT_FOUND";
    public static final String SCHEDULE_NOT_FOUND = "SCHEDULE_NOT_FOUND";
    public static final String SCHEDULE_ALREADY_EXISTS = "SCHEDULE_ALREADY_EXISTS";
    public static final String CLASS_NOT_FOUND = "CLASS_NOT_FOUND";
    public static final String INVALID_DAY_OF_WEEK = "INVALID_DAY_OF_WEEK";
    public static final String INVALID_PERIOD_NUMBER = "INVALID_PERIOD_NUMBER";
    public static final String ENTRY_CONFLICT = "ENTRY_CONFLICT";
    public static final String CANNOT_DELETE_DEFAULT_TEMPLATE = "CANNOT_DELETE_DEFAULT_TEMPLATE";
    public static final String UNAUTHORIZED_TEMPLATE_ACCESS = "UNAUTHORIZED_TEMPLATE_ACCESS";

    private ScheduleErrorCode() {}
}
