# Service Migration Notes

**Version**: 1.0.0
**Last Updated**: 2025-11-22
**Purpose**: Document configuration deviations in existing services and migration strategy

---

## Overview

This document tracks configuration deviations from architectural standards in existing services. It serves as:

- ✅ **Migration Roadmap**: Prioritized list of configuration updates needed
- ✅ **Technical Debt Tracker**: Known non-compliance issues
- ✅ **Migration Guide**: Step-by-step instructions to bring services into compliance

**Migration Strategy**: Standardize incrementally to avoid disruption. Focus on new services first, migrate existing services during maintenance windows.

---

## Service Status Summary

| Service | Status | Compliance Rate | Priority | Target Date |
|---------|--------|-----------------|----------|-------------|
| **auth-service** | ✅ Compliant | 95-100% | - | Complete |
| **student-service** | ❌ Non-Compliant | ~40% | P2 - Deferred | After core features |
| **api-gateway** | ⚠️ Unknown | TBD | P3 | TBD |
| **eureka-server** | ⚠️ Unknown | TBD | P3 | TBD |

---

## student-service: Configuration Deviations

**Service**: student-service
**Current Status**: ❌ NON-COMPLIANT (Expected - Deferred Migration)
**Compliance Rate**: ~40%
**Migration Priority**: P2 (Deferred until after core features complete)

**Validation Report**: `.standards/validation-reports/student-service.md`

---

### Known Configuration Issues

#### 1. Profile Configuration ❌ (HIGH PRIORITY)

**Issue**: Service has 4 profile files instead of 2

**Current State**:
```
student-service/src/main/resources/
├── application.yml          ✅
├── application-dev.yml      ❌ Extra profile (remove)
├── application-docker.yml   ✅
└── application-prod.yml     ❌ Extra profile (consolidate into docker)
```

**Expected State**:
```
student-service/src/main/resources/
├── application.yml          ✅ Default profile (local dev)
└── application-docker.yml   ✅ Docker profile (containerized)
```

**Impact**: HIGH
- Configuration confusion
- Deployment complexity
- Profile selection errors
- Maintenance overhead

**Migration Steps**:

1. **Consolidate application-dev.yml**:
   ```bash
   # Merge dev-specific settings into application.yml
   # Then delete the file
   rm student-service/src/main/resources/application-dev.yml
   ```

2. **Rename application-prod.yml**:
   ```bash
   # If prod settings are different from docker settings:
   # Review and merge into application-docker.yml
   # Then delete application-prod.yml
   rm student-service/src/main/resources/application-prod.yml
   ```

3. **Update docker-compose.yml**:
   ```yaml
   # Change from:
   - SPRING_PROFILES_ACTIVE=prod

   # To:
   - SPRING_PROFILES_ACTIVE=docker
   ```

**Verification**:
```bash
ls -1 student-service/src/main/resources/application*.yml | wc -l
# Should output: 2
```

**Estimated Time**: 15 minutes

---

#### 2. Environment Variable Naming ❌ (HIGH PRIORITY)

**Issue**: Uses custom variable names instead of Spring Boot standards

**Current (Non-Compliant)**:
```yaml
# In docker-compose.yml
environment:
  - DB_USERNAME=sms_user               ❌
  - DB_PASSWORD=${DB_PASSWORD}         ❌
  - EUREKA_CLIENT_SERVICE_URL=...      ❌
  - EUREKA_INSTANCE_HOSTNAME=...       ❌ (should be in YAML)
  - EUREKA_INSTANCE_PREFER_IP_ADDRESS=...  ❌ (should be in YAML)
```

**Expected (Compliant)**:
```yaml
# In docker-compose.yml
environment:
  - SPRING_DATASOURCE_USERNAME=sms_user                           ✅
  - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}                     ✅
  - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/  ✅
  # Remove EUREKA_INSTANCE_* env vars (configure in application-docker.yml instead)
```

**Impact**: HIGH
- Deployment inconsistency across services
- Configuration errors
- Non-standard patterns

