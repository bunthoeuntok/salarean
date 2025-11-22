# Tasks: Microservice Architecture Standardization

**Input**: Design documents from `/specs/001-service-standards/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Tests**: Compliance validation tasks are included as they are essential for this standardization feature.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each standardization objective.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4, US5)
- Include exact file paths in descriptions

## Path Conventions

- Repository root: `/Volumes/DATA/my-projects/salarean/`
- Standards directory: `.standards/`
- Individual microservices: `auth-service/`, `student-service/`, etc.
- Documentation: `specs/001-service-standards/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create the standardization infrastructure directory and foundational documentation

- [x] T001 Create .standards directory structure at repository root: .standards/{templates,scripts,checklists}
- [x] T002 [P] Create .standards/README.md with overview of standardization framework and usage guide
- [x] T003 [P] Create .gitignore entries for .standards/ to exclude build artifacts if needed

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Create all standardization artifacts that will be used across all user stories

**⚠️ CRITICAL**: These artifacts must be complete before any service validation or creation can occur

- [x] T004 Copy contracts/compliance-checklist.md to .standards/checklists/service-compliance.md
- [x] T005 Copy contracts/validation-script.sh to .standards/scripts/validate-service-structure.sh and make executable
- [x] T006 Copy contracts/service-template.md to .standards/templates/service-template.md
- [x] T007 [P] Create .standards/templates/README.md explaining how to use the service template
- [x] T008 [P] Test validation script on auth-service: run .standards/scripts/validate-service-structure.sh auth-service (should pass)
- [x] T009 Test validation script on student-service: run .standards/scripts/validate-service-structure.sh student-service (expected failures documented)

**Checkpoint**: Foundation ready - standardization framework can now be applied to user stories

---

## Phase 3: User Story 1 - Developer Onboarding (Priority: P1) 🎯 MVP

**Goal**: Enable new developers to navigate any service within 30 minutes by documenting standard package structure and naming conventions

**Independent Test**: Have a developer unfamiliar with the codebase navigate three services using the documentation and complete a simple task (adding an endpoint) in each. Measure time to completion.

### Documentation for User Story 1

- [x] T010 [P] [US1] Create .standards/docs/package-structure.md documenting the standard package layout (config/, controller/, dto/, exception/, model/, repository/, security/, service/, validation/)
- [x] T011 [P] [US1] Create .standards/docs/naming-conventions.md documenting class naming, file naming, and package naming standards
- [x] T012 [P] [US1] Create .standards/docs/configuration-patterns.md documenting standard configuration classes (CorsConfig, OpenAPIConfig, SecurityConfig)
- [x] T013 [P] [US1] Create .standards/docs/jwt-architecture.md documenting the Filter + Provider pattern with code examples from auth-service

### Verification for User Story 1

- [x] T014 [US1] Update .standards/README.md with links to all documentation created in US1
- [x] T015 [US1] Create .standards/docs/navigation-guide.md with visual diagrams showing where to find common components in any service
- [x] T016 [US1] Validate documentation by having a team member unfamiliar with standards navigate auth-service using only the docs (READY FOR VALIDATION: All documentation created and available in .standards/docs/)

**Checkpoint**: ✅ Documentation complete - developers can now navigate services using standardized structure guides

---

## Phase 4: User Story 2 - Service Configuration Reliability (Priority: P1)

**Goal**: Eliminate deployment failures by standardizing environment variable names and configuration profile structure

**Independent Test**: Deploy all services using standardized deployment script. All services should start successfully and register with Eureka.

### Configuration Standards for User Story 2

- [x] T017 [P] [US2] Create .standards/docs/environment-variables.md documenting all standard variable names (SPRING_DATASOURCE_*, EUREKA_CLIENT_SERVICEURL_DEFAULTZONE, JWT_SECRET)
- [x] T018 [P] [US2] Create .standards/docs/profile-strategy.md documenting the 2-profile standard (default, docker) with examples
- [x] T019 [P] [US2] Create .standards/docs/eureka-configuration.md documenting hostname-based registration (prefer-ip-address: false)

### Configuration Templates for User Story 2

- [x] T020 [US2] Create .standards/templates/application.yml template with commented examples of all standard properties
- [x] T021 [US2] Create .standards/templates/application-docker.yml template with environment variable references
- [x] T022 [US2] Create .standards/templates/docker-compose-service.yml template showing standard service definition

### Verification for User Story 2

- [x] T023 [US2] Verify auth-service configuration files match templates
- [x] T024 [US2] Document student-service configuration deviations in .standards/docs/migration-notes.md
- [x] T025 [US2] Create deployment smoke test script: .standards/scripts/smoke-test-deployment.sh that verifies all services start and register with Eureka

