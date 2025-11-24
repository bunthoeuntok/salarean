package com.sms.student.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.student.dto.ClassSummaryDto;
import com.sms.student.security.JwtTokenProvider;
import com.sms.student.service.ClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
