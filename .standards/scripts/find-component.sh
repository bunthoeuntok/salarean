#!/bin/bash

#
# find-component.sh - Locate a component type across all microservices
#
# Usage: ./find-component.sh <ComponentName> [--path-only] [--content]
#
# Examples:
#   ./find-component.sh JwtTokenProvider
#   ./find-component.sh CorsConfig --path-only
#   ./find-component.sh SecurityConfig --content
#
# Exit Codes:
#   0 = Component found in at least one service
#   1 = Component not found
#   2 = Invalid usage
#

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SERVICES_PATTERN="*-service"

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[FOUND]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Print header
print_header() {
    echo ""
    echo "================================================================"
    echo "  Component Finder - Locate Components Across Services"
    echo "================================================================"
    echo ""
}

# Print usage
usage() {
    echo "Usage: $0 <ComponentName> [--path-only] [--content]"
    echo ""
    echo "Arguments:"
    echo "  ComponentName  Name of the component to find (e.g., JwtTokenProvider, CorsConfig)"
    echo ""
    echo "Options:"
    echo "  --path-only    Show only file paths, no additional information"
    echo "  --content      Show first 10 lines of each found file"
    echo "  --help         Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 JwtTokenProvider"
    echo "  $0 CorsConfig --path-only"
    echo "  $0 SecurityConfig --content"
    echo ""
}

# Parse arguments
COMPONENT_NAME=""
PATH_ONLY=false
SHOW_CONTENT=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --path-only)
            PATH_ONLY=true
            shift
            ;;
        --content)
            SHOW_CONTENT=true
            shift
            ;;
        --help|-h)
            usage
            exit 0
            ;;
        *)
            if [ -z "$COMPONENT_NAME" ]; then
                COMPONENT_NAME="$1"
            else
                log_error "Unknown argument: $1"
                usage
                exit 2
            fi
            shift
            ;;
    esac
done

# Validate arguments
if [ -z "$COMPONENT_NAME" ]; then
    log_error "Component name is required"
    usage
    exit 2
fi

# Main execution
main() {
    if [ "$PATH_ONLY" = false ]; then
        print_header
        log_info "Searching for: ${COMPONENT_NAME}"
        log_info "Project root: ${PROJECT_ROOT}"
        echo ""
    fi

    # Find all service directories
    SERVICES=$(find "$PROJECT_ROOT" -maxdepth 1 -type d -name "$SERVICES_PATTERN" 2>/dev/null | sort)

    if [ -z "$SERVICES" ]; then
        log_warning "No services found matching pattern: $SERVICES_PATTERN"
        exit 1
    fi

    FOUND_COUNT=0
    TOTAL_FILES=0

    # Search each service
    for service_dir in $SERVICES; do
        service_name=$(basename "$service_dir")

        # Find component files matching the name
        # Look for: ComponentName.java or *ComponentName*.java
        component_files=$(find "$service_dir/src" -type f \
            \( -name "${COMPONENT_NAME}.java" -o -name "*${COMPONENT_NAME}*.java" \) \
            2>/dev/null)

        if [ -n "$component_files" ]; then
            FOUND_COUNT=$((FOUND_COUNT + 1))

            while IFS= read -r file_path; do
                TOTAL_FILES=$((TOTAL_FILES + 1))

                # Extract package path
                package_path=$(echo "$file_path" | sed 's|.*/src/main/java/||' | sed 's|/[^/]*$||')
                file_name=$(basename "$file_path")

                if [ "$PATH_ONLY" = true ]; then
                    # Path-only mode: just print the path
                    echo "$file_path"
                else
                    # Full mode: show detailed information
                    log_success "${service_name}/${package_path}/${file_name}"

                    # Get file stats
                    line_count=$(wc -l < "$file_path" | tr -d ' ')
                    file_size=$(du -h "$file_path" | cut -f1)

                    echo "  Path: $file_path"
                    echo "  Package: $package_path"
                    echo "  Size: $file_size | Lines: $line_count"

                    # Show content if requested
                    if [ "$SHOW_CONTENT" = true ]; then
                        echo ""
                        echo "  First 10 lines:"
                        echo "  ─────────────────────────────────────────────────────"
                        head -n 10 "$file_path" | sed 's/^/  │ /'
                        echo "  ─────────────────────────────────────────────────────"
                    fi

                    echo ""
                fi
            done <<< "$component_files"
        fi
    done

    # Print summary
    if [ "$PATH_ONLY" = false ]; then
        echo "================================================================"
        echo "  Search Summary"
        echo "================================================================"
        echo ""

        if [ $FOUND_COUNT -eq 0 ]; then
            log_warning "Component '${COMPONENT_NAME}' not found in any service"
            echo ""
            echo "Suggestions:"
            echo "  • Check component name spelling"
            echo "  • Try searching for partial matches: find . -name '*${COMPONENT_NAME}*'"
            echo "  • Verify component exists: grep -r '${COMPONENT_NAME}' --include='*.java'"
            echo ""
            exit 1
        else
            log_success "Found in ${FOUND_COUNT} service(s)"
            echo "  Total files: ${TOTAL_FILES}"
            echo ""

            # Show quick commands
            echo "Quick Commands:"
            echo "  • View first file: cat \$(./find-component.sh ${COMPONENT_NAME} --path-only | head -1)"
            echo "  • Edit first file: vim \$(./find-component.sh ${COMPONENT_NAME} --path-only | head -1)"
            echo "  • Diff files: diff -u \$(./find-component.sh ${COMPONENT_NAME} --path-only)"
            echo ""
        fi
    fi

    # Exit with success if found
    if [ $TOTAL_FILES -gt 0 ]; then
        exit 0
    else
        exit 1
    fi
}

# Run main function
main
