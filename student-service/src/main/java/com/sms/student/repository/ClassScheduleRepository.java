package com.sms.student.repository;

import com.sms.student.model.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, UUID> {

    /**
     * Find schedule for a class in a specific academic year.
     */
    Optional<ClassSchedule> findByClassIdAndAcademicYear(UUID classId, String academicYear);

    /**
     * Find the active schedule for a class.
     */
    Optional<ClassSchedule> findByClassIdAndIsActiveTrue(UUID classId);

    /**
     * Check if a schedule exists for a class and academic year.
     */
    boolean existsByClassIdAndAcademicYear(UUID classId, String academicYear);

    /**
     * Find schedule by class ID (latest/active).
     */
    @Query("SELECT s FROM ClassSchedule s WHERE s.classId = :classId ORDER BY s.createdAt DESC")
    Optional<ClassSchedule> findLatestByClassId(UUID classId);
}
