# Frequently Asked Questions (FAQ)

**Version**: 1.0.0
**Date**: 2025-11-22
**Purpose**: Answer common questions about Salarean SMS microservice architecture standards

---

## General Questions

### Q: What are these standards for?

**A**: These standards ensure all Salarean SMS microservices follow consistent architectural patterns. This improves:
- **Developer onboarding**: New developers navigate services in <30 minutes
- **Deployment reliability**: Standardized configuration eliminates deployment failures
- **Maintenance efficiency**: Cross-service bug fixes reduced by 50% (2-4 hours → 1-2 hours)
- **Service creation**: New services created in <2 hours with guaranteed compliance

### Q: Do I have to follow these standards?

**A**: **Yes** for all new microservices created after 2025-11-22. Existing services (auth-service, student-service) will be migrated gradually after core features are complete.

### Q: What if I disagree with a standard?

**A**: Standards are based on research and cross-service analysis (see `research.md`). If you have a valid technical reason to deviate:
1. Document the rationale
2. Discuss with the team
3. Update standards if the new approach is better
4. Never silently deviate

---

## Service Creation

### Q: How do I create a new microservice?

**A**: Use the automated script:
```bash
.standards/scripts/create-service.sh <service-name> <port> <db-port>

# Example:
.standards/scripts/create-service.sh attendance 8083 5435
```

Then follow the quickstart guide: `.standards/docs/quickstart-service-creation.md`

### Q: How long does it take to create a new service?

**A**:
- **Automated setup** (via create-service.sh): ~5 minutes
- **Manual implementation**: 1.5-2.5 hours (domain code, Docker, API Gateway, tests)
- **Total**: <2 hours to deployment-ready service

### Q: Can I copy-paste from auth-service instead of using the script?

**A**: The script automates error-prone steps (package renaming, port updates) and ensures compliance. Manual copying takes 3-4 hours and is error-prone. **Use the script.**

### Q: What's the difference between auth-service and the template?

**A**: auth-service is the **living reference implementation**. The create-service.sh script copies auth-service and removes auth-specific code, leaving only the standardized structure.

---

## Package Structure

### Q: Why `model/` instead of `entity/`?

**A**: Consistency with Spring conventions and to avoid confusion with JPA's `@Entity` annotation. All services must use `model/` for JPA entities.

### Q: Why are JWT classes in `security/` not `config/`?

**A**: Separation of concerns:
- `config/` = Configuration classes (beans, settings)
- `security/` = Security logic (filters, token operations)

JWT classes contain security logic, not configuration.

### Q: Can I add `service/impl/` for service implementations?

**A**: **No**. Service interfaces and implementations belong in the same `service/` package. Extra nesting adds no value and complicates navigation.

---

## Configuration

### Q: Why only 2 profiles (default, docker)?

**A**: Simplicity. More profiles = more confusion and maintenance burden. Use:
- `application.yml` - Local development
- `application-docker.yml` - Docker/production

Environment-specific values go in environment variables, not profile files.

### Q: Why `SPRING_DATASOURCE_URL` instead of `DB_URL`?

**A**: Spring Boot standard property names enable auto-configuration. Custom names require extra @Value annotations and break Spring Boot conventions.

### Q: What's the JWT_SECRET default value?

**A**: `your-secret-key-for-jwt-token-generation-minimum-32-characters-long` (for development only). **Change this in production** via environment variables.

### Q: Why prefer-ip-address: false in Docker?

**A**: Hostname-based registration prevents multi-network IP registration issues. Services register as `attendance-service` instead of unstable IP addresses.

---

## Docker and Deployment

### Q: Why are there 2 database containers per service?

**A**: Each microservice has its own dedicated database (database-per-service pattern). This ensures:
- Service independence
- No schema conflicts
- Independent scaling
- Fault isolation

### Q: What ports should I use for my service?

**A**: Follow the allocation table:
- 8080: API Gateway
- 8081: auth-service (DB: 5433)
- 8082: student-service (DB: 5434)
- 8083: attendance-service (DB: 5435)
- 8084+: Your new services

