package com.sms.grade.repository;

import com.sms.grade.enums.AssessmentCategory;
import com.sms.grade.model.AssessmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AssessmentType entity.
 * Assessment types are reference data shared across all teachers.
 */
@Repository
public interface AssessmentTypeRepository extends JpaRepository<AssessmentType, UUID> {

    /**
     * Find assessment type by unique code.
     */
    Optional<AssessmentType> findByCode(String code);

    /**
     * Find all assessment types by category.
     */
    List<AssessmentType> findByCategory(AssessmentCategory category);

    /**
     * Find all monthly exam types ordered by display order.
     */
    List<AssessmentType> findByCategoryOrderByDisplayOrderAsc(AssessmentCategory category);

    /**
     * Find all assessment types ordered by display order.
     */
    List<AssessmentType> findAllByOrderByDisplayOrderAsc();

    /**
     * Check if an assessment type code exists.
     */
    boolean existsByCode(String code);
}
