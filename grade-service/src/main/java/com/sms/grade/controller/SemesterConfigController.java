package com.sms.grade.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.grade.dto.SemesterConfigRequest;
import com.sms.grade.dto.SemesterConfigResponse;
import com.sms.grade.service.interfaces.ISemesterConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for semester configuration management.
 * Provides endpoints for both admin (default configs) and teacher (custom configs).
 */
@Slf4j
@RestController
@RequestMapping("/api/semester-configs")
@RequiredArgsConstructor
@Tag(name = "Semester Configuration", description = "Semester exam schedule configuration endpoints")
public class SemesterConfigController {

    private final ISemesterConfigService configService;

    // =============================================
    // Teacher Endpoints (with fallback to default)
    // =============================================

    @GetMapping("/{academicYear}/{semesterExamCode}")
    @Operation(summary = "Get semester config (teacher's custom or default)")
    public ResponseEntity<ApiResponse<SemesterConfigResponse>> getConfig(
            @PathVariable String academicYear,
            @PathVariable String semesterExamCode) {
        log.debug("Getting config for academic year {} semester exam code {}", academicYear, semesterExamCode);
        SemesterConfigResponse config = configService.getConfig(academicYear, semesterExamCode);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @GetMapping("/{academicYear}")
    @Operation(summary = "Get all semester configs for an academic year")
    public ResponseEntity<ApiResponse<List<SemesterConfigResponse>>> getConfigsByAcademicYear(
            @PathVariable String academicYear) {
        List<SemesterConfigResponse> configs = configService.getConfigsByAcademicYear(academicYear);
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @PostMapping("/teacher")
    @Operation(summary = "Save teacher-specific configuration")
    public ResponseEntity<ApiResponse<SemesterConfigResponse>> saveTeacherConfig(
            @Valid @RequestBody SemesterConfigRequest request) {
        log.info("Saving teacher config for academic year {} semester exam code {}",
                request.getAcademicYear(), request.getSemesterExamCode());
        SemesterConfigResponse config = configService.saveTeacherConfig(request);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @DeleteMapping("/teacher/{academicYear}/{semesterExamCode}")
    @Operation(summary = "Delete teacher-specific configuration (will fall back to default)")
    public ResponseEntity<ApiResponse<Void>> deleteTeacherConfig(
            @PathVariable String academicYear,
            @PathVariable String semesterExamCode) {
        log.info("Deleting teacher config for academic year {} semester exam code {}", academicYear, semesterExamCode);
        configService.deleteTeacherConfig(academicYear, semesterExamCode);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // =============================================
    // Admin Endpoints (Default Configurations)
    // =============================================

    @GetMapping("/admin/defaults")
    @Operation(summary = "Get all default configurations (admin only)")
    public ResponseEntity<ApiResponse<List<SemesterConfigResponse>>> getAllDefaultConfigs() {
        List<SemesterConfigResponse> configs = configService.getAllDefaultConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @GetMapping("/admin/defaults/{academicYear}")
    @Operation(summary = "Get default configurations for an academic year")
    public ResponseEntity<ApiResponse<List<SemesterConfigResponse>>> getDefaultConfigsByAcademicYear(
            @PathVariable String academicYear) {
        List<SemesterConfigResponse> configs = configService.getDefaultConfigsByAcademicYear(academicYear);
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @PostMapping("/admin/defaults")
    @Operation(summary = "Create or update default configuration (admin only)")
    public ResponseEntity<ApiResponse<SemesterConfigResponse>> saveDefaultConfig(
            @Valid @RequestBody SemesterConfigRequest request) {
        log.info("Saving default config for academic year {} semester exam code {}",
                request.getAcademicYear(), request.getSemesterExamCode());
        SemesterConfigResponse config = configService.saveDefaultConfig(request);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @DeleteMapping("/admin/defaults/{academicYear}/{semesterExamCode}")
    @Operation(summary = "Delete default configuration (admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteDefaultConfig(
            @PathVariable String academicYear,
            @PathVariable String semesterExamCode) {
        log.info("Deleting default config for academic year {} semester exam code {}", academicYear, semesterExamCode);
        configService.deleteDefaultConfig(academicYear, semesterExamCode);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/admin/academic-years")
    @Operation(summary = "Get list of academic years with default configs")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableAcademicYears() {
        List<String> years = configService.getAvailableAcademicYears();
        return ResponseEntity.ok(ApiResponse.success(years));
    }
}
