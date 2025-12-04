package com.sms.student.service;

import com.sms.student.dto.BatchTransferRequest;
import com.sms.student.dto.BatchTransferResponse;
import com.sms.student.dto.EligibleClassResponse;
import com.sms.student.dto.UndoTransferResponse;
import com.sms.student.repository.ClassRepository;
import com.sms.student.repository.EnrollmentHistoryRepository;
import com.sms.student.repository.StudentClassEnrollmentRepository;
import com.sms.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service implementation for batch student transfer operations.
 * Handles transfer, undo, eligibility checking, and conflict detection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentTransferService implements IStudentTransferService {

    private static final int UNDO_WINDOW_MINUTES = 5;

    private final StudentRepository studentRepository;
    private final ClassRepository classRepository;
    private final StudentClassEnrollmentRepository enrollmentRepository;
    private final EnrollmentHistoryRepository enrollmentHistoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EligibleClassResponse> getEligibleDestinationClasses(UUID sourceClassId) {
        log.debug("Getting eligible destination classes for source class: {}", sourceClassId);

        // 1. Get source class and validate it exists
        var sourceClass = classRepository.findById(sourceClassId)
            .orElseThrow(() -> {
                log.error("Source class not found: {}", sourceClassId);
                return new RuntimeException("SOURCE_CLASS_NOT_FOUND");
            });

        log.debug("Source class found: grade={}, status={}", sourceClass.getGrade(), sourceClass.getStatus());

        // 2. Find all active classes with same grade level, excluding source class
        var eligibleClasses = classRepository.findAll().stream()
            .filter(cls -> cls.getStatus() == com.sms.student.enums.ClassStatus.ACTIVE)
            .filter(cls -> cls.getGrade().equals(sourceClass.getGrade()))
            .filter(cls -> !cls.getId().equals(sourceClassId))
            .filter(cls -> cls.hasCapacity()) // Filter by available capacity
            .map(cls -> EligibleClassResponse.builder()
                .id(cls.getId())
                .name(String.format("Grade %d - %s", cls.getGrade(), cls.getSection()))
                .code(cls.getSection())
                .gradeLevel(cls.getGrade())
                .capacity(cls.getMaxCapacity())
                .currentEnrollment(cls.getStudentCount())
                .teacherName("Teacher") // TODO: Fetch teacher name from auth-service in future
                .build())
            .toList();

        log.debug("Found {} eligible destination classes for source class {}",
            eligibleClasses.size(), sourceClassId);

        return eligibleClasses;
    }

    @Override
    @Transactional
    public BatchTransferResponse batchTransfer(
        UUID sourceClassId,
        BatchTransferRequest request,
        UUID performedByUserId
    ) {
        log.info("Executing batch transfer from class {} to class {} for {} students",
            sourceClassId, request.getDestinationClassId(), request.getStudentIds().size());

        // 1. Validate source and destination classes exist and are active
        var sourceClass = classRepository.findById(sourceClassId)
            .orElseThrow(() -> new RuntimeException("SOURCE_CLASS_NOT_FOUND"));

        var destinationClass = classRepository.findById(request.getDestinationClassId())
            .orElseThrow(() -> new RuntimeException("DESTINATION_CLASS_NOT_FOUND"));

        if (sourceClass.getStatus() != com.sms.student.enums.ClassStatus.ACTIVE) {
            throw new RuntimeException("SOURCE_CLASS_NOT_ACTIVE");
        }

        if (destinationClass.getStatus() != com.sms.student.enums.ClassStatus.ACTIVE) {
            throw new RuntimeException("DESTINATION_CLASS_NOT_ACTIVE");
        }

        // 2. Validate grade level match
        if (!sourceClass.getGrade().equals(destinationClass.getGrade())) {
            throw new RuntimeException("GRADE_MISMATCH");
        }

        // 3. Check destination capacity
        int availableCapacity = destinationClass.getMaxCapacity() - destinationClass.getStudentCount();
        if (availableCapacity < request.getStudentIds().size()) {
            throw new RuntimeException("INSUFFICIENT_CAPACITY");
        }

        // 4. Generate transfer ID
        UUID transferId = UUID.randomUUID();
        java.time.LocalDateTime transferredAt = java.time.LocalDateTime.now();

        // 5. Process each student
        java.util.List<BatchTransferResponse.FailedTransfer> failedTransfers = new java.util.ArrayList<>();
        int successCount = 0;

        for (UUID studentId : request.getStudentIds()) {
            try {
                // Validate student exists
                var student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("STUDENT_NOT_FOUND"));

                // Find active enrollment in source class
                var sourceEnrollment = enrollmentRepository
                    .findByStudentIdAndClassIdAndStatus(
                        studentId,
                        sourceClassId,
                        com.sms.student.enums.EnrollmentStatus.ACTIVE
                    )
                    .orElseThrow(() -> new RuntimeException("STUDENT_NOT_ENROLLED_IN_SOURCE"));

                // Check if student already enrolled in destination
                var existingEnrollment = enrollmentRepository
                    .findByStudentIdAndClassIdAndStatus(
                        studentId,
                        request.getDestinationClassId(),
                        com.sms.student.enums.EnrollmentStatus.ACTIVE
                    );

                if (existingEnrollment.isPresent()) {
                    failedTransfers.add(BatchTransferResponse.FailedTransfer.builder()
                        .studentId(studentId)
                        .studentName(student.getFirstName() + " " + student.getLastName())
                        .reason("ALREADY_ENROLLED_IN_DESTINATION")
                        .build());
                    continue;
                }

                // Update source enrollment - mark as TRANSFERRED
                sourceEnrollment.setStatus(com.sms.student.enums.EnrollmentStatus.TRANSFERRED);
                sourceEnrollment.setTransferDate(java.time.LocalDate.now());
                enrollmentRepository.save(sourceEnrollment);

                // Create new enrollment in destination class
                var newEnrollment = com.sms.student.model.StudentClassEnrollment.builder()
                    .studentId(studentId)
                    .classId(request.getDestinationClassId())
                    .enrollmentDate(java.time.LocalDate.now())
                    .status(com.sms.student.enums.EnrollmentStatus.ACTIVE)
                    .reason(com.sms.student.enums.EnrollmentReason.TRANSFER)
                    .build();
                enrollmentRepository.save(newEnrollment);

                // Create enrollment history record for this transfer
                var historyRecord = com.sms.student.model.EnrollmentHistory.builder()
                    .studentId(studentId)
                    .classId(request.getDestinationClassId())
                    .action(com.sms.student.model.EnrollmentHistory.EnrollmentAction.TRANSFERRED)
                    .reason("Batch transfer from " + sourceClass.getSection() + " to " + destinationClass.getSection())
                    .performedByUserId(performedByUserId)
                    .transferId(transferId)
                    .metadata("{\"sourceClassId\":\"" + sourceClassId + "\",\"destinationClassId\":\"" + request.getDestinationClassId() + "\"}")
                    .build();
                enrollmentHistoryRepository.save(historyRecord);

                // Update class student counts
                sourceClass.decrementEnrollment();
                destinationClass.incrementEnrollment();

                successCount++;

            } catch (Exception e) {
                log.error("Failed to transfer student {}: {}", studentId, e.getMessage());
                failedTransfers.add(BatchTransferResponse.FailedTransfer.builder()
                    .studentId(studentId)
                    .studentName("Unknown")
                    .reason(e.getMessage())
                    .build());
            }
        }

        // Save updated class counts
        classRepository.save(sourceClass);
        classRepository.save(destinationClass);

        log.info("Batch transfer completed: {} successful, {} failed", successCount, failedTransfers.size());

        return BatchTransferResponse.builder()
            .transferId(transferId)
            .sourceClassId(sourceClassId)
            .destinationClassId(request.getDestinationClassId())
            .successfulTransfers(successCount)
            .failedTransfers(failedTransfers)
            .transferredAt(transferredAt)
            .build();
    }

    @Override
    @Transactional
    public UndoTransferResponse undoTransfer(UUID transferId, UUID performedByUserId) {
        log.info("Undoing transfer {} by user {}", transferId, performedByUserId);

        // 1. Validate transfer exists and get all enrollment history records for this transfer
        var transferRecords = enrollmentHistoryRepository.findByTransferId(transferId);

        if (transferRecords.isEmpty()) {
            log.error("Transfer not found: {}", transferId);
            throw new RuntimeException("TRANSFER_NOT_FOUND");
        }

        // 2. Check authorization - verify performed by same user
        var firstRecord = transferRecords.get(0);
        if (!firstRecord.getPerformedByUserId().equals(performedByUserId)) {
            log.error("Unauthorized undo attempt: transfer performed by {}, undo requested by {}",
                      firstRecord.getPerformedByUserId(), performedByUserId);
            throw new RuntimeException("UNAUTHORIZED_UNDO");
        }

        // 3. Check not already undone
        var undoRecords = enrollmentHistoryRepository.findByUndoOfTransferId(transferId);
        if (!undoRecords.isEmpty()) {
            log.error("Transfer already undone: {}", transferId);
            throw new RuntimeException("TRANSFER_ALREADY_UNDONE");
        }

        // 4. Check within time window (5 minutes)
        var transferTime = firstRecord.getPerformedAt();
        var now = java.time.LocalDateTime.now();
        var minutesSinceTransfer = java.time.Duration.between(transferTime, now).toMinutes();

        if (minutesSinceTransfer > UNDO_WINDOW_MINUTES) {
            log.error("Undo window expired: transfer was {} minutes ago, window is {} minutes",
                      minutesSinceTransfer, UNDO_WINDOW_MINUTES);
            throw new RuntimeException("UNDO_WINDOW_EXPIRED");
        }

        // Extract source and destination class IDs from metadata
        var metadata = firstRecord.getMetadata();
        UUID sourceClassId = extractClassIdFromMetadata(metadata, "sourceClassId");
        UUID destinationClassId = extractClassIdFromMetadata(metadata, "destinationClassId");

        var sourceClass = classRepository.findById(sourceClassId)
            .orElseThrow(() -> new RuntimeException("SOURCE_CLASS_NOT_FOUND"));

        var destinationClass = classRepository.findById(destinationClassId)
            .orElseThrow(() -> new RuntimeException("DESTINATION_CLASS_NOT_FOUND"));

        // 5 & 6. Revert each student's enrollment
        int undoneCount = 0;

        for (var historyRecord : transferRecords) {
            UUID studentId = historyRecord.getStudentId();

            try {
                // 5. Check for conflicts - ensure student is still in destination class
                var currentEnrollment = enrollmentRepository
                    .findByStudentIdAndClassIdAndStatus(
                        studentId,
                        destinationClassId,
                        com.sms.student.enums.EnrollmentStatus.ACTIVE
                    );

                if (currentEnrollment.isEmpty()) {
                    log.warn("Student {} no longer in destination class, skipping undo", studentId);
                    continue;
                }

                // Find the original source enrollment (should be TRANSFERRED status)
                var originalEnrollment = enrollmentRepository
                    .findByStudentIdAndClassIdAndStatus(
                        studentId,
                        sourceClassId,
                        com.sms.student.enums.EnrollmentStatus.TRANSFERRED
                    );

                if (originalEnrollment.isEmpty()) {
                    log.warn("Original enrollment not found for student {}, skipping undo", studentId);
                    continue;
                }

                // 6. Revert enrollments
                // Remove from destination class
                var destEnrollment = currentEnrollment.get();
                enrollmentRepository.delete(destEnrollment);

                // Reactivate in source class
                var srcEnrollment = originalEnrollment.get();
                srcEnrollment.setStatus(com.sms.student.enums.EnrollmentStatus.ACTIVE);
                srcEnrollment.setTransferDate(null);
                enrollmentRepository.save(srcEnrollment);

                // Update class counts
                sourceClass.incrementEnrollment();
                destinationClass.decrementEnrollment();

                // 7. Create undo history record
                var undoRecord = com.sms.student.model.EnrollmentHistory.builder()
                    .studentId(studentId)
                    .classId(sourceClassId)
                    .action(com.sms.student.model.EnrollmentHistory.EnrollmentAction.UNDO)
                    .reason("Undo of batch transfer")
                    .performedByUserId(performedByUserId)
                    .undoOfTransferId(transferId)
                    .metadata("{\"originalTransferId\":\"" + transferId + "\"}")
                    .build();
                enrollmentHistoryRepository.save(undoRecord);

                undoneCount++;

            } catch (Exception e) {
                log.error("Failed to undo transfer for student {}: {}", studentId, e.getMessage());
                // Continue with other students
            }
        }

        // Save updated class counts
        classRepository.save(sourceClass);
        classRepository.save(destinationClass);

        log.info("Undo completed: {} students reverted", undoneCount);

        // 8. Return response
        return UndoTransferResponse.builder()
            .transferId(transferId)
            .undoneStudents(undoneCount)
            .sourceClassId(sourceClassId)
            .undoneAt(now)
            .build();
    }

    /**
     * Helper method to extract class ID from JSON metadata string
     */
    private UUID extractClassIdFromMetadata(String metadata, String key) {
        try {
            // Simple JSON parsing for "{\"sourceClassId\":\"uuid\",\"destinationClassId\":\"uuid\"}"
            String searchKey = "\"" + key + "\":\"";
            int startIdx = metadata.indexOf(searchKey) + searchKey.length();
            int endIdx = metadata.indexOf("\"", startIdx);
            String uuidStr = metadata.substring(startIdx, endIdx);
            return UUID.fromString(uuidStr);
        } catch (Exception e) {
            log.error("Failed to extract {} from metadata: {}", key, metadata);
            throw new RuntimeException("INVALID_METADATA");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUndoTransfer(UUID transferId, UUID performedByUserId) {
        log.debug("Checking if transfer {} can be undone by user {}", transferId, performedByUserId);

        // TODO: Implement in Phase 6 (T050)
        // 1. Validate transfer exists
        // 2. Check authorization (performedByUserId matches original user)
        // 3. Check not already undone
        // 4. Check within time window (5 minutes)
        // 5. Check for conflicts (students not transferred again)
        // 6. Return true only if all conditions pass

        throw new UnsupportedOperationException("Not yet implemented - Phase 6 (T050)");
    }
}
