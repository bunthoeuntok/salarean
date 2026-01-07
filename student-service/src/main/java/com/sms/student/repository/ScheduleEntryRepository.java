package com.sms.student.repository;

import com.sms.student.model.ScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, UUID> {

    /**
     * Find all entries for a class schedule.
     */
    List<ScheduleEntry> findByClassScheduleId(UUID classScheduleId);

    /**
     * Find all entries for a class schedule ordered by day and period.
     */
    @Query("SELECT e FROM ScheduleEntry e WHERE e.classSchedule.id = :scheduleId ORDER BY e.dayOfWeek, e.periodNumber")
    List<ScheduleEntry> findByClassScheduleIdOrdered(UUID scheduleId);

    /**
     * Find entry for a specific day and period.
     */
    Optional<ScheduleEntry> findByClassScheduleIdAndDayOfWeekAndPeriodNumber(
        UUID classScheduleId, Integer dayOfWeek, Integer periodNumber);

    /**
     * Delete all entries for a class schedule.
     */
    @Modifying
    void deleteByClassScheduleId(UUID classScheduleId);

    /**
     * Count entries for a class schedule.
     */
    long countByClassScheduleId(UUID classScheduleId);

    /**
     * Find all entries for a specific subject across all schedules.
     * Useful for impact analysis when modifying subjects.
     */
    List<ScheduleEntry> findBySubjectId(UUID subjectId);
}
