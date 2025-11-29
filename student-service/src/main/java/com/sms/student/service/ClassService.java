package com.sms.student.service;

import com.sms.student.cache.ClassCache;
import com.sms.student.dto.ClassDetailDto;
import com.sms.student.dto.ClassListResponse;
import com.sms.student.dto.ClassSummaryDto;
import com.sms.student.dto.StudentRosterItemDto;
import com.sms.student.enums.ClassStatus;
import com.sms.student.exception.ClassNotFoundException;
import com.sms.student.exception.UnauthorizedClassAccessException;
import com.sms.student.model.SchoolClass;
import com.sms.student.model.Student;
import com.sms.student.model.StudentClassEnrollment;
import com.sms.student.repository.ClassRepository;
import com.sms.student.repository.StudentClassEnrollmentRepository;
import com.sms.student.repository.StudentRepository;
import com.sms.student.repository.specification.ClassSpecification;
import com.sms.student.service.interfaces.IClassService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link IClassService}.
 *
 * <p>Handles business logic for class management with Redis caching
 * to optimize performance for frequently accessed data.</p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassService implements IClassService {

    private final ClassRepository classRepository;
    private final StudentClassEnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final ClassCache classCacheService;

    @Override
    @Transactional(readOnly = true)
    public List<ClassSummaryDto> listTeacherClasses(UUID teacherId, boolean includeArchived) {
        log.info("Fetching classes for teacher: {} (includeArchived: {})", teacherId, includeArchived);

        // Try to get from cache first
        Optional<List<ClassSummaryDto>> cachedClasses = classCacheService.getTeacherClasses(teacherId);
        if (cachedClasses.isPresent()) {
            log.debug("Cache HIT for teacher classes: {}", teacherId);
            List<ClassSummaryDto> classes = cachedClasses.get();

            // Filter archived classes if needed
            if (!includeArchived) {
                classes = classes.stream()
                    .filter(c -> c.getStatus() == ClassStatus.ACTIVE)
                    .collect(Collectors.toList());
            }

            return classes;
        }

        log.debug("Cache MISS for teacher classes: {}", teacherId);

        // Fetch from database
        List<SchoolClass> classEntities;
        if (includeArchived) {
            classEntities = classRepository.findByTeacherId(teacherId);
        } else {
            classEntities = classRepository.findByTeacherIdAndStatus(teacherId, ClassStatus.ACTIVE);
        }

        log.debug("Found {} classes for teacher: {}", classEntities.size(), teacherId);

        // Map to DTOs
        List<ClassSummaryDto> classSummaries = classEntities.stream()
            .map(this::mapToSummaryDto)
            .collect(Collectors.toList());

        // Cache the result (always cache all classes, filter happens on retrieval)
        if (!includeArchived) {
            // If we only fetched active classes, we should fetch all classes for caching
            List<SchoolClass> allClasses = classRepository.findByTeacherId(teacherId);
            List<ClassSummaryDto> allSummaries = allClasses.stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
            classCacheService.cacheTeacherClasses(teacherId, allSummaries);
        } else {
            classCacheService.cacheTeacherClasses(teacherId, classSummaries);
        }

        return classSummaries;
    }

    @Override
    @Transactional(readOnly = true)
    public ClassListResponse listTeacherClassesPaginated(UUID teacherId, boolean includeArchived, Pageable pageable) {
        log.info("Fetching paginated classes for teacher: {} (includeArchived: {}, page: {}, size: {})",
                 teacherId, includeArchived, pageable.getPageNumber(), pageable.getPageSize());

        // Fetch from database with pagination
        Page<SchoolClass> classPage;
        if (includeArchived) {
            classPage = classRepository.findByTeacherId(teacherId, pageable);
        } else {
            classPage = classRepository.findByTeacherIdAndStatus(teacherId, ClassStatus.ACTIVE, pageable);
        }

        log.debug("Found {} classes for teacher: {} (page {} of {})",
                 classPage.getNumberOfElements(), teacherId,
                 classPage.getNumber() + 1, classPage.getTotalPages());

        // Map to DTOs
        List<ClassSummaryDto> classSummaries = classPage.getContent().stream()
            .map(this::mapToSummaryDto)
            .collect(Collectors.toList());

        return ClassListResponse.builder()
            .content(classSummaries)
            .page(classPage.getNumber())
            .size(classPage.getSize())
            .totalElements(classPage.getTotalElements())
            .totalPages(classPage.getTotalPages())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ClassListResponse listClassesWithFilters(UUID teacherId, String search, String status,
                                                     String academicYear, String grade, Pageable pageable) {
        log.info("Fetching classes with filters for teacher: {} (search: {}, status: {}, academicYear: {}, grade: {})",
                 teacherId, search, status, academicYear, grade);

        // Build specification with all filters
        Specification<SchoolClass> spec = Specification.where(ClassSpecification.hasTeacherId(teacherId))
                .and(ClassSpecification.hasStatus(status))
                .and(ClassSpecification.hasAcademicYear(academicYear))
                .and(ClassSpecification.hasGrade(grade))
                .and(ClassSpecification.searchBySection(search));

        // Fetch from database with specification
        Page<SchoolClass> classPage = classRepository.findAll(spec, pageable);

        log.debug("Found {} classes for teacher: {} (page {} of {})",
                 classPage.getNumberOfElements(), teacherId,
                 classPage.getNumber() + 1, classPage.getTotalPages());

        // Map to DTOs
        List<ClassSummaryDto> classSummaries = classPage.getContent().stream()
            .map(this::mapToSummaryDto)
            .collect(Collectors.toList());

        return ClassListResponse.builder()
            .content(classSummaries)
            .page(classPage.getNumber())
            .size(classPage.getSize())
            .totalElements(classPage.getTotalElements())
            .totalPages(classPage.getTotalPages())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ClassDetailDto getClassDetails(UUID classId, UUID teacherId) {
        log.info("Fetching class details for classId: {}, teacherId: {}", classId, teacherId);

        // Try to get from cache first
        Optional<ClassDetailDto> cachedDetails = classCacheService.getClassDetails(classId);
        if (cachedDetails.isPresent()) {
            log.debug("Cache HIT for class details: {}", classId);
            ClassDetailDto details = cachedDetails.get();

            // Verify teacher ownership
            if (!details.getTeacherId().equals(teacherId)) {
                log.warn("Unauthorized access attempt to class: {} by teacher: {}", classId, teacherId);
                throw new UnauthorizedClassAccessException(
                    "Teacher " + teacherId + " is not authorized to access class " + classId
                );
            }

            return details;
        }

        log.debug("Cache MISS for class details: {}", classId);

        // Fetch class and verify ownership
        SchoolClass schoolClass = classRepository.findByIdAndTeacherId(classId, teacherId)
            .orElseThrow(() -> {
                log.error("Class not found or unauthorized: classId={}, teacherId={}", classId, teacherId);
                // Check if class exists at all
                if (classRepository.existsById(classId)) {
                    return new UnauthorizedClassAccessException(
                        "Teacher " + teacherId + " is not authorized to access class " + classId
                    );
                } else {
                    return new ClassNotFoundException("Class with ID " + classId + " not found");
                }
            });

        // Get enrolled students
        List<StudentRosterItemDto> students = getClassStudents(classId, teacherId);

        // Map to DTO
        ClassDetailDto classDetails = mapToDetailDto(schoolClass, students);

        // Cache the result
        classCacheService.cacheClassDetails(classId, classDetails);

        log.info("Found class details with {} students", students.size());
        return classDetails;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentRosterItemDto> getClassStudents(UUID classId, UUID teacherId) {
        log.info("Fetching students for classId: {}, teacherId: {}", classId, teacherId);

        // Get current enrollments for this class
        List<StudentClassEnrollment> enrollments = enrollmentRepository.findCurrentEnrollmentsByClassId(classId);

        log.debug("Found {} current enrollments for class {}", enrollments.size(), classId);

        // Fetch student details and map to roster items
        List<StudentRosterItemDto> rosterItems = enrollments.stream()
            .map(enrollment -> {
                Student student = studentRepository.findById(enrollment.getStudentId())
                    .orElse(null);
                if (student == null) {
                    log.warn("Student not found: {}", enrollment.getStudentId());
                    return null;
                }
                return mapToRosterItem(student, enrollment);
            })
            .filter(item -> item != null)
            .collect(Collectors.toList());

        log.info("Returning {} students for class {}", rosterItems.size(), classId);
        return rosterItems;
    }

    /**
     * Map SchoolClass to ClassSummaryDto.
     *
     * @param entity class entity
     * @return class summary DTO
     */
    private ClassSummaryDto mapToSummaryDto(SchoolClass entity) {
        // Build display name: "Grade X - Section Y" or just "Grade X" if no section
        String displayName = entity.getSection() != null && !entity.getSection().isEmpty()
            ? String.format("Grade %d - Section %s", entity.getGrade(), entity.getSection())
            : String.format("Grade %d", entity.getGrade());

        return ClassSummaryDto.builder()
            .id(entity.getId())
            .name(displayName)
            .grade(String.valueOf(entity.getGrade()))
            .section(entity.getSection())
            .academicYear(entity.getAcademicYear())
            .capacity(entity.getMaxCapacity())
            .currentEnrollment(entity.getStudentCount())
            .status(entity.getStatus())
            .teacherId(entity.getTeacherId())
            .teacherName(null) // TODO: Fetch from auth-service if needed
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /**
     * Map SchoolClass and students to ClassDetailDto.
     *
     * @param entity   class entity
     * @param students list of enrolled students
     * @return class detail DTO
     */
    private ClassDetailDto mapToDetailDto(SchoolClass entity, List<StudentRosterItemDto> students) {
        return ClassDetailDto.builder()
            .id(entity.getId())
            .schoolId(entity.getSchoolId())
            .teacherId(entity.getTeacherId())
            .grade(entity.getGrade())
            .section(entity.getSection())
            .academicYear(entity.getAcademicYear())
            .maxCapacity(entity.getMaxCapacity())
            .studentCount(entity.getStudentCount())
            .status(entity.getStatus())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .students(students)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.sms.student.dto.EnrollmentHistoryDto> getEnrollmentHistory(UUID classId, UUID teacherId) {
        log.info("Fetching enrollment history for classId: {}, teacherId: {}", classId, teacherId);

        // Try to get from cache first
        Optional<List<com.sms.student.dto.EnrollmentHistoryDto>> cachedHistory =
            classCacheService.getEnrollmentHistory(classId);
        if (cachedHistory.isPresent()) {
            log.debug("Cache HIT for enrollment history: {}", classId);
            return cachedHistory.get();
        }

        log.debug("Cache MISS for enrollment history: {}", classId);

        // Get all enrollment records for this class (current and past)
        List<StudentClassEnrollment> enrollments =
            enrollmentRepository.findAllByClassIdOrderByEnrollmentDateDesc(classId);

        log.debug("Found {} enrollment records for class {}", enrollments.size(), classId);

        // Map to DTOs
        List<com.sms.student.dto.EnrollmentHistoryDto> history = enrollments.stream()
            .map(enrollment -> {
                Student student = studentRepository.findById(enrollment.getStudentId())
                    .orElse(null);
                if (student == null) {
                    log.warn("Student not found for enrollment: {}", enrollment.getStudentId());
                    return null;
                }
                return mapToEnrollmentHistoryDto(student, enrollment);
            })
            .filter(item -> item != null)
            .collect(Collectors.toList());

        // Cache the result
        classCacheService.cacheEnrollmentHistory(classId, history);

        log.info("Returning {} enrollment history records for class {}", history.size(), classId);
        return history;
    }

    /**
     * Map Student and enrollment to StudentRosterItemDto.
     *
     * @param student    student entity
     * @param enrollment enrollment record
     * @return student roster item DTO
     */
    private StudentRosterItemDto mapToRosterItem(Student student, StudentClassEnrollment enrollment) {
        return StudentRosterItemDto.builder()
            .studentId(student.getId())
            .studentCode(student.getStudentCode())
            .firstName(student.getFirstName())
            .lastName(student.getLastName())
            .firstNameKhmer(student.getFirstNameKhmer())
            .lastNameKhmer(student.getLastNameKhmer())
            .gender(student.getGender())
            .photoUrl(student.getPhotoUrl())
            .enrollmentDate(enrollment.getEnrollmentDate())
            .status(student.getStatus())
            .build();
    }

    /**
     * Map Student and enrollment to EnrollmentHistoryDto.
     *
     * @param student    student entity
     * @param enrollment enrollment record
     * @return enrollment history DTO
     */
    private com.sms.student.dto.EnrollmentHistoryDto mapToEnrollmentHistoryDto(
            Student student, StudentClassEnrollment enrollment) {
        return com.sms.student.dto.EnrollmentHistoryDto.builder()
            .id(enrollment.getId())
            .classId(enrollment.getClassId())
            .studentId(student.getId())
            .studentCode(student.getStudentCode())
            .studentName(student.getFirstName() + " " + student.getLastName())
            .studentNameKhmer(
                (student.getFirstNameKhmer() != null && student.getLastNameKhmer() != null)
                    ? student.getFirstNameKhmer() + " " + student.getLastNameKhmer()
                    : null
            )
            .reason(enrollment.getReason())
            .enrollmentDate(enrollment.getEnrollmentDate())
            .endDate(enrollment.getEndDate())
            .isCurrent(enrollment.isCurrent())
            .notes(enrollment.getNotes())
            .createdAt(enrollment.getCreatedAt())
            .updatedAt(enrollment.getUpdatedAt())
            .build();
    }

    @Override
    @Transactional
    public ClassDetailDto createClass(com.sms.student.dto.ClassCreateRequest request, UUID teacherId) {
        log.info("Creating new class for teacher: {}, grade: {}, section: {}, academicYear: {}",
                 teacherId, request.getGrade(), request.getSection(), request.getAcademicYear());

        // Validate academic year format
        validateAcademicYear(request.getAcademicYear());

        // Check for duplicate class
        checkDuplicateClass(request.getSchoolId(), request.getGrade(),
                           request.getSection(), request.getAcademicYear());

        // Create new class entity
        SchoolClass schoolClass = SchoolClass.builder()
            .schoolId(request.getSchoolId())
            .teacherId(teacherId)
            .grade(request.getGrade())
            .section(request.getSection())
            .academicYear(request.getAcademicYear())
            .maxCapacity(request.getMaxCapacity())
            .studentCount(0)
            .status(ClassStatus.ACTIVE)
            .build();

        // Save to database
        SchoolClass savedClass = classRepository.save(schoolClass);

        log.info("Successfully created class with ID: {}", savedClass.getId());

        // Evict teacher's classes cache
        classCacheService.evictTeacherClasses(teacherId);

        // Return class details (with empty student list)
        return mapToDetailDto(savedClass, List.of());
    }

    /**
     * Validate academic year format (YYYY-YYYY) and ensure second year = first year + 1.
     *
     * @param academicYear academic year string
     * @throws IllegalArgumentException if format is invalid or years don't follow pattern
     */
    private void validateAcademicYear(String academicYear) {
        if (academicYear == null || !academicYear.matches("^(\\d{4})-(\\d{4})$")) {
            throw new IllegalArgumentException(
                "Academic year must be in format 'YYYY-YYYY'"
            );
        }

        String[] years = academicYear.split("-");
        int firstYear = Integer.parseInt(years[0]);
        int secondYear = Integer.parseInt(years[1]);

        if (secondYear != firstYear + 1) {
            throw new IllegalArgumentException(
                "Academic year must be consecutive years (e.g., '2024-2025')"
            );
        }
    }

    /**
     * Check if a class with the same schoolId, grade, section, and academicYear already exists.
     *
     * @param schoolId     school ID
     * @param grade        grade level
     * @param section      section identifier
     * @param academicYear academic year
     * @throws com.sms.student.exception.DuplicateClassException if duplicate exists
     */
    private void checkDuplicateClass(UUID schoolId, Integer grade, String section, String academicYear) {
        boolean exists = classRepository.existsBySchoolIdAndGradeAndSectionAndAcademicYear(
            schoolId, grade, section, academicYear
        );

        if (exists) {
            log.error("Duplicate class detected: schoolId={}, grade={}, section={}, academicYear={}",
                     schoolId, grade, section, academicYear);
            throw new com.sms.student.exception.DuplicateClassException(
                String.format("Class already exists for grade %d, section %s in academic year %s",
                             grade, section, academicYear)
            );
        }
    }

    @Override
    @Transactional
    public ClassDetailDto updateClass(UUID classId, com.sms.student.dto.ClassUpdateRequest request, UUID teacherId) {
        log.info("Updating class: {} by teacher: {}", classId, teacherId);

        // Fetch class and verify ownership
        SchoolClass schoolClass = classRepository.findByIdAndTeacherId(classId, teacherId)
            .orElseThrow(() -> {
                log.error("Class not found or unauthorized: classId={}, teacherId={}", classId, teacherId);
                if (classRepository.existsById(classId)) {
                    return new UnauthorizedClassAccessException(
                        "Teacher " + teacherId + " is not authorized to update class " + classId
                    );
                } else {
                    return new ClassNotFoundException("Class with ID " + classId + " not found");
                }
            });

        // Track if we need to check for duplicates
        boolean needsDuplicateCheck = false;

        // Update grade if provided
        if (request.getGrade() != null && !request.getGrade().equals(schoolClass.getGrade())) {
            log.debug("Updating grade from {} to {}", schoolClass.getGrade(), request.getGrade());
            schoolClass.setGrade(request.getGrade());
            needsDuplicateCheck = true;
        }

        // Update section if provided
        if (request.getSection() != null && !request.getSection().equals(schoolClass.getSection())) {
            log.debug("Updating section from {} to {}", schoolClass.getSection(), request.getSection());
            schoolClass.setSection(request.getSection());
            needsDuplicateCheck = true;
        }

        // Update academic year if provided
        if (request.getAcademicYear() != null && !request.getAcademicYear().equals(schoolClass.getAcademicYear())) {
            log.debug("Updating academic year from {} to {}", schoolClass.getAcademicYear(), request.getAcademicYear());
            validateAcademicYear(request.getAcademicYear());
            schoolClass.setAcademicYear(request.getAcademicYear());
            needsDuplicateCheck = true;
        }

        // Check for duplicates if key fields changed
        if (needsDuplicateCheck) {
            // Exclude current class from duplicate check
            List<SchoolClass> duplicates = classRepository.findBySchoolIdAndGradeAndSectionAndAcademicYear(
                schoolClass.getSchoolId(), schoolClass.getGrade(),
                schoolClass.getSection(), schoolClass.getAcademicYear()
            );

            // Filter out the current class being updated
            boolean hasDuplicate = duplicates.stream()
                .anyMatch(c -> !c.getId().equals(classId));

            if (hasDuplicate) {
                log.error("Update would create duplicate class: schoolId={}, grade={}, section={}, academicYear={}",
                         schoolClass.getSchoolId(), schoolClass.getGrade(),
                         schoolClass.getSection(), schoolClass.getAcademicYear());
                throw new com.sms.student.exception.DuplicateClassException(
                    String.format("Class already exists for grade %d, section %s in academic year %s",
                                 schoolClass.getGrade(), schoolClass.getSection(), schoolClass.getAcademicYear())
                );
            }
        }

        // Update max capacity if provided (can be set to null to remove limit)
        if (request.getMaxCapacity() != null) {
            log.debug("Updating max capacity from {} to {}", schoolClass.getMaxCapacity(), request.getMaxCapacity());
            schoolClass.setMaxCapacity(request.getMaxCapacity());
        }

        // Save updated class
        SchoolClass updatedClass = classRepository.save(schoolClass);

        log.info("Successfully updated class: {}", classId);

        // Evict caches
        classCacheService.evictTeacherClasses(teacherId);
        classCacheService.evictClassDetails(classId);
        classCacheService.evictEnrollmentHistory(classId);

        // Get current students for response
        List<StudentRosterItemDto> students = getClassStudents(classId, teacherId);

        // Return updated class details
        return mapToDetailDto(updatedClass, students);
    }

    @Override
    @Transactional
    public ClassDetailDto archiveClass(UUID classId, UUID teacherId) {
        log.info("Archiving class: {} by teacher: {}", classId, teacherId);

        // Fetch class and verify ownership
        SchoolClass schoolClass = classRepository.findByIdAndTeacherId(classId, teacherId)
            .orElseThrow(() -> {
                log.error("Class not found or unauthorized: classId={}, teacherId={}", classId, teacherId);
                if (classRepository.existsById(classId)) {
                    return new UnauthorizedClassAccessException(
                        "Teacher " + teacherId + " is not authorized to archive class " + classId
                    );
                } else {
                    return new ClassNotFoundException("Class with ID " + classId + " not found");
                }
            });

        // Check if already completed/archived
        if (schoolClass.getStatus() == ClassStatus.COMPLETED) {
            log.warn("Class {} is already archived", classId);
            // Return current state without modification
            List<StudentRosterItemDto> students = getClassStudents(classId, teacherId);
            return mapToDetailDto(schoolClass, students);
        }

        // Archive the class (mark as completed)
        schoolClass.setStatus(ClassStatus.COMPLETED);
        SchoolClass archivedClass = classRepository.save(schoolClass);

        log.info("Successfully archived class: {}", classId);

        // Evict caches (teacher's class list needs refresh to exclude archived class from default view)
        classCacheService.evictTeacherClasses(teacherId);
        classCacheService.evictClassDetails(classId);

        // Get current students for response
        List<StudentRosterItemDto> students = getClassStudents(classId, teacherId);

        // Return archived class details
        return mapToDetailDto(archivedClass, students);
    }
}
