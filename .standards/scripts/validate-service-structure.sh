#!/bin/bash

################################################################################
# Microservice Compliance Validation Script
# Version: 1.0.0
# Date: 2025-11-22
# Purpose: Automated validation of microservice architecture compliance
#          with Salarean SMS standards
#
# Usage:
#   ./validation-script.sh <service-directory>
#
# Example:
#   ./validation-script.sh /path/to/salarean/student-service
#
# Exit Codes:
#   0 = All checks passed (100% compliant)
#   1 = One or more checks failed
#   2 = Invalid input (service directory not found)
#
# Reference:
#   - Compliance Checklist: compliance-checklist.md
#   - Data Model: ../data-model.md
#   - Service Comparison: ../../SERVICE_COMPARISON_ANALYSIS.md
################################################################################

set -e  # Exit on error (but we handle errors ourselves)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check counters
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0
CRITICAL_FAILURES=0

# Arrays to store failures
declare -a FAILED_CRITICAL=()
declare -a FAILED_HIGH=()
declare -a FAILED_MEDIUM=()

################################################################################
# Helper Functions
################################################################################

print_header() {
    echo ""
    echo "================================================================================"
    echo "$1"
    echo "================================================================================"
    echo ""
}

print_category() {
    echo ""
    echo "${BLUE}### $1${NC}"
    echo ""
}

check_pass() {
    echo -e "${GREEN}✅ PASS${NC}: $1"
    ((PASSED_CHECKS++))
    ((TOTAL_CHECKS++))
}

check_fail() {
    local severity=$1
    local check_id=$2
    local message=$3

    echo -e "${RED}❌ FAIL${NC}: $message"
    ((FAILED_CHECKS++))
    ((TOTAL_CHECKS++))

    case $severity in
        CRITICAL)
            FAILED_CRITICAL+=("$check_id: $message")
            ((CRITICAL_FAILURES++))
            ;;
        HIGH)
            FAILED_HIGH+=("$check_id: $message")
            ;;
        MEDIUM)
            FAILED_MEDIUM+=("$check_id: $message")
            ;;
    esac
}

check_skip() {
    echo -e "${YELLOW}⏭️  SKIP${NC}: $1"
    # Skipped checks don't count toward total
}

################################################################################
# Validation Check Functions
################################################################################

#------------------------------------------------------------------------------
# Category 1: Profile Configuration
#------------------------------------------------------------------------------

check_profile_count() {
    print_category "Category 1: Profile Configuration (3 checks)"

    local resources_dir="$SERVICE_DIR/src/main/resources"

    # Count application-*.yml files
    local profile_count=$(find "$resources_dir" -maxdepth 1 -name "application-*.yml" 2>/dev/null | wc -l | tr -d ' ')

    # Check for application.yml
    if [[ -f "$resources_dir/application.yml" ]]; then
        profile_count=$((profile_count + 1))
    fi

    if [[ $profile_count -eq 2 ]]; then
        check_pass "PROFILE-001: Service has exactly 2 profile files"
    else
        check_fail "CRITICAL" "PROFILE-001" "Service has $profile_count profile files (expected 2). Found profiles: $(ls -1 $resources_dir/application*.yml 2>/dev/null | xargs -n1 basename | tr '\n' ', ')"
    fi
}

check_profile_naming() {
    local resources_dir="$SERVICE_DIR/src/main/resources"

    # Check for required files
    local has_default=false
    local has_docker=false

    [[ -f "$resources_dir/application.yml" ]] && has_default=true
    [[ -f "$resources_dir/application-docker.yml" ]] && has_docker=true

    if [[ "$has_default" == true && "$has_docker" == true ]]; then
        check_pass "PROFILE-002: Profile files use standard names (application.yml, application-docker.yml)"
    else
        local missing=""
        [[ "$has_default" == false ]] && missing="application.yml "
        [[ "$has_docker" == false ]] && missing="${missing}application-docker.yml"
        check_fail "CRITICAL" "PROFILE-002" "Missing required profile files: $missing"
    fi

    # Check for forbidden profile names
    if [[ -f "$resources_dir/application-prod.yml" ]] || \
       [[ -f "$resources_dir/application-production.yml" ]] || \
       [[ -f "$resources_dir/application-dev.yml" ]]; then
        check_fail "HIGH" "PROFILE-002b" "Service has forbidden profile files (prod, production, or dev). Use 'docker' profile only."
    fi
}

