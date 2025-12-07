package com.sms.student.cache;

import com.sms.common.cache.CacheKeyGenerator;
import com.sms.common.cache.CacheService;
import com.sms.student.dto.StudentListResponse;
import com.sms.student.dto.StudentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Cache service for student management operations.
 *
 * <p>Handles Redis caching for student data to reduce database load.
 * Uses standardized cache keys from {@link CacheKeyGenerator}.</p>
 *
 * <p><strong>Cache Strategy</strong>:
 * <ul>
 *   <li>Student details: 30-minute TTL</li>
 *   <li>Student lists: 30-minute TTL</li>
 *   <li>Cache key format: student-service:teacher:students:{teacherId}:...</li>
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
public class StudentCache {

    private static final String SERVICE_NAME = "student-service";
    private static final String TEACHER_ENTITY = "teacher";
    private static final String STUDENT_ENTITY = "student";
    private static final String STUDENTS_SUFFIX = "students";
    private static final String FILTERED_SUFFIX = "filtered";
    private static final Duration STUDENT_DETAILS_TTL = Duration.ofMinutes(30);
    private static final Duration STUDENT_LIST_TTL = Duration.ofMinutes(30);

    private final CacheService cacheService;

    /**
     * Get cached student details by ID.
     *
     * @param teacherId UUID of the teacher
     * @param studentId UUID of the student
     * @return optional containing cached student details, or empty if not cached
     */
    public Optional<StudentResponse> getStudentById(UUID teacherId, UUID studentId) {
        String cacheKey = buildStudentKey(teacherId, studentId);
        log.debug("Fetching cached student details for key: {}", cacheKey);
        return cacheService.get(cacheKey, StudentResponse.class);
    }

    /**
     * Cache student details.
     *
     * @param teacherId UUID of the teacher
     * @param studentId UUID of the student
     * @param student   student response to cache
     */
    public void cacheStudent(UUID teacherId, UUID studentId, StudentResponse student) {
        String cacheKey = buildStudentKey(teacherId, studentId);
        log.debug("Caching student details for key: {} (TTL: {})", cacheKey, STUDENT_DETAILS_TTL);
        cacheService.put(cacheKey, student, STUDENT_DETAILS_TTL);
    }

    /**
     * Get cached student details by student code.
     *
     * @param teacherId   UUID of the teacher
     * @param studentCode student code (e.g., STU-2025-0001)
     * @return optional containing cached student details, or empty if not cached
     */
    public Optional<StudentResponse> getStudentByCode(UUID teacherId, String studentCode) {
        String cacheKey = buildStudentCodeKey(teacherId, studentCode);
        log.debug("Fetching cached student by code for key: {}", cacheKey);
        return cacheService.get(cacheKey, StudentResponse.class);
    }

    /**
     * Cache student details by student code.
     *
     * @param teacherId   UUID of the teacher
     * @param studentCode student code
     * @param student     student response to cache
     */
    public void cacheStudentByCode(UUID teacherId, String studentCode, StudentResponse student) {
        String cacheKey = buildStudentCodeKey(teacherId, studentCode);
        log.debug("Caching student by code for key: {} (TTL: {})", cacheKey, STUDENT_DETAILS_TTL);
        cacheService.put(cacheKey, student, STUDENT_DETAILS_TTL);
    }

    /**
     * Get cached active students list.
     *
     * @param teacherId  UUID of the teacher
     * @param pageNumber page number
     * @param pageSize   page size
     * @return optional containing cached student list, or empty if not cached
     */
    public Optional<StudentListResponse> getActiveStudents(UUID teacherId, int pageNumber, int pageSize) {
        String cacheKey = buildActiveStudentsKey(teacherId, pageNumber, pageSize);
        log.debug("Fetching cached active students for key: {}", cacheKey);
        return cacheService.get(cacheKey, StudentListResponse.class);
    }

    /**
     * Cache active students list.
     *
     * @param teacherId  UUID of the teacher
     * @param pageNumber page number
     * @param pageSize   page size
     * @param students   student list response to cache
     */
    public void cacheActiveStudents(UUID teacherId, int pageNumber, int pageSize, StudentListResponse students) {
        String cacheKey = buildActiveStudentsKey(teacherId, pageNumber, pageSize);
        log.debug("Caching active students for key: {} (TTL: {})", cacheKey, STUDENT_LIST_TTL);
        cacheService.put(cacheKey, students, STUDENT_LIST_TTL);
    }

