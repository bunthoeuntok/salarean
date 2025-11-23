# Docker Build Optimization for sms-common

## Problem
Currently, each service (auth-service, student-service, etc.) duplicates the sms-common build stage in their Dockerfiles:
- ❌ Wastes build time (sms-common built N times for N services)
- ❌ Duplicates Dockerfile code across all services
- ❌ Increases maintenance burden when sms-common changes

## Recommended Solutions

### ✅ **Option 1: Docker Compose Build Dependencies (RECOMMENDED)**

**Best for**: Projects with multiple services sharing a common library

**Approach**: Build sms-common as a separate service in docker-compose, then reference it.

**Benefits**:
- ✅ sms-common built once per `docker-compose build`
- ✅ Automatic build ordering via depends_on
- ✅ Cleaner than duplicating stages
- ✅ No external registry needed
- ✅ Works well with docker-compose workflow

**Implementation**:

#### 1. Add sms-common-builder to docker-compose.yml:

```yaml
version: '3.8'

services:
  # ============================================
  # SMS COMMON LIBRARY BUILDER
  # ============================================
  sms-common-builder:
    build:
      context: ./sms-common
      dockerfile: Dockerfile
    image: sms-common-builder:latest
    # This service only exists to build the base image
    # It won't run as a container
    profiles:
      - build-only

  # ============================================
  # AUTH SERVICE
  # ============================================
  auth-service:
    build:
      context: .
      dockerfile: ./auth-service/Dockerfile
    depends_on:
      - sms-common-builder
    # ... rest of config
```

#### 2. Simplified auth-service/Dockerfile:

```dockerfile
# Use the pre-built sms-common image
FROM sms-common-builder:latest AS common-builder

# Stage 2: Build auth-service
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy sms-common from base image
COPY --from=common-builder /root/.m2/repository/com/sms/sms-common /root/.m2/repository/com/sms/sms-common

# Copy auth-service pom and download dependencies
COPY auth-service/pom.xml .
RUN mvn dependency:go-offline -B

# Copy auth-service source and build
COPY auth-service/src src
RUN mvn clean package -DskipTests -B

# Extract layers
RUN java -Djarmode=layertools -jar target/*.jar extract

# Stage 3: Runtime (unchanged)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8081

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

#### 3. Build commands:

```bash
# Build sms-common first
docker-compose build sms-common-builder

# Build all services (they'll use the cached sms-common-builder image)
docker-compose build auth-service student-service

# Or build everything at once
docker-compose build
```

---

### ⚙️ **Option 2: Docker BuildKit Cache Mounts (ADVANCED)**

**Best for**: CI/CD pipelines and advanced users

**Approach**: Use BuildKit's cache mount feature to share Maven cache across builds.

**Benefits**:
- ✅ Fastest builds with proper caching
- ✅ No duplicate stages needed
- ✅ Works great in CI/CD

**Requirements**:
- Docker BuildKit enabled (`DOCKER_BUILDKIT=1`)
- Docker 18.09+

**Implementation**:

```dockerfile
# syntax=docker/dockerfile:1.4

# Stage 1: Build with cache mounts
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

RUN apk add --no-cache maven

# Build sms-common with cache mount
WORKDIR /build/sms-common
COPY sms-common/pom.xml .
COPY sms-common/src src
RUN --mount=type=cache,target=/root/.m2 mvn clean install -DskipTests -B

# Build service
WORKDIR /app
COPY auth-service/pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

COPY auth-service/src src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -B

