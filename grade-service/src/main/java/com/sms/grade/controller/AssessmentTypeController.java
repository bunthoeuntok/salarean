package com.sms.grade.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.grade.dto.AssessmentTypeResponse;
import com.sms.grade.enums.AssessmentCategory;
import com.sms.grade.model.AssessmentType;
import com.sms.grade.repository.AssessmentTypeRepository;
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
 * REST controller for assessment type reference data.
 */
@Slf4j
@RestController
@RequestMapping("/api/assessment-types")
@RequiredArgsConstructor
@Tag(name = "Assessment Types", description = "Assessment type reference data endpoints")
public class AssessmentTypeController {

    private final AssessmentTypeRepository assessmentTypeRepository;

    @GetMapping
    @Operation(summary = "Get all assessment types")
    public ResponseEntity<ApiResponse<List<AssessmentTypeResponse>>> getAllAssessmentTypes() {
        List<AssessmentType> types = assessmentTypeRepository.findAllByOrderByDisplayOrderAsc();
        List<AssessmentTypeResponse> responses = types.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get assessment type by ID")
    public ResponseEntity<ApiResponse<AssessmentTypeResponse>> getAssessmentType(@PathVariable UUID id) {
        AssessmentType type = assessmentTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment type not found"));
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(type)));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get assessment type by code")
    public ResponseEntity<ApiResponse<AssessmentTypeResponse>> getAssessmentTypeByCode(@PathVariable String code) {
        AssessmentType type = assessmentTypeRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Assessment type not found"));
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(type)));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get assessment types by category")
    public ResponseEntity<ApiResponse<List<AssessmentTypeResponse>>> getByCategory(
            @PathVariable AssessmentCategory category) {
        List<AssessmentType> types = assessmentTypeRepository.findByCategoryOrderByDisplayOrderAsc(category);
        List<AssessmentTypeResponse> responses = types.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get all monthly exam types")
    public ResponseEntity<ApiResponse<List<AssessmentTypeResponse>>> getMonthlyExamTypes() {
        List<AssessmentType> types = assessmentTypeRepository
                .findByCategoryOrderByDisplayOrderAsc(AssessmentCategory.MONTHLY_EXAM);
        List<AssessmentTypeResponse> responses = types.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/semester")
    @Operation(summary = "Get semester exam type")
    public ResponseEntity<ApiResponse<AssessmentTypeResponse>> getSemesterExamType() {
        List<AssessmentType> types = assessmentTypeRepository
                .findByCategoryOrderByDisplayOrderAsc(AssessmentCategory.SEMESTER_EXAM);
        if (types.isEmpty()) {
            throw new RuntimeException("Semester exam type not found");
        }
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(types.get(0))));
    }

    private AssessmentTypeResponse mapToResponse(AssessmentType type) {
        return AssessmentTypeResponse.builder()
                .id(type.getId())
                .name(type.getName())
                .nameKhmer(type.getNameKhmer())
                .code(type.getCode())
                .category(type.getCategory())
                .defaultWeight(type.getDefaultWeight())
                .maxScore(type.getMaxScore())
                .displayOrder(type.getDisplayOrder())
                .build();
    }
}
