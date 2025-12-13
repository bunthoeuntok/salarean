package com.sms.student.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.ErrorCode;
import com.sms.student.dto.TeacherSchoolRequest;
import com.sms.student.dto.TeacherSchoolResponse;
import com.sms.student.service.interfaces.ITeacherSchoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for teacher-school association management.
 * All endpoints require TEACHER role.
 */
@RestController
@RequestMapping("/api/teacher-school")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Teacher School", description = "Teacher-school association APIs")
public class TeacherSchoolController {

    private final ITeacherSchoolService teacherSchoolService;

    @Operation(summary = "Create or update teacher-school association")
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<TeacherSchoolResponse>> createOrUpdateAssociation(
            @Valid @RequestBody TeacherSchoolRequest request) {
        UUID userId = extractUserIdFromToken();
        TeacherSchoolResponse response = teacherSchoolService.createOrUpdate(userId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "Get teacher-school association for authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Association retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Association not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<TeacherSchoolResponse>> getAssociation() {
        UUID userId = extractUserIdFromToken();
        log.info("GET /api/teacher-school - Fetching association for user: {}", userId);

        TeacherSchoolResponse response = teacherSchoolService.getByUserId(userId);

        if (response == null) {
            log.info("No teacher-school association found for user: {}", userId);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ErrorCode.RESOURCE_NOT_FOUND));
        }

        log.info("Teacher-school association found for user: {}", userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Extract user ID from JWT token in SecurityContext.
     *
     * @return User ID (UUID)
     * @throws IllegalStateException if user ID cannot be extracted
     */
    private UUID extractUserIdFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        String userId = authentication.getName();

        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format in token: {}", userId);
            throw new IllegalStateException("Invalid user ID in token");
        }
    }
}