# Extract layers
RUN java -Djarmode=layertools -jar target/*.jar extract

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
# ... (same as before)
```

**Build command**:
```bash
DOCKER_BUILDKIT=1 docker build -f auth-service/Dockerfile .
```

---

### 🏗️ **Option 3: Multi-Service Dockerfile (ALTERNATIVE)**

**Best for**: Small projects with few services

**Approach**: Single Dockerfile that can build any service using build args.

**Benefits**:
- ✅ Single Dockerfile to maintain
- ✅ sms-common stage written once
- ✅ Simple architecture

**Cons**:
- ❌ Less flexible for service-specific needs
- ❌ Harder to read

**Implementation**:

```dockerfile
# Universal Dockerfile for all services
ARG SERVICE_NAME

# Stage 1: Build sms-common
FROM eclipse-temurin:21-jdk-alpine AS common-builder
WORKDIR /app/sms-common

RUN apk add --no-cache maven
COPY sms-common/pom.xml .
COPY sms-common/src src
RUN mvn clean install -DskipTests -B

# Stage 2: Build service
FROM eclipse-temurin:21-jdk-alpine AS builder
ARG SERVICE_NAME

WORKDIR /app
RUN apk add --no-cache maven

COPY --from=common-builder /root/.m2/repository/com/sms/sms-common /root/.m2/repository/com/sms/sms-common

COPY ${SERVICE_NAME}/pom.xml .
RUN mvn dependency:go-offline -B

COPY ${SERVICE_NAME}/src src
RUN mvn clean package -DskipTests -B
RUN java -Djarmode=layertools -jar target/*.jar extract

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

**docker-compose.yml**:
```yaml
auth-service:
  build:
    context: .
    dockerfile: Dockerfile
    args:
      SERVICE_NAME: auth-service
```

---

## Comparison Matrix

| Approach | Build Time | Complexity | CI/CD | Maintenance | Recommended |
|----------|-----------|------------|-------|-------------|-------------|
| **Option 1: Compose Dependencies** | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ✅ **YES** |
| Option 2: BuildKit Cache | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | For CI/CD |
| Option 3: Multi-Service | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | No |
| Current (Duplicated) | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐ | ❌ **Current** |

---

## Recommended Implementation Plan

### Phase 1: Immediate (Option 1)
1. Create `sms-common/Dockerfile`
2. Add `sms-common-builder` service to docker-compose.yml
3. Update auth-service Dockerfile to use base image
4. Update student-service Dockerfile to use base image
5. Test builds

### Phase 2: CI/CD Optimization (Option 2)
1. Enable BuildKit in CI/CD pipeline
2. Add cache mount directives to Dockerfiles
3. Configure CI/CD to use BuildKit caching

### Phase 3: Future Services
- All new services automatically use the optimized pattern
- No duplication of sms-common build stage

---

## Migration Guide

### Step 1: Create sms-common/Dockerfile
Already created at `/sms-common/Dockerfile`

### Step 2: Update docker-compose.yml
Add before all services:
```yaml
  sms-common-builder:
    build:
      context: ./sms-common
      dockerfile: Dockerfile
    image: sms-common-builder:latest
    profiles:
      - build-only
```

### Step 3: Update Service Dockerfiles
Replace:
```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS common-builder
WORKDIR /app/sms-common
RUN apk add --no-cache maven
COPY sms-common/pom.xml .
COPY sms-common/src src
RUN mvn clean install -DskipTests -B
```

With:
```dockerfile
FROM sms-common-builder:latest AS common-builder
```

### Step 4: Build Order
```bash
# Build sms-common once
docker-compose build sms-common-builder

# Build services (use cached sms-common)
docker-compose build
```

---

## Performance Metrics (Expected)

### Current Approach
- First build: ~5 minutes per service
- sms-common built: N times (N = number of services)
- Total build time (3 services): ~15 minutes

### Optimized Approach (Option 1)
- First build: ~5 minutes total
- sms-common built: 1 time
- Total build time (3 services): ~7 minutes
- **Improvement: 53% faster**

### With BuildKit Cache (Option 2)
- First build: ~5 minutes
- Incremental builds: ~30 seconds
- **Improvement: 90% faster for rebuilds**

---

## Conclusion

**Recommended**: Implement **Option 1 (Docker Compose Build Dependencies)** immediately for:
- ✅ Significant build time reduction
- ✅ Cleaner Dockerfile maintenance
- ✅ Better developer experience
- ✅ No external dependencies

Consider **Option 2 (BuildKit Cache)** for CI/CD pipelines to achieve maximum build performance.
