package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for batch student transfer operation.
 * Contains transfer result including successful and failed transfers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTransferResponse {

    private UUID transferId;
    private UUID sourceClassId;
    private UUID destinationClassId;
    private Integer successfulTransfers;
    private List<FailedTransfer> failedTransfers;
    private LocalDateTime transferredAt;

    /**
     * Details of a student that failed to transfer.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedTransfer {
        private UUID studentId;
        private String studentName;
        private String reason; // Error code (e.g., "ALREADY_ENROLLED", "STUDENT_NOT_FOUND")
    }
}
