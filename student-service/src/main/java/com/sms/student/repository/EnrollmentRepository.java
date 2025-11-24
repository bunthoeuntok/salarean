package com.sms.student.repository;

import com.sms.student.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    /**
     * Find all enrollments for a student with class details.
     * Uses JOIN FETCH to prevent N+1 query problem.
     * Orders by enrollment date descending (most recent first).
     *
     * @param studentId the student's UUID
     * @return list of enrollments with eager-loaded class
     */
    @Query("""
        SELECT e FROM Enrollment e
        JOIN FETCH e.schoolClass c
        WHERE e.student.id = :studentId
        ORDER BY e.enrollmentDate DESC, e.createdAt DESC
    """)
    List<Enrollment> findEnrollmentHistoryByStudentId(@Param("studentId") UUID studentId);

    /**
     * Check if a student has an active enrollment in a specific class.
     * Used for duplicate enrollment prevention.
     *
     * @param studentId the student's UUID
     * @param classId the class UUID
     * @return true if active enrollment exists, false otherwise
     */
    @Query("""
        SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
        FROM Enrollment e
        WHERE e.student.id = :studentId
        AND e.schoolClass.id = :classId
        AND e.status = 'ACTIVE'
    """)
    boolean existsActiveEnrollment(@Param("studentId") UUID studentId, @Param("classId") UUID classId);

    /**
     * Find the active enrollment for a student.
     * Used for transfer operations to locate the current enrollment.
     * Uses JOIN FETCH to prevent N+1 query problem.
     *
     * @param studentId the student's UUID
     * @return optional containing the active enrollment with eager-loaded class, or empty if not found
     */
    @Query("""
        SELECT e FROM Enrollment e
        JOIN FETCH e.schoolClass c
        JOIN FETCH e.student s
        WHERE e.student.id = :studentId
        AND e.status = 'ACTIVE'
    """)
    Optional<Enrollment> findActiveEnrollmentByStudentId(@Param("studentId") UUID studentId);
}