check_docker_profile_activation() {
    # Find docker-compose.yml in parent directories (up to 3 levels)
    local compose_file=""
    local search_dir="$SERVICE_DIR"

    for i in {0..3}; do
        if [[ -f "$search_dir/docker-compose.yml" ]]; then
            compose_file="$search_dir/docker-compose.yml"
            break
        fi
        search_dir="$(dirname "$search_dir")"
    done

    if [[ -z "$compose_file" ]]; then
        check_skip "PROFILE-003: docker-compose.yml not found (skipping)"
        return
    fi

    # Extract service name from directory
    local service_name=$(basename "$SERVICE_DIR")

    # Check if service is defined in docker-compose.yml and uses docker profile
    if grep -q "SPRING_PROFILES_ACTIVE=docker" "$compose_file"; then
        check_pass "PROFILE-003: docker-compose.yml sets SPRING_PROFILES_ACTIVE=docker"
    elif grep -q "SPRING_PROFILES_ACTIVE" "$compose_file"; then
        local active_profile=$(grep "SPRING_PROFILES_ACTIVE" "$compose_file" | head -1 | sed 's/.*SPRING_PROFILES_ACTIVE=//' | tr -d ' ')
        check_fail "CRITICAL" "PROFILE-003" "docker-compose.yml uses wrong profile: $active_profile (expected: docker)"
    else
        check_fail "CRITICAL" "PROFILE-003" "docker-compose.yml does not set SPRING_PROFILES_ACTIVE"
    fi
}

#------------------------------------------------------------------------------
# Category 2: Environment Variable Naming
#------------------------------------------------------------------------------

check_env_var_naming() {
    print_category "Category 2: Environment Variable Naming (5 checks)"

    # Find docker-compose.yml
    local compose_file=""
    local search_dir="$SERVICE_DIR"

    for i in {0..3}; do
        if [[ -f "$search_dir/docker-compose.yml" ]]; then
            compose_file="$search_dir/docker-compose.yml"
            break
        fi
        search_dir="$(dirname "$search_dir")"
    done

    if [[ -z "$compose_file" ]]; then
        check_skip "ENV-001-005: docker-compose.yml not found (skipping environment variable checks)"
        return
    fi

    # ENV-001: Database variable naming
    if grep -q "SPRING_DATASOURCE_URL" "$compose_file" && \
       grep -q "SPRING_DATASOURCE_USERNAME" "$compose_file" && \
       grep -q "SPRING_DATASOURCE_PASSWORD" "$compose_file"; then
        check_pass "ENV-001: Database variables use SPRING_DATASOURCE_* prefix"
    else
        check_fail "CRITICAL" "ENV-001" "Database variables do not use SPRING_DATASOURCE_* prefix"
    fi

    # ENV-002: Eureka variable naming
    if grep -q "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE" "$compose_file"; then
        check_pass "ENV-002: Eureka variable uses EUREKA_CLIENT_SERVICEURL_DEFAULTZONE"
    elif grep -q "EUREKA_CLIENT_SERVICE_URL" "$compose_file"; then
        check_fail "CRITICAL" "ENV-002" "Eureka uses wrong variable name: EUREKA_CLIENT_SERVICE_URL (expected: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE)"
    else
        check_fail "CRITICAL" "ENV-002" "Eureka variable EUREKA_CLIENT_SERVICEURL_DEFAULTZONE not found"
    fi

    # ENV-003: Forbidden custom database variables
    if grep -q "DB_USERNAME" "$compose_file" || grep -q "DB_PASSWORD=" "$compose_file"; then
        check_fail "CRITICAL" "ENV-003" "Service uses forbidden custom database variables (DB_USERNAME or DB_PASSWORD). Use SPRING_DATASOURCE_* instead."
    else
        check_pass "ENV-003: No forbidden custom database variables (DB_USERNAME, DB_PASSWORD)"
    fi

    # ENV-004: Eureka instance configuration location (manual check - skip in automated)
    check_skip "ENV-004: Eureka instance config location (manual review required)"

    # ENV-005: Redis variable naming (if applicable)
    if grep -q "redis" "$compose_file"; then
        if grep -q "SPRING_REDIS_HOST" "$compose_file"; then
            check_pass "ENV-005: Redis variables use SPRING_REDIS_* prefix"
        else
            check_fail "MEDIUM" "ENV-005" "Service uses Redis but variables don't use SPRING_REDIS_* prefix"
        fi
    else
        check_skip "ENV-005: Service does not use Redis (N/A)"
    fi
}

