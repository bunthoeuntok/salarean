package com.sms.student.service.interfaces;

import com.sms.student.dto.SchoolRequest;
import com.sms.student.dto.SchoolResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for school management operations.
 * Handles school listing and retrieval.
 */
public interface ISchoolService {

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

    /**
     * Get schools by district ID.
     *
     * @param districtId District UUID
     * @return List of schools in the district
     * @throws com.sms.student.exception.DistrictNotFoundException if district not found
     */
    List<SchoolResponse> getSchoolsByDistrict(UUID districtId);

    /**
     * Create a new school.
     *
     * @param request School creation request
     * @return Created school details
     * @throws com.sms.student.exception.ProvinceNotFoundException if province not found
     * @throws com.sms.student.exception.DistrictNotFoundException if district not found
     */
    SchoolResponse createSchool(SchoolRequest request);
}
