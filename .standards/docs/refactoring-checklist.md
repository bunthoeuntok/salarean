# Refactoring Checklist

## Purpose

This checklist ensures safe refactoring of shared patterns across multiple microservices. Use this when making architectural changes that affect more than one service to prevent breaking changes and maintain system stability.

---

## Pre-Refactoring Assessment

### Scope Analysis

- [ ] **Identify affected services**: List all services that use the pattern being refactored
- [ ] **Map dependencies**: Document which services depend on the current implementation
- [ ] **Check for variations**: Identify any service-specific customizations that might break
- [ ] **Estimate impact**: Determine if this is a breaking change or backward-compatible update

### Risk Evaluation

- [ ] **Production status**: Are any affected services deployed to production?
- [ ] **Testing coverage**: Do affected services have automated tests for this pattern?
- [ ] **Rollback plan**: Can changes be reverted quickly if issues arise?
- [ ] **Deployment coordination**: Do all services need simultaneous deployment?

---

## Planning Phase

### Documentation

- [ ] **Document current state**: Screenshot/copy current implementation across all services
- [ ] **Define target state**: Create clear specification of desired refactored pattern
- [ ] **Create migration guide**: Document step-by-step changes for each service
- [ ] **Update architecture docs**: Reflect changes in `.standards/` documentation

### Communication

- [ ] **Notify team**: Alert developers about upcoming changes
- [ ] **Review with stakeholders**: Get approval for breaking changes
- [ ] **Schedule deployment**: Coordinate timing across services if needed
- [ ] **Create feature branch**: Use consistent branch naming (e.g., `refactor/jwt-validation`)

---

## Common Refactoring Scenarios

### Scenario 1: Updating Shared Configuration Pattern

**Example**: Changing CORS allowed origins from wildcard to specific domains

**Checklist**:
- [ ] Identify all services with CorsConfig.java
- [ ] Define new allowed origins (development + production)
- [ ] Update `.standards/templates/java/CorsConfig.java` template
- [ ] Update each service's CorsConfig.java
- [ ] Test each service with new CORS settings
- [ ] Deploy to development environment first
- [ ] Verify cross-origin requests work
- [ ] Deploy to production with rollback plan

**Files Affected**: `{service}/src/main/java/com/sms/{service}/config/CorsConfig.java`

---

### Scenario 2: Refactoring JWT Architecture

**Example**: Adding refresh token validation to JWT filter

**Checklist**:
- [ ] Update `.standards/templates/java/JwtAuthenticationFilter.java`
- [ ] Update `.standards/templates/java/JwtTokenProvider.java`
- [ ] Document new refresh token logic
- [ ] Update each service's JWT classes
- [ ] Add/update tests for refresh token flow
- [ ] Verify backward compatibility with existing tokens
- [ ] Test token expiration and refresh cycle
- [ ] Update API documentation (Swagger)

**Files Affected**:
- `{service}/src/main/java/com/sms/{service}/security/JwtAuthenticationFilter.java`
- `{service}/src/main/java/com/sms/{service}/security/JwtTokenProvider.java`

---

### Scenario 3: Package Restructuring

**Example**: Moving validation classes from util/ to new validation/ package

**Checklist**:
- [ ] Create new `validation/` package in all affected services
- [ ] Move validator classes to new package
- [ ] Update import statements across all classes
- [ ] Update package scanning in Spring configuration if needed
- [ ] Run all tests to verify nothing broke
- [ ] Update `.standards/docs/common-locations.md`
- [ ] Delete old `util/` package after verification
- [ ] Commit changes with clear message

**Impact**: Medium (affects imports across codebase)

---

### Scenario 4: Updating Environment Variable Names

**Example**: Renaming JWT_SECRET to JWT_SIGNING_KEY

**Checklist**:
- [ ] Update `.standards/docs/environment-variables.md`
- [ ] Update all `application.yml` files
- [ ] Update all `application-docker.yml` files
- [ ] Update all `docker-compose.yml` service definitions
- [ ] Update `.env.example` file
- [ ] Update actual `.env` file (not committed)
- [ ] Test local startup with new variable name
- [ ] Test Docker startup with new variable name
- [ ] Update deployment documentation

**Files Affected**:
- `{service}/src/main/resources/application.yml`
- `{service}/src/main/resources/application-docker.yml`
- `docker-compose.yml`
- `.env`

---

### Scenario 5: Updating OpenAPI Configuration

**Example**: Adding production server URL to Swagger configuration

**Checklist**:
- [ ] Update `.standards/templates/java/OpenAPIConfig.java`
- [ ] Define production server URL (e.g., `https://api.yourdomain.com`)
- [ ] Add environment-based server selection
- [ ] Update each service's OpenAPIConfig.java
- [ ] Test Swagger UI shows correct server dropdown
- [ ] Verify API calls work through production URL
- [ ] Update `.standards/docs/openapi-setup.md`

