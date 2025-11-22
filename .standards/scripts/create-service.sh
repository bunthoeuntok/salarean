#!/bin/bash

#
# create-service.sh - Automated service creation from auth-service template
#
# Usage: ./create-service.sh <service-name> <port> <db-port> [database-name]
#
# Examples:
#   ./create-service.sh attendance 8083 5435
#   ./create-service.sh grade 8084 5436 grade_db
#
# This script automates the creation of a new compliant microservice by:
# 1. Copying auth-service as template
# 2. Renaming packages (com.sms.auth → com.sms.{service})
# 3. Updating configuration files
# 4. Renaming main application class
# 5. Updating port numbers
# 6. Cleaning up auth-specific code
#
# Exit Codes:
#   0 = Success
#   1 = Invalid arguments or prerequisites not met
#   2 = Service already exists
#

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TEMPLATE_SERVICE="auth-service"
TEMPLATE_PORT="8081"

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Print usage
usage() {
    cat << EOF
Usage: $0 <service-name> <port> <db-port> [database-name]

Arguments:
  service-name    Name of the new service (lowercase, no hyphens)
                  Examples: attendance, grade, report

  port            Unique port for the service (8080-8099)
                  Examples: 8083, 8084, 8085

  db-port         Unique external database port (5432-5499)
                  Examples: 5435, 5436, 5437

  database-name   Optional: Database name (default: {service-name}_db)

Examples:
  $0 attendance 8083 5435
  $0 grade 8084 5436 grading_db
  $0 notification 8086 5438

Port Allocation Reference:
  8080: API Gateway
  8081: auth-service (DB: 5433)
  8082: student-service (DB: 5434)
  8083: attendance-service (DB: 5435)
  8084: grade-service (DB: 5436)
  8085: report-service (DB: 5437)
  8086: notification-service (DB: 5438)

EOF
}

