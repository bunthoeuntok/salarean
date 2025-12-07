package com.sms.student.service;

import com.sms.student.dto.CacheReloadResponse;
import com.sms.student.security.TeacherContextHolder;
import com.sms.student.service.interfaces.ICacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of cache management service.
 * Handles teacher-scoped cache eviction for student data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService implements ICacheService {

    private final CacheManager cacheManager;

    @Override
    public CacheReloadResponse clearTeacherCache(UUID teacherId) {
        log.info("Clearing cache for teacher: {}", teacherId);

        // Clear the "students" cache
        var cache = cacheManager.getCache("students");
        if (cache != null) {
            cache.clear();
            log.info("Successfully cleared students cache for teacher: {}", teacherId);
        } else {
            log.warn("Students cache not found for teacher: {}", teacherId);
        }

        return CacheReloadResponse.builder()
                .message("Cache successfully cleared for teacher")
                .teacherId(teacherId.toString())
                .reloadedAt(LocalDateTime.now())
                .entriesCleared(null) // Redis doesn't provide exact count easily
                .build();
    }

    @Override
    public CacheReloadResponse clearCurrentTeacherCache() {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Clearing cache for current authenticated teacher: {}", teacherId);
        return clearTeacherCache(teacherId);
    }
}