**Migration Steps**:

1. **Update docker-compose.yml**:
   ```yaml
   student-service:
     environment:
       # Database (change variable names)
       - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-student:5432/student_db
       - SPRING_DATASOURCE_USERNAME=sms_user
       - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}

       # Eureka (fix variable name)
       - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

       # Remove these:
       # - EUREKA_INSTANCE_HOSTNAME=student-service
       # - EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
   ```

2. **Update application-docker.yml**:
   ```yaml
   spring:
     datasource:
       url: ${SPRING_DATASOURCE_URL}          # Changed from DB_URL
       username: ${SPRING_DATASOURCE_USERNAME}  # Changed from DB_USERNAME
       password: ${SPRING_DATASOURCE_PASSWORD}  # Changed from DB_PASSWORD

   eureka:
     instance:
       hostname: student-service    # Moved from environment variable
       prefer-ip-address: false     # Moved from environment variable
     client:
       service-url:
         defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}  # Fixed variable name
   ```

**Verification**:
```bash
grep -E "DB_USERNAME|DB_PASSWORD|EUREKA_CLIENT_SERVICE_URL" docker-compose.yml
# Should return no results
```

**Estimated Time**: 15 minutes

**Reference**: `.standards/docs/environment-variables.md`

---

#### 3. Eureka Configuration ❌ (MEDIUM PRIORITY)

**Issue**: Eureka instance configuration in environment variables instead of YAML

**Current (Non-Compliant)**:
```yaml
# In docker-compose.yml
environment:
  - EUREKA_INSTANCE_HOSTNAME=student-service        ❌
  - EUREKA_INSTANCE_PREFER_IP_ADDRESS=false         ❌
```

**Expected (Compliant)**:
```yaml
# In application-docker.yml
eureka:
  instance:
    hostname: student-service       # Hardcoded in YAML
    prefer-ip-address: false        # Hardcoded in YAML
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}  # From env var
```

**Impact**: MEDIUM
- Inconsistent Eureka configuration pattern
- Environment variable proliferation

**Migration Steps**:

1. **Move to application-docker.yml**:
   ```yaml
   eureka:
     instance:
       hostname: student-service
       prefer-ip-address: false
     client:
       service-url:
         defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
   ```

2. **Remove from docker-compose.yml**:
   ```yaml
   # Delete these lines:
   # - EUREKA_INSTANCE_HOSTNAME=student-service
   # - EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
   ```

**Verification**:
```bash
grep "EUREKA_INSTANCE" docker-compose.yml
# Should return no results
```

**Estimated Time**: 10 minutes

**Reference**: `.standards/docs/eureka-configuration.md`

---

### Package Structure Issues

**Note**: Package structure issues (entity/ vs model/, JWT in config/, etc.) are documented in the main validation report. Configuration migration should be done first, followed by code structure migration.

**See**: `.standards/validation-reports/student-service.md` for complete package structure migration plan.

---

## Migration Timeline

### Phase 1: Configuration Only (PRIORITY)

**Estimated Time**: 40-50 minutes
**When**: During next maintenance window

**Tasks**:
1. ✅ Consolidate to 2 profiles (15 min)
2. ✅ Update environment variable names (15 min)
3. ✅ Move Eureka config to YAML (10 min)
4. ✅ Testing and verification (10 min)

**Blockers**: None - can be done independently

---

### Phase 2: Package Structure (DEFERRED)

**Estimated Time**: 1-1.5 hours
**When**: After core features complete

**Tasks**:
1. Rename entity/ to model/
2. Move JWT classes to security/
3. Flatten service/impl/ to service/
4. Rename OpenApiConfig to OpenAPIConfig
5. Add missing CorsConfig

**Blockers**: Requires code changes, testing, potential breaking changes

**See**: `.standards/validation-reports/student-service.md` for detailed steps

---

## Migration Commands Reference

### Quick Migration Script (student-service Configuration)

