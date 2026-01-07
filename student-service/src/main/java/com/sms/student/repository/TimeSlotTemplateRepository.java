package com.sms.student.repository;

import com.sms.student.enums.ClassShift;
import com.sms.student.model.TimeSlotTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimeSlotTemplateRepository extends JpaRepository<TimeSlotTemplate, UUID> {

    /**
     * Find all templates for a specific shift (includes defaults and teacher's custom templates).
     */
    @Query("SELECT t FROM TimeSlotTemplate t WHERE t.shift = :shift AND (t.teacherId IS NULL OR t.teacherId = :teacherId)")
    List<TimeSlotTemplate> findByShiftAndTeacher(ClassShift shift, UUID teacherId);

    /**
     * Find all templates available to a teacher (defaults + their custom ones).
     */
    @Query("SELECT t FROM TimeSlotTemplate t WHERE t.teacherId IS NULL OR t.teacherId = :teacherId")
    List<TimeSlotTemplate> findAllByTeacher(UUID teacherId);

    /**
     * Find system default templates only.
     */
    List<TimeSlotTemplate> findByTeacherIdIsNullAndIsDefaultTrue();

    /**
     * Find custom templates created by a specific teacher.
     */
    List<TimeSlotTemplate> findByTeacherId(UUID teacherId);

    /**
     * Find default template for a specific shift.
     */
    @Query("SELECT t FROM TimeSlotTemplate t WHERE t.shift = :shift AND t.teacherId IS NULL AND t.isDefault = true")
    TimeSlotTemplate findDefaultByShift(ClassShift shift);
}
