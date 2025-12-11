package com.sms.student.controller;

import com.sms.common.dto.ApiResponse;
import com.sms.student.dto.ProvinceResponse;
import com.sms.student.service.interfaces.IProvinceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/provinces")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Provinces", description = "Province management APIs")
public class ProvinceController {

    private final IProvinceService provinceService;

    @Operation(summary = "Get all provinces")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Provinces retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProvinceResponse>>> getAllProvinces() {
        log.info("GET /api/provinces - Fetching all provinces");

        List<ProvinceResponse> provinces = provinceService.getAllProvinces();

        log.info("Retrieved {} provinces", provinces.size());

        return ResponseEntity.ok(ApiResponse.success(provinces));
    }
}
