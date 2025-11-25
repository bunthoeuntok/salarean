package com.sms.student.service.interfaces;

import com.sms.student.dto.ParentContactRequest;
import com.sms.student.dto.ParentContactResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing parent contacts independently.
 * Provides CRUD operations for parent contacts with validation.
 */
public interface IParentContactService {

    /**
     * Add a new parent contact to a student.
     * Validates that only one primary contact exists per student.
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
     * Validates primary contact constraint if isPrimary is changed.
     *
     * @param contactId Parent contact UUID
     * @param request Updated parent contact data
     * @return Updated parent contact response
     * @throws com.sms.student.exception.ParentContactNotFoundException if contact not found
     * @throws com.sms.student.exception.InvalidStudentDataException if validation fails
     */
    ParentContactResponse updateParentContact(UUID contactId, ParentContactRequest request);

    /**
     * Get all parent contacts for a student.
     *
     * @param studentId Student UUID
     * @return List of parent contact responses
     * @throws com.sms.student.exception.StudentNotFoundException if student not found
     */
    List<ParentContactResponse> getParentContactsByStudent(UUID studentId);

    /**
     * Get a single parent contact by ID.
     *
     * @param contactId Parent contact UUID
     * @return Parent contact response
     * @throws com.sms.student.exception.ParentContactNotFoundException if contact not found
     */
    ParentContactResponse getParentContactById(UUID contactId);

    /**
     * Delete a parent contact.
     * Validates that at least one contact remains after deletion.
     *
     * @param contactId Parent contact UUID
     * @throws com.sms.student.exception.ParentContactNotFoundException if contact not found
     * @throws com.sms.student.exception.InvalidStudentDataException if would leave student with no contacts
     */
    void deleteParentContact(UUID contactId);
}
