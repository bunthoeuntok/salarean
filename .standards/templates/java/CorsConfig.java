package com.sms.SERVICENAME.config;  // TODO: Replace SERVICENAME with your service name (e.g., auth, student, teacher)

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) Configuration
 *
 * Purpose: Allows cross-origin HTTP requests from frontend applications
 *
 * CUSTOMIZATION REQUIRED:
 * 1. Update package name: Replace 'SERVICENAME' with your service name
 * 2. (Optional) Update allowed origins for production
 *
 * NO OTHER CHANGES NEEDED - This template works as-is after package rename
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed Origins
        // Development: Allow all origins with "*"
        // Production: Replace with specific URLs (e.g., "https://sms.example.com")
        configuration.setAllowedOrigins(List.of("*"));

        // Allowed HTTP Methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allowed Headers
        // "*" allows all headers including Authorization (required for JWT)
        configuration.setAllowedHeaders(List.of("*"));

        // Allow Credentials
        // false when using AllowedOrigins("*")
        // Set to true when using specific origins with cookies/authentication
        configuration.setAllowCredentials(false);

        // Preflight Request Cache Duration (seconds)
        // Browsers cache preflight OPTIONS requests for this duration
        configuration.setMaxAge(3600L);

        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
