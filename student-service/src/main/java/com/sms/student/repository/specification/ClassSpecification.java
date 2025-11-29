package com.sms.student.repository.specification;

import com.sms.student.enums.ClassStatus;
import com.sms.student.model.SchoolClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA Specifications for dynamic filtering of SchoolClass entities.
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
public class ClassSpecification {

    private ClassSpecification() {
        // Utility class
    }

    /**
     * Filter by teacher ID.
     */
    public static Specification<SchoolClass> hasTeacherId(UUID teacherId) {
        return (root, query, cb) -> cb.equal(root.get("teacherId"), teacherId);
    }

    /**
     * Filter by status (supports comma-separated values).
     */
    public static Specification<SchoolClass> hasStatus(String statusFilter) {
        return (root, query, cb) -> {
            if (statusFilter == null || statusFilter.isBlank()) {
                return cb.conjunction();
            }
            List<ClassStatus> statuses = Arrays.stream(statusFilter.split(","))
                    .map(String::trim)
                    .map(ClassStatus::valueOf)
                    .collect(Collectors.toList());
            return root.get("status").in(statuses);
        };
    }

    /**
     * Filter by academic year.
     */
    public static Specification<SchoolClass> hasAcademicYear(String academicYear) {
        return (root, query, cb) -> {
            if (academicYear == null || academicYear.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("academicYear"), academicYear);
        };
    }

    /**
     * Filter by grade.
     */
    public static Specification<SchoolClass> hasGrade(String gradeFilter) {
        return (root, query, cb) -> {
            if (gradeFilter == null || gradeFilter.isBlank()) {
                return cb.conjunction();
            }
            try {
                Integer grade = Integer.parseInt(gradeFilter);
                return cb.equal(root.get("grade"), grade);
            } catch (NumberFormatException e) {
                return cb.conjunction();
            }
        };
    }

    /**
     * Search by class section (contains, case-insensitive).
     * Note: Since class name is computed as "Grade X - Section Y",
     * we search by section field.
     */
    public static Specification<SchoolClass> searchBySection(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("section")), pattern);
        };
    }
}