```bash
#!/bin/bash
# Student Service Configuration Migration
# Run from repository root

SERVICE_DIR="student-service"
RESOURCES_DIR="$SERVICE_DIR/src/main/resources"

echo "Starting configuration migration for student-service..."

# Step 1: Backup current configuration
echo "Creating backup..."
cp -r "$RESOURCES_DIR" "$RESOURCES_DIR.backup-$(date +%Y%m%d)"

# Step 2: Remove extra profiles
echo "Removing extra profile files..."
rm -f "$RESOURCES_DIR/application-dev.yml"
rm -f "$RESOURCES_DIR/application-prod.yml"

# Step 3: Verify profile count
PROFILE_COUNT=$(ls -1 "$RESOURCES_DIR"/application*.yml | wc -l)
if [ "$PROFILE_COUNT" -eq 2 ]; then
    echo "✅ Profile count correct: 2"
else
    echo "❌ Profile count incorrect: $PROFILE_COUNT (expected 2)"
    exit 1
fi

# Step 4: Update application-docker.yml
echo "Updating application-docker.yml..."
# Manual step - review and update configuration

echo "Configuration migration complete!"
echo "Next steps:"
echo "1. Update docker-compose.yml environment variables"
echo "2. Review application-docker.yml for Eureka config"
echo "3. Test service startup"
echo "4. Run validation: .standards/scripts/validate-service-structure.sh student-service"
```

---

## Validation After Migration

### Automated Validation

```bash
.standards/scripts/validate-service-structure.sh student-service
```

**Expected Output** (after configuration migration):
```
✅ PASS: Service has exactly 2 profile files
✅ PASS: Environment variable naming (Spring Boot standard names)
✅ PASS: Eureka configuration (hostname-based, prefer-ip-address: false)
```

### Manual Validation

**Checklist**:
- [ ] Exactly 2 profile files exist
- [ ] docker-compose.yml uses `SPRING_DATASOURCE_*` variables
- [ ] docker-compose.yml uses `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`
- [ ] No `EUREKA_INSTANCE_*` environment variables
- [ ] application-docker.yml has `hostname: student-service`
- [ ] application-docker.yml has `prefer-ip-address: false`
- [ ] Service starts successfully with `docker-compose up`
- [ ] Service registers with Eureka
- [ ] API endpoints work through API Gateway

---

## Risk Assessment

### Configuration Migration Risks

**LOW RISK**:
- Profile consolidation (easy rollback)
- Environment variable renaming (Spring Boot handles both old/new)
- Eureka configuration move (no functional change)

**MEDIUM RISK**:
- None identified for configuration changes

**HIGH RISK**:
- None identified for configuration changes

**Rollback Plan**:
- Configuration backups created before migration
- Can restore from `src/main/resources.backup-*` if issues occur
- Changes are reversible

---

## Success Criteria

### Configuration Migration Complete When:

- [ ] Service has exactly 2 profile files
- [ ] All environment variables use Spring Boot standard names
- [ ] Eureka configuration in YAML (not env vars)
- [ ] Validation script shows 100% configuration compliance
- [ ] Service deploys successfully via Docker Compose
- [ ] Service registers with Eureka correctly
- [ ] All API endpoints functional

---

## Related Documentation

- **Validation Report**: `.standards/validation-reports/student-service.md`
- **Environment Variables**: `.standards/docs/environment-variables.md`
- **Profile Strategy**: `.standards/docs/profile-strategy.md`
- **Eureka Configuration**: `.standards/docs/eureka-configuration.md`
- **Configuration Templates**: `.standards/templates/`

---

## Version History

| Version | Date       | Changes                                    |
|---------|------------|--------------------------------------------|
| 1.0.0   | 2025-11-22 | Initial migration notes for student-service |

---

## Support

For migration assistance:

1. Review this document for step-by-step instructions
2. Check validation reports for detailed issue analysis
3. Consult configuration templates for correct patterns
4. Run validation script to verify compliance
5. Test thoroughly before deploying to production
