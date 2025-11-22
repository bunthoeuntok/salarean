# Standard Dockerfile Template for Salarean SMS Microservices
#
# This multi-stage Dockerfile builds optimized Docker images for Spring Boot microservices
# using layered JARs for efficient caching and smaller image updates.
#
# Usage:
# 1. Copy this file to your service root directory
# 2. Update the EXPOSE port number to match your service's port (line 30)
# 3. Build: docker build -t {service-name} .
# 4. Run: docker run -p {port}:{port} {service-name}
#
# Template Version: 1.0.0
# Last Updated: 2025-11-22
# Based on: auth-service/Dockerfile

# ==============================================================================
# Build Stage: Compile and package the Spring Boot application
# ==============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven POM and download dependencies
# This layer is cached unless pom.xml changes
COPY pom.xml .
RUN apk add --no-cache maven && mvn dependency:go-offline -B

# Copy source code and build
COPY src src
RUN mvn clean package -DskipTests -B

# Extract layered JAR for optimized Docker layers
# Spring Boot layered JARs separate dependencies, resources, and application code
# This allows Docker to cache dependencies even when code changes
RUN java -Djarmode=layertools -jar target/*.jar extract

# ==============================================================================
# Runtime Stage: Create minimal production image
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy extracted layers from build stage (order matters for caching)
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# REQUIRED: Update this port to match your service's server.port in application.yml
# Default ports:
#   8081: auth-service
#   8082: student-service
#   8083: attendance-service
#   8084: grade-service
#   etc.
EXPOSE 8081  # TODO: Change this to your service's port

# Start the Spring Boot application using layered JAR launcher
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

# ==============================================================================
# Build and Run Instructions
# ==============================================================================
#
# Build image:
#   docker build -t {service-name}:latest .
#
# Run container (local testing):
#   docker run -p 8081:8081 \
#     -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/{service}_db \
#     -e SPRING_DATASOURCE_USERNAME=sms_user \
#     -e SPRING_DATASOURCE_PASSWORD=your_password \
#     -e JWT_SECRET=your_jwt_secret \
#     {service-name}:latest
#
# Run with Docker Compose (recommended):
#   docker-compose up -d {service-name}
#
# ==============================================================================
# Multi-Architecture Build (Optional)
# ==============================================================================
#
# To build for multiple platforms (amd64, arm64):
#   docker buildx build --platform linux/amd64,linux/arm64 -t {service-name}:latest .
#
# ==============================================================================
# Image Optimization Tips
# ==============================================================================
#
# 1. Layer Caching:
#    - Dependencies layer (~200MB) cached unless pom.xml changes
#    - Application layer (~10-50KB) rebuilt on every code change
#    - This drastically reduces build time for frequent changes
#
# 2. Image Size:
#    - Base image: eclipse-temurin:21-jre-alpine (~180MB)
#    - Dependencies: ~150-200MB (cached)
#    - Application code: 10-50KB
#    - Total: ~350-400MB per service
#
# 3. Security:
#    - Non-root user (spring:spring)
#    - Minimal Alpine Linux base image
#    - No unnecessary tools or packages
#    - JRE-only (no JDK) for reduced attack surface
#
# ==============================================================================
