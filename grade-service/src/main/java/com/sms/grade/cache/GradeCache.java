package com.sms.grade.cache;

import com.sms.grade.dto.StudentGradesSummary;
import com.sms.grade.dto.RankingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis cache manager for grade-related data.
 * Provides caching for student grades and rankings with teacher isolation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GradeCache {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "grade-service:";
    private static final long DEFAULT_TTL_MINUTES = 30;
    private static final long RANKINGS_TTL_MINUTES = 60;

    // =============================================
    // Cache Key Generation
    // =============================================

    /**
     * Generate cache key for student semester grades.
     */
    public String studentGradesKey(UUID teacherId, UUID studentId, Integer semester, String academicYear) {
        return CACHE_PREFIX + "teacher:" + teacherId + ":student:" + studentId +
                ":semester:" + semester + ":year:" + academicYear;
    }

    /**
     * Generate cache key for class grades.
     */
    public String classGradesKey(UUID teacherId, UUID classId, Integer semester, String academicYear) {
        return CACHE_PREFIX + "teacher:" + teacherId + ":class:" + classId +
                ":semester:" + semester + ":year:" + academicYear;
    }

    /**
     * Generate cache key for class rankings.
     */
    public String classRankingsKey(UUID teacherId, UUID classId, Integer semester, String academicYear) {
        return CACHE_PREFIX + "teacher:" + teacherId + ":class:" + classId +
                ":rankings:semester:" + semester + ":year:" + academicYear;
    }

    /**
     * Generate cache key for subject rankings.
     */
    public String subjectRankingsKey(UUID teacherId, UUID classId, UUID subjectId,
                                      Integer semester, String academicYear) {
        return CACHE_PREFIX + "teacher:" + teacherId + ":class:" + classId +
                ":subject:" + subjectId + ":rankings:semester:" + semester + ":year:" + academicYear;
    }

    // =============================================
    // Cache Operations
    // =============================================

    /**
     * Cache student grades summary.
     */
    public void cacheStudentGrades(UUID teacherId, UUID studentId, Integer semester,
                                    String academicYear, StudentGradesSummary summary) {
        String key = studentGradesKey(teacherId, studentId, semester, academicYear);
        redisTemplate.opsForValue().set(key, summary, DEFAULT_TTL_MINUTES, TimeUnit.MINUTES);
        log.debug("Cached student grades: {}", key);
    }

    /**
     * Get cached student grades.
     */
    public StudentGradesSummary getStudentGrades(UUID teacherId, UUID studentId,
                                                  Integer semester, String academicYear) {
        String key = studentGradesKey(teacherId, studentId, semester, academicYear);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof StudentGradesSummary) {
            log.debug("Cache hit for student grades: {}", key);
            return (StudentGradesSummary) cached;
        }
        return null;
    }

    /**
     * Cache class rankings.
     */
    public void cacheClassRankings(UUID teacherId, UUID classId, Integer semester,
                                    String academicYear, RankingResponse rankings) {
        String key = classRankingsKey(teacherId, classId, semester, academicYear);
        redisTemplate.opsForValue().set(key, rankings, RANKINGS_TTL_MINUTES, TimeUnit.MINUTES);
        log.debug("Cached class rankings: {}", key);
    }

    /**
     * Get cached class rankings.
     */
    public RankingResponse getClassRankings(UUID teacherId, UUID classId,
                                             Integer semester, String academicYear) {
        String key = classRankingsKey(teacherId, classId, semester, academicYear);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof RankingResponse) {
            log.debug("Cache hit for class rankings: {}", key);
            return (RankingResponse) cached;
        }
        return null;
    }

    // =============================================
    // Cache Eviction
    // =============================================

    /**
     * Evict cached grades for a student.
     */
    public void evictStudentGrades(UUID teacherId, UUID studentId, Integer semester, String academicYear) {
        String key = studentGradesKey(teacherId, studentId, semester, academicYear);
        redisTemplate.delete(key);
        log.debug("Evicted student grades cache: {}", key);
    }

    /**
     * Evict all cached data for a class.
     */
    public void evictClassCache(UUID teacherId, UUID classId, Integer semester, String academicYear) {
        String pattern = CACHE_PREFIX + "teacher:" + teacherId + ":class:" + classId + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Evicted {} keys matching pattern: {}", keys.size(), pattern);
        }
    }

    /**
     * Evict all cached data for a teacher.
     */
    public void evictTeacherCache(UUID teacherId) {
        String pattern = CACHE_PREFIX + "teacher:" + teacherId + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Evicted {} keys for teacher: {}", keys.size(), teacherId);
        }
    }

    /**
     * Evict all grade service cache.
     */
    public void evictAllCache() {
        String pattern = CACHE_PREFIX + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Evicted all {} grade-service cache keys", keys.size());
        }
    }

    /**
     * Called when a grade is modified - evicts related caches.
     */
    public void onGradeModified(UUID teacherId, UUID studentId, UUID classId,
                                 Integer semester, String academicYear) {
        // Evict student's cached grades
        evictStudentGrades(teacherId, studentId, semester, academicYear);

        // Evict class rankings (need recalculation)
        String rankingsKey = classRankingsKey(teacherId, classId, semester, academicYear);
        redisTemplate.delete(rankingsKey);

        log.debug("Evicted caches after grade modification for student {} in class {}",
                studentId, classId);
    }
}
