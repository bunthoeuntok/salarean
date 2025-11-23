# Dockerfile for building sms-common library
# This creates a base image with sms-common installed to local Maven repo
FROM eclipse-temurin:21-jdk-alpine AS sms-common-builder

WORKDIR /app/sms-common

# Install Maven
RUN apk add --no-cache maven

# Copy sms-common source
COPY pom.xml .
COPY src src

# Build and install sms-common to /root/.m2/repository
RUN mvn clean install -DskipTests -B

# This image can be used as a base for all services
# Usage in service Dockerfile:
#   COPY --from=sms-common-builder /root/.m2/repository/com/sms/sms-common /root/.m2/repository/com/sms/sms-common