**Files Affected**: `{service}/src/main/java/com/sms/{service}/config/OpenAPIConfig.java`

---

## Execution Phase

### Step 1: Update Template

- [ ] **Modify standard template**: Update `.standards/templates/` with new pattern
- [ ] **Document changes**: Add changelog entry to template file
- [ ] **Commit template**: Push updated template to version control
- [ ] **Notify team**: Share template update in team channel

### Step 2: Update Reference Service (auth-service)

- [ ] **Apply refactoring**: Update auth-service as the first implementation
- [ ] **Run all tests**: Ensure auth-service tests pass
- [ ] **Manual testing**: Verify functionality works end-to-end
- [ ] **Document issues**: Note any unexpected problems
- [ ] **Commit changes**: Push auth-service refactoring

### Step 3: Update Remaining Services

For each service:

- [ ] **Create feature branch**: `refactor/{change-description}`
- [ ] **Apply refactoring**: Use auth-service as reference
- [ ] **Run service tests**: Verify tests pass
- [ ] **Integration testing**: Test with other services if needed
- [ ] **Code review**: Get peer review before merging
- [ ] **Merge to main**: Merge feature branch
- [ ] **Deploy**: Deploy to development environment

### Step 4: Validation

- [ ] **Smoke testing**: Run `.standards/scripts/smoke-test-deployment.sh`
- [ ] **Service registration**: Verify all services register with Eureka
- [ ] **Health checks**: Confirm all `/actuator/health` endpoints return UP
- [ ] **End-to-end testing**: Test cross-service workflows
- [ ] **Performance testing**: Verify no performance degradation

---

## Post-Refactoring Tasks

### Documentation Updates

- [ ] **Update common-locations.md**: Reflect any location changes
- [ ] **Update reusable-components.md**: Update component documentation
- [ ] **Update CLAUDE.md**: Add refactoring notes if architectural
- [ ] **Update SERVICE_COMPARISON_ANALYSIS.md**: Document new patterns

### Knowledge Sharing

- [ ] **Team presentation**: Demo refactored pattern to team
- [ ] **Update onboarding**: Modify onboarding docs if needed
- [ ] **Code review guide**: Update review checklist
- [ ] **Lessons learned**: Document what went well/poorly

### Cleanup

- [ ] **Delete old branches**: Remove feature branches after merge
- [ ] **Archive old code**: If keeping for reference, tag or document
- [ ] **Update validation scripts**: Ensure scripts check new pattern
- [ ] **Remove deprecated code**: Delete any obsolete implementations

---

## Breaking Change Management

### When Changes Are Breaking

A change is **breaking** if:
- Existing code will not compile without modifications
- API contracts change (request/response formats)
- Environment variable names change
- Database schema changes
- Service startup requires new dependencies

### Breaking Change Checklist

- [ ] **Version bump**: Increment major version number
- [ ] **Deprecation notice**: Announce change at least 1 sprint in advance
- [ ] **Migration guide**: Provide detailed upgrade instructions
- [ ] **Dual support**: Support old pattern temporarily if possible
- [ ] **Coordinated deployment**: Schedule simultaneous service updates
- [ ] **Rollback strategy**: Have tested rollback procedure ready
- [ ] **Production notification**: Alert operations team before deployment

---

## Rollback Procedures

### Quick Rollback (< 5 minutes)

If refactoring causes immediate production issues:

1. **Revert Git commit**: `git revert {commit-hash}`
2. **Redeploy previous version**: `docker-compose up -d {service}`
3. **Verify services**: Check health endpoints
4. **Notify team**: Alert team of rollback

### Full Rollback (< 30 minutes)

If multiple services affected:

1. **Stop all affected services**: `docker-compose stop {service-list}`
2. **Checkout previous version**: `git checkout {previous-tag}`
3. **Rebuild if needed**: `docker-compose build {service-list}`
4. **Restart services**: `docker-compose up -d {service-list}`
5. **Verify Eureka**: Check service registration
6. **Run smoke tests**: `.standards/scripts/smoke-test-deployment.sh`
7. **Document incident**: Create postmortem

---

## Testing Strategy

### Unit Testing

- [ ] **Test in isolation**: Verify refactored component works standalone
- [ ] **Mock dependencies**: Use mocks to isolate component
- [ ] **Edge cases**: Test boundary conditions
- [ ] **Negative cases**: Verify error handling

### Integration Testing

- [ ] **Service-to-service**: Test interactions with other services
- [ ] **Database integration**: Verify data persistence works
- [ ] **External APIs**: Test third-party integrations still work
- [ ] **Authentication flow**: Verify JWT authentication end-to-end

### System Testing

