package com.sms.grade.dto;

import com.sms.grade.enums.AssessmentCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating/updating assessment types.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentTypeRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Khmer name is required")
    @Size(max = 100, message = "Khmer name must not exceed 100 characters")
    private String nameKhmer;

    @NotBlank(message = "Code is required")
    @Size(max = 30, message = "Code must not exceed 30 characters")
    private String code;

    @NotNull(message = "Category is required")
    private AssessmentCategory category;

    @NotNull(message = "Default weight is required")
    @DecimalMin(value = "0.00", message = "Default weight must be at least 0")
    @DecimalMax(value = "100.00", message = "Default weight must not exceed 100")
    private BigDecimal defaultWeight;

    @NotNull(message = "Max score is required")
    @DecimalMin(value = "1.00", message = "Max score must be at least 1")
    @DecimalMax(value = "1000.00", message = "Max score must not exceed 1000")
    private BigDecimal maxScore;

    @NotNull(message = "Display order is required")
    @Min(value = 1, message = "Display order must be at least 1")
    private Integer displayOrder;
}
