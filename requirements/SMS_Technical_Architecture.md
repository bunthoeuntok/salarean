# Student Management System
## Technical Architecture Document
### Docker-Based Infrastructure

**Version:** 1.0  
**Date:** November 19, 2025  
**Last Updated:** November 19, 2025

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Technology Stack](#2-technology-stack)
3. [Infrastructure Setup](#3-infrastructure-setup)
4. [Docker Configuration](#4-docker-configuration)
5. [Microservices Details](#5-microservices-details)
6. [Database Schema Overview](#6-database-schema-overview)
7. [API Gateway & Routing](#7-api-gateway--routing)
8. [Security Architecture](#8-security-architecture)
9. [Deployment Strategy](#9-deployment-strategy)
10. [Monitoring & Logging](#10-monitoring--logging)

---

## 1. Architecture Overview

### 1.1 High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        USERS                                     │
│              (Teachers via Web Browser)                          │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTPS
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│                     NGINX REVERSE PROXY                          │
│                     (Docker Container)                           │
│              - SSL Termination                                   │
│              - Load Balancing                                    │
│              - Static File Serving                               │
└────────────────────────────┬────────────────────────────────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
                ↓                         ↓
┌─────────────────────────┐   ┌─────────────────────────┐
│   NEXT.JS FRONTEND      │   │    API GATEWAY          │
│   (Docker Container)    │   │  (Spring Cloud Gateway) │
│   - React Components    │   │   (Docker Container)    │
│   - Server-Side Render  │   │   - Routing             │
│   - Static Assets       │   │   - JWT Validation      │
└─────────────────────────┘   │   - Rate Limiting       │
                              └──────────┬──────────────┘
                                         │
                    ┌────────────────────┼────────────────────┐
                    │                    │                    │
                    ↓                    ↓                    ↓
        ┌───────────────────┐ ┌──────────────────┐ ┌─────────────────┐
        │  AUTH SERVICE     │ │ STUDENT SERVICE  │ │ ATTENDANCE SVC  │
        │  (Port 8081)      │ │  (Port 8082)     │ │  (Port 8083)    │
        │  Docker Container │ │ Docker Container │ │ Docker Container│
        └─────────┬─────────┘ └────────┬─────────┘ └────────┬────────┘
                  │                    │                     │
                  ↓                    ↓                     ↓
        ┌──────────────────┐ ┌──────────────────┐ ┌─────────────────┐
        │   auth_db        │ │   student_db     │ │  attendance_db  │
        │   (PostgreSQL)   │ │   (PostgreSQL)   │ │   (PostgreSQL)  │
        │ Docker Container │ │ Docker Container │ │ Docker Container│
        └──────────────────┘ └──────────────────┘ └─────────────────┘

                    ┌────────────────────┼────────────────────┐
                    │                    │                    │
                    ↓                    ↓                    ↓
        ┌───────────────────┐ ┌──────────────────┐ ┌─────────────────┐
        │  GRADE SERVICE    │ │  REPORT SERVICE  │ │ NOTIFICATION    │
        │  (Port 8084)      │ │  (Port 8085)     │ │  SERVICE        │
        │  Docker Container │ │ Docker Container │ │  (Port 8086)    │
        └─────────┬─────────┘ └────────┬─────────┘ │ Docker Container│
                  │                    │           └────────┬────────┘
                  ↓                    ↓                    │
        ┌──────────────────┐ ┌──────────────────┐         │
        │   grade_db       │ │   report_db      │         ↓
        │   (PostgreSQL)   │ │   (PostgreSQL)   │ ┌─────────────────┐
        │ Docker Container │ │ Docker Container │ │ notification_db │
        └──────────────────┘ └──────────────────┘ │  (PostgreSQL)   │
                                                   │ Docker Container│
                                                   └─────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    SHARED SERVICES                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │    REDIS     │  │   RABBITMQ   │  │   EUREKA     │         │
│  │   (Cache)    │  │ (Msg Queue)  │  │  (Service    │         │
│  │   Docker     │  │   Docker     │  │  Discovery)  │         │
│  │  Container   │  │  Container   │  │   Docker     │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Design Principles

1. **Microservices Architecture**: Each service is independently deployable
2. **Database Per Service**: Each microservice owns its database
3. **Docker Containerization**: All components run in Docker containers
4. **API Gateway Pattern**: Single entry point for all backend APIs
5. **Responsive Web Design**: Works on mobile, tablet, and desktop
6. **Stateless Services**: Services don't maintain client state (use Redis)
7. **Asynchronous Communication**: RabbitMQ for event-driven messaging

---

## 2. Technology Stack

### 2.1 Frontend Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Next.js** | 14.2+ | React framework with SSR |
| **React** | 18.2+ | UI library |
| **TypeScript** | 5.0+ | Type safety |
| **TailwindCSS** | 3.4+ | Utility-first CSS |
| **shadcn/ui** | Latest | Pre-built components |
| **Zustand** | 4.5+ | State management |
| **React Query** | 5.0+ | Data fetching & caching |
| **Axios** | 1.6+ | HTTP client |
| **React Hook Form** | 7.50+ | Form handling |
| **Zod** | 3.22+ | Schema validation |
| **date-fns** | 3.0+ | Date manipulation |
| **i18next** | 23.0+ | Internationalization |

### 2.2 Backend Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Spring Boot** | 3.2+ | Microservices framework |
| **Java** | 21 LTS | Programming language |
| **Spring Cloud Gateway** | 4.1+ | API Gateway |
| **Spring Security** | 6.2+ | Authentication & authorization |
| **Spring Data JPA** | 3.2+ | ORM & database access |
| **PostgreSQL** | 15+ | Primary database |
| **Redis** | 7.2+ | Caching & session store |
| **RabbitMQ** | 3.12+ | Message broker |
| **Netflix Eureka** | 4.1+ | Service discovery |
| **JasperReports** | 6.20+ | PDF report generation |
| **Lombok** | 1.18+ | Reduce boilerplate |
| **MapStruct** | 1.5+ | DTO mapping |

### 2.3 Infrastructure Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Docker** | 24.0+ | Containerization |
| **Docker Compose** | 2.23+ | Multi-container orchestration |
| **Nginx** | 1.25+ | Reverse proxy & load balancer |
| **GitHub Actions** | Latest | CI/CD pipeline |
| **AWS/GCP/DO** | - | Cloud hosting |
| **Let's Encrypt** | - | SSL certificates |

### 2.4 Development Tools

| Tool | Purpose |
|------|---------|
| **Git** | Version control |
| **VS Code** | IDE (frontend) |
| **IntelliJ IDEA** | IDE (backend) |
| **Postman** | API testing |
| **pgAdmin** | Database management |
| **Docker Desktop** | Local Docker environment |

---

## 3. Infrastructure Setup

### 3.1 Environment Structure

```
Development Environment:
- All services run locally via Docker Compose
- Hot reload enabled for frontend and backend
- Local PostgreSQL databases
- Local Redis and RabbitMQ

Staging Environment:
- Docker Compose on dedicated staging server
- Mirrors production configuration
- Separate databases (staging data)
- CI/CD auto-deploys from 'develop' branch

Production Environment:
- Docker Compose on production server(s)
- Load balancing via Nginx
- Auto-restart on failure
- Monitoring and alerting enabled
- Daily backups
```

### 3.2 Network Architecture

```yaml
Docker Networks:
  - frontend-network: Next.js ↔ Nginx
  - backend-network: All backend services
  - database-network: Services ↔ PostgreSQL
  - cache-network: Services ↔ Redis
  - message-network: Services ↔ RabbitMQ
```

### 3.3 Volume Management

```yaml
Persistent Volumes:
  - postgres-auth-data: /var/lib/postgresql/data
  - postgres-student-data: /var/lib/postgresql/data
  - postgres-attendance-data: /var/lib/postgresql/data
  - postgres-grade-data: /var/lib/postgresql/data
  - postgres-report-data: /var/lib/postgresql/data
  - postgres-notification-data: /var/lib/postgresql/data
  - redis-data: /data
  - rabbitmq-data: /var/lib/rabbitmq
  - nginx-logs: /var/log/nginx
  - uploads: /app/uploads (student photos, etc.)
```

---

## 4. Docker Configuration

### 4.1 Complete docker-compose.yml

```yaml
version: '3.8'

services:
  # ============================================
  # FRONTEND
  # ============================================
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: sms-frontend
    ports:
      - "3000:3000"
    environment:
      - NODE_ENV=production
      - NEXT_PUBLIC_API_URL=http://api-gateway:8080
    networks:
      - frontend-network
    depends_on:
      - api-gateway
    restart: unless-stopped

  # ============================================
  # REVERSE PROXY
  # ============================================
  nginx:
    image: nginx:alpine
    container_name: sms-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
      - ./nginx/logs:/var/log/nginx
    networks:
      - frontend-network
      - backend-network
    depends_on:
      - frontend
      - api-gateway
    restart: unless-stopped

  # ============================================
  # API GATEWAY
  # ============================================
  api-gateway:
    build:
      context: ./api-gateway
      dockerfile: Dockerfile
    container_name: sms-api-gateway
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
    networks:
      - backend-network
      - cache-network
    depends_on:
      - eureka-server
      - redis
    restart: unless-stopped

  # ============================================
  # SERVICE DISCOVERY
  # ============================================
  eureka-server:
    build:
      context: ./eureka-server
      dockerfile: Dockerfile
    container_name: sms-eureka
    ports:
      - "8761:8761"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    networks:
      - backend-network
    restart: unless-stopped

  # ============================================
  # AUTH SERVICE
  # ============================================
  auth-service:
    build:
      context: ./auth-service
      dockerfile: Dockerfile
    container_name: sms-auth-service
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-auth:5432/auth_db
      - SPRING_DATASOURCE_USERNAME=sms_user
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_REDIS_HOST=redis
      - JWT_SECRET=${JWT_SECRET}
    networks:
      - backend-network
      - database-network
      - cache-network
    depends_on:
      - postgres-auth
      - eureka-server
      - redis
    restart: unless-stopped

  # ============================================
  # STUDENT SERVICE
  # ============================================
  student-service:
    build:
      context: ./student-service
      dockerfile: Dockerfile
    container_name: sms-student-service
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-student:5432/student_db
      - SPRING_DATASOURCE_USERNAME=sms_user
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_REDIS_HOST=redis
    volumes:
      - uploads:/app/uploads
    networks:
      - backend-network
      - database-network
      - cache-network
    depends_on:
      - postgres-student
      - eureka-server
      - redis
    restart: unless-stopped

  # ============================================
  # ATTENDANCE SERVICE
  # ============================================
  attendance-service:
    build:
      context: ./attendance-service
      dockerfile: Dockerfile
    container_name: sms-attendance-service
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-attendance:5432/attendance_db
      - SPRING_DATASOURCE_USERNAME=sms_user
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_RABBITMQ_HOST=rabbitmq
    networks:
      - backend-network
      - database-network
      - message-network
    depends_on:
      - postgres-attendance
      - eureka-server
      - rabbitmq
    restart: unless-stopped

  # ============================================
  # GRADE SERVICE
  # ============================================
  grade-service:
    build:
      context: ./grade-service
      dockerfile: Dockerfile
    container_name: sms-grade-service
    ports:
      - "8084:8084"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-grade:5432/grade_db
      - SPRING_DATASOURCE_USERNAME=sms_user
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_RABBITMQ_HOST=rabbitmq
    networks:
      - backend-network
      - database-network
      - message-network
    depends_on:
      - postgres-grade
      - eureka-server
      - rabbitmq
    restart: unless-stopped

  # ============================================
  # REPORT SERVICE
  # ============================================
  report-service:
    build:
      context: ./report-service
      dockerfile: Dockerfile
    container_name: sms-report-service
    ports:
      - "8085:8085"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-report:5432/report_db
      - SPRING_DATASOURCE_USERNAME=sms_user
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    networks:
      - backend-network
      - database-network
    depends_on:
      - postgres-report
      - eureka-server
    restart: unless-stopped

  # ============================================
  # NOTIFICATION SERVICE
  # ============================================
  notification-service:
    build:
      context: ./notification-service
      dockerfile: Dockerfile
    container_name: sms-notification-service
    ports:
      - "8086:8086"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-notification:5432/notification_db
      - SPRING_DATASOURCE_USERNAME=sms_user
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_RABBITMQ_HOST=rabbitmq
      - TWILIO_ACCOUNT_SID=${TWILIO_ACCOUNT_SID}
      - TWILIO_AUTH_TOKEN=${TWILIO_AUTH_TOKEN}
    networks:
      - backend-network
      - database-network
      - message-network
    depends_on:
      - postgres-notification
      - eureka-server
      - rabbitmq
    restart: unless-stopped

  # ============================================
  # DATABASES
  # ============================================
  postgres-auth:
    image: postgres:15-alpine
    container_name: sms-postgres-auth
    environment:
      - POSTGRES_DB=auth_db
      - POSTGRES_USER=sms_user
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres-auth-data:/var/lib/postgresql/data
    networks:
      - database-network
    restart: unless-stopped

  postgres-student:
    image: postgres:15-alpine
    container_name: sms-postgres-student
    environment:
      - POSTGRES_DB=student_db
      - POSTGRES_USER=sms_user
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres-student-data:/var/lib/postgresql/data
    networks:
      - database-network
    restart: unless-stopped

  postgres-attendance:
    image: postgres:15-alpine
    container_name: sms-postgres-attendance
    environment:
      - POSTGRES_DB=attendance_db
      - POSTGRES_USER=sms_user
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres-attendance-data:/var/lib/postgresql/data
    networks:
      - database-network
    restart: unless-stopped

  postgres-grade:
    image: postgres:15-alpine
    container_name: sms-postgres-grade
    environment:
      - POSTGRES_DB=grade_db
      - POSTGRES_USER=sms_user
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres-grade-data:/var/lib/postgresql/data
    networks:
      - database-network
    restart: unless-stopped

  postgres-report:
    image: postgres:15-alpine
    container_name: sms-postgres-report
    environment:
      - POSTGRES_DB=report_db
      - POSTGRES_USER=sms_user
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres-report-data:/var/lib/postgresql/data
    networks:
      - database-network
    restart: unless-stopped

  postgres-notification:
    image: postgres:15-alpine
    container_name: sms-postgres-notification
    environment:
      - POSTGRES_DB=notification_db
      - POSTGRES_USER=sms_user
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres-notification-data:/var/lib/postgresql/data
    networks:
      - database-network
    restart: unless-stopped

  # ============================================
  # REDIS CACHE
  # ============================================
  redis:
    image: redis:7-alpine
    container_name: sms-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - cache-network
    restart: unless-stopped

  # ============================================
  # RABBITMQ MESSAGE BROKER
  # ============================================
  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: sms-rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      - RABBITMQ_DEFAULT_USER=sms_user
      - RABBITMQ_DEFAULT_PASS=${RABBITMQ_PASSWORD}
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
    networks:
      - message-network
    restart: unless-stopped

# ============================================
# NETWORKS
# ============================================
networks:
  frontend-network:
    driver: bridge
  backend-network:
    driver: bridge
  database-network:
    driver: bridge
  cache-network:
    driver: bridge
  message-network:
    driver: bridge

# ============================================
# VOLUMES
# ============================================
volumes:
  postgres-auth-data:
  postgres-student-data:
  postgres-attendance-data:
  postgres-grade-data:
  postgres-report-data:
  postgres-notification-data:
  redis-data:
  rabbitmq-data:
  uploads:
```

### 4.2 Frontend Dockerfile

```dockerfile
# frontend/Dockerfile
FROM node:20-alpine AS base

# Install dependencies only when needed
FROM base AS deps
RUN apk add --no-cache libc6-compat
WORKDIR /app

# Copy package files
COPY package.json package-lock.json* ./
RUN npm ci

# Rebuild the source code only when needed
FROM base AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .

# Build Next.js app
ENV NEXT_TELEMETRY_DISABLED 1
RUN npm run build

# Production image
FROM base AS runner
WORKDIR /app

ENV NODE_ENV production
ENV NEXT_TELEMETRY_DISABLED 1

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

EXPOSE 3000

ENV PORT 3000
ENV HOSTNAME "0.0.0.0"

CMD ["node", "server.js"]
```

### 4.3 Backend Service Dockerfile Template

```dockerfile
# Example: auth-service/Dockerfile
FROM eclipse-temurin:21-jre-alpine AS builder
WORKDIR /app
COPY target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy layers
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8081

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

### 4.4 Environment Variables (.env)

```bash
# .env file (DO NOT COMMIT TO GIT)

# Database
DB_PASSWORD=your_secure_database_password

# JWT Secret (generate random 256-bit key)
JWT_SECRET=your_jwt_secret_key_minimum_32_characters

# RabbitMQ
RABBITMQ_PASSWORD=your_rabbitmq_password

# Twilio (SMS)
TWILIO_ACCOUNT_SID=your_twilio_account_sid
TWILIO_AUTH_TOKEN=your_twilio_auth_token

# SendGrid (Email)
SENDGRID_API_KEY=your_sendgrid_api_key

# Application
APP_ENV=production
```

---

## 5. Microservices Details

### 5.1 Auth Service (Port 8081)

**Purpose:** User authentication, JWT token management

**Endpoints:**
```
POST   /api/auth/register       - Register new teacher
POST   /api/auth/login          - Login and get JWT
POST   /api/auth/refresh        - Refresh JWT token
POST   /api/auth/logout         - Logout (invalidate token)
POST   /api/auth/forgot-password - Request password reset
POST   /api/auth/reset-password  - Reset password with token
GET    /api/auth/me             - Get current user info
PUT    /api/auth/change-password - Change password
```

**Database:** auth_db
- users table
- refresh_tokens table
- password_reset_tokens table

**Dependencies:** PostgreSQL, Redis, Eureka

---

### 5.2 Student Service (Port 8082)

**Purpose:** Student and class management

**Endpoints:**
```
# Students
GET    /api/students                  - List students (by class)
GET    /api/students/{id}             - Get student details
POST   /api/students                  - Create student
PUT    /api/students/{id}             - Update student
DELETE /api/students/{id}             - Delete student (soft)
POST   /api/students/import           - Bulk import from CSV
GET    /api/students/class/{classId}  - Get students by class

# Student Enrollments (Class Changes)
GET    /api/students/{id}/current-class        - Get student's current class
GET    /api/students/{id}/enrollment-history   - Get complete enrollment history
POST   /api/students/{id}/enroll               - Enroll student in a class
POST   /api/students/{id}/transfer             - Transfer student to new class
GET    /api/students/{id}/class-on-date        - Get class student was in on specific date

# Classes
GET    /api/classes                   - List teacher's classes
GET    /api/classes/{id}              - Get class details
GET    /api/classes/{id}/roster       - Get current class roster
GET    /api/classes/{id}/history      - Get class enrollment history
POST   /api/classes                   - Create class
PUT    /api/classes/{id}              - Update class
DELETE /api/classes/{id}              - Archive class

# Schools
GET    /api/schools                   - List schools
GET    /api/schools/{id}              - Get school details
```

**Database:** student_db
- schools table
- classes table
- students table
- student_class_enrollments table (enrollment history)

**Dependencies:** PostgreSQL, Redis, Eureka, S3/MinIO (photos)

---

### 5.3 Attendance Service (Port 8083)

**Purpose:** Daily attendance tracking and reporting

**Endpoints:**
```
POST   /api/attendance/bulk                     - Mark attendance for class
GET    /api/attendance/class/{id}/date/{date}   - Get attendance by date
GET    /api/attendance/student/{id}             - Get student attendance history
GET    /api/attendance/student/{id}/summary     - Get attendance statistics
PUT    /api/attendance/{id}                     - Update attendance record
GET    /api/attendance/report/monthly/{classId} - Monthly attendance report
GET    /api/attendance/report/summary/{classId} - Class attendance summary
GET    /api/attendance/at-risk/{classId}        - List at-risk students
```

**Database:** attendance_db
- attendance_records table

**Dependencies:** PostgreSQL, RabbitMQ, Eureka

**Events Published:**
- `attendance.marked` → triggers notifications

---

### 5.4 Grade Service (Port 8084)

**Purpose:** Grade entry, calculations, and analytics

**Endpoints:**
```
# Grade Entry
POST   /api/grades                                    - Add single grade
POST   /api/grades/bulk                               - Bulk grade entry
PUT    /api/grades/{id}                               - Update grade
DELETE /api/grades/{id}                               - Delete grade

# Monthly Exams
POST   /api/grades/monthly/{classId}/{subjectId}      - Enter monthly exam grades for class
GET    /api/grades/monthly/{studentId}/semester/{sem} - Get all monthly exam scores
GET    /api/grades/monthly/summary/{classId}          - Monthly exam summary for class

# Semester Exams
POST   /api/grades/semester/{classId}/{subjectId}     - Enter semester exam grades
GET    /api/grades/semester/{studentId}               - Get semester exam scores

# Student Grades
GET    /api/grades/student/{id}/semester/{sem}        - Get all grades for semester
GET    /api/grades/student/{id}/subject/{subjectId}   - Get grades for specific subject
GET    /api/grades/student/{id}/year/{year}           - Get all grades for academic year

# Calculations & Analytics
GET    /api/grades/calculate/{studentId}/semester/{sem}     - Calculate semester averages
GET    /api/grades/calculate/{studentId}/annual             - Calculate annual average
GET    /api/grades/rankings/{classId}/semester/{sem}        - Get class rankings
GET    /api/grades/rankings/{classId}/subject/{subjectId}   - Get subject rankings

# Configuration (Teacher Customization)
GET    /api/grades/config/{classId}                   - Get grading configuration
PUT    /api/grades/config/{classId}                   - Update grading configuration
POST   /api/grades/config/{classId}/reset             - Reset to default (4 monthly exams)

# Assessment Types
GET    /api/assessment-types                          - List assessment types
GET    /api/assessment-types/grade/{grade}            - Get for specific grade level

# Subjects
GET    /api/subjects                                  - List all subjects
GET    /api/subjects/grade/{grade}                    - Get subjects for grade level
```

**Database:** grade_db
- subjects table
- assessment_types table (monthly, semester)
- teacher_assessment_config table (customization)
- grades table (actual scores)
- grade_averages table (cached calculations)

**Calculation Logic:**
- Monthly Average = Average of all monthly exam scores (default: 4 exams)
- Semester Average = (Monthly Average + Semester Exam) / 2
- Annual Average = (Semester 1 Average + Semester 2 Average) / 2

**Dependencies:** PostgreSQL, RabbitMQ, Eureka

**Events Published:**
- `grade.entered` → triggers notifications

---

### 5.5 Report Service (Port 8085)

**Purpose:** PDF report generation

**Endpoints:**
```
# Progress Reports (Monthly)
POST   /api/reports/monthly/{studentId}/month/{month}    - Generate monthly progress report
POST   /api/reports/monthly/bulk/{classId}/month/{month} - Bulk monthly reports for class

# Semester Report Cards
POST   /api/reports/semester/{studentId}/semester/{sem}  - Generate semester report card
POST   /api/reports/semester/bulk/{classId}/sem/{sem}    - Bulk semester reports for class

# Annual Transcripts
POST   /api/reports/annual/{studentId}/year/{year}       - Generate annual transcript
POST   /api/reports/annual/bulk/{classId}/year/{year}    - Bulk annual transcripts for class

# Downloads
GET    /api/reports/{id}/download                        - Download generated report
GET    /api/reports/student/{studentId}/history          - Get all reports for student

# Summary Reports (for teachers)
POST   /api/reports/class-summary/{classId}/semester/{sem} - Class performance summary
POST   /api/reports/subject-analysis/{classId}             - Subject-wise analysis

# Report Templates (admin/teacher customization)
GET    /api/reports/templates                            - List report templates
GET    /api/reports/templates/{id}                       - Get template details
```

**Report Types:**

1. **Monthly Progress Report** (10-12 per year)
   - Current month's exam scores
   - All subjects
   - Monthly average
   - Teacher comments (optional)
   - Attendance summary

2. **Semester Report Card** (2 per year)
   - All monthly exam scores
   - Semester exam score
   - Calculated averages per subject
   - Overall semester average
   - Class rank
   - Attendance statistics
   - Teacher and principal signatures

3. **Annual Transcript** (1 per year)
   - Both semester averages
   - Annual average per subject
   - Overall annual average
   - Final class rank
   - Promotion status
   - Complete attendance record

**Database:** report_db
- report_templates table
- generated_reports table

**Dependencies:** PostgreSQL, Eureka, JasperReports, Calls other services (Student, Attendance, Grade) via Feign clients

---

### 5.6 Notification Service (Port 8086)

**Purpose:** SMS and email notifications

**Endpoints:**
```
POST   /api/notifications/sms     - Send SMS
POST   /api/notifications/email   - Send email
GET    /api/notifications/history - Get notification history
```

**Database:** notification_db
- notifications table
- sms_logs table

**Dependencies:** PostgreSQL, RabbitMQ, Twilio (SMS), SendGrid (Email), Eureka

**Events Consumed:**
- `attendance.marked` → Send absence alerts
- `grade.entered` → Send grade notifications

---

## 6. Database Schema Overview

### 6.1 auth_db Schema

```sql
-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    profile_photo_url VARCHAR(500),
    role VARCHAR(50) NOT NULL DEFAULT 'TEACHER',
    school_id UUID,
    preferred_language VARCHAR(2) DEFAULT 'km',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Refresh tokens table
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
```

### 6.2 student_db Schema

```sql
-- Schools table
CREATE TABLE schools (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    name_km VARCHAR(255),
    address VARCHAR(500),
    province VARCHAR(100),
    district VARCHAR(100),
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Classes table
CREATE TABLE classes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id UUID NOT NULL REFERENCES schools(id),
    teacher_id UUID NOT NULL,
    grade INTEGER NOT NULL CHECK (grade BETWEEN 1 AND 12),
    section VARCHAR(10) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    max_capacity INTEGER,
    student_count INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(school_id, grade, section, academic_year)
);

-- Students table
CREATE TABLE students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_code VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    first_name_km VARCHAR(100),
    last_name_km VARCHAR(100),
    date_of_birth DATE NOT NULL,
    gender VARCHAR(1) NOT NULL CHECK (gender IN ('M', 'F')),
    photo_url VARCHAR(500),
    parent_name VARCHAR(255),
    parent_phone VARCHAR(20),
    emergency_contact VARCHAR(20),
    address VARCHAR(500),
    enrollment_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Student class enrollments (tracks complete enrollment history)
CREATE TABLE student_class_enrollments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    class_id UUID NOT NULL REFERENCES classes(id) ON DELETE RESTRICT,
    enrollment_date DATE NOT NULL,
    end_date DATE NULL,  -- NULL means currently enrolled
    reason VARCHAR(50) NOT NULL DEFAULT 'NEW' CHECK (
        reason IN ('NEW', 'TRANSFER', 'PROMOTION', 'DEMOTION', 'CORRECTION')
    ),
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (end_date IS NULL OR end_date >= enrollment_date)
);

-- Indexes
CREATE INDEX idx_classes_teacher ON classes(teacher_id);
CREATE INDEX idx_classes_school ON classes(school_id);
CREATE INDEX idx_students_code ON students(student_code);
CREATE INDEX idx_enrollment_student ON student_class_enrollments(student_id);
CREATE INDEX idx_enrollment_class ON student_class_enrollments(class_id);
CREATE INDEX idx_enrollment_dates ON student_class_enrollments(enrollment_date, end_date);
CREATE INDEX idx_enrollment_current ON student_class_enrollments(student_id, end_date) WHERE end_date IS NULL;
CREATE UNIQUE INDEX idx_enrollment_student_active ON student_class_enrollments(student_id) WHERE end_date IS NULL;

-- Utility functions
CREATE OR REPLACE FUNCTION get_student_current_class(p_student_id UUID)
RETURNS UUID AS $$
    SELECT class_id FROM student_class_enrollments
    WHERE student_id = p_student_id AND end_date IS NULL
    LIMIT 1;
$$ LANGUAGE sql STABLE;

-- View: Current class roster
CREATE VIEW v_current_class_roster AS
SELECT 
    c.id as class_id, c.grade, c.section, c.academic_year,
    s.id as student_id, s.student_code, s.first_name, s.last_name,
    sce.enrollment_date
FROM student_class_enrollments sce
JOIN students s ON sce.student_id = s.id
JOIN classes c ON sce.class_id = c.id
WHERE sce.end_date IS NULL AND s.status = 'ACTIVE'
ORDER BY c.grade, c.section, s.last_name;
```

### 6.3 attendance_db Schema

```sql
-- Attendance records table
CREATE TABLE attendance_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    class_id UUID NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PRESENT', 'ABSENT', 'LATE', 'SICK', 'EXCUSED')),
    note VARCHAR(500),
    marked_by UUID NOT NULL,
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(student_id, date)
);

-- Indexes
CREATE INDEX idx_attendance_student_date ON attendance_records(student_id, date);
CREATE INDEX idx_attendance_class_date ON attendance_records(class_id, date);
CREATE INDEX idx_attendance_date ON attendance_records(date);
CREATE INDEX idx_attendance_status ON attendance_records(status);
```

### 6.4 grade_db Schema

```sql
-- Subjects table
CREATE TABLE subjects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    name_km VARCHAR(100) NOT NULL,
    grade_level INTEGER,
    is_compulsory BOOLEAN DEFAULT TRUE,
    display_order INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Assessment types table (configurable templates)
CREATE TABLE assessment_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    name_km VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL CHECK (category IN ('MONTHLY', 'SEMESTER', 'ANNUAL')),
    grade_level INTEGER,
    display_order INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Teacher assessment configuration (per class/semester)
CREATE TABLE teacher_assessment_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID NOT NULL,
    class_id UUID NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    semester VARCHAR(10) NOT NULL,  -- 'SEMESTER_1', 'SEMESTER_2'
    assessment_type_id UUID NOT NULL REFERENCES assessment_types(id),
    assessment_number INTEGER,  -- For monthly: 1, 2, 3, 4 (or more if customized)
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(teacher_id, class_id, academic_year, semester, assessment_type_id, assessment_number)
);

-- Grades table (actual student scores)
CREATE TABLE grades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    class_id UUID NOT NULL,
    subject_id UUID NOT NULL REFERENCES subjects(id),
    assessment_type_id UUID NOT NULL REFERENCES assessment_types(id),
    
    -- Time tracking
    academic_year VARCHAR(20) NOT NULL,
    semester VARCHAR(10) NOT NULL,  -- 'SEMESTER_1', 'SEMESTER_2'
    assessment_number INTEGER,      -- For monthly exams: 1, 2, 3, 4
    assessment_date DATE,            -- When exam was taken
    
    -- Score data
    score DECIMAL(5,2) NOT NULL,
    max_score DECIMAL(5,2) NOT NULL DEFAULT 100,
    percentage DECIMAL(5,2) GENERATED ALWAYS AS ((score / max_score) * 100) STORED,
    
    -- Metadata
    note VARCHAR(500),
    entered_by UUID NOT NULL,
    entered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(student_id, subject_id, assessment_type_id, academic_year, semester, assessment_number)
);

-- Computed averages (cached for performance)
CREATE TABLE grade_averages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    class_id UUID NOT NULL,
    subject_id UUID NOT NULL REFERENCES subjects(id),
    
    -- Period
    academic_year VARCHAR(20) NOT NULL,
    semester VARCHAR(10),  -- NULL for annual average
    
    -- Calculated values
    monthly_average DECIMAL(5,2),    -- Average of monthly exams
    semester_exam_score DECIMAL(5,2), -- Semester exam score
    overall_average DECIMAL(5,2),     -- Final average (monthly + semester)
    letter_grade VARCHAR(2),          -- A, B, C, D, E, F
    rank INTEGER,                     -- Rank in class for this subject
    
    -- Metadata
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(student_id, subject_id, academic_year, semester)
);

-- Insert default assessment types
INSERT INTO assessment_types (code, name, name_km, category, display_order) VALUES
('monthly_exam', 'Monthly Exam', 'ប្រឡងប្រចាំខែ', 'MONTHLY', 1),
('semester_exam', 'Semester Exam', 'ប្រឡងឆមាស', 'SEMESTER', 2);

-- Indexes for performance
CREATE INDEX idx_grades_student ON grades(student_id);
CREATE INDEX idx_grades_class_subject ON grades(class_id, subject_id);
CREATE INDEX idx_grades_period ON grades(academic_year, semester);
CREATE INDEX idx_grades_assessment ON grades(assessment_type_id, assessment_number);
CREATE INDEX idx_averages_student_period ON grade_averages(student_id, academic_year, semester);
CREATE INDEX idx_averages_class_rank ON grade_averages(class_id, subject_id, rank);

-- View: Current semester grades
CREATE VIEW v_current_semester_grades AS
SELECT 
    g.student_id,
    s.student_code,
    s.first_name || ' ' || s.last_name as student_name,
    g.class_id,
    g.subject_id,
    sub.name as subject_name,
    g.academic_year,
    g.semester,
    at.name as assessment_name,
    g.assessment_number,
    g.score,
    g.max_score,
    g.percentage
FROM grades g
JOIN students s ON g.student_id = s.id
JOIN subjects sub ON g.subject_id = sub.id
JOIN assessment_types at ON g.assessment_type_id = at.id
ORDER BY g.student_id, g.subject_id, at.display_order, g.assessment_number;

-- Function: Calculate student's subject average
CREATE OR REPLACE FUNCTION calculate_subject_average(
    p_student_id UUID,
    p_subject_id UUID,
    p_academic_year VARCHAR,
    p_semester VARCHAR
)
RETURNS TABLE (
    monthly_avg DECIMAL(5,2),
    semester_score DECIMAL(5,2),
    overall_avg DECIMAL(5,2)
) AS $$
BEGIN
    RETURN QUERY
    WITH monthly_exams AS (
        SELECT AVG(percentage) as avg_monthly
        FROM grades
        WHERE student_id = p_student_id
          AND subject_id = p_subject_id
          AND academic_year = p_academic_year
          AND semester = p_semester
          AND assessment_type_id = (SELECT id FROM assessment_types WHERE code = 'monthly_exam')
    ),
    semester_exam AS (
        SELECT percentage as semester_score
        FROM grades
        WHERE student_id = p_student_id
          AND subject_id = p_subject_id
          AND academic_year = p_academic_year
          AND semester = p_semester
          AND assessment_type_id = (SELECT id FROM assessment_types WHERE code = 'semester_exam')
        LIMIT 1
    )
    SELECT 
        COALESCE(m.avg_monthly, 0) as monthly_avg,
        COALESCE(s.semester_score, 0) as semester_score,
        -- Overall average: average of all assessments
        CASE 
            WHEN s.semester_score IS NOT NULL AND m.avg_monthly IS NOT NULL 
            THEN (m.avg_monthly + s.semester_score) / 2
            WHEN m.avg_monthly IS NOT NULL THEN m.avg_monthly
            WHEN s.semester_score IS NOT NULL THEN s.semester_score
            ELSE 0
        END as overall_avg
    FROM monthly_exams m
    FULL OUTER JOIN semester_exam s ON true;
END;
$$ LANGUAGE plpgsql STABLE;

-- Function: Get letter grade from percentage
CREATE OR REPLACE FUNCTION get_letter_grade(p_percentage DECIMAL)
RETURNS VARCHAR(2) AS $$
BEGIN
    RETURN CASE
        WHEN p_percentage >= 85 THEN 'A'
        WHEN p_percentage >= 70 THEN 'B'
        WHEN p_percentage >= 55 THEN 'C'
        WHEN p_percentage >= 40 THEN 'D'
        WHEN p_percentage >= 25 THEN 'E'
        ELSE 'F'
    END;
END;
$$ LANGUAGE plpgsql IMMUTABLE;
```

---

### 6.5 Handling Student Class Changes

**Design Approach:** Enrollment History Table

The system uses a dedicated `student_class_enrollments` table to track complete enrollment history:

**Key Features:**
- ✅ Complete audit trail of all class changes
- ✅ Can query student's class on any historical date
- ✅ Supports multiple transfer types (NEW, TRANSFER, PROMOTION, etc.)
- ✅ No data loss when students change classes
- ✅ Accurate historical reporting

**Example Scenarios:**

**1. New Student Enrollment:**
```sql
INSERT INTO student_class_enrollments (student_id, class_id, enrollment_date, reason)
VALUES ('student-123', 'class-5a', '2024-11-01', 'NEW');
```

**2. Student Transfer:**
```sql
-- End current enrollment
UPDATE student_class_enrollments
SET end_date = '2025-02-28'
WHERE student_id = 'student-123' AND end_date IS NULL;

-- Create new enrollment
INSERT INTO student_class_enrollments (student_id, class_id, enrollment_date, reason)
VALUES ('student-123', 'class-5b', '2025-03-01', 'TRANSFER');
```

**3. Get Current Class:**
```sql
SELECT class_id FROM student_class_enrollments
WHERE student_id = 'student-123' AND end_date IS NULL;
```

**4. Get Class on Specific Date:**
```sql
SELECT class_id FROM student_class_enrollments
WHERE student_id = 'student-123'
  AND enrollment_date <= '2025-01-15'
  AND (end_date IS NULL OR end_date >= '2025-01-15');
```

**Benefits:**
- Attendance records remain historically accurate (show correct class at time)
- Reports can be generated for any time period
- Transfer patterns can be analyzed
- No confusion about which class a student "was" vs "is" in

---

### 6.4.1 Grading Calculation Logic

**Default Structure (per semester):**
- 4 Monthly Exams (customizable: teacher can set 2-6)
- 1 Semester Exam

**Calculation Method:**
```
Student: Sokha Chan
Subject: Mathematics
Semester 1, 2024-2025

Monthly Exams:
- Month 1: 75/100 = 75%
- Month 2: 80/100 = 80%
- Month 3: 70/100 = 70%
- Month 4: 85/100 = 85%

Monthly Average = (75 + 80 + 70 + 85) / 4 = 77.5%

Semester Exam: 82/100 = 82%

Semester Average = (77.5 + 82) / 2 = 79.75%
Letter Grade = B (70-84 range)
```

**Annual Calculation:**
```
Semester 1 Average: 79.75%
Semester 2 Average: 83.25%

Annual Average = (79.75 + 83.25) / 2 = 81.5%
Final Letter Grade = B
```

**Letter Grade Scale (MoEYS Standard):**
- A: 85-100%
- B: 70-84%
- C: 55-69%
- D: 40-54%
- E: 25-39%
- F: 0-24%

**Teacher Customization:**

Teachers can configure number of monthly exams per semester:

```sql
-- Default: 4 monthly exams
INSERT INTO teacher_assessment_config 
VALUES (teacher_id, class_id, '2024-2025', 'SEMESTER_1', 'monthly_exam', 1, true);
VALUES (teacher_id, class_id, '2024-2025', 'SEMESTER_1', 'monthly_exam', 2, true);
VALUES (teacher_id, class_id, '2024-2025', 'SEMESTER_1', 'monthly_exam', 3, true);
VALUES (teacher_id, class_id, '2024-2025', 'SEMESTER_1', 'monthly_exam', 4, true);

-- Teacher wants only 3 monthly exams (removes month 4)
UPDATE teacher_assessment_config 
SET is_active = false
WHERE assessment_number = 4;
```

**Report Generation:**

**1. Monthly Progress Report:**
```
Student: Sokha Chan
Month: January 2025 (Month 1)

Mathematics: 75% (C)
Khmer: 82% (B)
English: 68% (C)
Science: 80% (B)
...

Monthly Average: 76.25% (C)
```

**2. Semester Report Card:**
```
Student: Sokha Chan
Semester 1, 2024-2025

Subject         | Monthly Avg | Semester Exam | Final | Grade
----------------|-------------|---------------|-------|------
Mathematics     | 77.5%       | 82%          | 79.8% | B
Khmer          | 80.0%       | 85%          | 82.5% | B
English        | 72.5%       | 75%          | 73.8% | B
Science        | 83.0%       | 88%          | 85.5% | A
Social Studies | 75.0%       | 80%          | 77.5% | B

Overall Average: 79.8% (B)
Class Rank: 12/35
```

**3. Annual Transcript:**
```
Student: Sokha Chan
Academic Year: 2024-2025

Subject         | Sem 1 | Sem 2 | Annual | Grade
----------------|-------|-------|--------|------
Mathematics     | 79.8% | 83.2% | 81.5%  | B
Khmer          | 82.5% | 85.0% | 83.8%  | B
English        | 73.8% | 78.5% | 76.2%  | B
Science        | 85.5% | 87.0% | 86.3%  | A
Social Studies | 77.5% | 80.0% | 78.8%  | B

Annual Average: 81.3% (B)
Final Class Rank: 10/35
Result: PROMOTED to Grade 6
```

---

## 7. API Gateway & Routing

### 7.1 Route Configuration

```yaml
# api-gateway/src/main/resources/application-docker.yml
spring:
  cloud:
    gateway:
      routes:
        # Auth Service
        - id: auth-service
          uri: lb://AUTH-SERVICE
          predicates:
            - Path=/api/auth/**
          filters:
            - RewritePath=/api/auth/(?<segment>.*), /${segment}
            - name: RateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
        
        # Student Service
        - id: student-service
          uri: lb://STUDENT-SERVICE
          predicates:
            - Path=/api/students/**,/api/classes/**,/api/schools/**
          filters:
            - JwtAuthenticationFilter
            
        # Attendance Service
        - id: attendance-service
          uri: lb://ATTENDANCE-SERVICE
          predicates:
            - Path=/api/attendance/**
          filters:
            - JwtAuthenticationFilter
            
        # Grade Service
        - id: grade-service
          uri: lb://GRADE-SERVICE
          predicates:
            - Path=/api/grades/**,/api/subjects/**,/api/assessment-types/**
          filters:
            - JwtAuthenticationFilter
            
        # Report Service
        - id: report-service
          uri: lb://REPORT-SERVICE
          predicates:
            - Path=/api/reports/**
          filters:
            - JwtAuthenticationFilter
            
        # Notification Service
        - id: notification-service
          uri: lb://NOTIFICATION-SERVICE
          predicates:
            - Path=/api/notifications/**
          filters:
            - JwtAuthenticationFilter
```

### 7.2 Nginx Configuration

```nginx
# nginx/nginx.conf
events {
    worker_connections 1024;
}

http {
    upstream frontend {
        server frontend:3000;
    }

    upstream api_gateway {
        server api-gateway:8080;
    }

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;

    server {
        listen 80;
        server_name sms.com.kh www.sms.com.kh;

        # Redirect HTTP to HTTPS
        return 301 https://$server_name$request_uri;
    }

    server {
        listen 443 ssl http2;
        server_name sms.com.kh www.sms.com.kh;

        ssl_certificate /etc/nginx/ssl/fullchain.pem;
        ssl_certificate_key /etc/nginx/ssl/privkey.pem;

        # Frontend (Next.js)
        location / {
            proxy_pass http://frontend;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection 'upgrade';
            proxy_set_header Host $host;
            proxy_cache_bypass $http_upgrade;
        }

        # Backend API
        location /api/ {
            limit_req zone=api_limit burst=20 nodelay;
            
            proxy_pass http://api_gateway;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # Static files (if served by Nginx)
        location /uploads/ {
            alias /app/uploads/;
            expires 30d;
            add_header Cache-Control "public, immutable";
        }
    }
}
```

---

## 8. Security Architecture

### 8.1 Authentication Flow

```
1. User submits credentials to /api/auth/login
2. Auth Service validates credentials
3. If valid, generates JWT access token (24h) and refresh token (30d)
4. Stores refresh token in Redis and database
5. Returns both tokens to client
6. Client stores tokens securely (httpOnly cookies or secure storage)
7. Client includes access token in Authorization header for all API requests
8. API Gateway validates JWT on each request
9. If token expired, client uses refresh token to get new access token
```

### 8.2 JWT Token Structure

```json
{
  "sub": "user-uuid",
  "email": "teacher@school.com",
  "role": "TEACHER",
  "school_id": "school-uuid",
  "iat": 1700000000,
  "exp": 1700086400
}
```

### 8.3 Security Headers

```yaml
# Added by API Gateway
Access-Control-Allow-Origin: https://sms.com.kh
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
Content-Security-Policy: default-src 'self'
```

---

## 9. Deployment Strategy

### 9.1 Deployment Commands

```bash
# Development
docker-compose up -d

# Build and start
docker-compose up --build -d

# View logs
docker-compose logs -f [service-name]

# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: deletes data)
docker-compose down -v

# Scale a service (if needed)
docker-compose up -d --scale student-service=3
```

### 9.2 CI/CD Pipeline (GitHub Actions)

```yaml
# .github/workflows/deploy.yml
name: Deploy to Production

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          
      - name: Build backend services
        run: |
          cd auth-service && ./mvnw clean package -DskipTests
          cd ../student-service && ./mvnw clean package -DskipTests
          # ... repeat for all services
          
      - name: Build frontend
        run: |
          cd frontend
          npm ci
          npm run build
          
      - name: Deploy to server
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            cd /opt/sms
            git pull
            docker-compose down
            docker-compose up --build -d
```

---

## 10. Monitoring & Logging

### 10.1 Health Checks

Each service exposes health check endpoints:

```
GET /actuator/health
```

Add to docker-compose.yml:

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 40s
```

### 10.2 Logging Strategy

**Application Logs:**
- All services log to stdout/stderr
- Docker captures logs automatically
- Use `docker-compose logs` to view

**Log Format (JSON):**
```json
{
  "timestamp": "2025-11-19T10:30:00Z",
  "level": "INFO",
  "service": "student-service",
  "message": "Student created successfully",
  "user_id": "uuid",
  "trace_id": "xyz123"
}
```

**Log Aggregation (Optional):**
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Graylog
- Splunk

### 10.3 Monitoring (Optional)

**Prometheus + Grafana:**
- Add Prometheus exporter to each Spring Boot service
- Scrape metrics from /actuator/prometheus
- Visualize in Grafana dashboards

**Key Metrics:**
- Request rate (requests/sec)
- Response time (p50, p95, p99)
- Error rate (%)
- Database connection pool usage
- Memory and CPU usage

---

## 11. Grading System Summary

### Academic Year Structure

```
Academic Year 2024-2025 (10 months: November - August)
│
├── Semester 1 (5 months: November - March)
│   ├── Monthly Exam 1 (November)
│   ├── Monthly Exam 2 (December)
│   ├── Monthly Exam 3 (January)
│   ├── Monthly Exam 4 (February)
│   └── Semester 1 Final Exam (March)
│
└── Semester 2 (5 months: April - August)
    ├── Monthly Exam 1 (April)
    ├── Monthly Exam 2 (May)
    ├── Monthly Exam 3 (June)
    ├── Monthly Exam 4 (July)
    └── Semester 2 Final Exam (August)
```

### Assessment Configuration

| Assessment Type | Default Count | Customizable | Frequency |
|----------------|---------------|--------------|-----------|
| Monthly Exams | 4 per semester | ✅ Yes (2-6) | Monthly |
| Semester Exams | 1 per semester | ❌ No | End of semester |

### Grade Calculation Formula

```
For each subject:

1. Monthly Average = (Exam1 + Exam2 + Exam3 + Exam4) / 4
2. Semester Average = (Monthly Average + Semester Exam) / 2
3. Annual Average = (Semester 1 Average + Semester 2 Average) / 2
```

### Letter Grades (MoEYS Standard)

| Grade | Percentage | Description |
|-------|-----------|-------------|
| A | 85-100% | Excellent |
| B | 70-84% | Very Good |
| C | 55-69% | Good |
| D | 40-54% | Fair |
| E | 25-39% | Poor |
| F | 0-24% | Fail |

### Report Schedule

| Report Type | Frequency | Purpose |
|------------|-----------|---------|
| Monthly Progress Report | 10 per year | Track ongoing performance |
| Semester Report Card | 2 per year | Comprehensive semester summary |
| Annual Transcript | 1 per year | Full year with promotion status |

### Example Grade Entry Flow

**Teacher enters monthly exam grades:**

```
POST /api/grades/monthly/{classId}/{subjectId}
Body: [
  { studentId: "...", month: 1, score: 75, maxScore: 100 },
  { studentId: "...", month: 1, score: 80, maxScore: 100 },
  ...
]
```

**System automatically:**
1. Stores individual scores in `grades` table
2. Calculates monthly average when all 4 months completed
3. Updates `grade_averages` table
4. Triggers notification to parents (if enabled)

**Teacher generates semester report:**

```
POST /api/reports/semester/{studentId}/semester/SEMESTER_1
```

**System:**
1. Fetches all monthly exam scores
2. Fetches semester exam score
3. Calculates averages
4. Determines class rank
5. Generates PDF report in <30 seconds
6. Returns download link

### Database Example

**grades table:**
```sql
| student_id | subject_id | assessment_type | year      | semester    | month | score | percentage |
|-----------|-----------|-----------------|-----------|-------------|-------|-------|------------|
| student-1 | math-id   | monthly_exam    | 2024-2025 | SEMESTER_1  | 1     | 75    | 75.00      |
| student-1 | math-id   | monthly_exam    | 2024-2025 | SEMESTER_1  | 2     | 80    | 80.00      |
| student-1 | math-id   | monthly_exam    | 2024-2025 | SEMESTER_1  | 3     | 70    | 70.00      |
| student-1 | math-id   | monthly_exam    | 2024-2025 | SEMESTER_1  | 4     | 85    | 85.00      |
| student-1 | math-id   | semester_exam   | 2024-2025 | SEMESTER_1  | NULL  | 82    | 82.00      |
```

**grade_averages table:**
```sql
| student_id | subject_id | year      | semester   | monthly_avg | semester_exam | overall_avg | letter | rank |
|-----------|-----------|-----------|------------|-------------|---------------|-------------|--------|------|
| student-1 | math-id   | 2024-2025 | SEMESTER_1 | 77.50       | 82.00         | 79.75       | B      | 12   |
```

---

## 12. Conclusion

This Docker-based architecture provides:

✅ **Simplified deployment** - Single command to start entire stack  
✅ **Consistent environments** - Same setup from dev to production  
✅ **Easy scaling** - Add more containers as needed  
✅ **Isolation** - Each service runs in its own container  
✅ **Portability** - Run on any server with Docker installed  
✅ **Cost-effective** - No need for Kubernetes complexity initially

**Next Steps:**
1. Set up development environment with Docker Compose
2. Build and test each microservice
3. Implement CI/CD pipeline
4. Deploy to staging for testing
5. Production deployment with monitoring

---

**Document Version:** 1.0  
**Last Updated:** November 19, 2025
