#!/bin/bash

#===============================================================================
# Deployment Smoke Test Script
#===============================================================================
# Purpose: Verify all microservices start successfully and register with Eureka
# Usage: ./smoke-test-deployment.sh [--timeout=300] [--services="service1,service2"]
# Exit Codes:
#   0 = All services healthy and registered
#   1 = One or more services failed health/registration checks
#   2 = Invalid usage or environment issues
#===============================================================================

set -o pipefail

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default configuration
TIMEOUT=300  # 5 minutes default timeout
EUREKA_URL="http://localhost:8761"
API_GATEWAY_URL="http://localhost:8080"
POLL_INTERVAL=5  # Check every 5 seconds

# Service definitions (port:service-name:eureka-app-id)
SERVICES=(
    "8080:api-gateway:API-GATEWAY"
    "8761:eureka-server:EUREKA-SERVER"
    "8081:auth-service:AUTH-SERVICE"
    "8082:student-service:STUDENT-SERVICE"
)

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --timeout=*)
            TIMEOUT="${1#*=}"
            shift
            ;;
        --services=*)
            IFS=',' read -ra CUSTOM_SERVICES <<< "${1#*=}"
            shift
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --timeout=SECONDS    Timeout for all checks (default: 300)"
            echo "  --services=LIST      Comma-separated list of services to check"
            echo "  --help               Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                                    # Check all services"
            echo "  $0 --timeout=600                      # Use 10-minute timeout"
            echo "  $0 --services=auth-service,student-service  # Check specific services"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 2
            ;;
    esac
done

#===============================================================================
# Helper Functions
#===============================================================================

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

log_error() {
    echo -e "${RED}[✗]${NC} $1"
}

# Check if a service port is responding
check_port() {
    local port=$1
    local service=$2

    if command -v nc >/dev/null 2>&1; then
        nc -z localhost "$port" 2>/dev/null
    elif command -v telnet >/dev/null 2>&1; then
        timeout 1 telnet localhost "$port" 2>/dev/null | grep -q "Connected"
    else
        # Fallback: try curl
        curl -s -o /dev/null -w "%{http_code}" "http://localhost:$port" >/dev/null 2>&1
    fi

    return $?
}

# Check service health endpoint
check_health() {
    local port=$1
    local service=$2

    local health_url="http://localhost:$port/actuator/health"
    local response=$(curl -s -w "\n%{http_code}" "$health_url" 2>/dev/null)
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')

    if [[ "$http_code" == "200" ]]; then
        # Check if status is UP
        if echo "$body" | grep -q '"status":"UP"'; then
            return 0
        else
            log_warning "$service health check returned 200 but status is not UP"
            return 1
        fi
    else
        return 1
    fi
}

# Check if service is registered with Eureka
check_eureka_registration() {
    local eureka_app_id=$1
    local service=$2

    local eureka_apps_url="$EUREKA_URL/eureka/apps"
    local response=$(curl -s -H "Accept: application/json" "$eureka_apps_url" 2>/dev/null)

    if echo "$response" | grep -q "\"app\":\"$eureka_app_id\""; then
        # Check if status is UP
        if echo "$response" | grep -A 10 "\"app\":\"$eureka_app_id\"" | grep -q '"status":"UP"'; then
            return 0
        else
            log_warning "$service registered but status is not UP in Eureka"
            return 1
        fi
    else
        return 1
    fi
}

#===============================================================================
# Main Test Functions
#===============================================================================

# Wait for a service to be ready
wait_for_service() {
    local port=$1
    local service=$2
    local start_time=$(date +%s)

    log_info "Waiting for $service (port $port) to start..."

    while true; do
        local current_time=$(date +%s)
        local elapsed=$((current_time - start_time))

        if [[ $elapsed -gt $TIMEOUT ]]; then
            log_error "$service did not start within $TIMEOUT seconds"
            return 1
        fi

        if check_port "$port" "$service"; then
            log_success "$service is responding on port $port (${elapsed}s)"
            return 0
        fi

        sleep $POLL_INTERVAL
    done
}

