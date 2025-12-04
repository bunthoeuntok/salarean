package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for eligible destination classes.
 * Contains class information for transfer destination dropdown.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibleClassResponse {

    private UUID id;
    private String name;
    private String code;
    private Integer gradeLevel;
    private Integer capacity;
    private Integer currentEnrollment;
    private String teacherName;
}
