package com.sms.student.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.student.dto.ClassCreateRequest;
import com.sms.student.dto.ClassDetailDto;
import com.sms.student.dto.ClassSummaryDto;
import com.sms.student.dto.ClassUpdateRequest;
import com.sms.student.dto.EnrollmentHistoryDto;
import com.sms.student.dto.StudentRosterItemDto;
import com.sms.student.security.JwtTokenProvider;
import com.sms.student.service.ClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for class management operations.
 *
 * <p>All endpoints return {@link ApiResponse} wrapper following SMS API standards.
 * Teacher authorization is enforced via @PreAuthorize annotations.</p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Class Management", description = "APIs for teachers to manage their academic classes")
public class ClassController {

    private final ClassService classService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * List all classes for the authenticated teacher.
     *
     * <p>GET /api/classes</p>
     *
     * <p>Query Parameters:
     * <ul>
     *   <li>includeArchived (optional, default: false) - Include archived classes in results</li>
     * </ul>
     * </p>
     *
     * <p>Returns list of class summaries with current enrollment counts.</p>
     *
     * @param includeArchived whether to include archived classes (default: false)
     * @param request         HTTP request to extract JWT token
     * @return list of class summaries
     */
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
        summary = "List teacher's classes",
        description = "Retrieve all classes assigned to the authenticated teacher. " +
                      "By default, only active classes are returned. " +
                      "Set includeArchived=true to see archived classes."
    )
    public ResponseEntity<ApiResponse<List<ClassSummaryDto>>> listClasses(
            @Parameter(description = "Include archived classes in results", example = "false")
            @RequestParam(defaultValue = "false") boolean includeArchived,
            HttpServletRequest request) {

        // Extract teacher ID from JWT token
        UUID teacherId = extractTeacherIdFromRequest(request);

        log.info("Fetching classes for teacher: {} (includeArchived: {})", teacherId, includeArchived);

        List<ClassSummaryDto> classes = classService.listTeacherClasses(teacherId, includeArchived);

        log.info("Found {} classes for teacher: {}", classes.size(), teacherId);

        return ResponseEntity.ok(ApiResponse.success(classes));
    }

    /**
     * Get detailed information about a specific class.
     *
     * <p>GET /api/classes/{id}</p>
     *
     * <p>Returns complete class information including all enrolled students.</p>
     *
     * @param id      UUID of the class
     * @param request HTTP request to extract JWT token
     * @return detailed class information with student roster
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
        summary = "Get class details",
        description = "Retrieve detailed information about a specific class including " +
                      "all enrolled students. Only accessible by the teacher who owns the class."
    )
    public ResponseEntity<ApiResponse<ClassDetailDto>> getClassDetails(
            @Parameter(description = "Class UUID", required = true)
            @PathVariable UUID id,
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);

        log.info("Fetching class details for classId: {} by teacher: {}", id, teacherId);

        ClassDetailDto classDetails = classService.getClassDetails(id, teacherId);

        log.info("Returning class details with {} students",
                 classDetails.getStudents() != null ? classDetails.getStudents().size() : 0);

        return ResponseEntity.ok(ApiResponse.success(classDetails));
    }

    /**
     * Get list of students enrolled in a specific class.
     *
     * <p>GET /api/classes/{id}/students</p>
     *
     * <p>Returns roster of all students currently enrolled in the class.</p>
     *
     * @param id      UUID of the class
     * @param request HTTP request to extract JWT token
     * @return list of enrolled students
     */
    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
        summary = "Get class students",
        description = "Retrieve list of all students enrolled in a specific class. " +
                      "Only accessible by the teacher who owns the class."
    )
    public ResponseEntity<ApiResponse<List<StudentRosterItemDto>>> getClassStudents(
            @Parameter(description = "Class UUID", required = true)
            @PathVariable UUID id,
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);

        log.info("Fetching students for classId: {} by teacher: {}", id, teacherId);

        List<StudentRosterItemDto> students = classService.getClassStudents(id, teacherId);

        log.info("Returning {} students for class: {}", students.size(), id);

        return ResponseEntity.ok(ApiResponse.success(students));
    }

    /**
     * Get enrollment history for a specific class.
     *
     * <p>GET /api/classes/{id}/history</p>
     *
     * <p>Returns chronological history of all enrollment events for the class,
     * including current and past enrollments, ordered by date (newest first).</p>
     *
     * @param id      UUID of the class
     * @param request HTTP request to extract JWT token
     * @return list of enrollment history events
     */
    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
        summary = "Get enrollment history",
        description = "Retrieve complete enrollment history for a class, including " +
                      "all current and past enrollments. Results are ordered by " +
                      "enrollment date (newest first). Only accessible by the class teacher."
    )
    public ResponseEntity<ApiResponse<List<EnrollmentHistoryDto>>> getEnrollmentHistory(
            @Parameter(description = "Class UUID", required = true)
            @PathVariable UUID id,
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);

        log.info("Fetching enrollment history for classId: {} by teacher: {}", id, teacherId);

        List<EnrollmentHistoryDto> history = classService.getEnrollmentHistory(id, teacherId);

        log.info("Returning {} enrollment history records for class: {}", history.size(), id);

        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * Create a new class.
     *
     * <p>POST /api/classes</p>
     *
     * <p>Creates a new class with the provided information. The teacher ID is extracted
     * from the JWT token. Validates academic year format and checks for duplicate classes.</p>
     *
     * @param createRequest class creation request with required fields
     * @param request       HTTP request to extract JWT token
     * @return detailed information about the newly created class
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
        summary = "Create new class",
        description = "Create a new class with specified grade, section, and academic year. " +
                      "The class will be assigned to the authenticated teacher. " +
                      "Academic year must be in format 'YYYY-YYYY' with consecutive years. " +
                      "Validates against duplicate classes (same school, grade, section, academic year)."
    )
    public ResponseEntity<ApiResponse<ClassDetailDto>> createClass(
            @Parameter(description = "Class creation request", required = true)
            @Valid @RequestBody ClassCreateRequest createRequest,
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);

        log.info("Creating class for teacher: {}, grade: {}, section: {}, academicYear: {}",
                 teacherId, createRequest.getGrade(), createRequest.getSection(),
                 createRequest.getAcademicYear());

        ClassDetailDto classDetails = classService.createClass(createRequest, teacherId);

        log.info("Successfully created class with ID: {}", classDetails.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(classDetails));
    }

    /**
     * Update an existing class.
     *
     * <p>PUT /api/classes/{id}</p>
     *
     * <p>Updates class information with the provided fields. Only non-null fields
     * are updated. The teacher ID is extracted from the JWT token and verified
     * for class ownership. Validates academic year format if provided and checks
     * for duplicates if key fields change.</p>
     *
     * @param id            UUID of the class to update
     * @param updateRequest class update request with fields to update
     * @param request       HTTP request to extract JWT token
     * @return detailed information about the updated class
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
        summary = "Update class information",
        description = "Update an existing class's information. Only provided fields will be updated. " +
                      "Only the teacher who owns the class can update it. " +
                      "Academic year format is validated if provided. " +
                      "Validates against duplicate classes if grade, section, or academic year changes."
    )
    public ResponseEntity<ApiResponse<ClassDetailDto>> updateClass(
            @Parameter(description = "Class UUID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Class update request", required = true)
            @Valid @RequestBody ClassUpdateRequest updateRequest,
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);

        log.info("Updating class: {} by teacher: {}", id, teacherId);

        ClassDetailDto classDetails = classService.updateClass(id, updateRequest, teacherId);

        log.info("Successfully updated class: {}", id);

        return ResponseEntity.ok(ApiResponse.success(classDetails));
    }

    /**
     * Archive a class.
     *
     * <p>PUT /api/classes/{id}/archive</p>
     *
     * <p>Changes the class status to ARCHIVED. Archived classes are excluded from
     * default class lists but remain accessible when explicitly requested. Students
     * remain enrolled in archived classes. The teacher ID is extracted from the JWT
     * token and verified for class ownership.</p>
     *
     * @param id      UUID of the class to archive
     * @param request HTTP request to extract JWT token
     * @return detailed information about the archived class
     */
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
        summary = "Archive class",
        description = "Archive a class by changing its status to ARCHIVED. " +
                      "Archived classes do not appear in default class lists but remain accessible " +
                      "for historical purposes. Students remain enrolled in archived classes. " +
                      "Only the teacher who owns the class can archive it."
    )
    public ResponseEntity<ApiResponse<ClassDetailDto>> archiveClass(
            @Parameter(description = "Class UUID", required = true)
            @PathVariable UUID id,
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);

        log.info("Archiving class: {} by teacher: {}", id, teacherId);

        ClassDetailDto classDetails = classService.archiveClass(id, teacherId);

        log.info("Successfully archived class: {}", id);

        return ResponseEntity.ok(ApiResponse.success(classDetails));
    }

    /**
     * Extract teacher UUID from JWT token in the Authorization header.
     *
     * @param request HTTP request
     * @return teacher UUID
     * @throws IllegalStateException if JWT token is missing or invalid
     */
    private UUID extractTeacherIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.error("Missing or invalid Authorization header");
            throw new IllegalStateException("Missing or invalid Authorization header");
        }

        String jwt = bearerToken.substring(7);

        if (!jwtTokenProvider.validateToken(jwt)) {
            log.error("Invalid JWT token");
            throw new IllegalStateException("Invalid JWT token");
        }

        UUID userId = jwtTokenProvider.extractUserId(jwt);
        log.debug("Extracted teacher ID from JWT: {}", userId);

        return userId;
    }
}
