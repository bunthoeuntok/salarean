package com.sms.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.embedded.RedisServer;

import java.io.IOException;

/**
 * Test configuration for embedded Redis server.
 * Starts a Redis server on port 6379 for integration tests.
 */
@TestConfiguration
public class TestRedisConfiguration {

    private static RedisServer redisServer;
    private static boolean redisStarted = false;

    @PostConstruct
    public void startRedis() {
        if (!redisStarted) {
            try {
                redisServer = new RedisServer(6379);
                redisServer.start();
                redisStarted = true;
                System.out.println("Embedded Redis started on port 6379");
            } catch (Exception e) {
                // Redis might already be running or port is in use
                System.out.println("Could not start embedded Redis: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
            redisStarted = false;
        }
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() throws IOException {
        // Ensure Redis is started first
        startRedis();

        // Give Redis a moment to fully start
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory("localhost", 6379);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Create ObjectMapper with Java 8 date/time support
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }
}
