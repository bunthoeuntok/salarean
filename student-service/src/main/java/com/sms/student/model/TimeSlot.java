package com.sms.student.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot {
    private Integer periodNumber;  // null for breaks
    private String startTime;
    private String endTime;
    private String label;
    private String labelKm;
    private Boolean isBreak;
}