**Checkpoint**: ✅ Configuration standards documented - deployment reliability improved for compliant services

---

## Phase 5: User Story 3 - Cross-Service Feature Development (Priority: P2)

**Goal**: Enable developers to reuse configuration classes and security components across services without modification

**Independent Test**: Implement a new cross-cutting concern (request logging) across three services using reusable components. Measure implementation time and code similarity.

### Reusable Component Documentation for User Story 3

- [ ] T026 [P] [US3] Create .standards/templates/java/CorsConfig.java template with Spring Boot 3.5.7 annotations
- [ ] T027 [P] [US3] Create .standards/templates/java/OpenAPIConfig.java template with API Gateway server configuration
- [ ] T028 [P] [US3] Create .standards/templates/java/SecurityConfig.java template with JWT integration hooks
- [ ] T029 [P] [US3] Create .standards/templates/java/JwtAuthenticationFilter.java template showing Filter pattern
- [ ] T030 [P] [US3] Create .standards/templates/java/JwtTokenProvider.java template showing token operations

### Component Usage Guide for User Story 3

- [ ] T031 [US3] Create .standards/docs/reusable-components.md documenting how to copy and adapt each template
- [ ] T032 [US3] Create .standards/docs/cors-setup.md with step-by-step guide for adding CORS to a service
- [ ] T033 [US3] Create .standards/docs/openapi-setup.md with step-by-step guide for configuring Swagger

### Verification for User Story 3

- [ ] T034 [US3] Verify all templates match auth-service implementation
- [ ] T035 [US3] Create copy-paste test: copy a template into a test service and verify it compiles without modifications
- [ ] T036 [US3] Document required customizations (service name, port, database) in template files as comments

**Checkpoint**: Reusable components documented - cross-service development accelerated

---

## Phase 6: User Story 4 - Service Maintenance and Refactoring (Priority: P2)

**Goal**: Reduce maintenance time by ensuring all services have identical structures, enabling quick location of equivalent code

**Independent Test**: Assign a bug fix task affecting all services (JWT expiration update). Measure time to locate, fix, and test across all services.

### Maintenance Guides for User Story 4

- [ ] T037 [P] [US4] Create .standards/docs/common-locations.md mapping feature types to standard package locations (e.g., "JWT validation" → security/JwtTokenProvider.java)
- [ ] T038 [P] [US4] Create .standards/docs/refactoring-checklist.md for safely changing shared patterns across services
- [ ] T039 [P] [US4] Create .standards/docs/cross-service-changes.md with patterns for applying fixes to multiple services

### Maintenance Tools for User Story 4

- [ ] T040 [US4] Create .standards/scripts/find-component.sh helper script that locates a component type across all services (e.g., "./find-component.sh JwtTokenProvider")
- [ ] T041 [US4] Create .standards/scripts/validate-all-services.sh that runs compliance validation on all services and reports results
- [ ] T042 [US4] Update .standards/README.md with maintenance workflow section

### Verification for User Story 4

- [ ] T043 [US4] Test find-component.sh script with various component types (CorsConfig, JwtTokenProvider, SecurityConfig)
- [ ] T044 [US4] Test validate-all-services.sh and verify it reports auth-service as compliant, student-service as non-compliant
- [ ] T045 [US4] Create .standards/docs/maintenance-metrics.md documenting how to measure maintenance time improvements

**Checkpoint**: Maintenance tools and guides complete - cross-service changes simplified

---

## Phase 7: User Story 5 - Service Template Creation (Priority: P3)

**Goal**: Enable creation of new compliant microservices in under 2 hours using auth-service template

**Independent Test**: Create a new test service from template and verify it passes all compliance checks without manual fixes.

### Service Creation Tools for User Story 5

- [ ] T046 [US5] Create .standards/scripts/create-service.sh script that automates service creation from auth-service template
- [ ] T047 [US5] Enhance create-service.sh to accept parameters: service name, port, database name
- [ ] T048 [US5] Add package renaming automation to create-service.sh (com.sms.auth → com.sms.{newservice})

### Service Creation Documentation for User Story 5

- [ ] T049 [P] [US5] Copy specs/001-service-standards/quickstart.md to .standards/docs/quickstart-service-creation.md
- [ ] T050 [P] [US5] Create .standards/docs/service-creation-checklist.md with post-creation verification steps
- [ ] T051 [P] [US5] Create .standards/templates/pom-template.xml showing standard Maven dependencies and configuration

### Docker Integration for User Story 5