    /**
     * Get cached filtered students list.
     *
     * @param teacherId  UUID of the teacher
     * @param search     search term (nullable)
     * @param status     status filter (nullable)
     * @param gender     gender filter (nullable)
     * @param level      level filter (nullable)
     * @param grade      grade filter (nullable)
     * @param classId    class ID filter (nullable)
     * @param pageNumber page number
     * @param pageSize   page size
     * @return optional containing cached student list, or empty if not cached
     */
    public Optional<StudentListResponse> getFilteredStudents(
            UUID teacherId,
            String search,
            String status,
            String gender,
            String level,
            Integer grade,
            String classId,
            int pageNumber,
            int pageSize) {

        String cacheKey = buildFilteredKey(teacherId, search, status, gender, level, grade, classId, pageNumber, pageSize);

        log.debug("Fetching cached filtered students for key: {}", cacheKey);
        return cacheService.get(cacheKey, StudentListResponse.class);
    }

    /**
     * Cache filtered students list.
     *
     * @param teacherId  UUID of the teacher
     * @param search     search term (nullable)
     * @param status     status filter (nullable)
     * @param gender     gender filter (nullable)
     * @param level      level filter (nullable)
     * @param grade      grade filter (nullable)
     * @param classId    class ID filter (nullable)
     * @param pageNumber page number
     * @param pageSize   page size
     * @param students   student list response to cache
     */
    public void cacheFilteredStudents(
            UUID teacherId,
            String search,
            String status,
            String gender,
            String level,
            Integer grade,
            String classId,
            int pageNumber,
            int pageSize,
            StudentListResponse students) {

        String cacheKey = buildFilteredKey(teacherId, search, status, gender, level, grade, classId, pageNumber, pageSize);

        log.debug("Caching filtered students for key: {} (TTL: {})", cacheKey, STUDENT_LIST_TTL);
        cacheService.put(cacheKey, students, STUDENT_LIST_TTL);
    }

    /**
     * Evict all cached student data for a teacher.
     * Called when student data changes (create, update, delete).
     *
     * @param teacherId UUID of the teacher
     */
    public void evictTeacherStudents(UUID teacherId) {
        String pattern = CacheKeyGenerator.generatePattern(
            SERVICE_NAME,
            TEACHER_ENTITY,
            teacherId.toString(),
            "*"
        );

        log.debug("Evicting all cached students for teacher with pattern: {}", pattern);
        cacheService.evictPattern(pattern);
    }

    /**
     * Evict cached student details.
     *
     * @param teacherId UUID of the teacher
     * @param studentId UUID of the student
     */
    public void evictStudent(UUID teacherId, UUID studentId) {
        String cacheKey = buildStudentKey(teacherId, studentId);
        log.debug("Evicting cached student for key: {}", cacheKey);
        cacheService.evict(cacheKey);
    }

    // ========== Private Helper Methods ==========

    /**
     * Build cache key for student details by ID.
     */
    private String buildStudentKey(UUID teacherId, UUID studentId) {
        return String.join(":",
            SERVICE_NAME,
            TEACHER_ENTITY,
            teacherId.toString(),
            STUDENT_ENTITY,
            studentId.toString()
        );
    }

    /**
     * Build cache key for student details by code.
     */
    private String buildStudentCodeKey(UUID teacherId, String studentCode) {
        return String.join(":",
            SERVICE_NAME,
            TEACHER_ENTITY,
            teacherId.toString(),
            STUDENT_ENTITY,
            "code",
            studentCode
        );
    }

    /**
     * Build cache key for active students list.
     */
    private String buildActiveStudentsKey(UUID teacherId, int pageNumber, int pageSize) {
        return String.join(":",
            SERVICE_NAME,
            TEACHER_ENTITY,
            teacherId.toString(),
            STUDENTS_SUFFIX,
            "all",
            "page", String.valueOf(pageNumber),
            "size", String.valueOf(pageSize)
        );
    }

    /**
     * Build cache key for filtered student list.
     */
    private String buildFilteredKey(
            UUID teacherId,
            String search,
            String status,
            String gender,
            String level,
            Integer grade,
            String classId,
            int pageNumber,
            int pageSize) {

        return String.join(":",
            SERVICE_NAME,
            TEACHER_ENTITY,
            teacherId.toString(),
            STUDENTS_SUFFIX,
            FILTERED_SUFFIX,
            "search", nullSafe(search),
            "status", nullSafe(status),
            "gender", nullSafe(gender),
            "level", nullSafe(level),
            "grade", nullSafe(grade),
            "classId", nullSafe(classId),
            "page", String.valueOf(pageNumber),
            "size", String.valueOf(pageSize)
        );
    }

    /**
     * Convert null values to "null" string for consistent cache keys.
     */
    private String nullSafe(Object value) {
        return value == null ? "null" : value.toString();
    }
}
