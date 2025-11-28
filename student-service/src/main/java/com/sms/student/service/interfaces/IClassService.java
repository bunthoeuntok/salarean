package com.sms.student.service.interfaces;

import com.sms.student.dto.ClassDetailDto;
import com.sms.student.dto.ClassListResponse;
import com.sms.student.dto.ClassSummaryDto;
import com.sms.student.dto.StudentRosterItemDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for class management operations.
 *
 * <p>Handles business logic for:
 * <ul>
 *   <li>Viewing teacher's classes</li>
 *   <li>Viewing class details and enrollment history</li>
 *   <li>Creating and updating classes</li>
 *   <li>Archiving classes</li>
 * </ul>
 * </p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
public interface IClassService {

    /**
     * List all classes for a specific teacher.
     *
     * @param teacherId      UUID of the teacher
     * @param includeArchived whether to include archived classes
     * @return list of class summaries
     */
    List<ClassSummaryDto> listTeacherClasses(UUID teacherId, boolean includeArchived);

    /**
     * List all classes for a specific teacher with pagination.
     *
     * @param teacherId       UUID of the teacher
     * @param includeArchived whether to include archived classes
     * @param pageable        pagination parameters
     * @return paginated list of class summaries
     */
    ClassListResponse listTeacherClassesPaginated(UUID teacherId, boolean includeArchived, Pageable pageable);

    /**
     * Get detailed information about a specific class.
     *
     * <p>Verifies that the class belongs to the specified teacher before returning data.
     * Throws UnauthorizedClassAccessException if the teacher doesn't own the class.</p>
     *
     * @param classId   UUID of the class
     * @param teacherId UUID of the teacher requesting the details
     * @return detailed class information including enrolled students
     * @throws com.sms.student.exception.ClassNotFoundException if class doesn't exist
     * @throws com.sms.student.exception.UnauthorizedClassAccessException if class doesn't belong to teacher
     */
    ClassDetailDto getClassDetails(UUID classId, UUID teacherId);

    /**
     * Get list of students enrolled in a specific class.
     *
     * <p>Verifies that the class belongs to the specified teacher before returning data.</p>
     *
     * @param classId   UUID of the class
     * @param teacherId UUID of the teacher requesting the roster
     * @return list of students enrolled in the class
     * @throws com.sms.student.exception.ClassNotFoundException if class doesn't exist
     * @throws com.sms.student.exception.UnauthorizedClassAccessException if class doesn't belong to teacher
     */
    List<StudentRosterItemDto> getClassStudents(UUID classId, UUID teacherId);

    /**
     * Get enrollment history for a specific class.
     *
     * <p>Returns all enrollment events (current and past) for the class,
     * ordered by enrollment date descending (newest first).</p>
     *
     * @param classId   UUID of the class
     * @param teacherId UUID of the teacher requesting the history
     * @return list of enrollment history events
     * @throws com.sms.student.exception.ClassNotFoundException if class doesn't exist
     * @throws com.sms.student.exception.UnauthorizedClassAccessException if class doesn't belong to teacher
     */
    List<com.sms.student.dto.EnrollmentHistoryDto> getEnrollmentHistory(UUID classId, UUID teacherId);

    /**
     * Create a new class.
     *
     * <p>Validates academic year format, checks for duplicate classes,
     * and creates a new class with the specified details.</p>
     *
     * @param request   class creation request containing class details
     * @param teacherId UUID of the teacher creating the class
     * @return detailed information about the newly created class
     * @throws com.sms.student.exception.DuplicateClassException if class with same schoolId, grade, section, and academicYear already exists
     * @throws IllegalArgumentException if academic year format is invalid
     */
    ClassDetailDto createClass(com.sms.student.dto.ClassCreateRequest request, UUID teacherId);

    /**
     * Update an existing class.
     *
     * <p>Updates class information with the provided fields. Only non-null fields
     * are updated. Validates academic year format if provided and checks for
     * duplicate classes if schoolId, grade, section, or academicYear changes.</p>
     *
     * @param classId   UUID of the class to update
     * @param request   class update request containing fields to update
     * @param teacherId UUID of the teacher updating the class
     * @return detailed information about the updated class
     * @throws com.sms.student.exception.ClassNotFoundException if class doesn't exist
     * @throws com.sms.student.exception.UnauthorizedClassAccessException if class doesn't belong to teacher
     * @throws com.sms.student.exception.DuplicateClassException if update would create a duplicate class
     * @throws IllegalArgumentException if academic year format is invalid
     */
    ClassDetailDto updateClass(UUID classId, com.sms.student.dto.ClassUpdateRequest request, UUID teacherId);

    /**
     * Archive a class.
     *
     * <p>Changes the class status to ARCHIVED. Archived classes are excluded from
     * default class lists but remain accessible for historical purposes. Students
     * remain enrolled in archived classes.</p>
     *
     * @param classId   UUID of the class to archive
     * @param teacherId UUID of the teacher archiving the class
     * @return detailed information about the archived class
     * @throws com.sms.student.exception.ClassNotFoundException if class doesn't exist
     * @throws com.sms.student.exception.UnauthorizedClassAccessException if class doesn't belong to teacher
     */
    ClassDetailDto archiveClass(UUID classId, UUID teacherId);
}
