package com.sms.student.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.student.dto.SchoolResponse;
import com.sms.student.service.interfaces.ISchoolService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for school management operations.
 * All endpoints return ApiResponse<T> wrapper following SMS API standards.
 */
@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
@Slf4j
public class SchoolController {

    private final ISchoolService schoolService;

    /**
     * List schools.
     * GET /api/schools - Returns all schools
     * GET /api/schools?districtId={uuid} - Returns schools for specific district
     * Requires TEACHER role.
     */
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<SchoolResponse>>> listSchools(
            @RequestParam(required = false) UUID districtId) {
        if (districtId != null) {
            log.info("Received request to list schools for district ID: {}", districtId);
            List<SchoolResponse> schools = schoolService.getSchoolsByDistrict(districtId);
            log.info("Returning {} schools for district", schools.size());
            return ResponseEntity.ok(ApiResponse.success(schools));
        } else {
            log.info("Received request to list all schools");
            List<SchoolResponse> schools = schoolService.listAllSchools();
            log.info("Returning {} schools", schools.size());
            return ResponseEntity.ok(ApiResponse.success(schools));
        }
    }

    /**
     * Get school details by ID.
     * GET /api/schools/{id}
     * Public endpoint (allows service-to-service calls without auth).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolResponse>> getSchoolById(@PathVariable UUID id) {
        log.info("Received request to get school by ID: {}", id);

        SchoolResponse school = schoolService.getSchoolById(id);

        log.info("Returning school: {}", school.getName());

        return ResponseEntity.ok(ApiResponse.success(school));
    }
}
