package com.sms.student.repository.specification;

import com.sms.student.enums.ClassLevel;
import com.sms.student.enums.Gender;
import com.sms.student.enums.StudentStatus;
import com.sms.student.model.SchoolClass;
import com.sms.student.model.Student;
import com.sms.student.model.StudentClassEnrollment;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA Specifications for dynamic filtering of Student entities.
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
public class StudentSpecification {

    private StudentSpecification() {
        // Utility class
    }

    /**
     * Filter by teacher ID (teacher-based data isolation).
     */
    public static Specification<Student> hasTeacherId(UUID teacherId) {
        return (root, query, cb) -> {
            if (teacherId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("teacherId"), teacherId);
        };
    }

    /**
     * Filter by status (supports comma-separated values).
     */
    public static Specification<Student> hasStatus(String statusFilter) {
        return (root, query, cb) -> {
            if (statusFilter == null || statusFilter.isBlank()) {
                return cb.conjunction();
            }
            List<StudentStatus> statuses = Arrays.stream(statusFilter.split(","))
                    .map(String::trim)
                    .map(StudentStatus::valueOf)
                    .collect(Collectors.toList());
            return root.get("status").in(statuses);
        };
    }

    /**
     * Filter by gender (supports comma-separated values).
     */
    public static Specification<Student> hasGender(String genderFilter) {
        return (root, query, cb) -> {
            if (genderFilter == null || genderFilter.isBlank()) {
                return cb.conjunction();
            }
            List<Gender> genders = Arrays.stream(genderFilter.split(","))
                    .map(String::trim)
                    .map(Gender::valueOf)
                    .collect(Collectors.toList());
            return root.get("gender").in(genders);
        };
    }

    /**
     * Filter by current class ID (uses subquery on enrollment table).
     * A student is currently enrolled if endDate is null.
     * Pass "NONE" to filter students without any current class enrollment.
     */
    public static Specification<Student> hasClassId(String classIdFilter) {
        return (root, query, cb) -> {
            if (classIdFilter == null || classIdFilter.isBlank()) {
                return cb.conjunction();
            }

            // Special case: filter students without any class
            if ("NONE".equalsIgnoreCase(classIdFilter)) {
                // Subquery to find students with ANY active enrollment
                Subquery<UUID> subquery = query.subquery(UUID.class);
                Root<StudentClassEnrollment> enrollment = subquery.from(StudentClassEnrollment.class);
                subquery.select(enrollment.get("studentId"))
                        .where(cb.isNull(enrollment.get("endDate"))); // Current enrollment
                // Return students NOT in the subquery (no active enrollment)
                return cb.not(cb.in(root.get("id")).value(subquery));
            }

            // Parse as UUID and filter by specific class
            UUID classId = UUID.fromString(classIdFilter);
            // Subquery to find students currently enrolled in the specified class
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<StudentClassEnrollment> enrollment = subquery.from(StudentClassEnrollment.class);
            subquery.select(enrollment.get("studentId"))
                    .where(
                            cb.equal(enrollment.get("classId"), classId),
                            cb.isNull(enrollment.get("endDate")) // Current enrollment
                    );
            return cb.in(root.get("id")).value(subquery);
        };
    }

    /**
     * Search by name or student code (case-insensitive).
     */
    public static Specification<Student> searchByNameOrCode(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("firstNameKhmer")), pattern),
                    cb.like(cb.lower(root.get("lastNameKhmer")), pattern),
                    cb.like(cb.lower(root.get("studentCode")), pattern)
            );
        };
    }

    /**
     * Filter by class level (uses subquery to join with class table through enrollment).
     * A student is filtered if their current class matches the specified level.
     */
    public static Specification<Student> hasLevel(String levelFilter) {
        return (root, query, cb) -> {
            if (levelFilter == null || levelFilter.isBlank()) {
                return cb.conjunction();
            }

            ClassLevel level = ClassLevel.valueOf(levelFilter);

            // Subquery to find class IDs with the specified level
            Subquery<UUID> classSubquery = query.subquery(UUID.class);
            Root<SchoolClass> classRoot = classSubquery.from(SchoolClass.class);
            classSubquery.select(classRoot.get("id"))
                    .where(cb.equal(classRoot.get("level"), level));

            // Subquery to find students currently enrolled in those classes
            Subquery<UUID> enrollmentSubquery = query.subquery(UUID.class);
            Root<StudentClassEnrollment> enrollment = enrollmentSubquery.from(StudentClassEnrollment.class);
            enrollmentSubquery.select(enrollment.get("studentId"))
                    .where(
                            cb.in(enrollment.get("classId")).value(classSubquery),
                            cb.isNull(enrollment.get("endDate")) // Current enrollment only
                    );

            return cb.in(root.get("id")).value(enrollmentSubquery);
        };
    }

    /**
     * Filter by grade (uses subquery to join with class table through enrollment).
     * A student is filtered if their current class matches the specified grade.
     */
    public static Specification<Student> hasGrade(Integer gradeFilter) {
        return (root, query, cb) -> {
            if (gradeFilter == null) {
                return cb.conjunction();
            }

            // Subquery to find class IDs with the specified grade
            Subquery<UUID> classSubquery = query.subquery(UUID.class);
            Root<SchoolClass> classRoot = classSubquery.from(SchoolClass.class);
            classSubquery.select(classRoot.get("id"))
                    .where(cb.equal(classRoot.get("grade"), gradeFilter));

            // Subquery to find students currently enrolled in those classes
            Subquery<UUID> enrollmentSubquery = query.subquery(UUID.class);
            Root<StudentClassEnrollment> enrollment = enrollmentSubquery.from(StudentClassEnrollment.class);
            enrollmentSubquery.select(enrollment.get("studentId"))
                    .where(
                            cb.in(enrollment.get("classId")).value(classSubquery),
                            cb.isNull(enrollment.get("endDate")) // Current enrollment only
                    );

            return cb.in(root.get("id")).value(enrollmentSubquery);
        };
    }
}
