#!/bin/bash

#
# validate-all-services.sh - Run compliance validation on all microservices
#
# Usage: ./validate-all-services.sh [--json] [--detailed] [--fail-fast]
#
# Options:
#   --json        Output results in JSON format
#   --detailed    Show detailed validation output for each service
#   --fail-fast   Stop on first service validation failure
#
# Exit Codes:
#   0 = All services passed validation
#   1 = One or more services failed validation
#   2 = Validation script not found or invalid usage
#

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
VALIDATION_SCRIPT="$PROJECT_ROOT/.standards/scripts/validate-service-structure.sh"
SERVICES_PATTERN="*-service"

# Flags
JSON_OUTPUT=false
DETAILED_OUTPUT=false
FAIL_FAST=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --json)
            JSON_OUTPUT=true
            shift
            ;;
        --detailed)
            DETAILED_OUTPUT=true
            shift
            ;;
        --fail-fast)
            FAIL_FAST=true
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [--json] [--detailed] [--fail-fast]"
            echo ""
            echo "Options:"
            echo "  --json        Output results in JSON format"
            echo "  --detailed    Show detailed validation output for each service"
            echo "  --fail-fast   Stop on first service validation failure"
            echo "  --help        Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 2
            ;;
    esac
done

# Logging functions (only used in non-JSON mode)
log_info() {
    if [ "$JSON_OUTPUT" = false ]; then
        echo -e "${BLUE}[INFO]${NC} $1"
    fi
}

log_success() {
    if [ "$JSON_OUTPUT" = false ]; then
        echo -e "${GREEN}[PASS]${NC} $1"
    fi
}

log_warning() {
    if [ "$JSON_OUTPUT" = false ]; then
        echo -e "${YELLOW}[WARN]${NC} $1"
    fi
}

log_error() {
    if [ "$JSON_OUTPUT" = false ]; then
        echo -e "${RED}[FAIL]${NC} $1"
    fi
}

# Print header
print_header() {
    if [ "$JSON_OUTPUT" = false ]; then
        echo ""
        echo "================================================================"
        echo "  Service Compliance Validation - All Services"
        echo "================================================================"
        echo ""
    fi
}

# Check prerequisites
check_prerequisites() {
    if [ ! -f "$VALIDATION_SCRIPT" ]; then
        log_error "Validation script not found: $VALIDATION_SCRIPT"
        exit 2
    fi

    if [ ! -x "$VALIDATION_SCRIPT" ]; then
        log_error "Validation script is not executable: $VALIDATION_SCRIPT"
        log_info "Run: chmod +x $VALIDATION_SCRIPT"
        exit 2
    fi
}

# Main validation function
main() {
    if [ "$JSON_OUTPUT" = false ]; then
        print_header
    fi

    check_prerequisites

    # Find all service directories
    SERVICES=$(find "$PROJECT_ROOT" -maxdepth 1 -type d -name "$SERVICES_PATTERN" 2>/dev/null | sort)

    if [ -z "$SERVICES" ]; then
        log_error "No services found matching pattern: $SERVICES_PATTERN"
        exit 2
    fi

    # Count services
    SERVICE_COUNT=$(echo "$SERVICES" | wc -l | tr -d ' ')

    if [ "$JSON_OUTPUT" = false ]; then
        log_info "Found $SERVICE_COUNT service(s) to validate"
        echo ""
    fi

    # Initialize counters
    PASSED=0
    FAILED=0
    TOTAL=0

    # Array to store results (for JSON output)
    declare -a RESULTS

    # Validate each service
    for service_dir in $SERVICES; do
        service_name=$(basename "$service_dir")
        TOTAL=$((TOTAL + 1))

        if [ "$JSON_OUTPUT" = false ] && [ "$DETAILED_OUTPUT" = false ]; then
            echo "────────────────────────────────────────────────────────────────"
            echo "Validating: $service_name"
            echo "────────────────────────────────────────────────────────────────"
        fi

        # Run validation script
        if [ "$DETAILED_OUTPUT" = true ]; then
            # Show detailed output
            validation_output=$("$VALIDATION_SCRIPT" "$service_name" 2>&1)
            validation_exit_code=$?

            echo "$validation_output"
        else
            # Capture output silently
            validation_output=$("$VALIDATION_SCRIPT" "$service_name" 2>&1)
            validation_exit_code=$?
        fi

        # Process result
        if [ $validation_exit_code -eq 0 ]; then
            PASSED=$((PASSED + 1))

            if [ "$JSON_OUTPUT" = false ]; then
                log_success "$service_name - All checks passed"
            fi

            RESULTS+=("{\"service\":\"$service_name\",\"status\":\"PASS\",\"exit_code\":0}")
        else
            FAILED=$((FAILED + 1))

            if [ "$JSON_OUTPUT" = false ]; then
                log_error "$service_name - Validation failed"

                # Show failure details if not in detailed mode
                if [ "$DETAILED_OUTPUT" = false ]; then
                    echo ""
                    echo "Failure details:"
                    echo "$validation_output" | grep -E "FAIL|ERROR|✗" || echo "$validation_output"
                fi
            fi

            # Escape quotes in validation output for JSON
            escaped_output=$(echo "$validation_output" | sed 's/"/\\"/g' | tr '\n' ' ')
            RESULTS+=("{\"service\":\"$service_name\",\"status\":\"FAIL\",\"exit_code\":$validation_exit_code,\"output\":\"$escaped_output\"}")

            # Fail fast if requested
            if [ "$FAIL_FAST" = true ]; then
                if [ "$JSON_OUTPUT" = false ]; then
                    echo ""
                    log_error "Stopping validation (--fail-fast enabled)"
                fi
                break
            fi
        fi

        if [ "$JSON_OUTPUT" = false ]; then
            echo ""
        fi
    done

    # Output results
    if [ "$JSON_OUTPUT" = true ]; then
        # JSON output
        echo "{"
        echo "  \"total\": $TOTAL,"
        echo "  \"passed\": $PASSED,"
        echo "  \"failed\": $FAILED,"
        echo "  \"services\": ["

        first=true
        for result in "${RESULTS[@]}"; do
            if [ "$first" = false ]; then
                echo ","
            fi
            echo "    $result"
            first=false
        done

        echo ""
        echo "  ]"
        echo "}"
    else
        # Human-readable summary
        echo "================================================================"
        echo "  Validation Summary"
        echo "================================================================"
        echo ""
        echo "Total services:    $TOTAL"
        echo "Passed:            $PASSED"
        echo "Failed:            $FAILED"
        echo ""

        if [ $FAILED -eq 0 ]; then
            log_success "All services passed validation!"
            echo ""
            echo "✅ All microservices are compliant with architectural standards"
            echo ""
        else
            log_error "$FAILED service(s) failed validation"
            echo ""
            echo "❌ Some services are not compliant with architectural standards"
            echo ""
            echo "Next Steps:"
            echo "  1. Review failure details above"
            echo "  2. Run validation on specific service: .standards/scripts/validate-service-structure.sh <service-name>"
            echo "  3. Refer to: .standards/docs/common-locations.md for standard locations"
            echo "  4. Refer to: .standards/docs/refactoring-checklist.md for fixing issues"
            echo ""
        fi

        echo "================================================================"
    fi

    # Exit with appropriate code
    if [ $FAILED -eq 0 ]; then
        exit 0
    else
        exit 1
    fi
}

# Run main function
main