### Q: Do I need to update docker-compose.yml manually?

**A**: Yes. The create-service.sh script creates the service, but you must manually add the Docker Compose entry using `.standards/templates/docker-compose-entry.yml` as a reference.

**Why not automated?** Safely modifying shared YAML files requires careful parsing. Manual verification is safer.

---

## Validation and Compliance

### Q: How do I check if my service is compliant?

**A**: Run the validation script:
```bash
.standards/scripts/validate-service-structure.sh <service-name>
```

All checks must pass (✅ 100%) before deployment.

### Q: What if validation fails?

**A**: Review the failure details:
1. Read the error message (tells you exactly what's wrong)
2. Check `.standards/docs/common-locations.md` for correct locations
3. Fix the issues
4. Re-run validation

Common fixes in `.standards/docs/refactoring-checklist.md`

### Q: Can I skip validation?

**A**: **No**. Validation ensures:
- Consistent architecture across all services
- No deployment failures
- Easier maintenance
- Code review efficiency

Validation takes <30 seconds. Don't skip it.

---

## Development Workflow

### Q: When should I run validation?

**A**:
- ✅ After creating a new service
- ✅ Before committing changes
- ✅ In CI/CD pipeline (automated)
- ✅ During code review

### Q: How do pre-commit hooks work?

**A**: Install the hook template:
```bash
cp .standards/templates/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

The hook runs validation automatically before each commit.

### Q: Can I bypass the pre-commit hook?

**A**: Yes, but **don't**:
```bash
git commit --no-verify
```

Only use this for non-code commits (documentation updates).

---

## API and Endpoints

### Q: Why does OpenAPI point to API Gateway (port 8080) instead of the service port?

**A**: To prevent CORS errors. Swagger UI calls APIs through the gateway, not directly. If OpenAPI points to the service port, CORS will block requests.

### Q: What's the standard API response format?

**A**: All endpoints return:
```json
{
  "errorCode": "SUCCESS",  // or error code like "NOT_FOUND"
  "data": { ... }          // payload or null
}
```

**Why?** Frontend can handle all responses uniformly and map error codes to localized messages.

### Q: Where do I define error codes?

**A**: In `GlobalExceptionHandler.java`. Use `UPPER_SNAKE_CASE` format:
- `SUCCESS` - operation successful
- `VALIDATION_ERROR` - input validation failed
- `RESOURCE_NOT_FOUND` - entity not found
- `UNAUTHORIZED` - authentication required

---

## JWT and Security

### Q: Do all services need JWT authentication?

**A**: Yes, except for public endpoints like `/actuator/health` and `/swagger-ui/**`. All business endpoints require JWT.

### Q: How do services validate JWT tokens?

**A**: Each service has its own `JwtTokenProvider` that validates tokens using the shared `JWT_SECRET`. Services don't call auth-service for validation.

### Q: What if JWT_SECRET changes?

**A**: Update the `.env` file and restart all services. All services must use the **same** JWT_SECRET.

---

## Testing

### Q: What tests are required?

**A**:
- Unit tests for services
- Integration tests for controllers
- Compliance validation (automated)

### Q: What's the test database?

**A**: H2 in-memory database for unit/integration tests. Configure in `src/test/resources/application-test.yml`.

### Q: Do I need to write tests for template code?

**A**: Yes. Remove auth-service tests and write tests for your domain logic.

---

## Troubleshooting

### Q: Service won't start - "Port already in use"

**A**: Another service is using the port. Change your port or stop the conflicting service:
```bash
lsof -ti:8083 | xargs kill -9  # Kill process on port 8083
```

### Q: Service not registering with Eureka

**A**: Check:
1. `eureka.instance.hostname` matches container name in docker-compose.yml
2. `eureka.instance.prefer-ip-address: false`
3. Service is on `backend-network`
4. Eureka server is running: `docker-compose ps eureka-server`

### Q: Database connection refused

**A**:
- **Local**: Ensure PostgreSQL is running: `brew services start postgresql@15`
- **Docker**: Check database container: `docker-compose logs postgres-{service}`

### Q: CORS errors in Swagger UI

**A**: Verify `OpenAPIConfig.java` server URL points to API Gateway (`http://localhost:8080`), not the service port.

### Q: JWT validation fails (401 Unauthorized)

**A**: Check:
1. JWT_SECRET is the same across all services (`.env` file)
2. Token is valid (not expired)
3. Authorization header format: `Bearer <token>`

### Q: Build fails - "Package does not exist"

**A**: Package references not updated after creating service. Re-run:
```bash
find . -type f -name "*.java" -exec sed -i '' 's/com.sms.auth/com.sms.{yourservice}/g' {} +
```

---

## Maintenance

### Q: How do I apply a fix across multiple services?

**A**: Follow this workflow:
1. Locate components: `.standards/scripts/find-component.sh <ComponentName>`
2. Update template: `.standards/templates/java/<ComponentName>.java`
3. Apply to services: Use patterns from `.standards/docs/cross-service-changes.md`
4. Validate all: `.standards/scripts/validate-all-services.sh`
5. Test and deploy

### Q: How do I track maintenance time improvements?

**A**: Use `.standards/docs/maintenance-metrics.md` templates:
- Log time for cross-service fixes
- Track tool usage
- Measure against baseline (2-4 hours → 1-2 hours target)

### Q: When should I update existing services to match standards?

**A**: After core feature development is complete. Migration is deferred for:
- student-service (has 4 profiles, needs cleanup)
- Other existing services

New services must be compliant from day 1.

---

## Architecture Decisions

### Q: Why Spring Boot 3.5.7 specifically?

**A**: Latest stable version with:
- Java 21 support
- Native compilation improvements
- Spring Cloud 2025.0.0 compatibility
- Security enhancements

### Q: Why PostgreSQL instead of MySQL?

**A**:
- Better JSON support
- Advanced indexing (GiST, GIN)
- Window functions
- Better concurrent performance
- Open-source licensing

### Q: Why Eureka instead of Consul/K8s discovery?

**A**:
- Simple setup for development
- Spring Cloud native integration
- No infrastructure dependencies
- Easy to replace with K8s in production

---

## Future Plans

### Q: Will these standards change?

**A**: Standards are versioned (v1.0.0). Changes will be:
- Documented in `CHANGELOG.md`
- Communicated to the team
- Backward-compatible when possible
- Migrated with automated scripts

### Q: Can standards be automated further?

**A**: Yes. Future improvements:
- Docker Compose YAML merging
- API Gateway route automation
- Code generation from entities
- Test scaffolding

See `.standards/docs/service-creation-manual-steps.md` for details.

### Q: What if I want to use a different technology (e.g., MongoDB, GraphQL)?

**A**: Discuss with the team first. Document the rationale. Update standards if the new approach is broadly applicable.

---

## Getting Help

### Q: Where can I find more information?

**A**: Check these documents in order:
1. `.standards/README.md` - Overview and quick links
2. `.standards/docs/quickstart-service-creation.md` - Step-by-step guide
3. `.standards/docs/FAQ.md` - This document
4. `.standards/docs/troubleshooting.md` - Common issues
5. `.standards/docs/common-locations.md` - Where to find things

### Q: Who maintains these standards?

**A**: Salarean Development Team. Contact via:
- GitHub Issues: Report problems, suggest improvements
- Team Slack: Quick questions
- Code Reviews: Architectural discussions

### Q: Can I contribute to standards?

**A**: Yes! Submit pull requests to:
- Fix errors in documentation
- Add troubleshooting solutions
- Improve automation scripts
- Suggest new standards

---

**Version**: 1.0.0
**Last Updated**: 2025-11-22
**Maintained By**: Salarean Development Team
**Related Docs**: README.md, troubleshooting.md, quickstart-service-creation.md
