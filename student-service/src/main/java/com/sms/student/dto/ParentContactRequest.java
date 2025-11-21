package com.sms.student.dto;

import com.sms.student.enums.Relationship;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentContactRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+855\\d{8,9}$", message = "Invalid Cambodia phone format. Must be +855XXXXXXXX")
    private String phoneNumber;

    @NotNull(message = "Relationship is required")
    private Relationship relationship;

    @Builder.Default
    private Boolean isPrimary = false;
}
