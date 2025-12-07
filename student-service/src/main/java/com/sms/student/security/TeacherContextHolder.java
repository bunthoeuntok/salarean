package com.sms.student.security;

import java.util.UUID;

/**
 * Thread-local storage for the authenticated teacher's ID.
 *
 * <p>This class provides a secure way to access the current teacher's ID
 * throughout the request lifecycle without passing it as a method parameter.</p>
 *
 * <p><b>Usage Pattern:</b></p>
 * <ol>
 *   <li>JwtAuthenticationFilter sets the teacher ID after successful JWT validation</li>
 *   <li>Service layer retrieves teacher ID using {@link #getTeacherId()}</li>
 *   <li>JwtAuthenticationFilter clears the context in the finally block</li>
 * </ol>
 *
 * <p><b>Thread Safety:</b> Uses ThreadLocal to ensure isolation between concurrent requests.</p>
 *
 * @see com.sms.student.security.JwtAuthenticationFilter
 */
public class TeacherContextHolder {

    private static final ThreadLocal<UUID> teacherContext = new ThreadLocal<>();

    /**
     * Sets the authenticated teacher's ID for the current thread.
     *
     * @param teacherId the UUID of the authenticated teacher
     * @throws IllegalArgumentException if teacherId is null
     */
    public static void setTeacherId(UUID teacherId) {
        if (teacherId == null) {
            throw new IllegalArgumentException("Teacher ID cannot be null");
        }
        teacherContext.set(teacherId);
    }

    /**
     * Retrieves the authenticated teacher's ID for the current thread.
     *
     * @return the UUID of the authenticated teacher
     * @throws IllegalStateException if no teacher context is set (unauthenticated request)
     */
    public static UUID getTeacherId() {
        UUID teacherId = teacherContext.get();
        if (teacherId == null) {
            throw new IllegalStateException("Teacher context not set. Ensure JWT authentication succeeded.");
        }
        return teacherId;
    }

    /**
     * Checks if a teacher context is currently set for this thread.
     *
     * @return true if teacher ID is set, false otherwise
     */
    public static boolean hasTeacherId() {
        return teacherContext.get() != null;
    }

    /**
     * Clears the teacher context for the current thread.
     *
     * <p>This method MUST be called in a finally block to prevent context leakage
     * between requests when using thread pools.</p>
     */
    public static void clear() {
        teacherContext.remove();
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private TeacherContextHolder() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
