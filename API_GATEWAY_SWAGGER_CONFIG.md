# API Gateway - Swagger UI Configuration

## Overview

The API Gateway has been configured to aggregate OpenAPI documentation from all microservices, providing a unified Swagger UI interface for the entire Student Management System.

## Configuration Summary

### Services Configured

1. **auth-service** - Authentication and authorization endpoints
2. **student-service** - Student management endpoints (NEW)

### Access Points

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI Config**: http://localhost:8080/v3/api-docs/swagger-config

### Gateway Routes Added

#### Student Service OpenAPI Route
```yaml
- id: student-service-openapi
  uri: lb://STUDENT-SERVICE
  predicates:
    - Path=/student-service/v3/api-docs/**
  filters:
    - RewritePath=/student-service/v3/api-docs/(?<segment>.*), /v3/api-docs/${segment}
```

#### Swagger UI Configuration
```yaml
springdoc:
  swagger-ui:
    urls:
      - name: auth-service
        url: /auth-service/v3/api-docs
      - name: student-service
        url: /student-service/v3/api-docs
  api-docs:
    enabled: true
```

## How to Use

### Accessing Swagger UI

1. Navigate to http://localhost:8080/swagger-ui/index.html
2. In the top-right dropdown, select the service you want to explore:
   - `auth-service` - Authentication APIs
   - `student-service` - Student Management APIs
3. Click "Authorize" to add your JWT token
4. Test endpoints directly from the Swagger UI

### Getting a JWT Token

```bash
# Login via auth-service
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teacher@school.com",
    "password": "password"
  }'

# Extract the accessToken from the response
```

### Testing Student API via Gateway

```bash
# Get students (requires authentication)
curl http://localhost:8080/api/students \
  -H "Authorization: Bearer {your-jwt-token}"

# Create a student
curl -X POST http://localhost:8080/api/students \
  -H "Authorization: Bearer {your-jwt-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Sokha",
    "lastName": "Kim",
    "dateOfBirth": "2010-01-15",
    "gender": "M",
    "address": "Phnom Penh",
    "emergencyContact": "+855999888777",
    "enrollmentDate": "2025-01-10",
    "parentContacts": [{
      "fullName": "Dara Kim",
      "phoneNumber": "+855123456789",
      "relationship": "MOTHER",
      "isPrimary": true
    }]
  }'
```

## Student Service Endpoints via Gateway

All student-service endpoints are accessible through the API Gateway with the `/api/students/**` prefix:

- `POST /api/students` - Create a new student
- `GET /api/students/{id}` - Get student by ID
- `GET /api/students/code/{code}` - Get student by code
- `PUT /api/students/{id}` - Update student
- `DELETE /api/students/{id}` - Soft delete student
- `GET /api/students/class/{classId}` - List students by class
- `GET /api/students` - List all students (paginated)
- `GET /api/students/search` - Search students
- `POST /api/students/{id}/photo` - Upload student photo

### Additional Routes via Gateway

- `/api/classes/**` - Class management endpoints
- `/api/schools/**` - School management endpoints

## Gateway Configuration Details

### Service Discovery

The gateway uses Eureka for service discovery:
- Services register with Eureka at: http://localhost:8761
- Gateway discovers services automatically via Eureka
- Load balancing is handled by Spring Cloud LoadBalancer

### Authentication Flow

```
Client Request
    ↓
API Gateway (port 8080)
    ↓
JWT Authentication Filter (validates token)
    ↓
Student Service (port 8082)
    ↓
Response
```

### Route Priority

Routes are evaluated in order. The OpenAPI docs routes are defined before the API routes to ensure proper routing:

1. `auth-service-openapi` - /auth-service/v3/api-docs/**
2. `student-service-openapi` - /student-service/v3/api-docs/** (NEW)
3. `auth-service` - /api/auth/**
4. `student-service` - /api/students/**, /api/classes/**, /api/schools/**
5. Other services...

## Files Modified

- `/api-gateway/src/main/resources/application.yml` - Added student-service routes and Swagger UI configuration

## Deployment

### Rebuild and Restart

```bash
# Rebuild the API Gateway
docker-compose build api-gateway

# Restart the service
docker-compose up -d api-gateway

# Verify it's running
curl http://localhost:8080/actuator/health
```

### Verification

```bash
# Check Swagger config includes student-service
curl http://localhost:8080/v3/api-docs/swagger-config

# Expected output:
{
  "urls": [
    {"url": "/auth-service/v3/api-docs", "name": "auth-service"},
    {"url": "/student-service/v3/api-docs", "name": "student-service"}
  ]
}
```

## Adding More Services

To add additional microservices to the Swagger UI aggregation:

1. Add an OpenAPI route:
```yaml
- id: your-service-openapi
  uri: lb://YOUR-SERVICE
  predicates:
    - Path=/your-service/v3/api-docs/**
  filters:
    - RewritePath=/your-service/v3/api-docs/(?<segment>.*), /v3/api-docs/${segment}
```

2. Add the service to Swagger UI URLs:
```yaml
springdoc:
  swagger-ui:
    urls:
      - name: auth-service
        url: /auth-service/v3/api-docs
      - name: student-service
        url: /student-service/v3/api-docs
      - name: your-service
        url: /your-service/v3/api-docs
```

3. Rebuild and restart the API Gateway

## Known Issues

### OpenAPI Generation Error in Student Service (✅ RESOLVED)

**Previous Issue**: The student-service's `/v3/api-docs` endpoint had a version compatibility issue between springdoc-openapi 2.3.0 and Spring Boot 3.5.7, causing a `NoSuchMethodError` with ControllerAdviceBean.

**Status**: ✅ **FULLY RESOLVED**

**Fix Applied**:
1. Upgraded springdoc-openapi-starter-webmvc-ui from 2.3.0 to 2.7.0 in student-service/pom.xml
2. Added Eureka instance configuration to student-service/application-prod.yml for correct IP registration
3. Restarted API Gateway to clear cached Eureka service discovery data

**Current Status**:
- ✅ All student-service API endpoints work perfectly via gateway
- ✅ Student-service OpenAPI docs generation working (`/v3/api-docs`)
- ✅ Student-service visible and accessible in Swagger UI dropdown
- ✅ API Gateway successfully aggregates docs from both auth-service and student-service

**Resolution Date**: 2025-11-22

## Monitoring

### Gateway Health

```bash
# Check gateway health
curl http://localhost:8080/actuator/health

# View registered routes
curl http://localhost:8080/actuator/gateway/routes
```

### Service Registration

```bash
# Check Eureka dashboard
open http://localhost:8761

# You should see:
# - API-GATEWAY
# - AUTH-SERVICE
# - STUDENT-SERVICE
```

## Security Notes

- All student-service endpoints require JWT authentication (role: TEACHER)
- Authentication is handled by the JwtAuthenticationFilter in the gateway
- OpenAPI documentation endpoints are public (no authentication required)
- Health and actuator endpoints are public

## Version Information

- **Spring Cloud Gateway**: 4.3.0 (Spring Cloud 2025.0.0)
- **Spring Boot**: 3.5.7
- **springdoc-openapi**: 2.7.0 (webflux-ui)
- **Configuration Date**: 2025-11-22

---

**Status**: ✅ Configured and Deployed
**Last Updated**: 2025-11-22
