package com.sms.student.service;

import com.sms.student.dto.SchoolResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for school management operations.
 * Handles school listing and retrieval.
 */
public interface SchoolService {

    /**
     * List all schools.
     *
     * @return List of all schools
     */
    List<SchoolResponse> listAllSchools();

    /**
     * Get school details by ID.
     *
     * @param id School UUID
     * @return School details
     * @throws com.sms.student.exception.SchoolNotFoundException if school not found
     */
    SchoolResponse getSchoolById(UUID id);
}
