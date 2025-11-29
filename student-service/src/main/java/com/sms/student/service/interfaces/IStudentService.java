package com.sms.student.service.interfaces;

import com.sms.student.dto.*;
import com.sms.student.enums.DeletionReason;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for student management operations.
 * Handles student CRUD operations, enrollment, and parent contact management.
 */
public interface IStudentService {

    /**
     * Create a new student profile with parent contacts.
     * Automatically generates student code in format: STU-YYYY-NNNN
     * Creates enrollment record if classId is provided.
     *
     * @param request Student creation request with required fields
     * @return Created student response with generated ID and student code
     * @throws com.sms.student.exception.InvalidStudentDataException if validation fails
     * @throws com.sms.student.exception.DuplicateStudentCodeException if student code already exists
     * @throws com.sms.student.exception.ClassNotFoundException if classId is invalid
     * @throws com.sms.student.exception.ClassCapacityExceededException if class is full
     */
    StudentResponse createStudent(StudentRequest request);

    /**
     * Update existing student information.
     * Cannot change student code or creation date.
     * Does not update enrollment info - use EnrollmentService for that.
     *
     * @param id Student UUID
     * @param request Updated student data (without enrollment fields)
     * @return Updated student response
     * @throws com.sms.student.exception.StudentNotFoundException if student not found
     * @throws com.sms.student.exception.InvalidStudentDataException if validation fails
     */
    StudentResponse updateStudent(UUID id, StudentUpdateRequest request);

    /**
     * Soft delete a student (set status to INACTIVE).
     * Records deletion reason and timestamp.
     *
     * @param id Student UUID
     * @param reason Deletion reason enum
     * @param deletedBy User ID performing deletion
     * @throws com.sms.student.exception.StudentNotFoundException if student not found
     */
    void deleteStudent(UUID id, DeletionReason reason, UUID deletedBy);

    /**
     * Get student details by ID including parent contacts and current class.
     *
     * @param id Student UUID
     * @return Student response with full details
     * @throws com.sms.student.exception.StudentNotFoundException if student not found
     */
    StudentResponse getStudentById(UUID id);

    /**
     * Get student details by student code.
     *
     * @param studentCode Student code (e.g., STU-2025-0001)
     * @return Student response with full details
     * @throws com.sms.student.exception.StudentNotFoundException if student not found
     */
    StudentResponse getStudentByCode(String studentCode);

    /**
     * List all active students in a specific class.
     *
     * @param classId Class UUID
     * @param pageable Pagination parameters
     * @return Paginated list of student summaries
     * @throws com.sms.student.exception.ClassNotFoundException if class not found
     */
    StudentListResponse listStudentsByClass(UUID classId, Pageable pageable);

    /**
     * List all active students with pagination.
     *
     * @param pageable Pagination parameters
     * @return Paginated list of student summaries
     */
    StudentListResponse listActiveStudents(Pageable pageable);

    /**
     * List students with pagination and filtering.
     *
     * @param search   search term for name or student code (optional)
     * @param status   filter by status, comma-separated (optional)
     * @param gender   filter by gender, comma-separated (optional)
     * @param classId  filter by class ID (optional)
     * @param pageable pagination parameters
     * @return Paginated list of student summaries
     */
    StudentListResponse listStudentsWithFilters(String search, String status, String gender,
                                                 UUID classId, Pageable pageable);

    /**
     * Upload student photo.
     *
     * @param id Student UUID
     * @param photoData Photo file bytes
     * @param contentType MIME type (image/jpeg or image/png)
     * @return Photo upload response with URL
     * @throws com.sms.student.exception.StudentNotFoundException if student not found
     * @throws com.sms.student.exception.PhotoSizeExceededException if file too large
     * @throws com.sms.student.exception.PhotoProcessingException if processing fails
     */
    PhotoUploadResponse uploadStudentPhoto(UUID id, byte[] photoData, String contentType);

    /**
     * Add a new parent contact to a student.
     * Convenience method that delegates to ParentContactService.
     *
     * @param studentId Student UUID
     * @param request Parent contact data
     * @return Created parent contact response
     * @throws com.sms.student.exception.StudentNotFoundException if student not found
     * @throws com.sms.student.exception.InvalidStudentDataException if validation fails
     */
    ParentContactResponse addParentContact(UUID studentId, ParentContactRequest request);

    /**
     * Update an existing parent contact.
     * Convenience method that delegates to ParentContactService.
     *
     * @param contactId Parent contact UUID
     * @param request Updated parent contact data
     * @return Updated parent contact response
     * @throws com.sms.student.exception.ParentContactNotFoundException if contact not found
     * @throws com.sms.student.exception.InvalidStudentDataException if validation fails
     */
    ParentContactResponse updateParentContact(UUID contactId, ParentContactRequest request);
}
