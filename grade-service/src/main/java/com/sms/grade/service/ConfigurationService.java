package com.sms.grade.service;

import com.sms.grade.dto.GradeConfigRequest;
import com.sms.grade.dto.TeacherAssessmentConfigResponse;
import com.sms.grade.exception.ConfigNotFoundException;
import com.sms.grade.exception.InvalidConfigException;
import com.sms.grade.exception.SubjectNotFoundException;
import com.sms.grade.exception.UnauthorizedAccessException;
import com.sms.grade.model.Subject;
import com.sms.grade.model.TeacherAssessmentConfig;
import com.sms.grade.repository.SubjectRepository;
import com.sms.grade.repository.TeacherAssessmentConfigRepository;
import com.sms.grade.security.TeacherContextHolder;
import com.sms.grade.service.interfaces.IConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for teacher assessment configuration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigurationService implements IConfigurationService {

    private final TeacherAssessmentConfigRepository configRepository;
    private final SubjectRepository subjectRepository;

    private static final int DEFAULT_MONTHLY_EXAM_COUNT = 4;
    private static final BigDecimal DEFAULT_MONTHLY_WEIGHT = new BigDecimal("50.00");
    private static final BigDecimal DEFAULT_SEMESTER_WEIGHT = new BigDecimal("50.00");

    @Override
    @Transactional
    public TeacherAssessmentConfigResponse saveConfig(GradeConfigRequest request) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Saving assessment config for teacher {} class {} subject {}",
                teacherId, request.getClassId(), request.getSubjectId());

        // Validate weights sum to 100
        BigDecimal totalWeight = request.getMonthlyWeight().add(request.getSemesterExamWeight());
        if (totalWeight.compareTo(new BigDecimal("100.00")) != 0) {
            throw new InvalidConfigException("Monthly and semester weights must sum to 100");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new SubjectNotFoundException(request.getSubjectId()));

        // Find existing or create new
        TeacherAssessmentConfig config = configRepository
                .findByTeacherIdAndClassIdAndSubjectIdAndSemesterAndAcademicYear(
                        teacherId, request.getClassId(), request.getSubjectId(),
                        request.getSemester(), request.getAcademicYear())
                .orElse(new TeacherAssessmentConfig());

        config.setTeacherId(teacherId);
        config.setClassId(request.getClassId());
        config.setSubject(subject);
        config.setSemester(request.getSemester());
        config.setAcademicYear(request.getAcademicYear());
        config.setMonthlyExamCount(request.getMonthlyExamCount());
        config.setMonthlyWeight(request.getMonthlyWeight());
        config.setSemesterExamWeight(request.getSemesterExamWeight());

        config = configRepository.save(config);
        log.info("Saved config {}", config.getId());

        return mapToResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherAssessmentConfigResponse getConfig(UUID classId, UUID subjectId,
                                                      Integer semester, String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        return configRepository.findByTeacherIdAndClassIdAndSubjectIdAndSemesterAndAcademicYear(
                        teacherId, classId, subjectId, semester, academicYear)
                .map(this::mapToResponse)
                .orElseGet(() -> getDefaultConfig(classId, subjectId, semester, academicYear));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherAssessmentConfigResponse> getClassConfigs(UUID classId,
                                                                  Integer semester, String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        return configRepository.findByTeacherIdAndClassIdAndSemesterAndAcademicYear(
                        teacherId, classId, semester, academicYear)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteConfig(UUID configId) {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        TeacherAssessmentConfig config = configRepository.findByIdAndTeacherId(configId, teacherId)
                .orElseThrow(() -> new UnauthorizedAccessException("Config not found or not authorized"));

        configRepository.delete(config);
        log.info("Deleted config {}", configId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean configExists(UUID classId, UUID subjectId, Integer semester, String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        return configRepository.existsByTeacherIdAndClassIdAndSubjectIdAndSemesterAndAcademicYear(
                teacherId, classId, subjectId, semester, academicYear);
    }

    @Override
    public TeacherAssessmentConfigResponse getDefaultConfig(UUID classId, UUID subjectId,
                                                             Integer semester, String academicYear) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new SubjectNotFoundException(subjectId));

        return TeacherAssessmentConfigResponse.builder()
                .id(null)
                .classId(classId)
                .subjectId(subjectId)
                .subjectName(subject.getName())
                .subjectNameKhmer(subject.getNameKhmer())
                .semester(semester)
                .academicYear(academicYear)
                .monthlyExamCount(DEFAULT_MONTHLY_EXAM_COUNT)
                .monthlyWeight(DEFAULT_MONTHLY_WEIGHT)
                .semesterExamWeight(DEFAULT_SEMESTER_WEIGHT)
                .createdAt(null)
                .updatedAt(null)
                .build();
    }

    private TeacherAssessmentConfigResponse mapToResponse(TeacherAssessmentConfig config) {
        return TeacherAssessmentConfigResponse.builder()
                .id(config.getId())
                .classId(config.getClassId())
                .subjectId(config.getSubject().getId())
                .subjectName(config.getSubject().getName())
                .subjectNameKhmer(config.getSubject().getNameKhmer())
                .semester(config.getSemester())
                .academicYear(config.getAcademicYear())
                .monthlyExamCount(config.getMonthlyExamCount())
                .monthlyWeight(config.getMonthlyWeight())
                .semesterExamWeight(config.getSemesterExamWeight())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
