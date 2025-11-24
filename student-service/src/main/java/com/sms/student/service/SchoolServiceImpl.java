package com.sms.student.service;

import com.sms.student.dto.SchoolResponse;
import com.sms.student.exception.SchoolNotFoundException;
import com.sms.student.model.School;
import com.sms.student.repository.SchoolRepository;
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
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;

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

    /**
     * Map School entity to SchoolResponse DTO.
     */
    private SchoolResponse mapToSchoolResponse(School school) {
        return SchoolResponse.builder()
                .id(school.getId())
                .name(school.getName())
                .nameKhmer(school.getNameKhmer())
                .address(school.getAddress())
                .province(school.getProvince())
                .district(school.getDistrict())
                .type(school.getType())
                .createdAt(school.getCreatedAt())
                .updatedAt(school.getUpdatedAt())
                .build();
    }
}