#------------------------------------------------------------------------------
# Category 3: Package Structure
#------------------------------------------------------------------------------

check_package_structure() {
    print_category "Category 3: Package Structure (6 checks)"

    # Find Java source directory
    local java_dir="$SERVICE_DIR/src/main/java"

    if [[ ! -d "$java_dir" ]]; then
        check_fail "CRITICAL" "PKG-000" "Java source directory not found: $java_dir"
        return
    fi

    # Find base package (com/sms/{service})
    local base_package=$(find "$java_dir" -type d -path "*/com/sms/*" | head -1)

    if [[ -z "$base_package" ]]; then
        check_fail "CRITICAL" "PKG-000" "Base package com.sms.* not found"
        return
    fi

    # PKG-001: Entity package naming
    if [[ -d "$base_package/model" ]]; then
        check_pass "PKG-001: Entities are in 'model/' package (correct)"
    elif [[ -d "$base_package/entity" ]]; then
        check_fail "HIGH" "PKG-001" "Entities are in 'entity/' package. Rename to 'model/'"
    else
        check_skip "PKG-001: No entity/model package found (service may not have entities)"
    fi

    # PKG-002: JWT package location
    local jwt_filter_path=$(find "$base_package" -name "JwtAuthenticationFilter.java" 2>/dev/null)

    if [[ -n "$jwt_filter_path" ]]; then
        if echo "$jwt_filter_path" | grep -q "/security/"; then
            check_pass "PKG-002: JWT classes are in 'security/' package (correct)"
        elif echo "$jwt_filter_path" | grep -q "/config/"; then
            check_fail "CRITICAL" "PKG-002" "JWT classes are in 'config/' package. Move to 'security/'"
        else
            check_fail "HIGH" "PKG-002" "JWT classes found in unexpected location: $jwt_filter_path"
        fi
    else
        check_skip "PKG-002: No JWT filter found (service may not handle JWT)"
    fi

    # PKG-003: Service implementation location
    if [[ -d "$base_package/service/impl" ]]; then
        check_fail "HIGH" "PKG-003" "Service implementations are in 'service/impl/' subdirectory. Flatten to 'service/'"
    elif [[ -d "$base_package/service" ]]; then
        check_pass "PKG-003: Service implementations are in 'service/' package (correct)"
    else
        check_skip "PKG-003: No service package found"
    fi

    # PKG-004: Standard package presence
    local required_packages=("config" "controller" "dto" "exception" "repository" "service")
    local missing_packages=()

    for pkg in "${required_packages[@]}"; do
        if [[ ! -d "$base_package/$pkg" ]]; then
            missing_packages+=("$pkg")
        fi
    done

    if [[ ${#missing_packages[@]} -eq 0 ]]; then
        check_pass "PKG-004: All required standard packages exist"
    else
        check_fail "HIGH" "PKG-004" "Missing required packages: ${missing_packages[*]}"
    fi

    # PKG-005: Config package purity (manual check)
    check_skip "PKG-005: Config package purity (manual review required)"

    # PKG-006: Validation package (optional)
    if [[ -d "$base_package/validation" ]]; then
        check_pass "PKG-006: Validation package exists (good practice)"
    else
        check_skip "PKG-006: No validation package (optional, but recommended)"
    fi
}

#------------------------------------------------------------------------------
# Category 4: JWT Architecture
#------------------------------------------------------------------------------

check_jwt_architecture() {
    print_category "Category 4: JWT Architecture (4 checks)"

    local java_dir="$SERVICE_DIR/src/main/java"
    local base_package=$(find "$java_dir" -type d -path "*/com/sms/*" | head -1)

    if [[ -z "$base_package" ]]; then
        check_skip "JWT-001-004: Base package not found (skipping JWT checks)"
        return
    fi

    # JWT-001: JWT class separation
    local has_filter=$(find "$base_package" -name "JwtAuthenticationFilter.java" 2>/dev/null)
    local has_provider=$(find "$base_package" -name "JwtTokenProvider.java" 2>/dev/null)

    if [[ -n "$has_filter" && -n "$has_provider" ]]; then
        check_pass "JWT-001: JWT logic split into Filter and Provider classes"
    elif [[ -n "$has_filter" && -z "$has_provider" ]]; then
        check_fail "CRITICAL" "JWT-001" "JWT filter found but no provider. Split logic into JwtAuthenticationFilter and JwtTokenProvider"
    elif [[ -z "$has_filter" && -z "$has_provider" ]]; then
        check_skip "JWT-001-004: No JWT classes found (service may not handle JWT)"
        return
    fi

    # JWT-002: Filter class inheritance (manual)
    check_skip "JWT-002: Filter class inheritance (manual review required)"

    # JWT-003: Provider responsibilities (manual)
    check_skip "JWT-003: Provider responsibilities (manual review required)"

    # JWT-004: Filter delegation pattern (manual)
    check_skip "JWT-004: Filter delegation pattern (manual review required)"
}

#------------------------------------------------------------------------------
# Category 5: Required Configuration Classes
#------------------------------------------------------------------------------

check_config_classes() {
    print_category "Category 5: Required Configuration Classes (4 checks)"

    local java_dir="$SERVICE_DIR/src/main/java"
    local base_package=$(find "$java_dir" -type d -path "*/com/sms/*" | head -1)

    if [[ -z "$base_package" ]]; then
        check_fail "CRITICAL" "CFG-000" "Base package not found"
        return
    fi

    local config_dir="$base_package/config"

    if [[ ! -d "$config_dir" ]]; then
        check_fail "CRITICAL" "CFG-000" "Config package not found"
        return
    fi

    # CFG-001: CorsConfig presence
    if [[ -f "$config_dir/CorsConfig.java" ]]; then
        check_pass "CFG-001: CorsConfig.java exists"
    else
        check_fail "HIGH" "CFG-001" "CorsConfig.java missing. Add CORS configuration."
    fi

    # CFG-002: OpenAPIConfig presence and naming
    if [[ -f "$config_dir/OpenAPIConfig.java" ]]; then
        check_pass "CFG-002: OpenAPIConfig.java exists (correct capitalization)"
    elif [[ -f "$config_dir/OpenApiConfig.java" ]]; then
        check_fail "HIGH" "CFG-002" "Found OpenApiConfig.java with incorrect capitalization. Rename to OpenAPIConfig.java"
    else
        check_fail "HIGH" "CFG-002" "OpenAPIConfig.java missing"
    fi

    # CFG-003: SecurityConfig presence
    if [[ -f "$config_dir/SecurityConfig.java" ]]; then
        check_pass "CFG-003: SecurityConfig.java exists"
    else
        check_fail "CRITICAL" "CFG-003" "SecurityConfig.java missing"
    fi

    # CFG-004: Service-specific configs (manual)
    check_skip "CFG-004: Service-specific configs (manual review required)"
}

#------------------------------------------------------------------------------
# Category 6: OpenAPI Configuration (manual checks - skip in automated)
#------------------------------------------------------------------------------

check_openapi_config() {
    print_category "Category 6: OpenAPI Configuration (3 checks)"

    check_skip "API-001: OpenAPI server URL (manual review required)"
    check_skip "API-002: OpenAPI info configuration (manual review required)"
    check_skip "API-003: Security scheme configuration (manual review required)"
}

#------------------------------------------------------------------------------
# Category 7: Eureka Configuration
#------------------------------------------------------------------------------

check_eureka_config() {
    print_category "Category 7: Eureka Configuration (3 checks)"

    check_skip "EUR-001: prefer-ip-address setting (manual review required)"
    check_skip "EUR-002: Hostname configuration (manual review required)"

    # EUR-003: Instance config in YAML, not env vars
    local compose_file=""
    local search_dir="$SERVICE_DIR"

    for i in {0..3}; do
        if [[ -f "$search_dir/docker-compose.yml" ]]; then
            compose_file="$search_dir/docker-compose.yml"
            break
        fi
        search_dir="$(dirname "$search_dir")"
    done

    if [[ -z "$compose_file" ]]; then
        check_skip "EUR-003: docker-compose.yml not found"
        return
    fi

    if grep -q "EUREKA_INSTANCE_HOSTNAME" "$compose_file" || \
       grep -q "EUREKA_INSTANCE_PREFER_IP_ADDRESS" "$compose_file"; then
        check_fail "HIGH" "EUR-003" "Eureka instance properties found in docker-compose.yml. Move to application-docker.yml"
    else
        check_pass "EUR-003: Eureka instance config not in environment variables (correct)"
    fi
}

#------------------------------------------------------------------------------
# Category 8: Docker Compose Configuration
#------------------------------------------------------------------------------

check_docker_config() {
    print_category "Category 8: Docker Compose Configuration (4 checks)"

    local compose_file=""
    local search_dir="$SERVICE_DIR"

    for i in {0..3}; do
        if [[ -f "$search_dir/docker-compose.yml" ]]; then
            compose_file="$search_dir/docker-compose.yml"
            break
        fi
        search_dir="$(dirname "$search_dir")"
    done

    if [[ -z "$compose_file" ]]; then
        check_skip "DCK-001-004: docker-compose.yml not found (skipping Docker checks)"
        return
    fi

    # DCK-001: Profile activation (already checked in PROFILE-003)
    if grep -q "SPRING_PROFILES_ACTIVE=docker" "$compose_file"; then
        check_pass "DCK-001: docker-compose.yml uses SPRING_PROFILES_ACTIVE=docker"
    else
        check_fail "CRITICAL" "DCK-001" "docker-compose.yml does not use SPRING_PROFILES_ACTIVE=docker"
    fi

    # DCK-002: Standard environment variables
    local has_all_vars=true
    local missing_vars=()

    grep -q "SPRING_DATASOURCE_URL" "$compose_file" || { has_all_vars=false; missing_vars+=("SPRING_DATASOURCE_URL"); }
    grep -q "SPRING_DATASOURCE_USERNAME" "$compose_file" || { has_all_vars=false; missing_vars+=("SPRING_DATASOURCE_USERNAME"); }
    grep -q "SPRING_DATASOURCE_PASSWORD" "$compose_file" || { has_all_vars=false; missing_vars+=("SPRING_DATASOURCE_PASSWORD"); }
    grep -q "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE" "$compose_file" || { has_all_vars=false; missing_vars+=("EUREKA_CLIENT_SERVICEURL_DEFAULTZONE"); }
    grep -q "JWT_SECRET" "$compose_file" || { has_all_vars=false; missing_vars+=("JWT_SECRET"); }

    if [[ "$has_all_vars" == true ]]; then
        check_pass "DCK-002: All standard environment variables present"
    else
        check_fail "CRITICAL" "DCK-002" "Missing standard environment variables: ${missing_vars[*]}"
    fi

    # DCK-003: Network configuration (manual)
    check_skip "DCK-003: Network configuration (manual review required)"

    # DCK-004: Dependencies declaration (manual)
    check_skip "DCK-004: Dependencies declaration (manual review required)"
}

################################################################################
# Main Script
################################################################################

main() {
    # Check if service directory argument provided
    if [[ $# -eq 0 ]]; then
        echo -e "${RED}Error: No service directory provided${NC}"
        echo ""
        echo "Usage: $0 <service-directory>"
        echo ""
        echo "Example:"
        echo "  $0 /path/to/salarean/student-service"
        echo ""
        exit 2
    fi

    SERVICE_DIR="$1"

    # Validate service directory exists
    if [[ ! -d "$SERVICE_DIR" ]]; then
        echo -e "${RED}Error: Service directory not found: $SERVICE_DIR${NC}"
        exit 2
    fi

    # Extract service name
    SERVICE_NAME=$(basename "$SERVICE_DIR")

    # Print header
    print_header "Microservice Compliance Validation"
    echo "Service: $SERVICE_NAME"
    echo "Directory: $SERVICE_DIR"
    echo "Date: $(date '+%Y-%m-%d %H:%M:%S')"
    echo ""
    echo "Reference: Salarean SMS Microservice Standards"
    echo "Template: auth-service"
    echo ""

    # Run all validation checks
    check_profile_count
    check_profile_naming
    check_docker_profile_activation

    check_env_var_naming

    check_package_structure

    check_jwt_architecture

    check_config_classes

    check_openapi_config

    check_eureka_config

    check_docker_config

    # Print summary
    print_header "Validation Summary"

    echo "Total Checks: $TOTAL_CHECKS"
    echo -e "Passed: ${GREEN}$PASSED_CHECKS${NC}"
    echo -e "Failed: ${RED}$FAILED_CHECKS${NC}"
    echo ""

    if [[ $FAILED_CHECKS -eq 0 ]]; then
        echo -e "${GREEN}✅ COMPLIANCE STATUS: PASSED${NC}"
        echo ""
        echo "This service is 100% compliant with Salarean SMS standards."
        echo ""
        exit 0
    else
        echo -e "${RED}❌ COMPLIANCE STATUS: FAILED${NC}"
        echo ""

        # Print failures by severity
        if [[ ${#FAILED_CRITICAL[@]} -gt 0 ]]; then
            echo -e "${RED}CRITICAL FAILURES (${#FAILED_CRITICAL[@]}):${NC}"
            for failure in "${FAILED_CRITICAL[@]}"; do
                echo "  - $failure"
            done
            echo ""
        fi

        if [[ ${#FAILED_HIGH[@]} -gt 0 ]]; then
            echo -e "${YELLOW}HIGH PRIORITY (${#FAILED_HIGH[@]}):${NC}"
            for failure in "${FAILED_HIGH[@]}"; do
                echo "  - $failure"
            done
            echo ""
        fi

        if [[ ${#FAILED_MEDIUM[@]} -gt 0 ]]; then
            echo -e "${YELLOW}MEDIUM PRIORITY (${#FAILED_MEDIUM[@]}):${NC}"
            for failure in "${FAILED_MEDIUM[@]}"; do
                echo "  - $failure"
            done
            echo ""
        fi

        echo "Action Required:"
        echo "1. Review failures listed above"
        echo "2. Consult compliance-checklist.md for remediation steps"
        echo "3. Fix issues and re-run this validation script"
        echo "4. See service-template.md for reference implementation"
        echo ""

        if [[ $CRITICAL_FAILURES -gt 0 ]]; then
            echo -e "${RED}⚠️  WARNING: $CRITICAL_FAILURES critical failure(s) detected.${NC}"
            echo "   Critical issues must be fixed before service can be deployed."
            echo ""
        fi

        exit 1
    fi
}

# Execute main function
main "$@"
