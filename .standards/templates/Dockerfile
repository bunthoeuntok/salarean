# Standard Dockerfile Template for Salarean SMS Microservices
#
# This optimized multi-stage Dockerfile builds Spring Boot microservices using:
# 1. Pre-built sms-common-builder base image (avoids rebuilding sms-common per service)
# 2. Layered JARs for efficient caching and smaller image updates
# 3. Non-root user for security
# 4. Health check for container orchestration
#
# Usage:
# 1. Copy this file to your service root directory as "Dockerfile"
# 2. Replace {service-name} with your service name (e.g., student-service)
# 3. Replace {service-port} with your service port (e.g., 8082)
# 4. Ensure sms-common-builder image is built first:
#    docker-compose build sms-common-builder
# 5. Build your service:
#    docker-compose build {service-name}
#
# Template Version: 2.0.0
# Last Updated: 2025-11-23
# Based on: auth-service/Dockerfile (optimized)
# Reference: .standards/docs/docker-build-optimization.md

# ==============================================================================
# Stage 1: Use pre-built sms-common library
# ==============================================================================
# This stage references the sms-common-builder base image which contains
# sms-common library already built and installed to Maven local repository.
# This eliminates duplicate sms-common builds across all services.
FROM sms-common-builder:latest AS common-builder

# ==============================================================================
# Stage 2: Build {service-name}
# ==============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy sms-common from base image to local Maven repo
# This makes sms-common available as a dependency during the build
COPY --from=common-builder /root/.m2/repository/com/sms/sms-common /root/.m2/repository/com/sms/sms-common

# Copy service POM and download dependencies
# This layer is cached unless pom.xml changes
COPY {service-name}/pom.xml .
RUN mvn dependency:go-offline -B

# Copy service source code and build
COPY {service-name}/src src
RUN mvn clean package -DskipTests -B

# Extract layered JAR for optimized Docker layers
# Spring Boot layered JARs separate dependencies, resources, and application code
# This allows Docker to cache dependencies even when code changes
RUN java -Djarmode=layertools -jar target/*.jar extract

# ==============================================================================
# Stage 3: Runtime - Create minimal production image
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
#   8080: api-gateway
#   8081: auth-service
#   8082: student-service
#   8083: attendance-service
#   8084: grade-service
#   8085: report-service
#   8086: notification-service
EXPOSE {service-port}  # TODO: Replace with your service's port

# Health check for container orchestration (Kubernetes, Docker Compose)
# Pings the Spring Boot Actuator health endpoint
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:{service-port}/actuator/health || exit 1

# Start the Spring Boot application using layered JAR launcher
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

# ==============================================================================
# Build Instructions
# ==============================================================================
#
# Prerequisites:
# 1. sms-common-builder base image must be built first:
#    cd /path/to/salarean
#    docker-compose build sms-common-builder
#
# Build this service:
#    docker-compose build {service-name}
#
# Or build all services:
#    docker-compose build
#
# ==============================================================================
# Docker Compose Configuration
# ==============================================================================
#
# In docker-compose.yml, configure your service like this:
#
# services:
#   {service-name}:
#     build:
#       context: .                              # Root directory (IMPORTANT!)
#       dockerfile: ./{service-name}/Dockerfile
#     container_name: {service-name}
#     environment:
#       - SPRING_PROFILES_ACTIVE=docker
#       - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-{service}:5432/{service}_db
#       - SPRING_DATASOURCE_USERNAME=sms_user
#       - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
#       - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
#       - JWT_SECRET=${JWT_SECRET}
#     ports:
#       - "{service-port}:{service-port}"
#     networks:
#       - backend-network
#       - database-network
#     depends_on:
#       - postgres-{service}
#       - eureka-server
#
# ==============================================================================
# Run Container Locally (for testing without Docker Compose)
# ==============================================================================
#
# docker run -p {service-port}:{service-port} \
#   -e SPRING_PROFILES_ACTIVE=docker \
#   -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/{service}_db \
#   -e SPRING_DATASOURCE_USERNAME=sms_user \
#   -e SPRING_DATASOURCE_PASSWORD=your_password \
#   -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://host.docker.internal:8761/eureka/ \
#   -e JWT_SECRET=your_jwt_secret \
#   {service-name}:latest
#
# ==============================================================================
# Image Optimization Details
# ==============================================================================
#
# 1. Layer Caching:
#    - sms-common layer: Built once, shared across all services
#    - Dependencies layer (~200MB): Cached unless pom.xml changes
#    - Application layer (~10-50KB): Rebuilt on every code change
#    - This drastically reduces build time for frequent changes
#
# 2. Build Performance:
#    - Before optimization: ~5 min per service (rebuilds sms-common each time)
#    - After optimization: ~2 min per service (uses cached sms-common)
#    - Total improvement: ~53% faster builds
#
# 3. Image Size:
#    - Base image: eclipse-temurin:21-jre-alpine (~180MB)
#    - Dependencies + sms-common: ~150-200MB (cached)
#    - Application code: 10-50KB
#    - Total: ~350-400MB per service
#
# 4. Security:
#    - Non-root user (spring:spring)
#    - Minimal Alpine Linux base image
#    - No unnecessary tools or packages
#    - JRE-only (no JDK) for reduced attack surface
#    - Health checks for orchestration
#
# ==============================================================================
# Troubleshooting
# ==============================================================================
#
# Error: "failed to solve: sms-common-builder:latest: not found"
# Solution: Build the base image first:
#   docker-compose build sms-common-builder
#
# Error: "COPY failed: file not found: sms-common/pom.xml"
# Solution: Ensure docker-compose.yml build context is root (.):
#   build:
#     context: .  # Root directory, not ./service-name
#     dockerfile: ./service-name/Dockerfile
#
# For complete troubleshooting guide, see:
# .standards/docs/docker-build-optimization.md
#
# ==============================================================================
# Multi-Architecture Build (Optional)
# ==============================================================================
#
# To build for multiple platforms (amd64, arm64):
#   docker buildx build --platform linux/amd64,linux/arm64 \
#     --build-arg SERVICE_NAME={service-name} \
#     -t {service-name}:latest .
#
# ==============================================================================
