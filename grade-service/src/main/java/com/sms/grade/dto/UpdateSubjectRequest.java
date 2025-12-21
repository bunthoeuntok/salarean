package com.sms.grade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for updating a subject.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubjectRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotBlank(message = "Khmer name is required")
    @Size(max = 100, message = "Khmer name must be at most 100 characters")
    private String nameKhmer;

    @NotBlank(message = "Code is required")
    @Size(max = 20, message = "Code must be at most 20 characters")
    private String code;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private Integer displayOrder;

    @NotNull(message = "Grade levels are required")
    private List<Integer> gradeLevels;
}
