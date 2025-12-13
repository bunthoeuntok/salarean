package com.sms.auth.service;

import com.sms.auth.dto.TeacherSchoolRequest;
import com.sms.auth.dto.TeacherSchoolResponse;
import com.sms.auth.exception.SchoolNotFoundException;
import com.sms.auth.exception.UserNotFoundException;
import com.sms.auth.model.TeacherSchool;
import com.sms.auth.repository.TeacherSchoolRepository;
import com.sms.auth.repository.UserRepository;
import com.sms.auth.service.interfaces.ITeacherSchoolService;
import com.sms.common.dto.ApiResponse;
import com.sms.common.dto.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherSchoolService implements ITeacherSchoolService {

    private final TeacherSchoolRepository teacherSchoolRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${student-service.url:http://student-service:8083}")
    private String studentServiceUrl;

    @Override
    @Transactional
    public TeacherSchoolResponse createOrUpdate(UUID userId, TeacherSchoolRequest request) {
        log.info("Creating or updating teacher-school association for user ID: {}", userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw new UserNotFoundException(ErrorCode.RESOURCE_NOT_FOUND, "User not found with ID: " + userId);
        }

        // Find existing association or create new one
        TeacherSchool teacherSchool = teacherSchoolRepository.findByUserId(userId)
                .orElse(TeacherSchool.builder()
                        .userId(userId)
                        .build());

        // Update fields
        teacherSchool.setSchoolId(request.getSchoolId());
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
                .map(teacherSchool -> {
                    return mapToResponse(teacherSchool);
                })
                .orElse(null);
    }

    private TeacherSchoolResponse mapToResponse(TeacherSchool teacherSchool) {
        return TeacherSchoolResponse.builder()
                .id(teacherSchool.getId())
                .userId(teacherSchool.getUserId())
                .schoolId(teacherSchool.getSchoolId())
                .principalName(teacherSchool.getPrincipalName())
                .principalGender(teacherSchool.getPrincipalGender())
                .createdAt(teacherSchool.getCreatedAt())
                .updatedAt(teacherSchool.getUpdatedAt())
                .build();
    }
}
