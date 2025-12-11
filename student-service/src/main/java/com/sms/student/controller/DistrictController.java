package com.sms.student.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.student.dto.DistrictResponse;
import com.sms.student.service.interfaces.IDistrictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/districts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Districts", description = "District management APIs")
public class DistrictController {

    private final IDistrictService districtService;

    @Operation(summary = "Get districts by province")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Districts retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Province not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<DistrictResponse>>> getDistrictsByProvince(
            @Parameter(description = "Province ID") @RequestParam UUID provinceId) {
        log.info("GET /api/districts?provinceId={} - Fetching districts", provinceId);

        List<DistrictResponse> districts = districtService.getDistrictsByProvince(provinceId);

        log.info("Retrieved {} districts for province ID: {}", districts.size(), provinceId);

        return ResponseEntity.ok(ApiResponse.success(districts));
    }
}
