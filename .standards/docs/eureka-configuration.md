# Eureka Service Discovery Configuration Standards

**Version**: 1.0.0
**Last Updated**: 2025-11-22
**Compliance**: MANDATORY for all microservices

---

## Overview

All microservices MUST configure Eureka with **hostname-based registration**. This ensures:

- ✅ **100% successful registration** in Docker environments
- ✅ **No multi-network IP conflicts** (services on multiple Docker networks)
- ✅ **Consistent service discovery** across all deployments
- ✅ **Predictable inter-service communication**

**Critical Rule**: ALWAYS use `prefer-ip-address: false` in Docker/containerized environments.

---

## The Problem with IP-Based Registration

### Why `prefer-ip-address: true` Fails in Docker

**Scenario**: Service on multiple Docker networks

```yaml
networks:
  - backend-network      # IP: 172.18.0.5
  - database-network     # IP: 172.19.0.3
  - cache-network        # IP: 172.20.0.2
```

**With `prefer-ip-address: true`**:
- Eureka registers service with ONE IP address (unpredictable which one)
- Other services try to connect using that IP
- Connection fails if they're not on the same network
- Result: **Service discovery fails**

**With `prefer-ip-address: false` + hostname**:
- Eureka registers service with hostname (`auth-service`)
- Docker's internal DNS resolves hostname to correct IP per network
- All services can connect regardless of network
- Result: **Service discovery works**

---

## Standard Configuration

### Default Profile (Local Development)

**File**: `application.yml`

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true  # OK for local development
```

**Why `true` for local**:
- Running directly on host machine (not in containers)
- No Docker networking complexity
- Simplifies local debugging

---

### Docker Profile (Containerized Deployment)

**File**: `application-docker.yml`

```yaml
eureka:
  instance:
    hostname: {service-name}       # MUST match Docker service name
    prefer-ip-address: false       # MUST be false
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

**Critical Requirements**:
1. ✅ `hostname` MUST match the service name in docker-compose.yml
2. ✅ `prefer-ip-address` MUST be `false`
3. ✅ `defaultZone` MUST use environment variable

**Example for auth-service**:

```yaml
eureka:
  instance:
    hostname: auth-service  # Matches 'auth-service' in docker-compose.yml
    prefer-ip-address: false
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

---

## Docker Compose Configuration

**docker-compose.yml** environment variable:

```yaml
auth-service:
  environment:
    - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
```

**Complete example**:

```yaml
auth-service:
  build:
    context: ./auth-service
    dockerfile: Dockerfile
  container_name: sms-auth-service
  ports:
    - "8081:8081"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    # ... other variables
  networks:
    - backend-network
    - database-network
  depends_on:
    - eureka-server
  restart: unless-stopped
```

---

## Hostname Requirements

### Hostname MUST Match Service Name

**docker-compose.yml**:
```yaml
services:
  auth-service:      # ← Service name
    # ...
```

**application-docker.yml**:
```yaml
eureka:
  instance:
    hostname: auth-service  # ← MUST match service name
```

**Why**: Docker's internal DNS uses service name for hostname resolution.

---

### Naming Conventions

**Hostname Format**: `{service-name}` (lowercase, hyphen-separated)

| Service | Docker Service Name | Eureka Hostname |
|---------|---------------------|-----------------|
| Auth Service | `auth-service` | `auth-service` |
| Student Service | `student-service` | `student-service` |
| Attendance Service | `attendance-service` | `attendance-service` |
| Teacher Service | `teacher-service` | `teacher-service` |

---

## Configuration Do's and Don'ts

### ✅ Correct Configuration (Docker)

```yaml
eureka:
  instance:
    hostname: auth-service
    prefer-ip-address: false
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

**In docker-compose.yml**:
```yaml
environment:
  - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
```

---

### ❌ Incorrect Configurations

**1. Using environment variable for hostname**:
```yaml
# WRONG
eureka:
  instance:
    hostname: ${EUREKA_INSTANCE_HOSTNAME}  # Don't use env var
```

**Why wrong**: Hostname should be hardcoded in YAML (matches service name).

**Correct**:
```yaml
eureka:
  instance:
    hostname: auth-service  # Hardcoded, matches docker-compose service name
```

---

**2. Using `prefer-ip-address: true` in Docker**:
```yaml
# WRONG
eureka:
  instance:
    prefer-ip-address: true  # Causes multi-network issues
```

**Correct**:
```yaml
eureka:
  instance:
    prefer-ip-address: false  # Always false for Docker
```

---

**3. Hardcoding Eureka URL in Docker profile**:
```yaml
# WRONG
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/  # Hardcoded
```

