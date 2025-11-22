#!/bin/bash

#
# Template Copy-Paste Test Script
#
# Purpose: Verify that Java templates can be copied and compiled without modifications
# Usage: ./.standards/scripts/test-template-copy-paste.sh
#
# Test Process:
# 1. Create temporary test service structure
# 2. Copy all Java templates
# 3. Replace SERVICENAME placeholder
# 4. Verify Java syntax (compilation test)
# 5. Clean up
#
# Exit Codes:
# 0 = All tests passed
# 1 = Test failed
# 2 = Setup error
#

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
TEST_SERVICE_NAME="testservice"
TEMP_DIR="/tmp/sms-template-test"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TEMPLATE_DIR="$PROJECT_ROOT/.standards/templates/java"

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Print header
print_header() {
    echo ""
    echo "================================================================"
    echo "  Template Copy-Paste Verification Test"
    echo "================================================================"
    echo ""
}

# Cleanup function
cleanup() {
    if [ -d "$TEMP_DIR" ]; then
        log_info "Cleaning up temporary test directory..."
        rm -rf "$TEMP_DIR"
        log_success "Cleanup complete"
    fi
}

# Setup trap for cleanup on exit
trap cleanup EXIT

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."

    # Check if template directory exists
    if [ ! -d "$TEMPLATE_DIR" ]; then
        log_error "Template directory not found: $TEMPLATE_DIR"
        exit 2
    fi

    # Check if templates exist
    local templates=("CorsConfig.java" "OpenAPIConfig.java" "SecurityConfig.java" "JwtAuthenticationFilter.java" "JwtTokenProvider.java")
    for template in "${templates[@]}"; do
        if [ ! -f "$TEMPLATE_DIR/$template" ]; then
            log_error "Template not found: $template"
            exit 2
        fi
    done

    # Check if sed is available
    if ! command -v sed &> /dev/null; then
        log_error "sed command not found - required for template customization"
        exit 2
    fi

    # Check if javac is available (optional - for compilation test)
    if ! command -v javac &> /dev/null; then
        log_warning "javac not found - skipping compilation test"
        SKIP_COMPILATION=true
    else
        local java_version=$(javac -version 2>&1 | awk '{print $2}')
        log_info "Found Java compiler: $java_version"
        SKIP_COMPILATION=false
    fi

    log_success "Prerequisites check passed"
}

# Create test service structure
create_test_structure() {
    log_info "Creating test service structure..."

    # Remove existing temp directory
    if [ -d "$TEMP_DIR" ]; then
        rm -rf "$TEMP_DIR"
    fi

    # Create directory structure
    mkdir -p "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/config"
    mkdir -p "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/security"

    log_success "Test structure created at: $TEMP_DIR"
}

# Copy templates
copy_templates() {
    log_info "Copying templates..."

    # Config templates
    cp "$TEMPLATE_DIR/CorsConfig.java" "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/config/"
    cp "$TEMPLATE_DIR/OpenAPIConfig.java" "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/config/"
    cp "$TEMPLATE_DIR/SecurityConfig.java" "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/config/"

    # Security templates
    cp "$TEMPLATE_DIR/JwtAuthenticationFilter.java" "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/security/"
    cp "$TEMPLATE_DIR/JwtTokenProvider.java" "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/security/"

    log_success "Templates copied successfully"
}

# Replace SERVICENAME placeholder
replace_placeholder() {
    log_info "Replacing SERVICENAME placeholder with '$TEST_SERVICE_NAME'..."

    # Find all Java files and replace SERVICENAME
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS (BSD sed)
        find "$TEMP_DIR" -name "*.java" -exec sed -i '' "s/SERVICENAME/$TEST_SERVICE_NAME/g" {} +
    else
        # Linux (GNU sed)
        find "$TEMP_DIR" -name "*.java" -exec sed -i "s/SERVICENAME/$TEST_SERVICE_NAME/g" {} +
    fi

    log_success "Placeholder replacement complete"
}

# Verify package declarations
verify_package_declarations() {
    log_info "Verifying package declarations..."

    local files=(
        "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/config/CorsConfig.java"
        "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/config/OpenAPIConfig.java"
        "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/config/SecurityConfig.java"
        "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/security/JwtAuthenticationFilter.java"
        "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/security/JwtTokenProvider.java"
    )

    local expected_packages=(
        "package com.sms.$TEST_SERVICE_NAME.config;"
        "package com.sms.$TEST_SERVICE_NAME.config;"
        "package com.sms.$TEST_SERVICE_NAME.config;"
        "package com.sms.$TEST_SERVICE_NAME.security;"
        "package com.sms.$TEST_SERVICE_NAME.security;"
    )

    for i in "${!files[@]}"; do
        local file="${files[$i]}"
        local expected="${expected_packages[$i]}"
        local filename=$(basename "$file")

        if grep -q "$expected" "$file"; then
            log_success "✓ $filename - package declaration correct"
        else
            log_error "✗ $filename - package declaration incorrect"
            log_error "  Expected: $expected"
            log_error "  File: $file"
            return 1
        fi
    done

    log_success "All package declarations verified"
}

