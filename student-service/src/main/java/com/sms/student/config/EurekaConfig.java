package com.sms.student.config;

import org.springframework.context.annotation.Configuration;

/**
 * Eureka Service Discovery configuration for the Student Service.
 * Eureka client is auto-configured via spring-cloud-starter-netflix-eureka-client dependency.
 * Configuration details are defined in application.yml.
 */
@Configuration
public class EurekaConfig {
    // Eureka client auto-configuration is enabled by spring-cloud-starter-netflix-eureka-client
    // Configuration is handled via application.yml (eureka.client.service-url.defaultZone, etc.)
    // No explicit @EnableEurekaClient annotation needed in Spring Cloud 2020.0+ versions
}