- [ ] **Full user journey**: Test complete workflows (e.g., login → API call → data retrieval)
- [ ] **Performance testing**: Ensure no slowdowns introduced
- [ ] **Load testing**: Verify system handles expected load
- [ ] **Failure scenarios**: Test graceful degradation

---

## Code Review Guidelines

### What Reviewers Should Check

- [ ] **Consistency**: Pattern applied identically across all services
- [ ] **Completeness**: All affected files updated
- [ ] **Testing**: Adequate test coverage for changes
- [ ] **Documentation**: Inline comments explain complex logic
- [ ] **Backward compatibility**: Existing functionality preserved
- [ ] **Standards compliance**: Follows architectural standards
- [ ] **Performance impact**: No unnecessary performance degradation

### Review Checklist

- [ ] Code compiles without warnings
- [ ] Tests pass locally
- [ ] No hardcoded values introduced
- [ ] Error handling adequate
- [ ] Logging appropriate
- [ ] Security considerations addressed
- [ ] OpenAPI documentation updated if API changed

---

## Metrics and Success Criteria

### Track These Metrics

- **Time to refactor**: How long did it take from start to completion?
- **Services affected**: How many services required changes?
- **Issues found**: How many bugs discovered during refactoring?
- **Rollbacks needed**: Did any service require rollback?
- **Test coverage**: Did coverage improve/decrease?

### Success Indicators

- ✅ All services deployed successfully
- ✅ No production incidents within 48 hours
- ✅ All automated tests passing
- ✅ Team confident in new pattern
- ✅ Documentation updated and accurate

---

## Refactoring Patterns by Package

### Config Package Refactoring

**Common Changes**:
- Update bean definitions
- Modify configuration properties
- Change integration patterns

**Test Focus**:
- Bean wiring
- Configuration loading
- Property injection

### Security Package Refactoring

**Common Changes**:
- Update authentication logic
- Modify authorization rules
- Change JWT handling

**Test Focus**:
- Authentication flows
- Authorization checks
- Security context

### Service Package Refactoring

**Common Changes**:
- Modify business logic
- Update transaction boundaries
- Change method signatures

**Test Focus**:
- Business rules
- Data validation
- Transaction rollback

### Controller Package Refactoring

**Common Changes**:
- Update request mappings
- Modify response formats
- Change validation

**Test Focus**:
- HTTP status codes
- Request/response serialization
- Validation errors

---

## Tools and Scripts

### Useful Commands

```bash
# Find all instances of a pattern across services
grep -r "pattern-to-find" --include="*.java" .

# Replace pattern across all services (use with caution!)
find . -name "*.java" -exec sed -i 's/old-pattern/new-pattern/g' {} +

# Validate all services after refactoring
.standards/scripts/validate-all-services.sh

# Run tests across all services
for service in auth-service student-service; do
    cd $service && ./mvnw test && cd ..
done

# Check Git diff for all services
git diff --stat

# Find component across services
.standards/scripts/find-component.sh ComponentName
```

---

## Emergency Contacts

**If refactoring causes production issues**:

1. **Rollback immediately** (see Rollback Procedures above)
2. **Notify team lead**
3. **Create incident report**
4. **Schedule postmortem**

---

## Related Documentation

- **Common Locations**: `.standards/docs/common-locations.md` - Find code quickly
- **Cross-Service Changes**: `.standards/docs/cross-service-changes.md` - Bulk update patterns
- **Service Template**: `.standards/templates/service-template.md` - Reference implementation
- **Validation Script**: `.standards/scripts/validate-all-services.sh` - Compliance checking

---

## Example: Complete JWT Expiration Refactoring

### Scenario

Update JWT token expiration from 24 hours to 48 hours across all services.

### Execution

1. **Pre-Refactoring**:
   - [x] Identified 2 services using JWT (auth-service, student-service)
   - [x] Checked production status (development only)
   - [x] Reviewed test coverage (both have JWT tests)

2. **Planning**:
   - [x] Created feature branch: `refactor/jwt-expiration-48h`
   - [x] Updated template: `.standards/templates/java/JwtTokenProvider.java`
   - [x] Documented in changelog

3. **Execution**:
   - [x] Updated auth-service JwtTokenProvider.java (line 20: `86400000` → `172800000`)
   - [x] Ran auth-service tests - PASSED
   - [x] Updated student-service JwtTokenProvider.java
   - [x] Ran student-service tests - PASSED
   - [x] Integration test: verified tokens expire after 48h

4. **Post-Refactoring**:
   - [x] Updated documentation
   - [x] Committed with message: "refactor: increase JWT expiration to 48 hours"
   - [x] Merged to main
   - [x] Deployed to development

**Time**: 30 minutes (down from 2 hours pre-standardization)

**Issues**: None

**Success**: ✅ All services updated consistently, tests passing, production-ready

---

## Version History

- **v1.0** (2025-11-22): Initial refactoring checklist created
