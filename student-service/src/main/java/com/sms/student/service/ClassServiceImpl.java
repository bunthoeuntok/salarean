package com.sms.student.service;

import com.sms.student.dto.ClassSummaryDto;
import com.sms.student.enums.ClassStatus;
import com.sms.student.model.SchoolClass;
import com.sms.student.repository.ClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ClassService}.
 *
 * <p>Handles business logic for class management with Redis caching
 * to optimize performance for frequently accessed data.</p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassServiceImpl implements ClassService {

    private final ClassRepository classRepository;
    private final ClassCacheService classCacheService;

    @Override
    @Transactional(readOnly = true)
    public List<ClassSummaryDto> listTeacherClasses(UUID teacherId, boolean includeArchived) {
        log.info("Fetching classes for teacher: {} (includeArchived: {})", teacherId, includeArchived);

        // Try to get from cache first
        Optional<List<ClassSummaryDto>> cachedClasses = classCacheService.getTeacherClasses(teacherId);
        if (cachedClasses.isPresent()) {
            log.debug("Cache HIT for teacher classes: {}", teacherId);
            List<ClassSummaryDto> classes = cachedClasses.get();

            // Filter archived classes if needed
            if (!includeArchived) {
                classes = classes.stream()
                    .filter(c -> c.getStatus() == ClassStatus.ACTIVE)
                    .collect(Collectors.toList());
            }

            return classes;
        }

        log.debug("Cache MISS for teacher classes: {}", teacherId);

        // Fetch from database
        List<SchoolClass> classEntities;
        if (includeArchived) {
            classEntities = classRepository.findByTeacherId(teacherId);
        } else {
            classEntities = classRepository.findByTeacherIdAndStatus(teacherId, ClassStatus.ACTIVE);
        }

        log.debug("Found {} classes for teacher: {}", classEntities.size(), teacherId);

        // Map to DTOs
        List<ClassSummaryDto> classSummaries = classEntities.stream()
            .map(this::mapToSummaryDto)
            .collect(Collectors.toList());

        // Cache the result (always cache all classes, filter happens on retrieval)
        if (!includeArchived) {
            // If we only fetched active classes, we should fetch all classes for caching
            List<SchoolClass> allClasses = classRepository.findByTeacherId(teacherId);
            List<ClassSummaryDto> allSummaries = allClasses.stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
            classCacheService.cacheTeacherClasses(teacherId, allSummaries);
        } else {
            classCacheService.cacheTeacherClasses(teacherId, classSummaries);
        }

        return classSummaries;
    }

    /**
     * Map SchoolClass to ClassSummaryDto.
     *
     * @param entity class entity
     * @return class summary DTO
     */
    private ClassSummaryDto mapToSummaryDto(SchoolClass entity) {
        return ClassSummaryDto.builder()
            .id(entity.getId())
            .schoolId(entity.getSchoolId())
            .teacherId(entity.getTeacherId())
            .grade(entity.getGrade())
            .section(entity.getSection())
            .academicYear(entity.getAcademicYear())
            .maxCapacity(entity.getMaxCapacity())
            .studentCount(entity.getStudentCount())
            .status(entity.getStatus())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
