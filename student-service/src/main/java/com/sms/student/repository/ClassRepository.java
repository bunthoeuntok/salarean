package com.sms.student.repository;

import com.sms.student.enums.ClassStatus;
import com.sms.student.model.SchoolClass;
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
     * Find a class by ID and verify teacher ownership.
     * Used to ensure teachers can only access/modify their own classes.
     *
     * @param classId   UUID of the class
     * @param teacherId UUID of the teacher
     * @return Optional containing the class if found and owned by teacher
     */
    Optional<SchoolClass> findByIdAndTeacherId(UUID classId, UUID teacherId);
}
