package com.sms.student.repository;

import com.sms.student.enums.ClassStatus;
import com.sms.student.model.SchoolClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing {@link SchoolClass} persistence.
 *
 * <p>Provides custom query methods for class management operations including:
 * <ul>
 *   <li>Finding classes by teacher (with optional status filter)</li>
 *   <li>Verifying teacher ownership of classes</li>
 * </ul>
 * </p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Repository
public interface ClassRepository extends JpaRepository<SchoolClass, UUID> {

    /**
     * Find all classes for a specific teacher with status filter.
     *
     * @param teacherId UUID of the teacher
     * @param status    class status (ACTIVE or ARCHIVED)
     * @return list of classes matching criteria
     */
    List<SchoolClass> findByTeacherIdAndStatus(UUID teacherId, ClassStatus status);

    /**
     * Find all classes for a specific teacher (all statuses).
     *
     * @param teacherId UUID of the teacher
     * @return list of all classes for the teacher
     */
    List<SchoolClass> findByTeacherId(UUID teacherId);

    /**
     * Find all classes for a specific teacher with pagination and status filter.
     *
     * @param teacherId UUID of the teacher
     * @param status    class status (ACTIVE or ARCHIVED)
     * @param pageable  pagination parameters
     * @return page of classes matching criteria
     */
    Page<SchoolClass> findByTeacherIdAndStatus(UUID teacherId, ClassStatus status, Pageable pageable);

    /**
     * Find all classes for a specific teacher with pagination (all statuses).
     *
     * @param teacherId UUID of the teacher
     * @param pageable  pagination parameters
     * @return page of all classes for the teacher
     */
    Page<SchoolClass> findByTeacherId(UUID teacherId, Pageable pageable);

    /**
     * Find a class by ID and verify teacher ownership.
     * Used to ensure teachers can only access/modify their own classes.
     *
     * @param classId   UUID of the class
     * @param teacherId UUID of the teacher
     * @return Optional containing the class if found and owned by teacher
     */
    Optional<SchoolClass> findByIdAndTeacherId(UUID classId, UUID teacherId);

    /**
     * Check if a class exists with the given schoolId, grade, section, and academic year.
     * Used to prevent duplicate class creation (enforces unique constraint).
     *
     * @param schoolId     school UUID
     * @param grade        grade level (1-12)
     * @param section      section identifier
     * @param academicYear academic year (format "YYYY-YYYY")
     * @return true if class exists, false otherwise
     */
    boolean existsBySchoolIdAndGradeAndSectionAndAcademicYear(
        UUID schoolId, Integer grade, String section, String academicYear
    );

    /**
     * Find all classes with the given schoolId, grade, section, and academic year.
     * Used for duplicate checking during updates (need to exclude current class).
     *
     * @param schoolId     school UUID
     * @param grade        grade level (1-12)
     * @param section      section identifier
     * @param academicYear academic year (format "YYYY-YYYY")
     * @return list of matching classes
     */
    List<SchoolClass> findBySchoolIdAndGradeAndSectionAndAcademicYear(
        UUID schoolId, Integer grade, String section, String academicYear
    );
}
