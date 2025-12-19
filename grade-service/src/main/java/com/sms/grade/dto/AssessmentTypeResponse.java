package com.sms.grade.dto;

import com.sms.grade.enums.AssessmentCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for assessment type information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentTypeResponse {

    private UUID id;
    private String name;
    private String nameKhmer;
    private String code;
    private AssessmentCategory category;
    private BigDecimal defaultWeight;
    private BigDecimal maxScore;
    private Integer displayOrder;
}
