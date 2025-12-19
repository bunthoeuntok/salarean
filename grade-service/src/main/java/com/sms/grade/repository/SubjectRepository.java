package com.sms.grade.repository;

import com.sms.grade.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Subject entity.
 * Subjects are reference data shared across all teachers.
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    /**
     * Find subject by unique code.
     */
    Optional<Subject> findByCode(String code);

    /**
     * Find all core subjects.
     */
    List<Subject> findByIsCoreTrue();

    /**
     * Find all subjects ordered by display order.
     */
    List<Subject> findAllByOrderByDisplayOrderAsc();

    /**
     * Check if a subject code exists.
     */
    boolean existsByCode(String code);
}