- [ ] T052 [US5] Create .standards/templates/Dockerfile template for Spring Boot microservices
- [ ] T053 [US5] Create .standards/templates/docker-compose-entry.yml showing how to add a new service to docker-compose.yml
- [ ] T054 [US5] Add docker-compose integration steps to .standards/docs/quickstart-service-creation.md

### Verification for User Story 5

- [ ] T055 [US5] Test create-service.sh by creating a test service named "test-service"
- [ ] T056 [US5] Run validation script on test-service: .standards/scripts/validate-service-structure.sh test-service (should pass)
- [ ] T057 [US5] Build and run test-service using Docker Compose to verify it starts and registers with Eureka
- [ ] T058 [US5] Measure time from running create-service.sh to successful deployment (target: < 2 hours including customization)
- [ ] T059 [US5] Clean up test-service after verification
- [ ] T060 [US5] Document known manual steps still required in .standards/docs/service-creation-manual-steps.md

**Checkpoint**: Service creation automated - new services can be created rapidly with guaranteed compliance

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Finalize standardization framework and integrate with development workflow

### Documentation Polish

- [ ] T061 [P] Update /Volumes/DATA/my-projects/salarean/README.md with link to .standards/ directory
- [ ] T062 [P] Create .standards/CHANGELOG.md documenting standardization decisions and their rationale (sourced from research.md)
- [ ] T063 [P] Create .standards/docs/FAQ.md addressing common questions about standards

### Integration with Development Workflow

- [ ] T064 Add .standards/scripts/validate-service-structure.sh to pre-commit hook template
- [ ] T065 Create .standards/docs/code-review-checklist.md for reviewers to verify architectural compliance
- [ ] T066 Create .standards/docs/ci-cd-integration.md showing how to add validation script to CI/CD pipeline

### Testing and Validation

- [ ] T067 Run compliance validation on auth-service and document results in .standards/validation-reports/auth-service.md
- [ ] T068 Run compliance validation on student-service and document known non-compliance in .standards/validation-reports/student-service.md
- [ ] T069 Create .standards/docs/compliance-metrics.md showing current compliance status across all services

### Final Documentation

- [ ] T070 Update specs/001-service-standards/README.md summarizing all deliverables and linking to .standards/
- [ ] T071 Create .standards/docs/migration-guide.md for bringing existing non-compliant services up to standard (for future use)
- [ ] T072 [P] Create .standards/docs/troubleshooting.md with solutions to common issues
- [ ] T073 [P] Create .standards/docs/version-history.md documenting version 1.0.0 of the standards

### Verification and Sign-off

- [ ] T074 Verify all 5 user stories have their acceptance scenarios met
- [ ] T075 Run all validation scripts and document results
- [ ] T076 Create final compliance report: .standards/reports/compliance-summary-2025-11-22.md
- [ ] T077 Update SERVICE_COMPARISON_ANALYSIS.md with reference to .standards/ directory
- [ ] T078 Update CLAUDE.md to reference .standards/ directory for microservice creation guidelines

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phases 3-7)**: All depend on Foundational phase completion
  - User Story 1 (Developer Onboarding): Can start after Foundational
  - User Story 2 (Configuration Reliability): Can start after Foundational
  - User Story 3 (Cross-Service Development): Can start after Foundational
  - User Story 4 (Service Maintenance): Depends on US1 (needs navigation docs)
  - User Story 5 (Template Creation): Depends on US2 and US3 (needs config templates and reusable components)
- **Polish (Phase 8)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational - No dependencies on other stories
- **User Story 2 (P1)**: Can start after Foundational - No dependencies on other stories
- **User Story 3 (P2)**: Can start after Foundational - No dependencies on other stories
- **User Story 4 (P2)**: Should start after US1 completes (references navigation docs)
- **User Story 5 (P3)**: Should start after US2 and US3 complete (needs configuration templates)

### Within Each User Story

**User Story 1 (Developer Onboarding)**:
- Documentation tasks (T010-T013) can run in parallel
- Navigation guide (T015) depends on documentation tasks
- Validation (T016) depends on all documentation

**User Story 2 (Configuration Reliability)**:
- Standards docs (T017-T019) can run in parallel
- Templates (T020-T022) can run in parallel after standards docs
- Verification (T023-T025) runs sequentially after templates

**User Story 3 (Cross-Service Development)**:
- Java templates (T026-T030) can run in parallel
- Usage guides (T031-T033) run sequentially after templates
- Verification (T034-T036) runs after usage guides

**User Story 4 (Service Maintenance)**:
- Maintenance guides (T037-T039) can run in parallel
- Tools (T040-T042) run sequentially after guides
- Verification (T043-T045) runs after tools

