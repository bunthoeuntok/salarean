package com.sms.student.service;

import com.sms.student.dto.ClassSummaryDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for class management operations.
 *
 * <p>Handles business logic for:
 * <ul>
 *   <li>Viewing teacher's classes</li>
 *   <li>Viewing class details and enrollment history</li>
 *   <li>Creating and updating classes</li>
 *   <li>Archiving classes</li>
 * </ul>
 * </p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
public interface ClassService {

    /**
     * List all classes for a specific teacher.
     *
     * @param teacherId      UUID of the teacher
     * @param includeArchived whether to include archived classes
     * @return list of class summaries
     */
    List<ClassSummaryDto> listTeacherClasses(UUID teacherId, boolean includeArchived);
}