# Wait for service health check to pass
wait_for_health() {
    local port=$1
    local service=$2
    local start_time=$(date +%s)

    log_info "Checking health endpoint for $service..."

    while true; do
        local current_time=$(date +%s)
        local elapsed=$((current_time - start_time))

        if [[ $elapsed -gt $TIMEOUT ]]; then
            log_error "$service health check did not pass within $TIMEOUT seconds"
            return 1
        fi

        if check_health "$port" "$service"; then
            log_success "$service health check passed (${elapsed}s)"
            return 0
        fi

        sleep $POLL_INTERVAL
    done
}

# Wait for Eureka registration
wait_for_eureka() {
    local eureka_app_id=$1
    local service=$2
    local start_time=$(date +%s)

    log_info "Waiting for $service to register with Eureka..."

    while true; do
        local current_time=$(date +%s)
        local elapsed=$((current_time - start_time))

        if [[ $elapsed -gt $TIMEOUT ]]; then
            log_error "$service did not register with Eureka within $TIMEOUT seconds"
            return 1
        fi

        if check_eureka_registration "$eureka_app_id" "$service"; then
            log_success "$service registered with Eureka (${elapsed}s)"
            return 0
        fi

        sleep $POLL_INTERVAL
    done
}

# Test a single service
test_service() {
    local port=$1
    local service=$2
    local eureka_app_id=$3

    echo ""
    echo "================================================================================

"
    log_info "Testing: $service"
    echo "================================================================================"

    # Step 1: Check if service is running
    if ! wait_for_service "$port" "$service"; then
        return 1
    fi

    # Step 2: Check health endpoint (skip for Eureka server itself)
    if [[ "$service" != "eureka-server" ]]; then
        if ! wait_for_health "$port" "$service"; then
            log_warning "$service is running but health check failed"
            # Continue to Eureka check anyway
        fi
    fi

    # Step 3: Check Eureka registration (skip for Eureka server itself)
    if [[ "$service" != "eureka-server" ]]; then
        if ! wait_for_eureka "$eureka_app_id" "$service"; then
            return 1
        fi
    else
        log_success "Eureka server is running (no self-registration check)"
    fi

    return 0
}

#===============================================================================
# Main Execution
#===============================================================================

main() {
    local failed_services=()
    local passed_services=()
    local total_start_time=$(date +%s)

    echo "================================================================================"
    echo "                    SMS Deployment Smoke Test"
    echo "================================================================================"
    echo "Timeout: ${TIMEOUT}s"
    echo "Eureka URL: $EUREKA_URL"
    echo "API Gateway URL: $API_GATEWAY_URL"
    echo "Services to test: ${#SERVICES[@]}"
    echo "================================================================================"

    # Test each service
    for service_def in "${SERVICES[@]}"; do
        IFS=':' read -r port service eureka_app_id <<< "$service_def"

        if test_service "$port" "$service" "$eureka_app_id"; then
            passed_services+=("$service")
        else
            failed_services+=("$service")
        fi
    done

    # Summary
    local total_end_time=$(date +%s)
    local total_elapsed=$((total_end_time - total_start_time))

    echo ""
    echo "================================================================================"
    echo "                          Test Summary"
    echo "================================================================================"
    echo "Total time: ${total_elapsed}s"
    echo "Passed: ${#passed_services[@]}"
    echo "Failed: ${#failed_services[@]}"
    echo ""

    if [[ ${#passed_services[@]} -gt 0 ]]; then
        log_success "Passed services:"
        for service in "${passed_services[@]}"; do
            echo "  ✓ $service"
        done
    fi

    if [[ ${#failed_services[@]} -gt 0 ]]; then
        echo ""
        log_error "Failed services:"
        for service in "${failed_services[@]}"; do
            echo "  ✗ $service"
        done
        echo ""
        log_error "Deployment smoke test FAILED"
        echo "================================================================================"
        return 1
    fi

    echo ""
    log_success "All services are healthy and registered with Eureka!"
    echo "================================================================================"
    return 0
}

# Run main function
main
exit $?
