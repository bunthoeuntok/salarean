# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and pom
COPY pom.xml .
COPY .mvn .mvn 2>/dev/null || true
COPY mvnw mvnw 2>/dev/null || true

# Download dependencies (if mvnw exists, otherwise use mvn directly)
RUN if [ -f mvnw ]; then chmod +x mvnw && ./mvnw dependency:go-offline -B; \
    else apk add --no-cache maven && mvn dependency:go-offline -B; fi

# Copy source and build
COPY src src
RUN if [ -f mvnw ]; then ./mvnw clean package -DskipTests -B; \
    else mvn clean package -DskipTests -B; fi

# Extract layers
RUN java -Djarmode=layertools -jar target/*.jar extract

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8761

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
