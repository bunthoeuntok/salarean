package com.sms.student.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.ErrorCode;
import com.sms.student.dto.*;
import com.sms.student.enums.ClassShift;
import com.sms.student.security.JwtTokenProvider;
import com.sms.student.service.interfaces.IScheduleService;
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

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Schedule Management", description = "APIs for managing class timetables and schedules")
public class ScheduleController {

    private final IScheduleService scheduleService;
    private final JwtTokenProvider jwtTokenProvider;

    // ===================== TIME SLOT TEMPLATES =====================

    @GetMapping("/templates")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get available templates", description = "Retrieve all time slot templates available to the teacher")
    public ResponseEntity<ApiResponse<List<TimeSlotTemplateResponse>>> getTemplates(
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);
        log.info("GET /api/schedules/templates - teacherId: {}", teacherId);

        List<TimeSlotTemplateResponse> templates = scheduleService.getAvailableTemplates(teacherId);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/templates/shift/{shift}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get templates by shift", description = "Retrieve templates for a specific shift type")
    public ResponseEntity<ApiResponse<List<TimeSlotTemplateResponse>>> getTemplatesByShift(
            @Parameter(description = "Shift type", example = "MORNING")
            @PathVariable ClassShift shift,
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);
        log.info("GET /api/schedules/templates/shift/{} - teacherId: {}", shift, teacherId);

        List<TimeSlotTemplateResponse> templates = scheduleService.getTemplatesByShift(shift, teacherId);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/templates/{templateId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get template by ID", description = "Retrieve a specific time slot template")
    public ResponseEntity<ApiResponse<TimeSlotTemplateResponse>> getTemplateById(
            @Parameter(description = "Template UUID")
            @PathVariable UUID templateId,
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);
        log.info("GET /api/schedules/templates/{} - teacherId: {}", templateId, teacherId);

        TimeSlotTemplateResponse template = scheduleService.getTemplateById(templateId, teacherId);
        return ResponseEntity.ok(ApiResponse.success(template));
    }

    @PostMapping("/templates")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Create custom template", description = "Create a custom time slot template")
    public ResponseEntity<ApiResponse<TimeSlotTemplateResponse>> createTemplate(
            @Valid @RequestBody CreateTimeSlotTemplateRequest createRequest,
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);
        log.info("POST /api/schedules/templates - teacherId: {}", teacherId);

        TimeSlotTemplateResponse template = scheduleService.createTemplate(createRequest, teacherId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(template));
    }

    @DeleteMapping("/templates/{templateId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Delete template", description = "Delete a custom time slot template")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @Parameter(description = "Template UUID")
            @PathVariable UUID templateId,
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);
        log.info("DELETE /api/schedules/templates/{} - teacherId: {}", templateId, teacherId);

        scheduleService.deleteTemplate(templateId, teacherId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ===================== CLASS SCHEDULES =====================

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get class schedule", description = "Retrieve the schedule for a specific class")
    public ResponseEntity<ApiResponse<ClassScheduleResponse>> getClassSchedule(
            @Parameter(description = "Class UUID")
            @PathVariable UUID classId,
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);
        log.info("GET /api/schedules/class/{} - teacherId: {}", classId, teacherId);

        ClassScheduleResponse schedule = scheduleService.getClassSchedule(classId, teacherId);
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    @PostMapping("/class")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Create class schedule", description = "Create a new schedule for a class")
    public ResponseEntity<ApiResponse<ClassScheduleResponse>> createClassSchedule(
            @Valid @RequestBody CreateClassScheduleRequest createRequest,
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);
        log.info("POST /api/schedules/class - teacherId: {}, classId: {}", teacherId, createRequest.getClassId());

        ClassScheduleResponse schedule = scheduleService.createClassSchedule(createRequest, teacherId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(schedule));
    }

    @PutMapping("/class/{classId}/entries")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Update schedule entries", description = "Add or update schedule entries for a class")
    public ResponseEntity<ApiResponse<ClassScheduleResponse>> updateScheduleEntries(
            @Parameter(description = "Class UUID")
            @PathVariable UUID classId,
            @Valid @RequestBody UpdateScheduleEntriesRequest updateRequest,
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);
        log.info("PUT /api/schedules/class/{}/entries - teacherId: {}", classId, teacherId);

        ClassScheduleResponse schedule = scheduleService.updateScheduleEntries(classId, updateRequest, teacherId);
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    @DeleteMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Delete class schedule", description = "Delete the schedule for a class")
    public ResponseEntity<ApiResponse<Void>> deleteClassSchedule(
            @Parameter(description = "Class UUID")
            @PathVariable UUID classId,
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);
        log.info("DELETE /api/schedules/class/{} - teacherId: {}", classId, teacherId);

        scheduleService.deleteClassSchedule(classId, teacherId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/class/{classId}/clear")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Clear schedule entries", description = "Clear all entries from a class schedule")
    public ResponseEntity<ApiResponse<Void>> clearScheduleEntries(
            @Parameter(description = "Class UUID")
            @PathVariable UUID classId,
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);
        log.info("POST /api/schedules/class/{}/clear - teacherId: {}", classId, teacherId);

        scheduleService.clearScheduleEntries(classId, teacherId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/class/{targetClassId}/copy-from/{sourceClassId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Copy schedule", description = "Copy schedule from one class to another")
    public ResponseEntity<ApiResponse<ClassScheduleResponse>> copySchedule(
            @Parameter(description = "Target class UUID")
            @PathVariable UUID targetClassId,
            @Parameter(description = "Source class UUID")
            @PathVariable UUID sourceClassId,
            HttpServletRequest request) {

        UUID teacherId = extractTeacherIdFromRequest(request);
        log.info("POST /api/schedules/class/{}/copy-from/{} - teacherId: {}", targetClassId, sourceClassId, teacherId);

        ClassScheduleResponse schedule = scheduleService.copySchedule(targetClassId, sourceClassId, teacherId);
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    /**
     * Extract teacher ID from JWT token in the request.
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
