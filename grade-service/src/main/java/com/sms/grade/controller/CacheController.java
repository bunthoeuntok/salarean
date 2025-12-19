package com.sms.grade.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.grade.cache.GradeCache;
import com.sms.grade.security.TeacherContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for cache management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Tag(name = "Cache", description = "Cache management endpoints")
public class CacheController {

    private final GradeCache gradeCache;

    @DeleteMapping("/teacher")
    @Operation(summary = "Clear all cached data for current teacher")
    public ResponseEntity<ApiResponse<String>> clearTeacherCache() {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Clearing cache for teacher {}", teacherId);
        gradeCache.evictTeacherCache(teacherId);
        return ResponseEntity.ok(ApiResponse.success("Cache cleared for teacher"));
    }

    @DeleteMapping("/class/{classId}/semester/{semester}")
    @Operation(summary = "Clear cached data for a specific class")
    public ResponseEntity<ApiResponse<String>> clearClassCache(
            @PathVariable UUID classId,
            @PathVariable Integer semester,
            @RequestParam String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Clearing cache for class {} semester {}", classId, semester);
        gradeCache.evictClassCache(teacherId, classId, semester, academicYear);
        return ResponseEntity.ok(ApiResponse.success("Cache cleared for class"));
    }

    @PostMapping("/reload")
    @Operation(summary = "Reload cache for current teacher (clears and allows fresh data)")
    public ResponseEntity<ApiResponse<String>> reloadCache() {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Reloading cache for teacher {}", teacherId);
        gradeCache.evictTeacherCache(teacherId);
        return ResponseEntity.ok(ApiResponse.success("Cache reloaded - fresh data will be cached on next request"));
    }
}
