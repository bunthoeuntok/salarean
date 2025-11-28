package com.sms.common.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Duration;
import java.util.Optional;

/**
 * Abstract cache service interface for standardized caching across all microservices.
 *
 * <p>This interface provides a common abstraction for cache operations, enabling different
 * implementations (Redis, Memcached, Hazelcast, etc.) to be used interchangeably.</p>
 *
 * <p><strong>Graceful Degradation</strong>: All implementations MUST handle failures gracefully
 * by returning Optional.empty() on errors rather than throwing exceptions. This ensures that
 * cache failures never break application functionality.</p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
public interface CacheService {

    /**
     * Retrieves a cached value by key.
     *
     * @param key the cache key (should follow format: service:entity:id:suffix)
     * @param type the class type to deserialize the cached value
     * @param <T> the type of the cached value
     * @return Optional containing the cached value if found and deserialization succeeds, empty otherwise
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * Retrieves a cached value by key with generic type support.
     *
     * <p>Use this method when caching generic types like List&lt;MyDto&gt;:</p>
     * <pre>
     * cacheService.get(key, new TypeReference&lt;List&lt;MyDto&gt;&gt;() {});
     * </pre>
     *
     * @param key the cache key (should follow format: service:entity:id:suffix)
     * @param typeRef the type reference for generic types
     * @param <T> the type of the cached value
     * @return Optional containing the cached value if found and deserialization succeeds, empty otherwise
     */
    <T> Optional<T> get(String key, TypeReference<T> typeRef);

    /**
     * Stores a value in the cache with a time-to-live (TTL).
     *
     * @param key the cache key (should follow format: service:entity:id:suffix)
     * @param value the value to cache
     * @param ttl the time-to-live duration for the cached entry
     * @param <T> the type of the value to cache
     */
    <T> void put(String key, T value, Duration ttl);

    /**
     * Removes a specific cache entry by key.
     *
     * @param key the cache key to evict
     */
    void evict(String key);

    /**
     * Removes all cache entries matching a pattern.
     *
     * <p>Pattern matching supports wildcards (*) for bulk eviction.
     * Example: "student-service:teacher:classes:*" evicts all teacher class lists</p>
     *
     * @param pattern the pattern to match cache keys (e.g., "service:entity:*")
     */
    void evictPattern(String pattern);
}
