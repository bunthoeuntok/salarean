package com.sms.student.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for student-service caching.
 *
 * <p>Configures RedisTemplate with JSON serialization for class and student data caching.
 * Uses Jackson for object serialization to support complex DTOs and Java 8 time types.</p>
 *
 * <p><strong>Configuration</strong>:
 * <ul>
 *   <li>Key Serializer: String (for cache keys like "student-service:class:{uuid}")</li>
 *   <li>Value Serializer: JSON (for ClassDTO and other complex objects)</li>
 *   <li>Java Time Support: Enabled via JavaTimeModule</li>
 * </ul>
 * </p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Configuration
public class RedisConfig {

    /**
     * Configure RedisTemplate with JSON serialization.
     *
     * @param connectionFactory Redis connection factory (auto-configured by Spring Boot)
     * @return configured RedisTemplate instance
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Use JSON serializer for values
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configure ObjectMapper for Redis JSON serialization.
     * Supports Java 8 time types (Instant, LocalDate, etc.).
     *
     * @return configured ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
