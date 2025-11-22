# Service Standards Compliance Contracts

**Version**: 1.0.0
**Date**: 2025-11-22
**Purpose**: Enforce and validate microservice architecture standards

---

## Overview

This directory contains compliance contract files that define, validate, and document the architectural standards for all Salarean SMS microservices. These contracts are based on comparative analysis of auth-service and student-service implementations.

---

## Files in This Directory

### 1. compliance-checklist.md (841 lines)
**Purpose**: Comprehensive manual checklist for service compliance validation

**What it contains**:
- 32 detailed compliance checks across 8 categories
- Pass/fail criteria with examples
- Manual vs automated check indicators
- Severity levels (CRITICAL, HIGH, MEDIUM, LOW)
- Compliance scorecard template

**When to use**:
- During code reviews
- Before merging new services
- When refactoring existing services
- As reference during development

**Usage**:
```bash
# Open checklist
open compliance-checklist.md

# Follow checklist for your service
# Mark items as ✅ PASS or ❌ FAIL
```

**Categories covered**:
1. Profile Configuration (3 checks)
2. Environment Variable Naming (5 checks)
3. Package Structure (6 checks)
4. JWT Architecture (4 checks)
5. Required Configuration Classes (4 checks)
6. OpenAPI Configuration (3 checks)
7. Eureka Configuration (3 checks)
8. Docker Compose Configuration (4 checks)

---

### 2. validation-script.sh (638 lines)
**Purpose**: Automated compliance validation executable

**What it does**:
- Runs 22 automated checks on a service
- Validates file structure, package naming, configuration
- Provides clear pass/fail output with emojis
- Categorizes failures by severity
- Returns exit code 0 (pass) or 1 (fail)

**When to use**:
- Before committing changes
- In CI/CD pipeline
- After creating new service
- During service migration

**Usage**:
```bash
# Make executable (if not already)
chmod +x validation-script.sh

# Run on a service
./validation-script.sh /path/to/service-directory

# Example: Validate auth-service
./validation-script.sh ../../auth-service

# Example: Validate student-service
./validation-script.sh ../../student-service
```

**Output example**:
```
================================================================================
Microservice Compliance Validation
================================================================================

Service: student-service
Directory: /path/to/student-service
Date: 2025-11-22 12:00:00

### Category 1: Profile Configuration (3 checks)

✅ PASS: PROFILE-001: Service has exactly 2 profile files
❌ FAIL: PROFILE-002: Service has forbidden profile files (dev, prod)
✅ PASS: PROFILE-003: docker-compose.yml sets SPRING_PROFILES_ACTIVE=docker

...

================================================================================
Validation Summary
================================================================================

Total Checks: 22
Passed: 15
Failed: 7

❌ COMPLIANCE STATUS: FAILED

CRITICAL FAILURES (3):
  - ENV-001: Database variables do not use SPRING_DATASOURCE_* prefix
  - PKG-002: JWT classes are in 'config/' package. Move to 'security/'
  - DCK-001: docker-compose.yml does not use SPRING_PROFILES_ACTIVE=docker
```

**Automated checks** (22 total):
- Profile count and naming
- Environment variable naming
- Package structure validation
- JWT class separation
- Configuration class presence
- Docker Compose configuration

**Manual checks** (10 total - require human review):
- Config package purity
- JWT implementation details
- OpenAPI configuration
- Eureka settings
- Network configuration

---

### 3. service-template.md (1,491 lines)
**Purpose**: Complete reference guide for creating standardized microservices

**What it contains**:
- Full directory structure with explanations
- Package organization standards
- Configuration file templates
- Required configuration classes with code examples
- JWT architecture patterns
- Docker Compose configuration
- Step-by-step service creation workflow
- Quick start guide
- Troubleshooting section

**When to use**:
- Creating new microservices
- Understanding package structure
- Copying configuration templates
- Migrating existing services
- Training new developers

**Key sections**:
1. **Directory Structure**: Complete service layout
2. **Package Structure**: Java package organization
3. **Configuration Files**: application.yml templates
4. **Required Configuration Classes**: CorsConfig, OpenAPIConfig, SecurityConfig
5. **JWT Architecture**: Filter + Provider pattern
6. **Docker Compose**: Service definition template
7. **Service Creation Workflow**: 10-step process
8. **Quick Start Guide**: 5-minute quickstart
9. **Troubleshooting**: Common issues and solutions

**Usage examples**:

**Create new service from template**:
```bash
# Follow Section 8: Service Creation Workflow
# Step-by-step instructions with code samples
```

**Copy configuration class**:
```bash
# Open service-template.md
# Navigate to "Required Configuration Classes"
# Copy template for CorsConfig, OpenAPIConfig, etc.
```

**Understand package structure**:
```bash
# Open service-template.md
# Section 2: Package Structure
# See responsibilities and examples for each package
```

---

## Workflow: Using These Contracts Together

### Creating a New Service

