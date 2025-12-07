package com.sms.student.service.interfaces;

import com.sms.student.dto.CacheReloadResponse;

import java.util.UUID;

/**
 * Service interface for cache management operations.
 * Handles teacher-scoped cache eviction for student data.
 */
public interface ICacheService {

    /**
     * Clear all cached student data for a specific teacher.
     * Evicts all cache entries with keys matching the teacher's ID prefix.
     *
     * @param teacherId UUID of the teacher whose cache should be cleared
     * @return Response containing cache reload confirmation and statistics
     */
    CacheReloadResponse clearTeacherCache(UUID teacherId);

    /**
     * Clear all cached student data for the currently authenticated teacher.
     * Uses TeacherContextHolder to get the teacher ID from the current request context.
     *
     * @return Response containing cache reload confirmation and statistics
     */
    CacheReloadResponse clearCurrentTeacherCache();
}
