package com.sms.grade.service;

import com.sms.grade.dto.ExamScheduleItemDto;
import com.sms.grade.dto.SemesterConfigRequest;
import com.sms.grade.dto.SemesterConfigResponse;
import com.sms.grade.exception.SemesterConfigNotFoundException;
import com.sms.grade.model.SemesterConfig;
import com.sms.grade.repository.SemesterConfigRepository;
import com.sms.grade.security.TeacherContextHolder;
import com.sms.grade.service.interfaces.ISemesterConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for semester configuration management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemesterConfigService implements ISemesterConfigService {

    private final SemesterConfigRepository configRepository;

    // =============================================
    // Teacher Configuration (with fallback to default)
    // =============================================

    @Override
    @Transactional(readOnly = true)
    public SemesterConfigResponse getConfig(String academicYear, String semesterExamCode) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.debug("Getting config for teacher {} academic year {} semester exam code {}",
                teacherId, academicYear, semesterExamCode);

        // Try teacher-specific config first
        return configRepository.findByTeacherIdAndAcademicYearAndSemesterExamCode(
                        teacherId, academicYear, semesterExamCode)
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    // Fall back to default config
                    return configRepository.findDefaultConfig(academicYear, semesterExamCode)
                            .map(this::mapToResponse)
                            .orElseThrow(() -> new SemesterConfigNotFoundException(
                                    academicYear, semesterExamCode));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<SemesterConfigResponse> getConfigsByAcademicYear(String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        List<SemesterConfigResponse> result = new ArrayList<>();

        // Get all semester exam codes for this academic year
        List<String> semesterExamCodes = configRepository.findSemesterExamCodesByAcademicYear(academicYear);

        for (String semesterExamCode : semesterExamCodes) {
            // Try teacher config, fall back to default
            SemesterConfigResponse config = configRepository
                    .findByTeacherIdAndAcademicYearAndSemesterExamCode(teacherId, academicYear, semesterExamCode)
                    .map(this::mapToResponse)
                    .orElseGet(() -> configRepository.findDefaultConfig(academicYear, semesterExamCode)
                            .map(this::mapToResponse)
                            .orElse(null));

            if (config != null) {
                result.add(config);
            }
        }

        return result;
    }

    @Override
    @Transactional
    public SemesterConfigResponse saveTeacherConfig(SemesterConfigRequest request) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Saving teacher config for {} academic year {} semester exam code {}",
                teacherId, request.getAcademicYear(), request.getSemesterExamCode());

        // Find existing or create new
        SemesterConfig config = configRepository
                .findByTeacherIdAndAcademicYearAndSemesterExamCode(
                        teacherId, request.getAcademicYear(), request.getSemesterExamCode())
                .orElse(new SemesterConfig());

        config.setTeacherId(teacherId);
        updateConfigFromRequest(config, request);

        config = configRepository.save(config);
        log.info("Saved teacher config {}", config.getId());

        return mapToResponse(config);
    }

    @Override
    @Transactional
    public void deleteTeacherConfig(String academicYear, String semesterExamCode) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Deleting teacher config for {} academic year {} semester exam code {}",
                teacherId, academicYear, semesterExamCode);

        configRepository.deleteByTeacherIdAndAcademicYearAndSemesterExamCode(
                teacherId, academicYear, semesterExamCode);
    }

    // =============================================
    // Admin Operations (Default Configurations)
    // =============================================

    @Override
    @Transactional(readOnly = true)
    public List<SemesterConfigResponse> getAllDefaultConfigs() {
        return configRepository.findAllDefaultConfigs().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SemesterConfigResponse> getDefaultConfigsByAcademicYear(String academicYear) {
        return configRepository.findDefaultConfigsByAcademicYear(academicYear).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SemesterConfigResponse saveDefaultConfig(SemesterConfigRequest request) {
        log.info("Saving default config for academic year {} semester exam code {}",
                request.getAcademicYear(), request.getSemesterExamCode());

        // Find existing or create new (teacher_id is null for default)
        SemesterConfig config = configRepository
                .findDefaultConfig(request.getAcademicYear(), request.getSemesterExamCode())
                .orElse(new SemesterConfig());

        config.setTeacherId(null); // Ensure it's a default config
        updateConfigFromRequest(config, request);

        config = configRepository.save(config);
        log.info("Saved default config {}", config.getId());

        return mapToResponse(config);
    }

    @Override
    @Transactional
    public void deleteDefaultConfig(String academicYear, String semesterExamCode) {
        log.info("Deleting default config for academic year {} semester exam code {}",
                academicYear, semesterExamCode);

        SemesterConfig config = configRepository.findDefaultConfig(academicYear, semesterExamCode)
                .orElseThrow(() -> new SemesterConfigNotFoundException(academicYear, semesterExamCode));

        configRepository.delete(config);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableAcademicYears() {
        return configRepository.findDistinctAcademicYears();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean defaultConfigExists(String academicYear, String semesterExamCode) {
        return configRepository.existsDefaultConfig(academicYear, semesterExamCode);
    }

    // =============================================
    // Helper Methods
    // =============================================

    private void updateConfigFromRequest(SemesterConfig config, SemesterConfigRequest request) {
        config.setAcademicYear(request.getAcademicYear());
        config.setSemesterExamCode(request.getSemesterExamCode());

        List<SemesterConfig.ExamScheduleItem> scheduleItems = request.getExamSchedule().stream()
                .map(dto -> SemesterConfig.ExamScheduleItem.builder()
                        .assessmentCode(dto.getAssessmentCode())
                        .title(dto.getTitle())
                        .displayOrder(dto.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        config.setExamSchedule(scheduleItems);
    }

    private SemesterConfigResponse mapToResponse(SemesterConfig config) {
        List<ExamScheduleItemDto> scheduleItems = config.getExamSchedule().stream()
                .map(item -> ExamScheduleItemDto.builder()
                        .assessmentCode(item.getAssessmentCode())
                        .title(item.getTitle())
                        .displayOrder(item.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        return SemesterConfigResponse.builder()
                .id(config.getId())
                .teacherId(config.getTeacherId())
                .academicYear(config.getAcademicYear())
                .semesterExamCode(config.getSemesterExamCode())
                .examSchedule(scheduleItems)
                .monthlyExamCount(config.getMonthlyExamCount())
                .isDefault(config.isDefaultConfig())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