```bash
# Step 1: Reference template documentation
open service-template.md
# Read Section 8: Service Creation Workflow

# Step 2: Copy auth-service template
cp -r ../../auth-service ../../new-service

# Step 3: Follow step-by-step workflow
# (Rename packages, update configs, etc.)

# Step 4: Run automated validation
./validation-script.sh ../../new-service

# Step 5: Manual checklist review
open compliance-checklist.md
# Go through manual checks (10 items)

# Step 6: Fix any failures and re-validate
./validation-script.sh ../../new-service
```

---

### Code Review Process

```bash
# Reviewer opens checklist
open compliance-checklist.md

# Run automated validation
./validation-script.sh /path/to/reviewed-service

# Review automated results
# Then manually verify 10 manual-only checks

# Reference template for clarification
open service-template.md
# Compare implementation against template
```

---

### Migrating Existing Service

```bash
# Step 1: Run validation to see current state
./validation-script.sh ../../student-service

# Step 2: Review failures
# Categorize by severity (CRITICAL, HIGH, MEDIUM, LOW)

# Step 3: Reference template for correct patterns
open service-template.md
# See how auth-service implements each standard

# Step 4: Fix issues one category at a time
# Start with CRITICAL failures

# Step 5: Re-validate after each fix
./validation-script.sh ../../student-service

# Step 6: Manual checklist for final verification
open compliance-checklist.md
```

---

## Integration with CI/CD

### GitHub Actions Example

```yaml
# .github/workflows/validate-service.yml
name: Service Compliance Validation

on:
  pull_request:
    paths:
      - 'auth-service/**'
      - 'student-service/**'
      - '**-service/**'

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Validate Service Structure
        run: |
          # Detect which service changed
          SERVICE_DIR=$(git diff --name-only ${{ github.event.pull_request.base.sha }} \
            | grep -E '^[a-z]+-service/' | cut -d/ -f1 | uniq)

          # Run validation
          ./specs/001-service-standards/contracts/validation-script.sh "./$SERVICE_DIR"

      - name: Comment PR with results
        if: failure()
        uses: actions/github-script@v6
        with:
          script: |
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: '❌ Service compliance validation failed. Please check the workflow logs and fix issues.'
            })
```

---

## Success Criteria

### Compliant Service Checklist

A service is considered **100% compliant** when:

- ✅ Automated validation script exits with code 0
- ✅ All 32 checklist items pass
- ✅ No CRITICAL or HIGH priority failures
- ✅ Code review approved
- ✅ Service successfully builds and runs in Docker

### Minimum Viable Compliance (for development)

For active development, minimum requirements:

- ✅ No CRITICAL failures
- ✅ Profile configuration correct
- ✅ Environment variables use Spring Boot standard names
- ✅ Package structure follows template
- ⚠️ HIGH priority issues documented as technical debt

---

## Maintenance

### Updating Standards

When standards change:

1. Update `../data-model.md` with new requirements
2. Modify `compliance-checklist.md` to reflect changes
3. Update `validation-script.sh` automated checks
4. Revise `service-template.md` examples
5. Increment version numbers in all files
6. Update auth-service (template) to match new standards
7. Communicate changes to development team

### Version History

Track changes in each file's header:

```markdown
**Version**: 1.1.0
**Date**: 2025-XX-XX
**Changes**: Added new check for XYZ requirement
```

---

## Support and Questions

### Reference Documents

For deeper understanding, see:

**Primary References**:
- `../data-model.md` - Conceptual models for standardization
- `../../SERVICE_COMPARISON_ANALYSIS.md` - Analysis of auth vs student service
- `../research.md` - Spring Boot best practices research

**Template Service**:
- Path: `/Volumes/DATA/my-projects/salarean/auth-service`
- Status: Production-ready, 100% compliant
- Use as: Authoritative example for all standards

### Common Questions

**Q: Which file should I reference first when creating a new service?**
A: Start with `service-template.md` Section 8 (Service Creation Workflow)

**Q: How do I know if my service is compliant?**
A: Run `./validation-script.sh ./your-service` - exit code 0 = compliant

**Q: What's the difference between automated and manual checks?**
A: Automated checks (22) can be verified by script. Manual checks (10) require human inspection of code quality and implementation details.

**Q: Can I skip LOW priority failures?**
A: Yes, for development. But all issues must be fixed before production deployment.

**Q: How often should I run validation?**
A: Run before every commit, after major changes, and in CI/CD pipeline.

**Q: What if validation script reports a false positive?**
A: Check the checklist for manual verification. If legitimately incorrect, file an issue to update the script.

---

## Quick Reference

### File Sizes
- `compliance-checklist.md`: 841 lines
- `validation-script.sh`: 638 lines
- `service-template.md`: 1,491 lines
- **Total**: 2,970 lines of comprehensive guidance

### Check Counts
- **Total Checks**: 32
- **Automated Checks**: 22 (69%)
- **Manual Checks**: 10 (31%)

### Categories
1. Profile Configuration
2. Environment Variable Naming
3. Package Structure
4. JWT Architecture
5. Required Configuration Classes
6. OpenAPI Configuration
7. Eureka Configuration
8. Docker Compose Configuration

---

**Maintained By**: Salarean Development Team
**Last Updated**: 2025-11-22
**Contract Version**: 1.0.0
