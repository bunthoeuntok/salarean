package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentHistoryResponse {

    private List<EnrollmentResponse> enrollments;
    private Integer totalCount;
    private Integer activeCount;      // Count of ACTIVE enrollments
    private Integer completedCount;   // Count of COMPLETED enrollments
    private Integer transferredCount; // Count of TRANSFERRED enrollments
}
