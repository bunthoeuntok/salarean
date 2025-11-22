package com.sms.SERVICENAME.config;  // TODO: Replace SERVICENAME with your service name

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 *
 * Purpose: Configures Swagger UI for interactive API documentation
 *
 * CUSTOMIZATION REQUIRED:
 * 1. Update package name: Replace 'SERVICENAME' with your service name
 * 2. Update method name: Replace 'servicenameAPI' with '{servicename}API'
 * 3. Update API title and description
 *
 * CRITICAL: Server URL MUST point to API Gateway (http://localhost:8080)
 *           NOT to the service's direct port - this prevents CORS errors
 */
@Configuration
public class OpenAPIConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI servicenameAPI() {  // TODO: Rename method to match your service (e.g., authServiceAPI, studentServiceAPI)
        // Server configuration - MUST point to API Gateway
        Server server = new Server();
        server.setUrl("http://localhost:8080");  // API Gateway port - DO NOT CHANGE
        server.setDescription("API Gateway");

        return new OpenAPI()
                .servers(List.of(server))
                .info(new Info()
                        .title("Service Name API")  // TODO: Update title (e.g., "Authentication Service API")
                        .description("Service description")  // TODO: Update description
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, createSecurityScheme()))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT token obtained from /api/auth/login or /api/auth/register");
    }
}
