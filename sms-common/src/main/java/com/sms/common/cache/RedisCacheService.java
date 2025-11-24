package com.sms.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * Redis-based implementation of the CacheService interface.
 *
 * <p>This service provides Redis caching with Jackson JSON serialization and graceful
 * degradation on failures. All cache operations are fault-tolerant - if Redis is unavailable
 * or any operation fails, the service logs a warning and returns empty/no-op results rather
 * than throwing exceptions.</p>
 *
 * <p><strong>Graceful Degradation Strategy</strong>:
 * <ul>
 *   <li>get() returns Optional.empty() on any failure (connection, deserialization, etc.)</li>
 *   <li>put() silently fails with warning log if Redis unavailable</li>
 *   <li>evict() silently fails with warning log if Redis unavailable</li>
 *   <li>Application continues to function using database fallback</li>
 * </ul>
 * </p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Service
public class RedisCacheService implements CacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached == null) {
                log.debug("Cache miss for key: {}", key);
                return Optional.empty();
            }

            log.debug("Cache hit for key: {}", key);
            T value = objectMapper.convertValue(cached, type);
            return Optional.ofNullable(value);

        } catch (Exception e) {
            log.warn("Cache get failed for key: {} - falling back to database", key, e);
            return Optional.empty(); // Graceful degradation
        }
    }

    @Override
    public <T> void put(String key, T value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Cached value for key: {} with TTL: {}", key, ttl);

        } catch (Exception e) {
            log.warn("Cache put failed for key: {} - continuing without cache", key, e);
            // Graceful degradation: don't throw, just log and continue
        }
    }

    @Override
    public void evict(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Evicted cache key: {}", key);
            } else {
                log.debug("Cache key not found for eviction: {}", key);
            }

        } catch (Exception e) {
            log.warn("Cache evict failed for key: {} - continuing without eviction", key, e);
            // Graceful degradation: don't throw, just log and continue
        }
    }

    @Override
    public void evictPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                log.debug("Evicted {} cache keys matching pattern: {}", deleted, pattern);
            } else {
                log.debug("No cache keys found matching pattern: {}", pattern);
            }

        } catch (Exception e) {
            log.warn("Cache evict pattern failed for: {} - continuing without eviction", pattern, e);
            // Graceful degradation: don't throw, just log and continue
        }
    }
}
