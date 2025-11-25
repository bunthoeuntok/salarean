package com.sms.student.service.interfaces;

import com.sms.student.dto.EnrollmentHistoryResponse;
import com.sms.student.dto.EnrollmentRequest;
import com.sms.student.dto.EnrollmentResponse;
import com.sms.student.dto.TransferRequest;

import java.util.UUID;

public interface IEnrollmentService {

    /**
     * Get complete enrollment history for a student.
     * Returns all past and current enrollments ordered by date (most recent first).
     *
     * @param studentId the student's UUID
     * @return enrollment history with status counts
     */
    EnrollmentHistoryResponse getEnrollmentHistory(UUID studentId);

    /**
     * Enroll a student in a class.
     * Validates student exists, class exists, no duplicate enrollment, and capacity available.
     * Creates enrollment with status ACTIVE and reason NEW.
     * Increments class student_count.
     *
     * @param studentId the student's UUID
     * @param request enrollment request containing classId and optional notes
     * @return created enrollment with denormalized fields
     * @throws StudentNotFoundException if student doesn't exist
     * @throws ClassNotFoundException if class doesn't exist
     * @throws DuplicateEnrollmentException if student already enrolled in the class
     * @throws ClassCapacityExceededException if class is full
     */
    EnrollmentResponse enrollStudent(UUID studentId, EnrollmentRequest request);

    /**
     * Transfer a student from their current class to a new class.
     * Validates student exists, has active enrollment, target class exists and has capacity.
     * Marks old enrollment as TRANSFERRED with end_date, transfer_date, and transfer_reason.
     * Creates new enrollment with status ACTIVE and reason TRANSFER.
     * Updates both class student counts atomically.
     *
     * @param studentId the student's UUID
     * @param request transfer request containing targetClassId and reason
     * @return new enrollment with denormalized fields
     * @throws StudentNotFoundException if student doesn't exist
     * @throws EnrollmentNotFoundException if student has no active enrollment
     * @throws ClassNotFoundException if target class doesn't exist
     * @throws ClassCapacityExceededException if target class is full
     */
    EnrollmentResponse transferStudent(UUID studentId, TransferRequest request);
}
