package com.sms.student.service;

import com.sms.student.dto.BatchTransferRequest;
import com.sms.student.dto.BatchTransferResponse;
import com.sms.student.dto.EligibleClassResponse;
import com.sms.student.dto.UndoTransferResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for batch student transfer operations.
 * Handles transfer, undo, and eligibility checking.
 */
public interface IStudentTransferService {

    /**
     * Execute batch transfer of students to a destination class.
     *
     * @param sourceClassId the source class ID
     * @param request the batch transfer request
     * @param performedByUserId the user performing the transfer
     * @return transfer response with results
     */
    BatchTransferResponse batchTransfer(
        UUID sourceClassId,
        BatchTransferRequest request,
        UUID performedByUserId
    );

    /**
     * Undo a batch transfer operation.
     * Returns all successfully transferred students back to their source class.
     *
     * @param transferId the transfer ID to undo
     * @param performedByUserId the user performing the undo (must match original user)
     * @return undo response with results
     */
    UndoTransferResponse undoTransfer(UUID transferId, UUID performedByUserId);

    /**
     * Check if a transfer can be undone.
     * Verifies: not already undone, within time window, no conflicts, authorized user.
     *
     * @param transferId the transfer ID
     * @param performedByUserId the user requesting the undo
     * @return true if undo is allowed, false otherwise
     */
    boolean canUndoTransfer(UUID transferId, UUID performedByUserId);
}
