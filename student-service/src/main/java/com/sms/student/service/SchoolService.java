package com.sms.student.service;

import com.sms.student.dto.SchoolRequest;
import com.sms.student.dto.SchoolResponse;
import com.sms.student.exception.DistrictNotFoundException;
import com.sms.student.exception.ProvinceNotFoundException;
import com.sms.student.exception.SchoolNotFoundException;
import com.sms.student.model.District;
import com.sms.student.model.Province;
import com.sms.student.model.School;
import com.sms.student.repository.DistrictRepository;
import com.sms.student.repository.ProvinceRepository;
import com.sms.student.repository.SchoolRepository;
import com.sms.student.service.interfaces.ISchoolService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of SchoolService.
 * Handles all school-related business logic including listing and retrieval.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolService implements ISchoolService {

    private final SchoolRepository schoolRepository;
    private final DistrictRepository districtRepository;
    private final ProvinceRepository provinceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SchoolResponse> listAllSchools() {
        log.info("Fetching all schools");

        List<School> schools = schoolRepository.findAll();

        log.info("Found {} schools", schools.size());

        return schools.stream()
                .map(this::mapToSchoolResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolResponse getSchoolById(UUID id) {
        log.info("Fetching school by ID: {}", id);

        School school = schoolRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("School not found: {}", id);
                    return new SchoolNotFoundException("School with ID " + id + " not found");
                });

        log.info("School found: {}", school.getName());

        return mapToSchoolResponse(school);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolResponse> getSchoolsByDistrict(UUID districtId) {
        log.debug("Fetching schools for district ID: {}", districtId);

        // Validate district exists
        if (!districtRepository.existsById(districtId)) {
            log.warn("District not found with ID: {}", districtId);
            throw new DistrictNotFoundException("District not found with ID: " + districtId);
        }

        List<School> schools = schoolRepository.findByDistrictIdOrderByNameAsc(districtId);

        log.debug("Found {} schools for district ID: {}", schools.size(), districtId);

        return schools.stream()
                .map(this::mapToSchoolResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SchoolResponse createSchool(SchoolRequest request) {
        log.info("Creating new school: {} in district: {}", request.getName(), request.getDistrictId());

        // Validate province exists
        Province province = provinceRepository.findById(request.getProvinceId())
                .orElseThrow(() -> {
                    log.error("Province not found: {}", request.getProvinceId());
                    return new ProvinceNotFoundException("Province not found with ID: " + request.getProvinceId());
                });

        // Validate district exists and belongs to the province
        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> {
                    log.error("District not found: {}", request.getDistrictId());
                    return new DistrictNotFoundException("District not found with ID: " + request.getDistrictId());
                });

        // Build school entity
        School school = School.builder()
                .name(request.getName())
                .nameKhmer(request.getNameKhmer())
                .address(request.getAddress())
                .provinceId(request.getProvinceId())
                .districtId(request.getDistrictId())
                .type(request.getType())
                // Also set deprecated fields for backward compatibility
                .province(province.getName())
                .district(district.getName())
                .build();

        // Save school
        School savedSchool = schoolRepository.save(school);

        log.info("School created successfully with ID: {}", savedSchool.getId());

        return mapToSchoolResponse(savedSchool);
    }

    /**
     * Map School entity to SchoolResponse DTO.
     * Enriches response with province and district names if foreign keys are present.
     */
    private SchoolResponse mapToSchoolResponse(School school) {
        SchoolResponse.SchoolResponseBuilder builder = SchoolResponse.builder()
                .id(school.getId())
                .name(school.getName())
                .nameKhmer(school.getNameKhmer())
                .address(school.getAddress())
                .provinceId(school.getProvinceId())
                .districtId(school.getDistrictId())
                .province(school.getProvince())  // Deprecated field
                .district(school.getDistrict())  // Deprecated field
                .type(school.getType())
                .createdAt(school.getCreatedAt())
                .updatedAt(school.getUpdatedAt());

        // Enrich with province name if province_id exists
        if (school.getProvinceId() != null) {
            provinceRepository.findById(school.getProvinceId())
                    .ifPresent(province -> builder.provinceName(province.getName()));
        }

        // Enrich with district name if district_id exists
        if (school.getDistrictId() != null) {
            districtRepository.findById(school.getDistrictId())
                    .ifPresent(district -> builder.districtName(district.getName()));
        }

        return builder.build();
    }
}
