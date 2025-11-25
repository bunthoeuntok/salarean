package com.sms.student.service;

import com.sms.student.dto.EnrollmentHistoryResponse;
import com.sms.student.dto.EnrollmentRequest;
import com.sms.student.dto.EnrollmentResponse;
import com.sms.student.dto.TransferRequest;
import com.sms.student.enums.EnrollmentReason;
import com.sms.student.exception.ClassCapacityExceededException;
import com.sms.student.exception.ClassNotFoundException;
import com.sms.student.exception.DuplicateEnrollmentException;
import com.sms.student.exception.EnrollmentNotFoundException;
import com.sms.student.exception.StudentNotFoundException;
import com.sms.student.model.Enrollment;
import com.sms.student.model.EnrollmentStatus;
import com.sms.student.model.School;
import com.sms.student.model.SchoolClass;
import com.sms.student.model.Student;
import com.sms.student.repository.ClassRepository;
import com.sms.student.repository.EnrollmentRepository;
import com.sms.student.repository.SchoolRepository;
import com.sms.student.repository.StudentRepository;
import com.sms.student.service.interfaces.IEnrollmentService;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EnrollmentService implements IEnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final ClassRepository classRepository;
    private final EntityManager entityManager;

    @Override
    public EnrollmentHistoryResponse getEnrollmentHistory(UUID studentId) {
        log.info("Getting enrollment history for student: {}", studentId);

        // Validate student exists
        studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + studentId));

        // Fetch enrollment history with eager loading of class
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentHistoryByStudentId(studentId);

        // Fetch all schools for these enrollments (to avoid N+1)
        List<UUID> schoolIds = enrollments.stream()
                .map(e -> e.getSchoolClass().getSchoolId())
                .distinct()
                .collect(Collectors.toList());

        Map<UUID, School> schoolMap = schoolRepository.findAllById(schoolIds).stream()
                .collect(Collectors.toMap(School::getId, Function.identity()));

        // Map to response DTOs
        List<EnrollmentResponse> enrollmentResponses = enrollments.stream()
                .map(e -> mapToResponse(e, schoolMap))
                .collect(Collectors.toList());

        // Calculate status counts
        int totalCount = enrollments.size();
        long activeCount = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .count();
        long completedCount = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED)
                .count();
        long transferredCount = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.TRANSFERRED)
                .count();

        log.info("Found {} enrollments for student {}: {} active, {} completed, {} transferred",
                totalCount, studentId, activeCount, completedCount, transferredCount);

        return EnrollmentHistoryResponse.builder()
                .enrollments(enrollmentResponses)
                .totalCount(totalCount)
                .activeCount((int) activeCount)
                .completedCount((int) completedCount)
                .transferredCount((int) transferredCount)
                .build();
    }

    @Override
    @Transactional
    public EnrollmentResponse enrollStudent(UUID studentId, EnrollmentRequest request) {
        log.info("Enrolling student {} in class {}", studentId, request.getClassId());

        // 1. Validate student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + studentId));

        // 2. Validate class exists
        SchoolClass schoolClass = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ClassNotFoundException("Class not found with id: " + request.getClassId()));

        // 3. Check for duplicate enrollment (student already enrolled in this class with ACTIVE status)
        if (enrollmentRepository.existsActiveEnrollment(studentId, request.getClassId())) {
            throw new DuplicateEnrollmentException(
                    String.format("Student %s is already enrolled in class %s", studentId, request.getClassId())
            );
        }

        // 4. Check class capacity (using optimistic locking via @Version field)
        if (!schoolClass.hasCapacity()) {
            throw new ClassCapacityExceededException(
                    String.format("Class %s has reached maximum capacity (%d/%d)",
                            request.getClassId(),
                            schoolClass.getStudentCount(),
                            schoolClass.getMaxCapacity())
            );
        }

        // 5. Create enrollment record
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .schoolClass(schoolClass)
                .enrollmentDate(LocalDate.now())
                .reason(EnrollmentReason.NEW)
                .status(EnrollmentStatus.ACTIVE)
                .notes(request.getNotes())
                .build();

        enrollment = enrollmentRepository.save(enrollment);

        // 6. Increment class student count (optimistic locking will handle concurrent enrollments)
        schoolClass.incrementEnrollment();
        classRepository.save(schoolClass);

        log.info("Successfully enrolled student {} in class {}. New student count: {}",
                studentId, request.getClassId(), schoolClass.getStudentCount());

        // 7. Fetch school for denormalization
        School school = schoolRepository.findById(schoolClass.getSchoolId())
                .orElse(null);

        // 8. Map to response DTO
        return mapToResponse(enrollment, school);
    }

    @Override
    @Transactional
    public EnrollmentResponse transferStudent(UUID studentId, TransferRequest request) {
        log.info("Transferring student {} to class {}", studentId, request.getTargetClassId());

        // 1. Validate student exists
        studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + studentId));

        // 2. Find active enrollment
        Enrollment currentEnrollment = enrollmentRepository.findActiveEnrollmentByStudentId(studentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(
                        "No active enrollment found for student: " + studentId));

        SchoolClass oldClass = currentEnrollment.getSchoolClass();
        log.info("Found active enrollment in class {} (Grade {} - Section {})",
                oldClass.getId(), oldClass.getGrade(), oldClass.getSection());

        // 3. Validate target class exists
        SchoolClass newClass = classRepository.findById(request.getTargetClassId())
                .orElseThrow(() -> new ClassNotFoundException(
                        "Target class not found with id: " + request.getTargetClassId()));

        // 4. Check target class capacity
        if (!newClass.hasCapacity()) {
            throw new ClassCapacityExceededException(
                    String.format("Target class %s has reached maximum capacity (%d/%d)",
                            request.getTargetClassId(),
                            newClass.getStudentCount(),
                            newClass.getMaxCapacity())
            );
        }

        // 5. Mark old enrollment as TRANSFERRED
        LocalDate today = LocalDate.now();
        currentEnrollment.setStatus(EnrollmentStatus.TRANSFERRED);
        currentEnrollment.setEndDate(today);
        currentEnrollment.setTransferDate(today);
        currentEnrollment.setTransferReason(request.getReason());
        enrollmentRepository.save(currentEnrollment);

        // 6. Decrement old class student count
        oldClass.decrementEnrollment();
        classRepository.save(oldClass);

        // 7. Flush changes to database before creating new enrollment
        // This ensures the unique constraint idx_enrollment_student_active is satisfied
        // (the old enrollment's end_date is no longer NULL before we insert the new one)
        entityManager.flush();

        log.info("Marked old enrollment as TRANSFERRED. Old class student count: {}", oldClass.getStudentCount());

        // 8. Create new enrollment
        Enrollment newEnrollment = Enrollment.builder()
                .student(currentEnrollment.getStudent())
                .schoolClass(newClass)
                .enrollmentDate(today)
                .reason(EnrollmentReason.TRANSFER)
                .status(EnrollmentStatus.ACTIVE)
                .notes(request.getReason())
                .build();

        newEnrollment = enrollmentRepository.save(newEnrollment);

        // 9. Increment new class student count
        newClass.incrementEnrollment();
        classRepository.save(newClass);

        log.info("Successfully transferred student {} from class {} to class {}. New class student count: {}",
                studentId, oldClass.getId(), newClass.getId(), newClass.getStudentCount());

        // 10. Fetch school for denormalization
        School school = schoolRepository.findById(newClass.getSchoolId())
                .orElse(null);

        // 11. Map to response DTO
        return mapToResponse(newEnrollment, school);
    }

    /**
     * Map Enrollment entity to EnrollmentResponse DTO with denormalized fields.
     * Overloaded version for single school lookup.
     */
    private EnrollmentResponse mapToResponse(Enrollment enrollment, School school) {
        String className = String.format("Grade %d - Section %s",
                enrollment.getSchoolClass().getGrade(),
                enrollment.getSchoolClass().getSection());

        String schoolName = school != null ? school.getName() : "Unknown School";

        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .classId(enrollment.getSchoolClass().getId())
                .className(className)
                .schoolName(schoolName)
                .enrollmentDate(enrollment.getEnrollmentDate())
                .endDate(enrollment.getEndDate())
                .reason(enrollment.getReason())
                .status(enrollment.getStatus())
                .transferDate(enrollment.getTransferDate())
                .transferReason(enrollment.getTransferReason())
                .notes(enrollment.getNotes())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();
    }

    /**
     * Map Enrollment entity to EnrollmentResponse DTO with denormalized fields.
     */
    private EnrollmentResponse mapToResponse(Enrollment enrollment, Map<UUID, School> schoolMap) {
        // Build class name from grade and section
        String className = String.format("Grade %d - Section %s",
                enrollment.getSchoolClass().getGrade(),
                enrollment.getSchoolClass().getSection());

        // Get school name from map
        School school = schoolMap.get(enrollment.getSchoolClass().getSchoolId());
        String schoolName = school != null ? school.getName() : "Unknown School";

        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .classId(enrollment.getSchoolClass().getId())
                .className(className)
                .schoolName(schoolName)
                .enrollmentDate(enrollment.getEnrollmentDate())
                .endDate(enrollment.getEndDate())
                .reason(enrollment.getReason())
                .status(enrollment.getStatus())
                .transferDate(enrollment.getTransferDate())
                .transferReason(enrollment.getTransferReason())
                .notes(enrollment.getNotes())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();
    }
}
