# Stage 1: Use pre-built sms-common library
FROM sms-common-builder:latest AS common-builder

# Stage 2: Build auth-service
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy sms-common from previous stage to local Maven repo
COPY --from=common-builder /root/.m2/repository/com/sms/sms-common /root/.m2/repository/com/sms/sms-common

# Copy auth-service pom and download dependencies
COPY auth-service/pom.xml .
RUN mvn dependency:go-offline -B

# Copy auth-service source and build
COPY auth-service/src src
RUN mvn clean package -DskipTests -B

# Extract layers for optimized Docker image
RUN java -Djarmode=layertools -jar target/*.jar extract

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy extracted layers from builder
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8081

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
