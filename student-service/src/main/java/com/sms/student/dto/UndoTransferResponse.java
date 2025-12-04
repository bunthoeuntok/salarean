package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for undo transfer operation.
 * Contains details of the undone transfer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UndoTransferResponse {

    private UUID transferId;
    private Integer undoneStudents;
    private UUID sourceClassId;
    private LocalDateTime undoneAt;
}
