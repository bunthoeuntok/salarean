package com.sms.student.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.student.dto.BatchTransferRequest;
import com.sms.student.dto.BatchTransferResponse;
import com.sms.student.dto.ClassCreateRequest;
import com.sms.student.dto.ClassDetailDto;
import com.sms.student.dto.ClassListResponse;
import com.sms.student.dto.ClassUpdateRequest;
import com.sms.student.dto.EligibleClassResponse;
import com.sms.student.dto.EnrollmentHistoryDto;
import com.sms.student.dto.StudentEnrollmentListResponse;
import com.sms.student.dto.UndoTransferResponse;
import com.sms.student.security.JwtTokenProvider;
import com.sms.student.service.interfaces.IClassService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private final IClassService classService;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.sms.student.service.IStudentTransferService studentTransferService;

    /**
     * List all classes for the authenticated teacher with pagination and filtering.
     *
     * <p>GET /api/classes</p>
     *
     * <p>Query Parameters:
     * <ul>
     *   <li>page (optional, default: 0) - Page number (0-indexed)</li>
     *   <li>size (optional, default: 20) - Page size</li>
     *   <li>sort (optional, default: "grade,asc") - Sort field and direction</li>
     *   <li>search (optional) - Search by class name</li>
     *   <li>status (optional) - Filter by status (comma-separated: ACTIVE,INACTIVE,COMPLETED)</li>
     *   <li>academicYear (optional) - Filter by academic year</li>
     *   <li>grade (optional) - Filter by grade</li>
     *   <li>level (optional) - Filter by class level (PRIMARY,SECONDARY,HIGH_SCHOOL)</li>
     *   <li>type (optional) - Filter by class type (NORMAL,SCIENCE,SOCIAL_SCIENCE)</li>
     * </ul>
     * </p>
     *
     * <p>Returns paginated list of class summaries with current enrollment counts.</p>
     *
     * @param page         page number (0-indexed)
     * @param size         page size
     * @param sort         sort field and direction (e.g., "grade,asc")
     * @param search       search term for class name
     * @param status       filter by status (comma-separated)
     * @param academicYear filter by academic year
     * @param grade        filter by grade
     * @param level        filter by class level
     * @param type         filter by class type
     * @param request      HTTP request to extract JWT token
     * @return paginated list of class summaries
     */
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
        summary = "List teacher's classes",
        description = "Retrieve all classes assigned to the authenticated teacher with pagination and filtering."
    )
    public ResponseEntity<ApiResponse<ClassListResponse>> listClasses(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field and direction", example = "grade,asc")
            @RequestParam(defaultValue = "grade,asc") String sort,
            @Parameter(description = "Search by class name")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by status (comma-separated: ACTIVE,INACTIVE,COMPLETED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by academic year", example = "2024-2025")
            @RequestParam(required = false) String academicYear,
            @Parameter(description = "Filter by grade", example = "10")
            @RequestParam(required = false) String grade,
            @Parameter(description = "Filter by class level (PRIMARY,SECONDARY,HIGH_SCHOOL)")
            @RequestParam(required = false) String level,
            @Parameter(description = "Filter by class type (NORMAL,SCIENCE,SOCIAL_SCIENCE)")
            @RequestParam(required = false) String type,
            HttpServletRequest request) {

        // Extract teacher ID from JWT token
        UUID teacherId = extractTeacherIdFromRequest(request);

        Pageable pageable = createPageable(page, size, sort);
        ClassListResponse response = classService.listClassesWithFilters(
                teacherId, search, status, academicYear, grade, level, type, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Helper method to create Pageable with sorting.
     * Maps frontend sort field names to database column names.
     */
    private Pageable createPageable(int page, int size, String sortParam) {
        String[] sortParts = sortParam.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // Map computed/virtual fields to actual database columns
        Sort sort = mapSortField(sortField, direction);

        return PageRequest.of(page, size, sort);
    }

    /**
     * Map frontend sort field names to database column names.
     * Handles computed fields like className that don't exist as DB columns.
     */
    private Sort mapSortField(String sortField, Sort.Direction direction) {
        return switch (sortField) {
            // className is computed from grade + section (e.g., "Grade 5A")
            case "className" -> Sort.by(direction, "grade").and(Sort.by(direction, "section"));
            default -> Sort.by(direction, sortField);
        };
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
        ClassDetailDto classDetails = classService.getClassDetails(id, teacherId);

        return ResponseEntity.ok(ApiResponse.success(classDetails));
    }

    /**
     * Get list of students enrolled in a specific class with optional status filter.
     *
     * <p>GET /api/classes/{id}/students</p>
     *
     * <p>Returns all students enrolled in the class with enrollment metadata.
     * Supports optional filtering by enrollment status (ACTIVE, COMPLETED, TRANSFERRED, WITHDRAWN).
     * Search filtering is performed client-side using TanStack Table for real-time feedback.</p>
     *
     * @param id      UUID of the class
     * @param status  optional enrollment status filter (ACTIVE, COMPLETED, TRANSFERRED, WITHDRAWN)
     * @param sort    sort field and direction (e.g., "studentName,asc")
     * @return student enrollment list response with students and total count
     */
    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
        summary = "Get class students",
        description = "Retrieve list of all students enrolled in a specific class. " +
                      "Supports optional filtering by enrollment status. " +
                      "Search filtering should be done client-side for real-time feedback."
    )
    public ResponseEntity<ApiResponse<StudentEnrollmentListResponse>> getStudentEnrollments(
            @Parameter(description = "Class UUID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Filter by enrollment status (ACTIVE, COMPLETED, TRANSFERRED, WITHDRAWN)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Sort field and direction", example = "studentName,asc")
            @RequestParam(defaultValue = "studentName,asc") String sort) {

        log.info("Fetching student enrollments for classId: {}, status: {}, sort: {}", id, status, sort);

        StudentEnrollmentListResponse response = classService.getStudentEnrollments(id, status, sort);

        log.info("Returning {} students for class: {}", response.getTotalCount(), id);

        return ResponseEntity.ok(ApiResponse.success(response));
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
     * Execute batch transfer of students to another class.
     *
     * <p>POST /api/classes/{id}/batch-transfer</p>
     *
     * <p>Transfers multiple students from the source class to a destination class in a single
     * atomic operation. The operation:
     * <ul>
     *   <li>Validates source and destination classes are active and have matching grades</li>
     *   <li>Checks destination class has sufficient capacity</li>
     *   <li>Updates enrollment status for all students</li>
     *   <li>Creates enrollment history records with transfer ID for undo capability</li>
     *   <li>Updates class enrollment counts</li>
     *   <li>Handles partial failures (some students transfer successfully, others fail)</li>
     * </ul>
     * </p>
     *
     * <p>Teacher authorization is enforced via JWT token extraction. Only the teacher who owns
     * the source class can initiate transfers.</p>
     *
     * @param id UUID of the source class
     * @param request batch transfer request containing destination class ID and student IDs
     * @param httpRequest HTTP request to extract JWT token
     * @return transfer response with success count, transfer ID, and any failed transfers
     */
    @PostMapping("/{id}/batch-transfer")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
        summary = "Execute batch student transfer",
        description = "Transfer multiple students from the source class to a destination class. " +
                      "Validates grade match, capacity, and creates complete audit trail. " +
                      "Returns transfer ID for potential undo operation. " +
                      "Handles partial failures gracefully."
    )
    public ResponseEntity<ApiResponse<BatchTransferResponse>> batchTransfer(
            @Parameter(description = "Source class UUID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Batch transfer request", required = true)
            @Valid @RequestBody BatchTransferRequest request,
            HttpServletRequest httpRequest) {

        UUID teacherId = extractTeacherIdFromRequest(httpRequest);

        log.info("Executing batch transfer from class {} to class {} by teacher {} for {} students",
                 id, request.getDestinationClassId(), teacherId, request.getStudentIds().size());

        BatchTransferResponse response = studentTransferService.batchTransfer(id, request, teacherId);

        log.info("Batch transfer completed: transferId={}, successful={}, failed={}",
                 response.getTransferId(), response.getSuccessfulTransfers(),
                 response.getFailedTransfers().size());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Undo a batch transfer operation.
     *
     * <p>POST /api/classes/transfers/{transferId}/undo</p>
     *
     * <p>Reverses a batch transfer operation within 5 minutes of the original transfer.
     * The operation:
     * <ul>
     *   <li>Validates the transfer exists and was performed by the requesting teacher</li>
     *   <li>Checks the undo is within the 5-minute time window</li>
     *   <li>Verifies the transfer has not already been undone</li>
     *   <li>Checks for conflicts (students not transferred again)</li>
     *   <li>Reverts all students back to the source class</li>
     *   <li>Updates enrollment statuses and class counts</li>
     *   <li>Creates undo history records for audit trail</li>
     * </ul>
     * </p>
     *
     * <p>Authorization: Only the teacher who performed the original transfer can undo it.</p>
     *
     * @param transferId UUID of the transfer to undo
     * @param httpRequest HTTP request to extract JWT token
     * @return undo response with count of reverted students
     */
    @PostMapping("/transfers/{transferId}/undo")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
        summary = "Undo batch student transfer",
        description = "Reverse a batch transfer operation within 5 minutes. " +
                      "Validates authorization, time window, and handles conflicts. " +
                      "Returns count of successfully reverted students."
    )
    public ResponseEntity<ApiResponse<UndoTransferResponse>> undoTransfer(
            @Parameter(description = "Transfer ID to undo", required = true)
            @PathVariable UUID transferId,
            HttpServletRequest httpRequest) {

        UUID teacherId = extractTeacherIdFromRequest(httpRequest);

        log.info("Undoing transfer {} by teacher {}", transferId, teacherId);

        UndoTransferResponse response = studentTransferService.undoTransfer(transferId, teacherId);

        log.info("Undo completed: {} students reverted to source class {}",
                 response.getUndoneStudents(), response.getSourceClassId());

        return ResponseEntity.ok(ApiResponse.success(response));
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
