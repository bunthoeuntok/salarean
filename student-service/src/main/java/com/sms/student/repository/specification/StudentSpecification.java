package com.sms.student.repository.specification;

import com.sms.student.enums.Gender;
import com.sms.student.enums.StudentStatus;
import com.sms.student.model.Student;
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
     * Filter by current class ID.
     */
    public static Specification<Student> hasClassId(UUID classId) {
        return (root, query, cb) -> {
            if (classId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("currentClassId"), classId);
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
}
