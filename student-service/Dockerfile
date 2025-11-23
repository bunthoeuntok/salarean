# Stage 1: Build sms-common library
FROM eclipse-temurin:21-jdk-alpine AS common-builder
WORKDIR /app/sms-common

# Install Maven
RUN apk add --no-cache maven

# Copy sms-common source and build it
COPY sms-common/pom.xml .
COPY sms-common/src src
RUN mvn clean install -DskipTests -B

# Stage 2: Build student-service
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy sms-common from previous stage to local Maven repo
COPY --from=common-builder /root/.m2/repository/com/sms/sms-common /root/.m2/repository/com/sms/sms-common

# Copy student-service pom and download dependencies
COPY student-service/pom.xml .
RUN mvn dependency:go-offline -B

# Copy student-service source and build
COPY student-service/src src
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

# Create non-root user and uploads directory
RUN addgroup -S spring && adduser -S spring -G spring && \
    mkdir -p /app/uploads/students && \
    chown -R spring:spring /app/uploads

USER spring:spring

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