**Correct**:
```yaml
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}  # From env var
```

---

**4. Using wrong environment variable name**:
```yaml
# WRONG (in docker-compose.yml)
environment:
  - EUREKA_CLIENT_SERVICE_URL=http://eureka-server:8761/eureka/
```

**Correct**:
```yaml
environment:
  - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
  # Note: SERVICEURL (no underscore between SERVICE and URL)
```

---

## Validation

### Automated Validation

```bash
.standards/scripts/validate-service-structure.sh <service-name>
```

**Checks**:
- ✅ `application-docker.yml` has `prefer-ip-address: false`
- ✅ `application-docker.yml` has `hostname: {service-name}`
- ✅ docker-compose.yml uses `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`

---

### Manual Validation

**Checklist**:
- [ ] `application-docker.yml` has `prefer-ip-address: false`
- [ ] `application-docker.yml` has `hostname` matching service name
- [ ] `hostname` matches docker-compose.yml service name exactly
- [ ] docker-compose.yml uses `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` env var
- [ ] No Eureka configuration in environment variables (except defaultZone)

---

### Runtime Validation

**Check Eureka Dashboard**:

1. Open http://localhost:8761
2. Look at registered instances
3. Verify hostname registration:

```
Application: AUTH-SERVICE
Instance ID: auth-service:8081
Status: UP
```

**Hostname should be**: `auth-service` (not an IP address)

---

## Troubleshooting

### Service Not Registering with Eureka

**Symptom**: Service starts but doesn't appear in Eureka dashboard

**Check 1**: Verify profile is active
```bash
docker logs sms-auth-service | grep "active profiles"
# Should show: "docker"
```

**Check 2**: Verify Eureka URL
```bash
docker logs sms-auth-service | grep "eureka"
# Should show connection to eureka-server:8761
```

**Check 3**: Verify network connectivity
```bash
docker exec sms-auth-service ping eureka-server
# Should successfully ping eureka-server
```

---

### Service Registered but Can't Be Reached

**Symptom**: Service appears in Eureka but other services can't connect

**Most Common Cause**: `prefer-ip-address: true` in Docker

**Fix**:
1. Set `prefer-ip-address: false` in `application-docker.yml`
2. Set `hostname: {service-name}` in `application-docker.yml`
3. Restart service

---

### Multiple Network Issues

**Symptom**: Service on multiple networks shows unpredictable behavior

**Root Cause**: IP-based registration picks one IP randomly

**Solution**: Hostname-based registration
```yaml
eureka:
  instance:
    hostname: auth-service
    prefer-ip-address: false
```

Docker DNS will resolve the hostname to the correct IP for each network.

---

## Migration Guide

### Migrating to Hostname-Based Registration

**Step 1**: Update `application-docker.yml`

**Before**:
```yaml
eureka:
  instance:
    prefer-ip-address: true  # Remove this
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/  # Hardcoded
```

**After**:
```yaml
eureka:
  instance:
    hostname: student-service       # Add this
    prefer-ip-address: false        # Change to false
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}  # Use env var
```

---

**Step 2**: Update `docker-compose.yml`

**Add environment variable**:
```yaml
student-service:
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
```

---

**Step 3**: Verify service name matches

**docker-compose.yml**:
```yaml
services:
  student-service:  # ← This name
```

**application-docker.yml**:
```yaml
eureka:
  instance:
    hostname: student-service  # ← Must match
```

---

**Step 4**: Test

```bash
docker-compose up student-service
docker logs sms-student-service
```

Look for:
```
Registered application STUDENT-SERVICE with eureka
```

---

## Quick Reference

### Configuration Cheat Sheet

**Default Profile** (local development):
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

**Docker Profile** (containerized):
```yaml
eureka:
  instance:
    hostname: {service-name}
    prefer-ip-address: false
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

**Docker Compose** (environment):
```yaml
environment:
  - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
```

---

## Related Documentation

- **Environment Variables**: `.standards/docs/environment-variables.md` - `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`
- **Profile Strategy**: `.standards/docs/profile-strategy.md` - Default vs Docker profiles
- **Service Template**: `.standards/templates/service-template.md` - Complete example

---

## Version History

| Version | Date       | Changes                            |
|---------|------------|------------------------------------|
| 1.0.0   | 2025-11-22 | Initial Eureka configuration guide |

---

## Support

For questions about Eureka configuration:

1. Check this document for hostname-based registration
2. Review auth-service as reference implementation
3. Verify multi-network services work correctly
4. Consult Eureka dashboard for registration status