# Verify import statements
verify_imports() {
    log_info "Verifying import statements..."

    # SecurityConfig should import from security package
    local security_config="$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/config/SecurityConfig.java"
    local expected_import="import com.sms.$TEST_SERVICE_NAME.security.JwtAuthenticationFilter;"

    if grep -q "$expected_import" "$security_config"; then
        log_success "✓ SecurityConfig.java - import statement correct"
    else
        log_error "✗ SecurityConfig.java - import statement incorrect"
        log_error "  Expected: $expected_import"
        return 1
    fi

    log_success "All import statements verified"
}

# Verify no remaining SERVICENAME placeholders
verify_no_placeholders() {
    log_info "Verifying no remaining SERVICENAME placeholders..."

    local remaining=$(grep -r "SERVICENAME" "$TEMP_DIR" || true)

    if [ -z "$remaining" ]; then
        log_success "✓ No SERVICENAME placeholders remaining"
    else
        log_error "✗ Found remaining SERVICENAME placeholders:"
        echo "$remaining"
        return 1
    fi
}

# Test Java syntax (basic compilation check)
test_java_syntax() {
    if [ "$SKIP_COMPILATION" = true ]; then
        log_warning "Skipping compilation test (javac not available)"
        return 0
    fi

    log_info "Testing Java syntax with javac..."

    # We can't fully compile without dependencies, but we can check syntax
    # Using javac with -Xplugin:ErrorProne would catch more issues, but basic syntax check is sufficient

    local files=(
        "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/config/CorsConfig.java"
        "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/config/OpenAPIConfig.java"
        "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/config/SecurityConfig.java"
        "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/security/JwtAuthenticationFilter.java"
        "$TEMP_DIR/src/main/java/com/sms/$TEST_SERVICE_NAME/security/JwtTokenProvider.java"
    )

    local syntax_errors=false

    for file in "${files[@]}"; do
        local filename=$(basename "$file")

        # Basic syntax check (will fail on missing dependencies, but syntax errors will be caught)
        # We capture stderr and check for syntax-specific errors
        if javac -Xlint:none "$file" 2>&1 | grep -q "error: cannot find symbol\|error: package .* does not exist"; then
            # Expected errors (missing dependencies) - these are OK
            log_success "✓ $filename - syntax valid (dependency errors expected)"
        elif javac -Xlint:none "$file" 2>&1 | grep -q "error:"; then
            # Unexpected errors (syntax issues) - these are NOT OK
            log_error "✗ $filename - syntax errors detected:"
            javac "$file" 2>&1 | grep "error:" || true
            syntax_errors=true
        else
            log_success "✓ $filename - syntax valid"
        fi
    done

    if [ "$syntax_errors" = true ]; then
        log_error "Syntax validation failed"
        return 1
    fi

    log_success "Java syntax validation passed"
}

# Verify file structure
verify_file_structure() {
    log_info "Verifying file structure..."

    local expected_files=(
        "src/main/java/com/sms/$TEST_SERVICE_NAME/config/CorsConfig.java"
        "src/main/java/com/sms/$TEST_SERVICE_NAME/config/OpenAPIConfig.java"
        "src/main/java/com/sms/$TEST_SERVICE_NAME/config/SecurityConfig.java"
        "src/main/java/com/sms/$TEST_SERVICE_NAME/security/JwtAuthenticationFilter.java"
        "src/main/java/com/sms/$TEST_SERVICE_NAME/security/JwtTokenProvider.java"
    )

    for file in "${expected_files[@]}"; do
        local full_path="$TEMP_DIR/$file"
        if [ -f "$full_path" ]; then
            log_success "✓ Found: $file"
        else
            log_error "✗ Missing: $file"
            return 1
        fi
    done

    log_success "File structure verified"
}

# Print summary
print_summary() {
    echo ""
    echo "================================================================"
    echo "  Test Summary"
    echo "================================================================"
    echo ""
    echo "✅ Templates copied successfully"
    echo "✅ Package declarations correct"
    echo "✅ Import statements correct"
    echo "✅ No remaining placeholders"
    echo "✅ File structure correct"
    if [ "$SKIP_COMPILATION" = false ]; then
        echo "✅ Java syntax valid"
    else
        echo "⚠️  Compilation test skipped (javac not available)"
    fi
    echo ""
    log_success "ALL TESTS PASSED"
    echo ""
    echo "Templates are COPY-PASTE READY!"
    echo ""
}

# Main execution
main() {
    print_header

    # Run all checks
    check_prerequisites
    create_test_structure
    copy_templates
    replace_placeholder
    verify_file_structure
    verify_package_declarations
    verify_imports
    verify_no_placeholders
    test_java_syntax

    # Print summary
    print_summary

    exit 0
}

# Run main function
main
