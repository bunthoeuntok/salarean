# Student Management System (SMS)

A comprehensive school management system for Cambodia built with microservices architecture.

## Tech Stack

### Frontend
- **Next.js 14** - React framework with SSR
- **TypeScript** - Type safety
- **TailwindCSS** - Styling
- **React Query** - Data fetching
- **Zustand** - State management

### Backend
- **Spring Boot 3.2** - Microservices framework
- **Java 21** - Programming language
- **PostgreSQL 15** - Database
- **Redis 7** - Caching
- **RabbitMQ 3** - Message broker

### Infrastructure
- **Docker & Docker Compose** - Containerization
- **Nginx** - Reverse proxy
- **Netflix Eureka** - Service discovery

## Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────────┐
│   Nginx     │────▶│  Frontend   │     │   API Gateway   │
│   (80/443)  │     │   (3000)    │     │     (8080)      │
└─────────────┘     └─────────────┘     └────────┬────────┘
                                                 │
        ┌────────────────────────────────────────┼────────────────┐
        │                    │                   │                │
        ▼                    ▼                   ▼                ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ Auth Service │   │Student Service│   │Grade Service │   │Report Service│
│    (8081)    │   │    (8082)     │   │    (8084)    │   │    (8085)    │
└──────────────┘   └──────────────┘   └──────────────┘   └──────────────┘
        │                    │                   │                │
        ▼                    ▼                   ▼                ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│   auth_db    │   │  student_db  │   │   grade_db   │   │  report_db   │
└──────────────┘   └──────────────┘   └──────────────┘   └──────────────┘
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| Frontend | 3000 | Next.js web application |
| API Gateway | 8080 | Request routing, JWT validation |
| Eureka Server | 8761 | Service discovery |
| Auth Service | 8081 | Authentication, JWT tokens |
| Student Service | 8082 | Student & class management |
| Attendance Service | 8083 | Daily attendance tracking |
| Grade Service | 8084 | Grades & calculations |
| Report Service | 8085 | PDF report generation |
| Notification Service | 8086 | SMS & email notifications |

## Quick Start

### Prerequisites

- Docker & Docker Compose
- Node.js 20+ (for frontend development)
- Java 21+ (for backend development)
- Maven 3.9+ (for building backend services)

### 1. Clone and Setup

```bash
cd /Volumes/DATA/my-projects/salarean

# Copy environment file
cp .env.example .env

# Edit .env with your values
# At minimum, set:
# - DB_PASSWORD
# - JWT_SECRET
# - RABBITMQ_PASSWORD
```

### 2. Generate JWT Secret

```bash
openssl rand -base64 32
```

### 3. Start Infrastructure

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### 4. Access Applications

- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **RabbitMQ Management**: http://localhost:15672

## Development

### Frontend Development

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build
```

### Backend Development

```bash
# Build all services
cd eureka-server && ./mvnw clean package -DskipTests
cd ../api-gateway && ./mvnw clean package -DskipTests
cd ../auth-service && ./mvnw clean package -DskipTests
cd ../student-service && ./mvnw clean package -DskipTests
cd ../attendance-service && ./mvnw clean package -DskipTests
cd ../grade-service && ./mvnw clean package -DskipTests
cd ../report-service && ./mvnw clean package -DskipTests
cd ../notification-service && ./mvnw clean package -DskipTests
```

### Running Individual Services

```bash
# Start a specific service
cd auth-service
./mvnw spring-boot:run
```

## API Endpoints

### Authentication
```
POST /api/auth/register     - Register new user
POST /api/auth/login        - Login
POST /api/auth/refresh      - Refresh token
POST /api/auth/logout       - Logout
GET  /api/auth/me           - Get current user
```

### Students
```
GET    /api/students              - List students
POST   /api/students              - Create student
GET    /api/students/{id}         - Get student
PUT    /api/students/{id}         - Update student
DELETE /api/students/{id}         - Delete student
POST   /api/students/{id}/transfer - Transfer student
```

### Grades
```
POST /api/grades/monthly/{classId}/{subjectId}  - Enter monthly grades
POST /api/grades/semester/{classId}/{subjectId} - Enter semester grades
GET  /api/grades/student/{id}/semester/{sem}    - Get student grades
GET  /api/grades/rankings/{classId}             - Get class rankings
```

### Reports
```
POST /api/reports/monthly/{studentId}   - Generate monthly report
POST /api/reports/semester/{studentId}  - Generate semester report
POST /api/reports/annual/{studentId}    - Generate annual transcript
GET  /api/reports/{id}/download         - Download report
```

## Grading System

### Academic Year Structure
- **10 months**: November - August
- **Semester 1**: November - March (5 months)
- **Semester 2**: April - August (5 months)

### Assessment Types
- **4 Monthly Exams** per semester (customizable: 2-6)
- **1 Semester Exam** per semester

### Grade Calculation
```
Monthly Average = (Exam1 + Exam2 + Exam3 + Exam4) / 4
Semester Average = (Monthly Average + Semester Exam) / 2
Annual Average = (Semester 1 + Semester 2) / 2
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

## Environment Variables

```bash
# Database
DB_PASSWORD=your_secure_password

# JWT (generate with: openssl rand -base64 32)
JWT_SECRET=your_256_bit_secret_key

# RabbitMQ
RABBITMQ_PASSWORD=your_rabbitmq_password

# Twilio (optional)
TWILIO_ACCOUNT_SID=your_sid
TWILIO_AUTH_TOKEN=your_token

# SendGrid (optional)
SENDGRID_API_KEY=your_api_key
```

## Deployment

### Production Checklist

1. **Security**
   - Update all passwords in `.env`
   - Enable HTTPS in Nginx
   - Configure firewall rules

2. **Database**
   - Set up automated backups
   - Configure connection pooling

3. **Monitoring**
   - Enable health checks
   - Set up log aggregation
   - Configure alerting

### Deploy to Server

```bash
# Build and deploy
docker-compose -f docker-compose.yml up --build -d

# Scale services if needed
docker-compose up -d --scale student-service=3
```

## Documentation

See the `/requirements` folder for detailed documentation:
- `SMS_PRD_Executive_Summary.md` - Product requirements
- `SMS_Technical_Architecture.md` - Technical specifications

## License

MIT License - see LICENSE file for details.

## Support

For issues and feature requests, please open an issue on GitHub.
