package com.sms.student.service;

import com.sms.student.dto.ParentContactRequest;
import com.sms.student.dto.ParentContactResponse;
import com.sms.student.model.ParentContact;
import com.sms.student.model.Student;
import com.sms.student.exception.InvalidStudentDataException;
import com.sms.student.exception.ParentContactNotFoundException;
import com.sms.student.exception.StudentNotFoundException;
import com.sms.student.repository.ParentContactRepository;
import com.sms.student.repository.StudentRepository;
import com.sms.student.service.ParentContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ParentContactService.
 * Handles parent contact CRUD operations with validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ParentContactServiceImpl implements ParentContactService {

    private final ParentContactRepository parentContactRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public ParentContactResponse addParentContact(UUID studentId, ParentContactRequest request) {
        log.info("Adding parent contact for student: {}", studentId);

        // Verify student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.error("Student not found: {}", studentId);
                    return new StudentNotFoundException("Student with ID " + studentId + " not found");
                });

        // T106: Validate primary contact constraint
        if (request.getIsPrimary()) {
            validateNoPrimaryContactExists(studentId);
        }

        // T107: Phone number validation is handled by @Pattern annotation in ParentContact entity

        // Create new parent contact
        ParentContact contact = ParentContact.builder()
                .student(student)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .relationship(request.getRelationship())
                .isPrimary(request.getIsPrimary())
                .build();

        ParentContact saved = parentContactRepository.save(contact);
        log.info("Added parent contact: {} for student: {}", saved.getId(), studentId);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ParentContactResponse updateParentContact(UUID contactId, ParentContactRequest request) {
        log.info("Updating parent contact: {}", contactId);

        // Find existing contact
        ParentContact contact = parentContactRepository.findById(contactId)
                .orElseThrow(() -> {
                    log.error("Parent contact not found: {}", contactId);
                    return new ParentContactNotFoundException("Parent contact with ID " + contactId + " not found");
                });

        UUID studentId = contact.getStudent().getId();

        // T106: Validate primary contact constraint if changing to primary
        if (request.getIsPrimary() && !contact.getIsPrimary()) {
            validateNoPrimaryContactExists(studentId);
        }

        // Update fields
        contact.setFullName(request.getFullName());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setRelationship(request.getRelationship());
        contact.setIsPrimary(request.getIsPrimary());

        ParentContact updated = parentContactRepository.save(contact);
        log.info("Updated parent contact: {}", contactId);

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentContactResponse> getParentContactsByStudent(UUID studentId) {
        log.info("Fetching parent contacts for student: {}", studentId);

        // Verify student exists
        if (!studentRepository.existsById(studentId)) {
            log.error("Student not found: {}", studentId);
            throw new StudentNotFoundException("Student with ID " + studentId + " not found");
        }

        List<ParentContact> contacts = parentContactRepository.findByStudentId(studentId);
        log.info("Found {} parent contacts for student: {}", contacts.size(), studentId);

        return contacts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ParentContactResponse getParentContactById(UUID contactId) {
        log.info("Fetching parent contact: {}", contactId);

        ParentContact contact = parentContactRepository.findById(contactId)
                .orElseThrow(() -> {
                    log.error("Parent contact not found: {}", contactId);
                    return new ParentContactNotFoundException("Parent contact with ID " + contactId + " not found");
                });

        return mapToResponse(contact);
    }

    @Override
    @Transactional
    public void deleteParentContact(UUID contactId) {
        log.info("Deleting parent contact: {}", contactId);

        ParentContact contact = parentContactRepository.findById(contactId)
                .orElseThrow(() -> {
                    log.error("Parent contact not found: {}", contactId);
                    return new ParentContactNotFoundException("Parent contact with ID " + contactId + " not found");
                });

        UUID studentId = contact.getStudent().getId();

        // Validate at least one contact will remain
        List<ParentContact> allContacts = parentContactRepository.findByStudentId(studentId);
        if (allContacts.size() <= 1) {
            log.error("Cannot delete last parent contact for student: {}", studentId);
            throw new InvalidStudentDataException("At least one parent contact must remain for the student");
        }

        parentContactRepository.delete(contact);
        log.info("Deleted parent contact: {}", contactId);
    }

    /**
     * T106: Validate that no primary contact already exists for the student
     */
    private void validateNoPrimaryContactExists(UUID studentId) {
        List<ParentContact> contacts = parentContactRepository.findByStudentId(studentId);
        boolean hasPrimary = contacts.stream().anyMatch(ParentContact::getIsPrimary);

        if (hasPrimary) {
            log.error("Student {} already has a primary contact", studentId);
            throw new InvalidStudentDataException("Only one parent contact can be marked as primary");
        }
    }

    /**
     * Map ParentContact entity to ParentContactResponse DTO
     */
    private ParentContactResponse mapToResponse(ParentContact contact) {
        return ParentContactResponse.builder()
                .id(contact.getId())
                .fullName(contact.getFullName())
                .phoneNumber(contact.getPhoneNumber())
                .relationship(contact.getRelationship())
                .isPrimary(contact.getIsPrimary())
                .build();
    }
}
