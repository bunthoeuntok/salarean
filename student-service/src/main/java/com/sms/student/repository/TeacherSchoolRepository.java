package com.sms.student.repository;

import com.sms.student.model.TeacherSchool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherSchoolRepository extends JpaRepository<TeacherSchool, UUID> {

    /**
     * Find teacher-school association by user ID.
     * Eagerly fetches the school to avoid N+1 queries.
     */
    @Query("SELECT ts FROM TeacherSchool ts JOIN FETCH ts.school WHERE ts.userId = :userId")
    Optional<TeacherSchool> findByUserId(UUID userId);

    /**
     * Check if a teacher-school association exists for a user.
     */
    boolean existsByUserId(UUID userId);
}
