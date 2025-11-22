package com.sms.SERVICENAME.config;  // TODO: Replace SERVICENAME with your service name

import com.sms.SERVICENAME.security.JwtAuthenticationFilter;  // TODO: Update package name
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security Configuration
 *
 * Purpose: Configures security filter chain with JWT authentication
 *
 * CUSTOMIZATION REQUIRED:
 * 1. Update package name: Replace 'SERVICENAME' with your service name
 * 2. Update import: com.sms.SERVICENAME.security.JwtAuthenticationFilter
 * 3. Update public endpoints: Add/remove endpoints in requestMatchers().permitAll()
 *
 * KEEP AS-IS:
 * - CORS integration
 * - CSRF disabled (stateless JWT auth)
 * - Stateless session management
 * - JWT filter ordering
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CORS integration (from CorsConfig)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // CSRF disabled for stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless session management (no HTTP sessions)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // TODO: Customize public endpoints based on your service
                        // Example for auth-service:
                        // .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()

                        // Standard public endpoints (KEEP THESE):
                        .requestMatchers("/actuator/**").permitAll()  // Health checks
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()  // Swagger

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Add JWT filter before Spring's default authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}
