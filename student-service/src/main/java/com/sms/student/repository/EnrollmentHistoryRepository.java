package com.sms.student.repository;

import com.sms.student.model.EnrollmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for enrollment history operations.
 * Supports transfer tracking and undo conflict detection.
 */
@Repository
public interface EnrollmentHistoryRepository extends JpaRepository<EnrollmentHistory, UUID> {

    /**
     * Find all enrollment history records for a specific transfer operation.
     *
     * @param transferId the transfer ID
     * @return list of enrollment history records
     */
    List<EnrollmentHistory> findByTransferId(UUID transferId);

    /**
     * Find all undo records for a specific transfer operation.
     *
     * @param undoOfTransferId the transfer ID that was undone
     * @return list of undo history records
     */
    List<EnrollmentHistory> findByUndoOfTransferId(UUID undoOfTransferId);

    /**
     * Find the most recent transfer operation by transfer ID.
     *
     * @param transferId the transfer ID
     * @return the most recent enrollment history record for this transfer
     */
    Optional<EnrollmentHistory> findFirstByTransferIdOrderByPerformedAtDesc(UUID transferId);

    /**
     * Check if any students from a transfer have been transferred again after a given timestamp.
     * Used for undo conflict detection.
     *
     * @param studentIds list of student IDs from the original transfer
     * @param afterTimestamp timestamp of the original transfer
     * @return true if any conflicts exist, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(eh) > 0 THEN true ELSE false END " +
           "FROM EnrollmentHistory eh " +
           "WHERE eh.studentId IN :studentIds " +
           "AND eh.action = 'TRANSFERRED' " +
           "AND eh.performedAt > :afterTimestamp")
    boolean existsConflictingTransfers(
        @Param("studentIds") List<UUID> studentIds,
        @Param("afterTimestamp") LocalDateTime afterTimestamp
    );

    /**
     * Find all enrollment history for a student within a date range.
     *
     * @param studentId the student ID
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of enrollment history records
     */
    @Query("SELECT eh FROM EnrollmentHistory eh " +
           "WHERE eh.studentId = :studentId " +
           "AND eh.performedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY eh.performedAt DESC")
    List<EnrollmentHistory> findByStudentIdAndDateRange(
        @Param("studentId") UUID studentId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find all enrollment history for a class within a date range.
     *
     * @param classId the class ID
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of enrollment history records
     */
    @Query("SELECT eh FROM EnrollmentHistory eh " +
           "WHERE eh.classId = :classId " +
           "AND eh.performedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY eh.performedAt DESC")
    List<EnrollmentHistory> findByClassIdAndDateRange(
        @Param("classId") UUID classId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Check if a transfer has already been undone.
     *
     * @param transferId the transfer ID
     * @return true if undo record exists, false otherwise
     */
    boolean existsByUndoOfTransferId(UUID transferId);

    /**
     * Find all students who were transferred in a specific transfer operation.
     *
     * @param transferId the transfer ID
     * @return list of student IDs
     */
    @Query("SELECT DISTINCT eh.studentId FROM EnrollmentHistory eh " +
           "WHERE eh.transferId = :transferId " +
           "AND eh.action = 'TRANSFERRED'")
    List<UUID> findStudentIdsByTransferId(@Param("transferId") UUID transferId);
}
