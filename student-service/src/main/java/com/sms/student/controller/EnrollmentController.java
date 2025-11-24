package com.sms.student.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.student.dto.EnrollmentHistoryResponse;
import com.sms.student.dto.EnrollmentRequest;
import com.sms.student.dto.EnrollmentResponse;
import com.sms.student.dto.TransferRequest;
import com.sms.student.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Student Enrollment", description = "Enrollment management APIs")
@RequiredArgsConstructor
@Slf4j
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Get complete enrollment history for a student.
     */
    @GetMapping("/{id}/enrollment-history")
    @Operation(
        summary = "Get student enrollment history",
        description = "Retrieve complete enrollment history for a student including all past and current class enrollments"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "History retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Student not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<EnrollmentHistoryResponse>> getEnrollmentHistory(
            @PathVariable UUID id) {
        log.info("GET /api/students/{}/enrollment-history", id);

        EnrollmentHistoryResponse response = enrollmentService.getEnrollmentHistory(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Enroll a student in a class.
     */
    @PostMapping("/{id}/enroll")
    @Operation(
        summary = "Enroll student in class",
        description = "Enroll a student in a specific class. Validates student exists, class exists, no duplicate enrollment, and capacity available. Creates enrollment with status ACTIVE and reason NEW. Increments class student_count."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Student enrolled successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error - invalid request data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Student or class not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate enrollment or class capacity exceeded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollStudent(
            @PathVariable UUID id,
            @Valid @RequestBody EnrollmentRequest request) {
        log.info("POST /api/students/{}/enroll - classId: {}", id, request.getClassId());

        EnrollmentResponse response = enrollmentService.enrollStudent(id, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    /**
     * Transfer a student from their current class to a new class.
     */
    @PostMapping("/{id}/transfer")
    @Operation(
        summary = "Transfer student to new class",
        description = "Transfer a student from their current class to a new class. Marks old enrollment as TRANSFERRED, creates new enrollment with status ACTIVE and reason TRANSFER. Updates both class student counts atomically."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Student transferred successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error - invalid request data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Student not found, no active enrollment, or target class not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Target class capacity exceeded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<EnrollmentResponse>> transferStudent(
            @PathVariable UUID id,
            @Valid @RequestBody TransferRequest request) {
        log.info("POST /api/students/{}/transfer - targetClassId: {}", id, request.getTargetClassId());

        EnrollmentResponse response = enrollmentService.transferStudent(id, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }
}
