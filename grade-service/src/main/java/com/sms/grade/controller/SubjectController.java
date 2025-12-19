package com.sms.grade.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.grade.dto.SubjectResponse;
import com.sms.grade.model.Subject;
import com.sms.grade.repository.SubjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for subject reference data.
 */
@Slf4j
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Tag(name = "Subjects", description = "Subject reference data endpoints")
public class SubjectController {

    private final SubjectRepository subjectRepository;

    @GetMapping
    @Operation(summary = "Get all subjects")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getAllSubjects() {
        List<Subject> subjects = subjectRepository.findAllByOrderByDisplayOrderAsc();
        List<SubjectResponse> responses = subjects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subject by ID")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubject(@PathVariable UUID id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(subject)));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get subject by code")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectByCode(@PathVariable String code) {
        Subject subject = subjectRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(subject)));
    }

    @GetMapping("/core")
    @Operation(summary = "Get all core subjects")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getCoreSubjects() {
        List<Subject> subjects = subjectRepository.findByIsCoreTrue();
        List<SubjectResponse> responses = subjects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/grade/{gradeLevel}")
    @Operation(summary = "Get subjects for a specific grade level")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getSubjectsForGrade(
            @PathVariable Integer gradeLevel) {
        List<Subject> allSubjects = subjectRepository.findAllByOrderByDisplayOrderAsc();
        List<SubjectResponse> responses = allSubjects.stream()
                .filter(s -> s.getGradeLevels() != null && s.getGradeLevels().contains(gradeLevel))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    private SubjectResponse mapToResponse(Subject subject) {
        return SubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .nameKhmer(subject.getNameKhmer())
                .code(subject.getCode())
                .gradeLevels(subject.getGradeLevels())
                .isCore(subject.getIsCore())
                .displayOrder(subject.getDisplayOrder())
                .build();
    }
}
