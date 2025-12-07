package com.sms.student.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.student.dto.*;
import com.sms.student.enums.DeletionReason;
import com.sms.student.service.interfaces.IStudentService;

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

import java.util.UUID;

/**
 * REST controller for student management operations.
 * All endpoints return ApiResponse<T> wrapper following SMS API standards.
 */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final IStudentService studentService;

    /**
     * Create a new student profile.
     * POST /api/students
     * Requires TEACHER role.
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            @Valid @RequestBody StudentRequest request) {
        StudentResponse response = studentService.createStudent(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * Update existing student information.
     * PUT /api/students/{id}
     * Requires TEACHER role.
     * Note: Does not update enrollment info - use EnrollmentController for that.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @PathVariable UUID id,
            @Valid @RequestBody StudentUpdateRequest request) {
        StudentResponse response = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Soft delete a student.
     * DELETE /api/students/{id}
     * Requires TEACHER role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(
            @PathVariable UUID id,
            @RequestParam(required = false) DeletionReason reason,
            @RequestParam(required = false) UUID deletedBy) {
        studentService.deleteStudent(id, reason, deletedBy);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Get student details by ID.
     * GET /api/students/{id}
     * Requires TEACHER role.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(@PathVariable UUID id) {
        StudentResponse response = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get student details by student code.
     * GET /api/students/code/{studentCode}
     * Requires TEACHER role.
     */
    @GetMapping("/code/{studentCode}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentByCode(
            @PathVariable String studentCode) {
        StudentResponse response = studentService.getStudentByCode(studentCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * List students with pagination and filtering.
     * GET /api/students
     * Requires TEACHER role.
     *
     * @param page    page number (0-indexed)
     * @param size    page size
     * @param sort    sort field and direction (e.g., "lastName,asc")
     * @param search  search by name or student code
     * @param status  filter by status (comma-separated: ACTIVE,INACTIVE)
     * @param gender  filter by gender (comma-separated: M,F)
     * @param level   filter by class level (PRIMARY, SECONDARY, HIGH_SCHOOL)
     * @param grade   filter by grade (1-12)
     * @param classId filter by class ID (use "NONE" for students without class)
     * @return paginated list of students
     */
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<StudentListResponse>> listStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName,asc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false) String classId) {

        Pageable pageable = createPageable(page, size, sort);
        StudentListResponse response = studentService.listStudentsWithFilters(
                search, status, gender, level, grade, classId, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Upload student photo.
     * POST /api/students/{id}/photo
     * Requires TEACHER role.
     */
    @PostMapping("/{id}/photo")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<PhotoUploadResponse>> uploadStudentPhoto(
            @PathVariable UUID id,
            @RequestParam("file") byte[] photoData,
            @RequestParam("contentType") String contentType) {

        PhotoUploadResponse response = studentService.uploadStudentPhoto(id, photoData, contentType);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Helper method to create Pageable with sorting.
     */
    private Pageable createPageable(int page, int size, String sortParam) {
        String[] sortParts = sortParam.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }
}
