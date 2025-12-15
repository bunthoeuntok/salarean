package com.sms.student.service;

import com.sms.student.dto.TeacherSchoolRequest;
import com.sms.student.dto.TeacherSchoolResponse;
import com.sms.student.exception.SchoolNotFoundException;
import com.sms.student.model.District;
import com.sms.student.model.Province;
import com.sms.student.model.School;
import com.sms.student.model.TeacherSchool;
import com.sms.student.repository.DistrictRepository;
import com.sms.student.repository.ProvinceRepository;
import com.sms.student.repository.SchoolRepository;
import com.sms.student.repository.TeacherSchoolRepository;
import com.sms.student.service.interfaces.ITeacherSchoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherSchoolService implements ITeacherSchoolService {

    private final TeacherSchoolRepository teacherSchoolRepository;
    private final SchoolRepository schoolRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;

    @Override
    @Transactional
    public TeacherSchoolResponse createOrUpdate(UUID userId, TeacherSchoolRequest request) {
        log.info("Creating or updating teacher-school association for user ID: {}", userId);

        // Validate school exists
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> {
                    log.error("School not found with ID: {}", request.getSchoolId());
                    return new SchoolNotFoundException("School not found with ID: " + request.getSchoolId());
                });

        // Find existing association or create new one
        TeacherSchool teacherSchool = teacherSchoolRepository.findByUserId(userId)
                .orElse(TeacherSchool.builder()
                        .userId(userId)
                        .build());

        // Update fields
        teacherSchool.setSchool(school);
        teacherSchool.setPrincipalName(request.getPrincipalName());
        teacherSchool.setPrincipalGender(request.getPrincipalGender());

        // Save
        teacherSchool = teacherSchoolRepository.save(teacherSchool);

        log.info("Teacher-school association saved with ID: {}", teacherSchool.getId());

        return mapToResponse(teacherSchool);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherSchoolResponse getByUserId(UUID userId) {
        log.debug("Fetching teacher-school association for user ID: {}", userId);

        return teacherSchoolRepository.findByUserId(userId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    private TeacherSchoolResponse mapToResponse(TeacherSchool teacherSchool) {
        School school = teacherSchool.getSchool();

        // Look up province name
        String provinceName = null;
        if (school.getProvinceId() != null) {
            provinceName = provinceRepository.findById(school.getProvinceId())
                    .map(Province::getName)
                    .orElse(null);
        }

        // Look up district name
        String districtName = null;
        if (school.getDistrictId() != null) {
            districtName = districtRepository.findById(school.getDistrictId())
                    .map(District::getName)
                    .orElse(null);
        }

        return TeacherSchoolResponse.builder()
                .id(teacherSchool.getId())
                .userId(teacherSchool.getUserId())
                .schoolId(school.getId())
                .schoolName(school.getName())
                .schoolType(school.getType())
                .provinceId(school.getProvinceId())
                .provinceName(provinceName)
                .districtId(school.getDistrictId())
                .districtName(districtName)
                .principalName(teacherSchool.getPrincipalName())
                .principalGender(teacherSchool.getPrincipalGender())
                .createdAt(teacherSchool.getCreatedAt())
                .updatedAt(teacherSchool.getUpdatedAt())
                .build();
    }
}
