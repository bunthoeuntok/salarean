package com.sms.student.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.student.dto.CacheReloadResponse;
import com.sms.student.service.interfaces.IStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for cache management operations.
 * Allows teachers to manually reload their cached student data.
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cache Management", description = "Cache reload and management endpoints")
public class CacheController {

    private final IStudentService studentService;

    /**
     * Reload cache for the authenticated teacher.
     * Clears all cached student data for the current teacher, forcing fresh retrieval from database.
     *
     * @return Response indicating cache reload success
     */
    @PostMapping("/reload")
    @Operation(
        summary = "Reload teacher's student cache",
        description = "Clears all cached student data for the authenticated teacher. " +
                      "Next API calls will fetch fresh data from the database."
    )
    public ResponseEntity<ApiResponse<CacheReloadResponse>> reloadCache() {
        log.info("Cache reload requested");

        CacheReloadResponse response = studentService.clearTeacherCache();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
