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
import com.sms.student.security.TeacherContextHolder;
import com.sms.student.service.interfaces.ICacheService;
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
import java.util.*;
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
    private final ICacheService cacheService;

    @Override
    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        // Validate parent contacts if provided
        List<ParentContact> contacts = new ArrayList<>();
        if (request.getParentContacts() != null && !request.getParentContacts().isEmpty()) {
            // Validate only one primary contact
            long primaryCount = request.getParentContacts().stream()
                    .filter(ParentContactRequest::getIsPrimary)
                    .count();
            if (primaryCount == 0) {
                throw new InvalidStudentDataException("At least one parent contact must be marked as primary");
            }
            if (primaryCount > 1) {
                throw new InvalidStudentDataException("Only one parent contact can be marked as primary");
            }
        }

        // Validate and check class capacity if classId provided
        SchoolClass schoolClass = null;
        if (request.getClassId() != null) {
            schoolClass = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> {
                        return new com.sms.student.exception.ClassNotFoundException("Class with ID " + request.getClassId() + " not found");
                    });

            // Check if class has capacity
            if (!schoolClass.hasCapacity()) {
                throw new ClassCapacityExceededException(
                        "Class is at full capacity (" + schoolClass.getMaxCapacity() + " students)");
            }
        }

        // Generate unique student code
        String studentCode = generateStudentCode();

        // Double-check uniqueness (teacher-scoped)
        int attempts = 0;
        while (studentRepository.existsByStudentCodeAndTeacherId(studentCode, teacherId) && attempts < 10) {
            studentCode = generateStudentCode();
            attempts++;
        }

        if (attempts >= 10) {
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
                .teacherId(teacherId)
                .createdBy(teacherId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .parentContacts(new ArrayList<>())
                .build();

        // Save student first to get ID
        student = studentRepository.save(student);

        // Create parent contacts if provided
        if (request.getParentContacts() != null && !request.getParentContacts().isEmpty()) {
            final Student savedStudent = student;
            contacts = request.getParentContacts().stream()
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
        }

        // Create enrollment record if class provided
        if (schoolClass != null) {
            StudentClassEnrollment enrollment = StudentClassEnrollment.builder()
                    .studentId(student.getId())
                    .classId(schoolClass.getId())
                    .enrollmentDate(request.getEnrollmentDate() != null ? request.getEnrollmentDate() : LocalDate.now())
                    .reason(EnrollmentReason.NEW)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            enrollmentRepository.save(enrollment);

            // Increment class student count
            schoolClass.incrementEnrollment();
            classRepository.save(schoolClass);
        }

        // Map to response DTO
        return mapToStudentResponse(student, contacts,
                                    schoolClass != null ? schoolClass.getId() : null);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(UUID id, StudentUpdateRequest request) {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        // Validate ownership before fetching
        Student student = studentRepository.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> {
                    return new UnauthorizedAccessException("You are not authorized to update this student");
                });

        // Validate parent contacts if provided
        if (request.getParentContacts() != null && !request.getParentContacts().isEmpty()) {
            // Validate only one primary contact
            long primaryCount = request.getParentContacts().stream()
                    .filter(ParentContactRequest::getIsPrimary)
                    .count();
            if (primaryCount == 0) {
                throw new InvalidStudentDataException("At least one parent contact must be marked as primary");
            }
            if (primaryCount > 1) {
                throw new InvalidStudentDataException("Only one parent contact can be marked as primary");
            }
        }

        // Update basic fields (student code, enrollment date, class, and teacher_id are immutable via this endpoint)
        // Use EnrollmentController to change class enrollment
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setFirstNameKhmer(request.getFirstNameKhmer());
        student.setLastNameKhmer(request.getLastNameKhmer());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setGender(request.getGender());
        student.setAddress(request.getAddress());
        student.setUpdatedBy(teacherId);
        student.setUpdatedAt(LocalDateTime.now());

        // Update parent contacts: delete all existing and create new ones
        // This is simpler than trying to match and update individual contacts
        List<ParentContact> existingContacts = parentContactRepository.findByStudentId(id);
        if (!existingContacts.isEmpty()) {
            parentContactRepository.deleteAll(existingContacts);
        }

        // Create new parent contacts from request if provided
        List<ParentContact> newContacts = new ArrayList<>();
        if (request.getParentContacts() != null && !request.getParentContacts().isEmpty()) {
            final Student savedStudent = student;
            newContacts = request.getParentContacts().stream()
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
        }

        // Save student changes
        student = studentRepository.save(student);

        // Get current enrollment
        UUID currentClassId = enrollmentRepository.findCurrentClassIdByStudentId(id)
                .orElse(null);

        return mapToStudentResponse(student, newContacts, currentClassId);
    }

    @Override
    @Transactional
    public void deleteStudent(UUID id, DeletionReason reason, UUID deletedBy) {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        Student student = studentRepository.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> {
                    return new UnauthorizedAccessException("You are not authorized to delete this student");
                });

        // Soft delete
        student.setStatus(StudentStatus.INACTIVE);
        student.setDeletedAt(LocalDateTime.now());
        student.setDeletedBy(teacherId);  // Use teacher ID from context
        student.setDeletionReason(reason != null ? reason.name() : null);
        student.setUpdatedBy(teacherId);
        student.setUpdatedAt(LocalDateTime.now());

        studentRepository.save(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(UUID id) {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        // Fetch from database
        Student student = studentRepository.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> {
                    return new UnauthorizedAccessException("You are not authorized to access this student");
                });

        List<ParentContact> contacts = parentContactRepository.findByStudentId(id);
        UUID currentClassId = enrollmentRepository.findCurrentClassIdByStudentId(id)
                .orElse(null);

        return mapToStudentResponse(student, contacts, currentClassId);
    }

    /**
     * Helper method for cache key generation
     */
    public UUID getTeacherId() {
        return TeacherContextHolder.getTeacherId();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentByCode(String studentCode) {

        Student student = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> {
                    return new StudentNotFoundException("Student with code " + studentCode + " not found");
                });

        List<ParentContact> contacts = parentContactRepository.findByStudentId(student.getId());
        UUID currentClassId = enrollmentRepository.findCurrentClassIdByStudentId(student.getId())
                .orElse(null);

        return mapToStudentResponse(student, contacts, currentClassId);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentListResponse listActiveStudents(Pageable pageable) {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        // Query database directly without caching
        Page<Student> studentPage = studentRepository.findByTeacherIdAndStatus(
                teacherId,
                StudentStatus.ACTIVE,
                pageable
        );

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
                                                        String level, Integer grade, String classId,
                                                        String academicYear, Pageable pageable) {
        // Get authenticated teacher ID for isolation
        UUID teacherId = TeacherContextHolder.getTeacherId();

        // Build specification with all filters (including teacher isolation and academic year)
        // Note: @Where annotation on Student entity handles soft-delete filtering
        org.springframework.data.jpa.domain.Specification<Student> spec =
                org.springframework.data.jpa.domain.Specification
                        .where(StudentSpecification.hasTeacherId(teacherId))
                        .and(StudentSpecification.hasStatus(status))
                        .and(StudentSpecification.hasGender(gender))
                        .and(StudentSpecification.hasLevel(level))
                        .and(StudentSpecification.hasGrade(grade))
                        .and(StudentSpecification.hasClassId(classId))
                        .and(StudentSpecification.hasAcademicYear(academicYear))
                        .and(StudentSpecification.searchByNameOrCode(search));

        // Fetch from database with specification (no caching)
        Page<Student> studentPage = studentRepository.findAll(spec, pageable);

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

        // Verify student exists
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    return new StudentNotFoundException("Student with ID " + id + " not found");
                });

        // Save photo (validates MIME type, file size, resizes, and deletes old photos)
        String photoPath = photoStorageService.savePhoto(id, photoData, contentType);

        // Update student entity with photo path
        student.setPhotoUrl(photoPath);
        student.setUpdatedAt(LocalDateTime.now());
        studentRepository.save(student);

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
        String fullName = student.getLastName() + " " + student.getFirstName();
        String fullNameKhmer = null;
        if (student.getFirstNameKhmer() != null && student.getLastNameKhmer() != null) {
            fullNameKhmer = student.getLastNameKhmer() + " " + student.getFirstNameKhmer();
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
        return parentContactService.addParentContact(studentId, request);
    }

    @Override
    @Transactional
    public ParentContactResponse updateParentContact(UUID contactId, ParentContactRequest request) {
        return parentContactService.updateParentContact(contactId, request);
    }

    @Override
    public CacheReloadResponse clearTeacherCache() {
        return cacheService.clearCurrentTeacherCache();
    }
}
