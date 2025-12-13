package com.sms.student.service.interfaces;

import com.sms.student.dto.TeacherSchoolRequest;
import com.sms.student.dto.TeacherSchoolResponse;

import java.util.UUID;

public interface ITeacherSchoolService {

    /**
     * Create or update teacher-school association.
     * Uses UPSERT logic: creates if not exists, updates if exists.
     *
     * @param userId User ID (teacher) from JWT token
     * @param request Teacher-school association data
     * @return Created or updated teacher-school association
     */
    TeacherSchoolResponse createOrUpdate(UUID userId, TeacherSchoolRequest request);

    /**
     * Get teacher-school association by user ID.
     *
     * @param userId User ID (teacher) from JWT token
     * @return Teacher-school association or null if not found
     */
    TeacherSchoolResponse getByUserId(UUID userId);
}
