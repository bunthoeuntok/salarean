package com.sms.common.cache;

/**
 * Utility class for generating standardized cache keys across all microservices.
 *
 * <p>Cache keys follow a hierarchical colon-separated format to enable pattern-based
 * eviction and prevent key collisions between services:</p>
 *
 * <pre>
 * Format: {service-name}:{entity-type}:{identifier}:{optional-suffix}
 * </pre>
 *
 * <p><strong>Examples</strong>:
 * <ul>
 *   <li>student-service:teacher:classes:{teacherId}</li>
 *   <li>student-service:class:{classId}</li>
 *   <li>student-service:class:{classId}:students</li>
 *   <li>student-service:class:{classId}:history</li>
 *   <li>auth-service:user:{userId}</li>
 *   <li>auth-service:refresh-token:{tokenId}</li>
 * </ul>
 * </p>
 *
 * <p><strong>Benefits</strong>:
 * <ul>
 *   <li>Consistent format across all services</li>
 *   <li>Service prefix prevents key collisions</li>
 *   <li>Hierarchical structure enables pattern matching for bulk eviction</li>
 *   <li>Self-documenting keys aid debugging</li>
 * </ul>
 * </p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
public final class CacheKeyGenerator {

    private static final String SEPARATOR = ":";

    private CacheKeyGenerator() {
        // Utility class - prevent instantiation
    }

    /**
     * Generates a cache key with service, entity type, and identifier.
     *
     * @param serviceName the name of the microservice (e.g., "student-service", "auth-service")
     * @param entityType the entity type (e.g., "class", "teacher", "user")
     * @param identifier the unique identifier (e.g., UUID, username)
     * @return the generated cache key
     */
    public static String generateKey(String serviceName, String entityType, String identifier) {
        return String.join(SEPARATOR, serviceName, entityType, identifier);
    }

    /**
     * Generates a cache key with service, entity type, identifier, and optional suffix.
     *
     * @param serviceName the name of the microservice (e.g., "student-service", "auth-service")
     * @param entityType the entity type (e.g., "class", "teacher", "user")
     * @param identifier the unique identifier (e.g., UUID, username)
     * @param suffix optional suffix for related data (e.g., "students", "history", "permissions")
     * @return the generated cache key
     */
    public static String generateKey(String serviceName, String entityType, String identifier, String suffix) {
        return String.join(SEPARATOR, serviceName, entityType, identifier, suffix);
    }

    /**
     * Generates a pattern for bulk cache eviction matching multiple keys.
     *
     * <p>Use the wildcard "*" to match multiple keys. For example:
     * <ul>
     *   <li>"student-service:teacher:classes:*" matches all teacher class lists</li>
     *   <li>"student-service:class:*" matches all class-related cache entries</li>
     *   <li>"auth-service:*" matches all auth-service cache entries</li>
     * </ul>
     * </p>
     *
     * @param serviceName the name of the microservice
     * @param entityType the entity type (or "*" for wildcard)
     * @param identifier the identifier (or "*" for wildcard)
     * @return the cache key pattern with wildcards
     */
    public static String generatePattern(String serviceName, String entityType, String identifier) {
        return String.join(SEPARATOR, serviceName, entityType, identifier);
    }

    /**
     * Generates a pattern for bulk cache eviction with optional suffix.
     *
     * @param serviceName the name of the microservice
     * @param entityType the entity type (or "*" for wildcard)
     * @param identifier the identifier (or "*" for wildcard)
     * @param suffix optional suffix (or "*" for wildcard)
     * @return the cache key pattern with wildcards
     */
    public static String generatePattern(String serviceName, String entityType, String identifier, String suffix) {
        return String.join(SEPARATOR, serviceName, entityType, identifier, suffix);
    }
}
