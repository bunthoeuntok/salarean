package com.sms.student.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for batch student transfer operation.
 * Used to transfer multiple students from one class to another.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTransferRequest {

    @NotNull(message = "Destination class ID is required")
    private UUID destinationClassId;

    @NotNull(message = "Student IDs are required")
    @Size(min = 1, max = 100, message = "Must transfer between 1 and 100 students")
    private List<UUID> studentIds;
}