# Validate arguments
if [ $# -lt 3 ]; then
    log_error "Insufficient arguments"
    usage
    exit 1
fi

SERVICE_NAME="$1"
SERVICE_PORT="$2"
DB_PORT="$3"
DB_NAME="${4:-${SERVICE_NAME}_db}"

# Validate service name
if [[ ! "$SERVICE_NAME" =~ ^[a-z]+$ ]]; then
    log_error "Service name must be lowercase letters only (no hyphens, numbers, or special characters)"
    log_info "Use: attendance (not attendance-service or Attendance)"
    exit 1
fi

# Validate port numbers
if [[ ! "$SERVICE_PORT" =~ ^[0-9]+$ ]] || [ "$SERVICE_PORT" -lt 8080 ] || [ "$SERVICE_PORT" -gt 8099 ]; then
    log_error "Service port must be between 8080-8099"
    exit 1
fi

if [[ ! "$DB_PORT" =~ ^[0-9]+$ ]] || [ "$DB_PORT" -lt 5432 ] || [ "$DB_PORT" -gt 5499 ]; then
    log_error "Database port must be between 5432-5499"
    exit 1
fi

# Derived variables
SERVICE_DIR="${SERVICE_NAME}-service"
SERVICE_CLASS="$(echo "$SERVICE_NAME" | sed 's/\b\(.\)/\u\1/g')ServiceApplication"  # CapitalCase
TEMPLATE_CLASS="AuthServiceApplication"

# Check if service already exists
if [ -d "$PROJECT_ROOT/$SERVICE_DIR" ]; then
    log_error "Service directory already exists: $SERVICE_DIR"
    log_info "Remove it first: rm -rf $PROJECT_ROOT/$SERVICE_DIR"
    exit 2
fi

# Check if template exists
if [ ! -d "$PROJECT_ROOT/$TEMPLATE_SERVICE" ]; then
    log_error "Template service not found: $TEMPLATE_SERVICE"
    log_info "Ensure you're running this from the project root"
    exit 1
fi

# Print summary
echo ""
echo "================================================================"
echo "  Service Creation Summary"
echo "================================================================"
echo ""
log_info "Service Name:        $SERVICE_NAME"
log_info "Service Directory:   $SERVICE_DIR"
log_info "Service Port:        $SERVICE_PORT"
log_info "Database Port:       $DB_PORT"
log_info "Database Name:       $DB_NAME"
log_info "Main Class:          $SERVICE_CLASS"
log_info "Template:            $TEMPLATE_SERVICE"
echo ""
read -p "Proceed with service creation? (y/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    log_warning "Service creation cancelled"
    exit 0
fi

# Step 1: Copy template
log_info "Step 1/7: Copying template service..."
cd "$PROJECT_ROOT"
cp -r "$TEMPLATE_SERVICE" "$SERVICE_DIR"
log_success "Template copied to $SERVICE_DIR"

# Step 2: Rename package references in files
log_info "Step 2/7: Updating package references..."
cd "$SERVICE_DIR"

# Find all Java, YAML, and XML files and update package names
find . -type f \( -name "*.java" -o -name "*.yml" -o -name "*.xml" -o -name "*.md" \) \
    -not -path "*/target/*" \
    -not -path "*/.git/*" \
    -exec sed -i '' "s/com\.sms\.auth/com.sms.$SERVICE_NAME/g" {} + 2>/dev/null || \
    find . -type f \( -name "*.java" -o -name "*.yml" -o -name "*.xml" -o -name "*.md" \) \
    -not -path "*/target/*" \
    -not -path "*/.git/*" \
    -exec sed -i "s/com\.sms\.auth/com.sms.$SERVICE_NAME/g" {} +

# Update service name references
find . -type f \( -name "*.java" -o -name "*.yml" -o -name "*.xml" -o -name "*.md" \) \
    -not -path "*/target/*" \
    -not -path "*/.git/*" \
    -exec sed -i '' "s/auth-service/$SERVICE_DIR/g" {} + 2>/dev/null || \
    find . -type f \( -name "*.java" -o -name "*.yml" -o -name "*.xml" -o -name "*.md" \) \
    -not -path "*/target/*" \
    -not -path "*/.git/*" \
    -exec sed -i "s/auth-service/$SERVICE_DIR/g" {} +

log_success "Package references updated"

# Step 3: Rename directory structure
log_info "Step 3/7: Renaming directory structure..."

# Main source
if [ -d "src/main/java/com/sms/auth" ]; then
    mv src/main/java/com/sms/auth src/main/java/com/sms/$SERVICE_NAME
    log_success "Main source directory renamed"
fi

# Test source
if [ -d "src/test/java/com/sms/auth" ]; then
    mv src/test/java/com/sms/auth src/test/java/com/sms/$SERVICE_NAME
    log_success "Test source directory renamed"
fi

# Step 4: Rename main application class
log_info "Step 4/7: Renaming main application class..."
if [ -f "src/main/java/com/sms/$SERVICE_NAME/AuthServiceApplication.java" ]; then
    mv "src/main/java/com/sms/$SERVICE_NAME/AuthServiceApplication.java" \
       "src/main/java/com/sms/$SERVICE_NAME/$SERVICE_CLASS.java"

    # Update class name inside the file
    sed -i '' "s/$TEMPLATE_CLASS/$SERVICE_CLASS/g" \
        "src/main/java/com/sms/$SERVICE_NAME/$SERVICE_CLASS.java" 2>/dev/null || \
        sed -i "s/$TEMPLATE_CLASS/$SERVICE_CLASS/g" \
        "src/main/java/com/sms/$SERVICE_NAME/$SERVICE_CLASS.java"

    log_success "Main class renamed to $SERVICE_CLASS"
fi

# Step 5: Update port numbers
log_info "Step 5/7: Updating port numbers..."

# Update application.yml
if [ -f "src/main/resources/application.yml" ]; then
    sed -i '' "s/$TEMPLATE_PORT/$SERVICE_PORT/g" src/main/resources/application.yml 2>/dev/null || \
        sed -i "s/$TEMPLATE_PORT/$SERVICE_PORT/g" src/main/resources/application.yml
    log_success "Port updated in application.yml"
fi

# Update Dockerfile
if [ -f "Dockerfile" ]; then
    sed -i '' "s/EXPOSE $TEMPLATE_PORT/EXPOSE $SERVICE_PORT/g" Dockerfile 2>/dev/null || \
        sed -i "s/EXPOSE $TEMPLATE_PORT/EXPOSE $SERVICE_PORT/g" Dockerfile
    log_success "Port updated in Dockerfile"
fi

# Step 6: Clean up auth-specific code
log_info "Step 6/7: Cleaning up auth-specific code..."

# Remove auth-specific controllers
rm -f src/main/java/com/sms/$SERVICE_NAME/controller/AuthController.java

# Remove auth-specific services
rm -f src/main/java/com/sms/$SERVICE_NAME/service/AuthService.java
rm -f src/main/java/com/sms/$SERVICE_NAME/service/AuthServiceImpl.java

# Remove auth-specific entities
rm -f src/main/java/com/sms/$SERVICE_NAME/model/RefreshToken.java
rm -f src/main/java/com/sms/$SERVICE_NAME/model/User.java

# Remove auth-specific repositories
rm -f src/main/java/com/sms/$SERVICE_NAME/repository/RefreshTokenRepository.java
rm -f src/main/java/com/sms/$SERVICE_NAME/repository/UserRepository.java

# Remove auth-specific DTOs
rm -rf src/main/java/com/sms/$SERVICE_NAME/dto/request/*
rm -rf src/main/java/com/sms/$SERVICE_NAME/dto/response/*

# Keep directory structure
mkdir -p src/main/java/com/sms/$SERVICE_NAME/controller
mkdir -p src/main/java/com/sms/$SERVICE_NAME/dto/request
mkdir -p src/main/java/com/sms/$SERVICE_NAME/dto/response
mkdir -p src/main/java/com/sms/$SERVICE_NAME/model
mkdir -p src/main/java/com/sms/$SERVICE_NAME/repository
mkdir -p src/main/java/com/sms/$SERVICE_NAME/service

# Create placeholder ApiResponse.java
cat > src/main/java/com/sms/$SERVICE_NAME/dto/response/ApiResponse.java << 'APIRESPONSE'
package com.sms.SERVICE_NAME.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String errorCode;  // "SUCCESS" or error code
    private T data;            // Response payload or null
}
APIRESPONSE

sed -i '' "s/SERVICE_NAME/$SERVICE_NAME/g" src/main/java/com/sms/$SERVICE_NAME/dto/response/ApiResponse.java 2>/dev/null || \
    sed -i "s/SERVICE_NAME/$SERVICE_NAME/g" src/main/java/com/sms/$SERVICE_NAME/dto/response/ApiResponse.java

# Remove Redis and PasswordEncoder configs if not needed (optional)
log_warning "Optional: Consider removing RedisConfig.java and PasswordEncoderConfig.java if not needed"

# Clean target directory
rm -rf target/

log_success "Auth-specific code cleaned up"

# Step 7: Build verification
log_info "Step 7/7: Building service to verify setup..."
mvn clean package -DskipTests -q

if [ $? -eq 0 ]; then
    log_success "Service built successfully!"
else
    log_error "Build failed. Please review errors above."
    exit 1
fi

# Print next steps
echo ""
echo "================================================================"
echo "  Service Created Successfully!"
echo "================================================================"
echo ""
log_success "New service created: $SERVICE_DIR"
log_success "Main class: $SERVICE_CLASS"
log_success "Service port: $SERVICE_PORT"
log_success "Database port: $DB_PORT"
echo ""
echo "Next Steps:"
echo ""
echo "1. Add domain-specific code:"
echo "   - Entities in: src/main/java/com/sms/$SERVICE_NAME/model/"
echo "   - Repositories in: src/main/java/com/sms/$SERVICE_NAME/repository/"
echo "   - Services in: src/main/java/com/sms/$SERVICE_NAME/service/"
echo "   - Controllers in: src/main/java/com/sms/$SERVICE_NAME/controller/"
echo ""
echo "2. Update docker-compose.yml:"
echo "   - Add service entry (use .standards/templates/docker-compose-entry.yml as reference)"
echo "   - Replace {SERVICE_NAME} with '$SERVICE_NAME'"
echo "   - Replace {PORT} with '$SERVICE_PORT'"
echo "   - Replace {DB_PORT} with '$DB_PORT'"
echo ""
echo "3. Add API Gateway route:"
echo "   - Edit: api-gateway/src/main/resources/application.yml"
echo "   - Add route for /api/$SERVICE_NAME/**"
echo ""
echo "4. Run validation:"
echo "   .standards/scripts/validate-service-structure.sh $SERVICE_DIR"
echo ""
echo "5. Test locally:"
echo "   cd $SERVICE_DIR && mvn spring-boot:run"
echo ""
echo "6. Build Docker image:"
echo "   docker-compose build $SERVICE_DIR"
echo ""
echo "7. Start in Docker:"
echo "   docker-compose up -d $SERVICE_DIR"
echo ""
echo "================================================================"
echo ""

# Success
exit 0
