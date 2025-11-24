package com.sms.student.service;

import com.sms.common.cache.CacheKeyGenerator;
import com.sms.common.cache.CacheService;
import com.sms.student.dto.ClassSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cache service for class management operations.
 *
 * <p>Handles Redis caching for class data to reduce database load.
 * Uses standardized cache keys from {@link CacheKeyGenerator}.</p>
 *
 * <p><strong>Cache Strategy</strong>:
 * <ul>
 *   <li>Teacher's classes list: 30-minute TTL</li>
 *   <li>Cache key format: student-service:teacher:classes:{teacherId}</li>
 *   <li>Graceful degradation: cache failures don't break application</li>
 * </ul>
 * </p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassCacheService {

    private static final String SERVICE_NAME = "student-service";
    private static final String TEACHER_ENTITY = "teacher";
    private static final String CLASSES_SUFFIX = "classes";
    private static final Duration TEACHER_CLASSES_TTL = Duration.ofMinutes(30);

    private final CacheService cacheService;

    /**
     * Get cached list of teacher's classes.
     *
     * @param teacherId UUID of the teacher
     * @return optional containing cached classes list, or empty if not cached
     */
    @SuppressWarnings("unchecked")
    public Optional<List<ClassSummaryDto>> getTeacherClasses(UUID teacherId) {
        String cacheKey = CacheKeyGenerator.generateKey(
            SERVICE_NAME,
            TEACHER_ENTITY,
            teacherId.toString(),
            CLASSES_SUFFIX
        );

        log.debug("Fetching cached teacher classes for key: {}", cacheKey);
        return (Optional<List<ClassSummaryDto>>) (Optional<?>) cacheService.get(cacheKey, List.class);
    }

    /**
     * Cache the list of teacher's classes.
     *
     * @param teacherId UUID of the teacher
     * @param classes   list of class summaries to cache
     */
    public void cacheTeacherClasses(UUID teacherId, List<ClassSummaryDto> classes) {
        String cacheKey = CacheKeyGenerator.generateKey(
            SERVICE_NAME,
            TEACHER_ENTITY,
            teacherId.toString(),
            CLASSES_SUFFIX
        );

        log.debug("Caching teacher classes for key: {} (TTL: {})", cacheKey, TEACHER_CLASSES_TTL);
        cacheService.put(cacheKey, classes, TEACHER_CLASSES_TTL);
    }

    /**
     * Evict cached teacher's classes list.
     * Called when class data changes (create, update, archive).
     *
     * @param teacherId UUID of the teacher
     */
    public void evictTeacherClasses(UUID teacherId) {
        String cacheKey = CacheKeyGenerator.generateKey(
            SERVICE_NAME,
            TEACHER_ENTITY,
            teacherId.toString(),
            CLASSES_SUFFIX
        );

        log.debug("Evicting cached teacher classes for key: {}", cacheKey);
        cacheService.evict(cacheKey);
    }
}