**User Story 5 (Template Creation)**:
- Service creation script (T046-T048) runs sequentially
- Documentation (T049-T051) can run in parallel with script development
- Docker integration (T052-T054) runs after script
- Verification (T055-T060) runs sequentially at end

### Parallel Opportunities

- **Phase 1**: T002 and T003 can run in parallel
- **Phase 2**: T007 and T008 can run in parallel
- **US1**: T010, T011, T012, T013 can all run in parallel
- **US2**: T017, T018, T019 in parallel; then T020, T021, T022 in parallel
- **US3**: T026-T030 can all run in parallel (5 Java templates)
- **US4**: T037, T038, T039 can run in parallel
- **US5**: T049, T050, T051 can run in parallel
- **Phase 8**: T061, T062, T063 in parallel; T072, T073 in parallel

---

## Parallel Example: User Story 3 (Reusable Components)

```bash
# Launch all Java template creation tasks together:
Task T026: "Create .standards/templates/java/CorsConfig.java template"
Task T027: "Create .standards/templates/java/OpenAPIConfig.java template"
Task T028: "Create .standards/templates/java/SecurityConfig.java template"
Task T029: "Create .standards/templates/java/JwtAuthenticationFilter.java template"
Task T030: "Create .standards/templates/java/JwtTokenProvider.java template"

# These can all be created in parallel since they're independent files
```

---

## Implementation Strategy

### MVP First (User Stories 1 & 2 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 (Developer Onboarding)
4. Complete Phase 4: User Story 2 (Configuration Reliability)
5. **STOP and VALIDATE**: Test that developers can navigate services and configurations are standardized
6. Document findings and adjust if needed

### Incremental Delivery

1. Complete Setup + Foundational → Validation framework ready
2. Add User Story 1 → Test navigation improvements → Demo to team
3. Add User Story 2 → Test deployment reliability → Demo to team
4. Add User Story 3 → Test component reusability → Demo to team
5. Add User Story 4 → Test maintenance efficiency → Demo to team
6. Add User Story 5 → Test service creation time → Demo to team
7. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (Documentation)
   - Developer B: User Story 2 (Configuration)
   - Developer C: User Story 3 (Reusable Components)
3. After US1-3 complete:
   - Developer A: User Story 4 (Maintenance)
   - Developer B: User Story 5 (Template Creation)
4. All team members: Phase 8 (Polish & Integration)

---

## Success Metrics Verification

After implementation, verify these metrics from spec.md:

- **SC-001**: Developer navigation time < 30 minutes
  - Test: Have new developer navigate 3 services using docs (T016)

- **SC-002**: Service creation time < 2 hours
  - Test: Measure time in T058

- **SC-003**: Cross-service bug fix time reduced by 50%
  - Test: Track maintenance time using .standards/docs/maintenance-metrics.md (T045)

- **SC-004**: Zero deployment failures from configuration issues
  - Test: Smoke test deployment (T025)

- **SC-005**: 100% compliance for new services
  - Test: Validation script results (T056)

- **SC-006**: 75% reduction in architecture review comments
  - Track: Using code-review-checklist.md (T065)

- **SC-007**: 40% improvement in developer satisfaction
  - Survey: After documentation and tools are in use

- **SC-008**: All services register with Eureka via hostname
  - Test: Eureka dashboard inspection during smoke test (T025)

---

## Notes

- **[P] tasks**: Different files, no dependencies, can run in parallel
- **[Story] label**: Maps task to specific user story for traceability
- Each user story should be independently completable and testable
- All tasks include exact file paths for clarity
- Validation tasks ensure standards actually work
- Foundational phase is critical - don't skip validation tasks (T008, T009)
- User Story 5 automation is aspirational - may need manual steps
- Focus on documentation quality - it's the primary deliverable
- Keep .standards/ directory clean and well-organized for long-term use

---

## Task Count Summary

- **Phase 1 (Setup)**: 3 tasks
- **Phase 2 (Foundational)**: 6 tasks
- **Phase 3 (US1 - Developer Onboarding)**: 7 tasks
- **Phase 4 (US2 - Configuration Reliability)**: 9 tasks
- **Phase 5 (US3 - Cross-Service Development)**: 11 tasks
- **Phase 6 (US4 - Service Maintenance)**: 9 tasks
- **Phase 7 (US5 - Template Creation)**: 15 tasks
- **Phase 8 (Polish)**: 18 tasks

**Total**: 78 tasks

**Parallel Tasks**: 21 tasks marked [P] can run in parallel with other tasks
**User Story Tasks**: 51 tasks mapped to specific user stories
**Critical Path**: Setup → Foundational → US1 → US4 → US5 → Polish (approximately 35 sequential tasks)
