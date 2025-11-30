package com.sms.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotNull(message = "Target class ID is required")
    private UUID targetClassId;

    @NotNull(message = "Transfer date is required")
    private LocalDate transferDate;

    @NotBlank(message = "Transfer reason is required")
    @Size(max = 500, message = "Transfer reason cannot exceed 500 characters")
    private String reason;
}
