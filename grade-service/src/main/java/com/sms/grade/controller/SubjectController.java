package com.sms.grade.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.grade.dto.CreateSubjectRequest;
import com.sms.grade.dto.SubjectResponse;
import com.sms.grade.dto.UpdateSubjectRequest;
import com.sms.grade.service.interfaces.ISubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for subject reference data.
 */
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Tag(name = "Subjects", description = "Subject reference data endpoints")
public class SubjectController {

    private final ISubjectService subjectService;

    @GetMapping
    @Operation(summary = "Get all subjects")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getAllSubjects() {
        return ResponseEntity.ok(ApiResponse.success(subjectService.getAllSubjects()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subject by ID")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubject(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(subjectService.getSubject(id)));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get subject by code")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(subjectService.getSubjectByCode(code)));
    }

    @GetMapping("/core")
    @Operation(summary = "Get all core subjects")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getCoreSubjects() {
        return ResponseEntity.ok(ApiResponse.success(subjectService.getCoreSubjects()));
    }

    @GetMapping("/grade/{gradeLevel}")
    @Operation(summary = "Get subjects for a specific grade level")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getSubjectsForGrade(
            @PathVariable Integer gradeLevel) {
        return ResponseEntity.ok(ApiResponse.success(subjectService.getSubjectsForGrade(gradeLevel)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a subject")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(subjectService.updateSubject(id, request)));
    }

    @PostMapping
    @Operation(summary = "Create a new subject")
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @Valid @RequestBody CreateSubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(subjectService.createSubject(request)));
    }
}
