package com.sms.student.service;

import com.sms.student.dto.*;
import com.sms.student.model.ParentContact;
import com.sms.student.model.SchoolClass;
import com.sms.student.model.Student;
import com.sms.student.model.StudentClassEnrollment;
import com.sms.student.enums.DeletionReason;
import com.sms.student.enums.EnrollmentReason;
import com.sms.student.enums.StudentStatus;
import com.sms.student.exception.*;
import com.sms.student.repository.*;
import com.sms.student.repository.specification.StudentSpecification;
import com.sms.student.service.interfaces.IParentContactService;
import com.sms.student.service.interfaces.IPhotoStorageService;
import com.sms.student.service.interfaces.IStudentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of StudentService.
 * Handles all student-related business logic including CRUD operations,
 * enrollment management, and photo uploads.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService implements IStudentService {

    private final StudentRepository studentRepository;
    private final ParentContactRepository parentContactRepository;
    private final StudentClassEnrollmentRepository enrollmentRepository;
    private final ClassRepository classRepository;
    private final IPhotoStorageService photoStorageService;
    private final IParentContactService parentContactService;

    @Override
    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        log.info("Creating new student: {} {}", request.getFirstName(), request.getLastName());

        // Validate at least one parent contact exists
        if (request.getParentContacts() == null || request.getParentContacts().isEmpty()) {
            log.error("No parent contacts provided");
            throw new InvalidStudentDataException("At least one parent contact is required");
        }

        // Validate only one primary contact
        long primaryCount = request.getParentContacts().stream()
                .filter(ParentContactRequest::getIsPrimary)
                .count();
        if (primaryCount == 0) {
            log.error("No primary contact designated");
            throw new InvalidStudentDataException("At least one parent contact must be marked as primary");
        }
        if (primaryCount > 1) {
            log.error("Multiple primary contacts designated: {}", primaryCount);
            throw new InvalidStudentDataException("Only one parent contact can be marked as primary");
        }

        // Validate and check class capacity if classId provided
        SchoolClass schoolClass = null;
        if (request.getClassId() != null) {
            schoolClass = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> {
                        log.error("Class not found: {}", request.getClassId());
                        return new com.sms.student.exception.ClassNotFoundException("Class with ID " + request.getClassId() + " not found");
                    });

            // Check if class has capacity
            if (!schoolClass.hasCapacity()) {
                log.error("Class {} is at full capacity: {}/{}",
                         schoolClass.getId(),
                         schoolClass.getStudentCount(),
                         schoolClass.getMaxCapacity());
                throw new ClassCapacityExceededException(
                        "Class is at full capacity (" + schoolClass.getMaxCapacity() + " students)");
            }
        }

        // Generate unique student code
        String studentCode = generateStudentCode();

        // Double-check uniqueness
        int attempts = 0;
        while (studentRepository.existsByStudentCode(studentCode) && attempts < 10) {
            log.warn("Student code {} already exists, regenerating...", studentCode);
            studentCode = generateStudentCode();
            attempts++;
        }

        if (attempts >= 10) {
            log.error("Failed to generate unique student code after 10 attempts");
            throw new DuplicateStudentCodeException("Failed to generate unique student code");
        }

        // Build student entity
        Student student = Student.builder()
                .studentCode(studentCode)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .firstNameKhmer(request.getFirstNameKhmer())
                .lastNameKhmer(request.getLastNameKhmer())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .emergencyContact(request.getEmergencyContact())
                .enrollmentDate(request.getEnrollmentDate())
                .status(StudentStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .parentContacts(new ArrayList<>())
                .build();

        // Save student first to get ID
        student = studentRepository.save(student);
        log.info("Student created with ID: {} and code: {}", student.getId(), student.getStudentCode());

        // Create parent contacts
        final Student savedStudent = student;
        List<ParentContact> contacts = request.getParentContacts().stream()
                .map(contactReq -> ParentContact.builder()
                        .student(savedStudent)
                        .fullName(contactReq.getFullName())
                        .phoneNumber(contactReq.getPhoneNumber())
                        .relationship(contactReq.getRelationship())
                        .isPrimary(contactReq.getIsPrimary())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        parentContactRepository.saveAll(contacts);
        log.info("Created {} parent contacts for student {}", contacts.size(), student.getId());

        // Create enrollment record if class provided
        if (schoolClass != null) {
            StudentClassEnrollment enrollment = StudentClassEnrollment.builder()
                    .studentId(student.getId())
                    .classId(schoolClass.getId())
                    .enrollmentDate(LocalDate.now())
                    .reason(EnrollmentReason.NEW)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            enrollmentRepository.save(enrollment);

            // Increment class student count
            schoolClass.incrementEnrollment();
            classRepository.save(schoolClass);

            log.info("Enrolled student {} in class {}", student.getId(), schoolClass.getId());
        }

        // Map to response DTO
        return mapToStudentResponse(student, contacts,
                                    schoolClass != null ? schoolClass.getId() : null);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(UUID id, StudentUpdateRequest request) {
        log.info("Updating student: {}", id);

        // Fetch student with retry logic for optimistic locking
        Student student = fetchStudentWithRetry(id, 3);

        // Validate at least one parent contact exists
        if (request.getParentContacts() == null || request.getParentContacts().isEmpty()) {
            log.error("No parent contacts provided for update");
            throw new InvalidStudentDataException("At least one parent contact is required");
        }

        // Validate only one primary contact
        long primaryCount = request.getParentContacts().stream()
                .filter(ParentContactRequest::getIsPrimary)
                .count();
        if (primaryCount == 0) {
            log.error("No primary contact designated");
            throw new InvalidStudentDataException("At least one parent contact must be marked as primary");
        }
        if (primaryCount > 1) {
            log.error("Multiple primary contacts designated: {}", primaryCount);
            throw new InvalidStudentDataException("Only one parent contact can be marked as primary");
        }

        // Update basic fields (student code, enrollment date, and class are immutable via this endpoint)
        // Use EnrollmentController to change class enrollment
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setFirstNameKhmer(request.getFirstNameKhmer());
        student.setLastNameKhmer(request.getLastNameKhmer());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setGender(request.getGender());
        student.setAddress(request.getAddress());
        student.setUpdatedAt(LocalDateTime.now());

        // Update parent contacts: delete all existing and create new ones
        // This is simpler than trying to match and update individual contacts
        List<ParentContact> existingContacts = parentContactRepository.findByStudentId(id);
        if (!existingContacts.isEmpty()) {
            parentContactRepository.deleteAll(existingContacts);
            log.info("Deleted {} existing parent contacts for student {}", existingContacts.size(), id);
        }

        // Create new parent contacts from request
        final Student savedStudent = student;
        List<ParentContact> newContacts = request.getParentContacts().stream()
                .map(contactReq -> ParentContact.builder()
                        .student(savedStudent)
                        .fullName(contactReq.getFullName())
                        .phoneNumber(contactReq.getPhoneNumber())
                        .relationship(contactReq.getRelationship())
                        .isPrimary(contactReq.getIsPrimary())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        parentContactRepository.saveAll(newContacts);
        log.info("Created {} new parent contacts for student {}", newContacts.size(), id);

        // Save student changes
        student = studentRepository.save(student);
        log.info("Student {} updated successfully", id);

        // Get current enrollment
        UUID currentClassId = enrollmentRepository.findCurrentClassIdByStudentId(id)
                .orElse(null);

        return mapToStudentResponse(student, newContacts, currentClassId);
    }

    /**
     * Fetch student with retry logic for optimistic locking conflicts
     */
    private Student fetchStudentWithRetry(UUID id, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return studentRepository.findById(id)
                        .orElseThrow(() -> {
                            log.error("Student not found: {}", id);
                            return new StudentNotFoundException("Student with ID " + id + " not found");
                        });
            } catch (org.springframework.dao.OptimisticLockingFailureException e) {
                if (attempt == maxAttempts) {
                    log.error("Failed to fetch student {} after {} attempts due to optimistic locking", id, maxAttempts);
                    throw new InvalidStudentDataException("Student is being updated by another process. Please try again.");
                }
                log.warn("Optimistic locking conflict fetching student {}, attempt {}/{}", id, attempt, maxAttempts);
                try {
                    Thread.sleep(100 * attempt); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new InvalidStudentDataException("Update interrupted");
                }
            }
        }
        throw new InvalidStudentDataException("Failed to fetch student for update");
    }

    @Override
    @Transactional
    public void deleteStudent(UUID id, DeletionReason reason, UUID deletedBy) {
        log.info("Soft deleting student: {} by user: {}", id, deletedBy);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Student not found: {}", id);
                    return new StudentNotFoundException("Student with ID " + id + " not found");
                });

        // Soft delete
        student.setStatus(StudentStatus.INACTIVE);
        student.setDeletedAt(LocalDateTime.now());
        student.setDeletedBy(deletedBy);
        student.setDeletionReason(reason != null ? reason.name() : null);
        student.setUpdatedAt(LocalDateTime.now());

        studentRepository.save(student);
        log.info("Student {} soft deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(UUID id) {
        log.info("Fetching student by ID: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Student not found: {}", id);
                    return new StudentNotFoundException("Student with ID " + id + " not found");
                });

        List<ParentContact> contacts = parentContactRepository.findByStudentId(id);
        UUID currentClassId = enrollmentRepository.findCurrentClassIdByStudentId(id)
                .orElse(null);

        return mapToStudentResponse(student, contacts, currentClassId);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentByCode(String studentCode) {
        log.info("Fetching student by code: {}", studentCode);

        Student student = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> {
                    log.error("Student not found with code: {}", studentCode);
                    return new StudentNotFoundException("Student with code " + studentCode + " not found");
                });

        List<ParentContact> contacts = parentContactRepository.findByStudentId(student.getId());
        UUID currentClassId = enrollmentRepository.findCurrentClassIdByStudentId(student.getId())
                .orElse(null);

        return mapToStudentResponse(student, contacts, currentClassId);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentListResponse listStudentsByClass(UUID classId, Pageable pageable) {
        log.info("Listing students for class: {}, page: {}", classId, pageable.getPageNumber());

        // Verify class exists
        classRepository.findById(classId)
                .orElseThrow(() -> {
                    log.error("Class not found: {}", classId);
                    return new com.sms.student.exception.ClassNotFoundException("Class with ID " + classId + " not found");
                });

        Page<Student> studentPage = studentRepository.findByClassIdAndStatus(classId, pageable);

        List<StudentSummary> summaries = studentPage.getContent().stream()
                .map(this::mapToStudentSummary)
                .collect(Collectors.toList());

        return StudentListResponse.builder()
                .content(summaries)
                .page(studentPage.getNumber())
                .size(studentPage.getSize())
                .totalElements(studentPage.getTotalElements())
                .totalPages(studentPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentListResponse listActiveStudents(Pageable pageable) {
        log.info("Listing active students, page: {}", pageable.getPageNumber());

        Page<Student> studentPage = studentRepository.findByStatus(StudentStatus.ACTIVE, pageable);

        List<StudentSummary> summaries = studentPage.getContent().stream()
                .map(this::mapToStudentSummary)
                .collect(Collectors.toList());

        return StudentListResponse.builder()
                .content(summaries)
                .page(studentPage.getNumber())
                .size(studentPage.getSize())
                .totalElements(studentPage.getTotalElements())
                .totalPages(studentPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentListResponse listStudentsWithFilters(String search, String status, String gender,
                                                        String classId, Pageable pageable) {
        log.info("Listing students with filters: search={}, status={}, gender={}, classId={}",
                 search, status, gender, classId);

        // Build specification with all filters
        // Note: @Where annotation on Student entity handles soft-delete filtering
        org.springframework.data.jpa.domain.Specification<Student> spec =
                org.springframework.data.jpa.domain.Specification
                        .where(StudentSpecification.hasStatus(status))
                        .and(StudentSpecification.hasGender(gender))
                        .and(StudentSpecification.hasClassId(classId))
                        .and(StudentSpecification.searchByNameOrCode(search));

        // Fetch from database with specification
        Page<Student> studentPage = studentRepository.findAll(spec, pageable);

        log.debug("Found {} students (page {} of {})",
                 studentPage.getNumberOfElements(),
                 studentPage.getNumber() + 1,
                 studentPage.getTotalPages());

        List<StudentSummary> summaries = studentPage.getContent().stream()
                .map(this::mapToStudentSummary)
                .collect(Collectors.toList());

        return StudentListResponse.builder()
                .content(summaries)
                .page(studentPage.getNumber())
                .size(studentPage.getSize())
                .totalElements(studentPage.getTotalElements())
                .totalPages(studentPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public PhotoUploadResponse uploadStudentPhoto(UUID id, byte[] photoData, String contentType) {
        log.info("Uploading photo for student: {}, size: {} bytes", id, photoData.length);

        // Verify student exists
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Student not found: {}", id);
                    return new StudentNotFoundException("Student with ID " + id + " not found");
                });

        // Save photo (validates MIME type, file size, resizes, and deletes old photos)
        String photoPath = photoStorageService.savePhoto(id, photoData, contentType);

        // Update student entity with photo path
        student.setPhotoUrl(photoPath);
        student.setUpdatedAt(LocalDateTime.now());
        studentRepository.save(student);

        log.info("Photo uploaded successfully for student: {}, path: {}", id, photoPath);

        return PhotoUploadResponse.builder()
                .photoUrl(photoPath)
                .thumbnailUrl(photoPath.replace(".jpg", "_thumb.jpg").replace(".png", "_thumb.png"))
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Generate student code in format: STU-YYYY-NNNN
     * Uses current year and a random 4-digit number.
     */
    private String generateStudentCode() {
        int currentYear = Year.now().getValue();
        int randomNumber = (int) (Math.random() * 10000); // 0-9999
        return String.format("STU-%04d-%04d", currentYear, randomNumber);
    }

    /**
     * Map Student entity to StudentResponse DTO
     */
    private StudentResponse mapToStudentResponse(Student student, List<ParentContact> contacts, UUID currentClassId) {
        List<ParentContactResponse> contactResponses = contacts.stream()
                .map(contact -> ParentContactResponse.builder()
                        .id(contact.getId())
                        .fullName(contact.getFullName())
                        .phoneNumber(contact.getPhoneNumber())
                        .relationship(contact.getRelationship())
                        .isPrimary(contact.getIsPrimary())
                        .build())
                .collect(Collectors.toList());

        // Build full names
        String fullName = student.getFirstName() + " " + student.getLastName();
        String fullNameKhmer = null;
        if (student.getFirstNameKhmer() != null && student.getLastNameKhmer() != null) {
            fullNameKhmer = student.getFirstNameKhmer() + " " + student.getLastNameKhmer();
        } else if (student.getFirstNameKhmer() != null) {
            fullNameKhmer = student.getFirstNameKhmer();
        } else if (student.getLastNameKhmer() != null) {
            fullNameKhmer = student.getLastNameKhmer();
        }

        return StudentResponse.builder()
                .id(student.getId())
                .studentCode(student.getStudentCode())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .fullName(fullName)
                .firstNameKhmer(student.getFirstNameKhmer())
                .lastNameKhmer(student.getLastNameKhmer())
                .fullNameKhmer(fullNameKhmer)
                .dateOfBirth(student.getDateOfBirth())
                .age(student.getAge())
                .gender(student.getGender())
                .address(student.getAddress())
                .emergencyContact(student.getEmergencyContact())
                .enrollmentDate(student.getEnrollmentDate())
                .photoUrl(student.getPhotoUrl())
                .status(student.getStatus())
                .currentClassId(currentClassId)
                .parentContacts(contactResponses)
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }

    /**
     * Map Student entity to StudentSummary DTO
     */
    private StudentSummary mapToStudentSummary(Student student) {
        UUID currentClassId = enrollmentRepository.findCurrentClassIdByStudentId(student.getId())
                .orElse(null);

        String currentClassName = null;
        if (currentClassId != null) {
            currentClassName = classRepository.findById(currentClassId)
                    .map(c -> "Grade " + c.getGrade() + c.getSection())
                    .orElse(null);
        }

        ParentContact primaryContact = parentContactRepository.findPrimaryContactByStudentId(student.getId())
                .orElse(null);

        String primaryContactInfo = null;
        if (primaryContact != null) {
            primaryContactInfo = primaryContact.getFullName() + " (" + primaryContact.getPhoneNumber() + ")";
        }

        // Build full names
        String fullName = student.getFirstName() + " " + student.getLastName();
        String fullNameKhmer = null;
        if (student.getFirstNameKhmer() != null && student.getLastNameKhmer() != null) {
            fullNameKhmer = student.getFirstNameKhmer() + " " + student.getLastNameKhmer();
        } else if (student.getFirstNameKhmer() != null) {
            fullNameKhmer = student.getFirstNameKhmer();
        } else if (student.getLastNameKhmer() != null) {
            fullNameKhmer = student.getLastNameKhmer();
        }

        return StudentSummary.builder()
                .id(student.getId())
                .studentCode(student.getStudentCode())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .fullName(fullName)
                .fullNameKhmer(fullNameKhmer)
                .dateOfBirth(student.getDateOfBirth())
                .age(student.getAge())
                .currentClassId(currentClassId)
                .currentClassName(currentClassName)
                .primaryParentContact(primaryContactInfo)
                .photoUrl(student.getPhotoUrl())
                .gender(student.getGender().name())
                .status(student.getStatus().name())
                .build();
    }

    @Override
    @Transactional
    public ParentContactResponse addParentContact(UUID studentId, ParentContactRequest request) {
        log.info("Adding parent contact for student via StudentService: {}", studentId);
        return parentContactService.addParentContact(studentId, request);
    }

    @Override
    @Transactional
    public ParentContactResponse updateParentContact(UUID contactId, ParentContactRequest request) {
        log.info("Updating parent contact via StudentService: {}", contactId);
        return parentContactService.updateParentContact(contactId, request);
    }

    /**
     * Convert entity field names to database column names for native query sorting.
     * This is necessary because native SQL queries use database column names (snake_case)
     * while entity field names use Java naming conventions (camelCase).
     */
    private Pageable convertToNativeQueryPageable(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        org.springframework.data.domain.Sort newSort = org.springframework.data.domain.Sort.unsorted();
        for (org.springframework.data.domain.Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            // Convert camelCase to snake_case
            String columnName = camelCaseToSnakeCase(property);
            newSort = newSort.and(org.springframework.data.domain.Sort.by(order.getDirection(), columnName));
        }

        return org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                newSort
        );
    }

    /**
     * Convert camelCase string to snake_case.
     * Example: lastName -> last_name, firstNameKhmer -> first_name_khmer
     */
    private String camelCaseToSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
