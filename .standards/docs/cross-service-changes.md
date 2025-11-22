# Cross-Service Changes Guide

## Purpose

This guide provides proven patterns and automation strategies for applying changes across multiple microservices efficiently and safely. Use these patterns to:

- Fix security vulnerabilities across all services simultaneously
- Update shared dependencies or frameworks
- Apply configuration changes uniformly
- Refactor common code patterns
- Maintain architectural consistency

---

## Quick Reference: Change Patterns

| Change Type | Difficulty | Time Estimate | Automation Level |
|-------------|------------|---------------|------------------|
| Update constant value | Easy | 5-10 min | High |
| Add configuration property | Easy | 10-15 min | High |
| Update dependency version | Medium | 20-30 min | Medium |
| Refactor method signature | Medium | 30-60 min | Low |
| Package restructuring | Hard | 1-2 hours | Low |
| Database schema change | Hard | 2-4 hours | None |

---

## Pattern 1: Update Constant Value Across Services

**Use Case**: Change JWT expiration time, update API version, modify timeout values

### Example: Update JWT Expiration from 24h to 48h

**Target File**: `{service}/src/main/java/com/sms/{service}/security/JwtTokenProvider.java`

**Target Line**: `private static final long EXPIRATION_MS = 86400000;` (line ~20)

#### Manual Approach (10 min)

1. Find all JWT providers:
   ```bash
   .standards/scripts/find-component.sh JwtTokenProvider
   ```

2. Open each file and update the constant:
   ```java
   // Before
   private static final long EXPIRATION_MS = 86400000; // 24 hours

   // After
   private static final long EXPIRATION_MS = 172800000; // 48 hours
   ```

3. Test each service locally

#### Automated Approach (5 min)

```bash
# Step 1: Find and update all occurrences
find . -name "JwtTokenProvider.java" -path "*/security/*" \
  -exec sed -i '' 's/EXPIRATION_MS = 86400000/EXPIRATION_MS = 172800000/g' {} +

# Step 2: Verify changes
find . -name "JwtTokenProvider.java" -path "*/security/*" \
  -exec grep "EXPIRATION_MS" {} +

# Step 3: Commit changes
git add .
git commit -m "refactor: increase JWT expiration to 48 hours"

# Step 4: Test all services
for service in auth-service student-service; do
    cd $service && ./mvnw test && cd ..
done
```

**Validation**:
- [ ] All JwtTokenProvider.java files updated
- [ ] All tests passing
- [ ] Generated tokens have new expiration time

---

## Pattern 2: Add New Configuration Property

**Use Case**: Add feature flags, new environment variables, service-specific settings

### Example: Add Request Timeout Configuration

#### Step 1: Update Template

```bash
# Update the standard template first
vim .standards/templates/application.yml
```

Add:
```yaml
# Add to application.yml template
server:
  servlet:
    session:
      timeout: 30m
  connection-timeout: 60000  # 60 seconds
```

#### Step 2: Apply to All Services

```bash
# For each service
for service in auth-service student-service; do
    echo "Updating $service..."

    # Add property to application.yml
    cat << EOF >> $service/src/main/resources/application.yml

# Request timeout configuration
server:
  servlet:
    session:
      timeout: 30m
  connection-timeout: 60000
EOF

    # Add environment variable to docker profile
    sed -i '' '/^server:/a\
  connection-timeout: \${SERVER_CONNECTION_TIMEOUT:60000}
' $service/src/main/resources/application-docker.yml

    echo "$service updated"
done
```

#### Step 3: Update Docker Compose

```bash
# Add environment variable to all services in docker-compose.yml
# Manual edit required due to YAML structure
vim docker-compose.yml

# Add to each service:
# environment:
#   - SERVER_CONNECTION_TIMEOUT=60000
```

**Validation**:
- [ ] Template updated
- [ ] All application.yml files have new property
- [ ] All application-docker.yml files have environment variable
- [ ] docker-compose.yml has new env var for all services
- [ ] Services start successfully

---

## Pattern 3: Update Maven Dependency Version

**Use Case**: Security patches, framework updates, library upgrades

### Example: Update Spring Boot from 3.5.6 to 3.5.7

#### Step 1: Update Template (if exists)

```bash
vim .standards/templates/pom-template.xml

# Update Spring Boot version property
<spring-boot.version>3.5.7</spring-boot.version>
```

#### Step 2: Update All Service pom.xml Files

```bash
# Find all pom.xml files
find . -name "pom.xml" -not -path "*/target/*"

# Update Spring Boot version
find . -name "pom.xml" -not -path "*/target/*" \
  -exec sed -i '' 's/<spring-boot.version>3.5.6<\/spring-boot.version>/<spring-boot.version>3.5.7<\/spring-boot.version>/g' {} +

# Verify changes
find . -name "pom.xml" -not -path "*/target/*" \
  -exec grep -H "spring-boot.version" {} +
```

#### Step 3: Rebuild All Services

```bash
# Clean and rebuild
for service in auth-service student-service; do
    echo "Building $service..."
    cd $service
    ./mvnw clean install
    if [ $? -ne 0 ]; then
        echo "❌ Build failed for $service"
        exit 1
    fi
    cd ..
done
```

#### Step 4: Test All Services

```bash
# Run tests
for service in auth-service student-service; do
    echo "Testing $service..."
    cd $service
    ./mvnw test
    if [ $? -ne 0 ]; then
        echo "❌ Tests failed for $service"
        exit 1
    fi
    cd ..
done
```

**Validation**:
- [ ] All pom.xml files updated to new version
- [ ] All services build successfully
- [ ] All tests passing
- [ ] Services start without errors
- [ ] Integration tests passing

---

## Pattern 4: Add New Public Endpoint to Security Config

**Use Case**: Make endpoint publicly accessible without authentication

### Example: Add /api/public/health to Public Endpoints

#### Manual Approach

1. Locate all SecurityConfig.java files:
   ```bash
   .standards/scripts/find-component.sh SecurityConfig
   ```

2. Edit each file and add to public endpoints:
   ```java
   .authorizeHttpRequests(auth -> auth
           .requestMatchers("/api/public/health").permitAll()  // Add this
           .requestMatchers("/actuator/**").permitAll()
           .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
           .anyRequest().authenticated()
   )
   ```

#### Semi-Automated Approach

```bash
# Create a script to add public endpoint
for service in auth-service student-service; do
    config_file=$(find $service -name "SecurityConfig.java")

    if [ -f "$config_file" ]; then
        # Find the line with first .requestMatchers and add before it
        sed -i '' '/\.requestMatchers.*permitAll/i\
                        .requestMatchers("/api/public/health").permitAll()  // Public health check
' "$config_file"
        echo "Updated $service SecurityConfig"
    fi
done
```

**Validation**:
- [ ] All SecurityConfig.java files updated
- [ ] Services compile
- [ ] `/api/public/health` accessible without token
- [ ] Other endpoints still require authentication

---

## Pattern 5: Update CORS Origins for Production

**Use Case**: Restrict CORS from wildcard (`*`) to specific production domains

### Step 1: Update CorsConfig Template

```bash
vim .standards/templates/java/CorsConfig.java

# Change from:
# configuration.setAllowedOrigins(List.of("*"));

# To:
# configuration.setAllowedOrigins(List.of(
#     "http://localhost:3000",
#     "https://yourdomain.com",
#     "https://admin.yourdomain.com"
# ));
# configuration.setAllowCredentials(true);
```

### Step 2: Create Environment-Based CORS Config

```java
// Update pattern to use environment variable
@Value("${cors.allowed.origins}")
private String allowedOrigins;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Split comma-separated origins from environment
    configuration.setAllowedOrigins(
        Arrays.asList(allowedOrigins.split(","))
    );

    // ... rest of configuration
}
```

### Step 3: Update All Services

```bash
# Script to update CORS configuration in all services
for service in auth-service student-service; do
    cors_file=$(find $service -name "CorsConfig.java")

    if [ -f "$cors_file" ]; then
        # Backup original
        cp "$cors_file" "$cors_file.bak"

        # Copy updated template
        cp .standards/templates/java/CorsConfig.java "$cors_file"

        # Update package name
        service_name=$(basename $service | sed 's/-service//')
        sed -i '' "s/SERVICENAME/$service_name/g" "$cors_file"

        echo "Updated $service CORS configuration"
    fi
done
```

### Step 4: Update application.yml

```bash
# Add CORS configuration to each service
for service in auth-service student-service; do
    cat << EOF >> $service/src/main/resources/application.yml

# CORS configuration
cors:
  allowed:
    origins: http://localhost:3000,http://localhost:4200
EOF

    cat << EOF >> $service/src/main/resources/application-docker.yml

# CORS configuration
cors:
  allowed:
    origins: \${CORS_ALLOWED_ORIGINS}
EOF
done
```

### Step 5: Update docker-compose.yml

Add to each service:
```yaml
environment:
  - CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://admin.yourdomain.com
```

**Validation**:
- [ ] All CorsConfig.java files use environment variable
- [ ] application.yml has default origins
- [ ] application-docker.yml has environment variable
- [ ] docker-compose.yml has production origins
- [ ] CORS preflight requests work from allowed origins
- [ ] CORS requests rejected from other origins

---

## Pattern 6: Bulk Package Renaming

**Use Case**: Rename packages for consistency (e.g., entity/ → model/)

### Example: Rename entity/ to model/

#### Step 1: Identify Affected Services

```bash
# Find services with entity/ package
find . -type d -name "entity" -path "*/src/main/java/*"

# List for confirmation
# ./student-service/src/main/java/com/sms/student/entity/
```

#### Step 2: Move Files

```bash
for service in student-service; do
    entity_dir=$(find $service -type d -name "entity" -path "*/src/main/java/*")

    if [ -d "$entity_dir" ]; then
        # Create model directory
        model_dir=$(dirname $entity_dir)/model
        mkdir -p "$model_dir"

        # Move all files
        mv $entity_dir/* "$model_dir/"

        # Remove empty entity directory
        rmdir "$entity_dir"

        echo "Moved entity/ to model/ in $service"
    fi
done
```

#### Step 3: Update Package Declarations

```bash
for service in student-service; do
    # Find all Java files in model/ package
    find $service -path "*/model/*.java" \
      -exec sed -i '' 's/package \(.*\)\.entity;/package \1.model;/g' {} +
done
```

#### Step 4: Update Import Statements

```bash
for service in student-service; do
    # Update all imports across the service
    find $service -name "*.java" \
      -exec sed -i '' 's/import \(.*\)\.entity\./import \1.model./g' {} +
done
```

#### Step 5: Update JPA Configuration (if needed)

```bash
# Update entity scan if explicitly configured
find . -name "*Application.java" \
  -exec sed -i '' 's/@EntityScan.*entity"/@EntityScan(basePackages = "com.sms.*.model")/g' {} +
```

#### Step 6: Rebuild and Test

```bash
for service in student-service; do
    cd $service
    ./mvnw clean compile
    ./mvnw test
    cd ..
done
```

**Validation**:
- [ ] No entity/ directories remaining
- [ ] All files in model/ package
- [ ] All package declarations updated
- [ ] All import statements updated
- [ ] Services compile without errors
- [ ] All tests passing
- [ ] JPA entities discovered correctly

---

## Pattern 7: Add New Standardized Configuration Class

**Use Case**: Add FileUploadConfig, RedisConfig, or other new standard config

### Example: Add FileUploadConfig to Services That Need It

#### Step 1: Create Template

```bash
vim .standards/templates/java/FileUploadConfig.java

# Create configuration class
```

```java
package com.sms.SERVICENAME.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {

    private String directory = "uploads/";
    private long maxFileSize = 10485760; // 10MB
    private long maxRequestSize = 10485760; // 10MB

    // Getters and setters
    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public long getMaxRequestSize() {
        return maxRequestSize;
    }

    public void setMaxRequestSize(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }
}
```

#### Step 2: Apply to Specific Services

```bash
# Only add to services that handle file uploads
for service in student-service; do  # Not auth-service
    config_dir="$service/src/main/java/com/sms/$(basename $service | sed 's/-service//g')/config"

    # Copy template
    cp .standards/templates/java/FileUploadConfig.java "$config_dir/"

    # Update package name
    service_name=$(basename $service | sed 's/-service//g')
    sed -i '' "s/SERVICENAME/$service_name/g" "$config_dir/FileUploadConfig.java"

    echo "Added FileUploadConfig to $service"
done
```

#### Step 3: Add Configuration Properties

```bash
# Add to application.yml
for service in student-service; do
    cat << EOF >> $service/src/main/resources/application.yml

# File upload configuration
file:
  upload:
    directory: uploads/
    max-file-size: 10485760  # 10MB
    max-request-size: 10485760  # 10MB
EOF
done
```

**Validation**:
- [ ] FileUploadConfig.java added to appropriate services
- [ ] Configuration properties in application.yml
- [ ] Services compile
- [ ] File upload functionality works

---

## Automation Scripts

### Script 1: Bulk Find and Replace

```bash
#!/bin/bash
# bulk-replace.sh - Find and replace text across all services

SEARCH_PATTERN="$1"
REPLACE_WITH="$2"
FILE_PATTERN="${3:-*.java}"

if [ -z "$SEARCH_PATTERN" ] || [ -z "$REPLACE_WITH" ]; then
    echo "Usage: ./bulk-replace.sh 'search-pattern' 'replace-with' ['file-pattern']"
    exit 1
fi

echo "Searching for: $SEARCH_PATTERN"
echo "Replacing with: $REPLACE_WITH"
echo "File pattern: $FILE_PATTERN"
echo ""

# Find and replace
find . -name "$FILE_PATTERN" -not -path "*/target/*" -not -path "*/.git/*" \
  -exec sed -i '' "s/$SEARCH_PATTERN/$REPLACE_WITH/g" {} +

echo "Replacement complete. Please review changes with 'git diff'"
```

### Script 2: Validate All Services

```bash
#!/bin/bash
# validate-and-test.sh - Compile and test all services

SERVICES="auth-service student-service"
FAILED_SERVICES=""

for service in $SERVICES; do
    echo "========================================"
    echo "Validating $service..."
    echo "========================================"

    cd $service

    # Compile
    echo "Compiling..."
    if ! ./mvnw clean compile; then
        echo "❌ Compilation failed for $service"
        FAILED_SERVICES="$FAILED_SERVICES $service"
        cd ..
        continue
    fi

    # Test
    echo "Running tests..."
    if ! ./mvnw test; then
        echo "❌ Tests failed for $service"
        FAILED_SERVICES="$FAILED_SERVICES $service"
        cd ..
        continue
    fi

    echo "✅ $service validated successfully"
    cd ..
done

echo ""
echo "========================================"
echo "Validation Summary"
echo "========================================"

if [ -z "$FAILED_SERVICES" ]; then
    echo "✅ All services validated successfully"
    exit 0
else
    echo "❌ Failed services:$FAILED_SERVICES"
    exit 1
fi
```

### Script 3: Apply Template to All Services

```bash
#!/bin/bash
# apply-template.sh - Copy template to all services

TEMPLATE_FILE="$1"
TARGET_SUBPATH="$2"

if [ -z "$TEMPLATE_FILE" ] || [ -z "$TARGET_SUBPATH" ]; then
    echo "Usage: ./apply-template.sh template-file target-subpath"
    echo "Example: ./apply-template.sh CorsConfig.java config/CorsConfig.java"
    exit 1
fi

SERVICES="auth-service student-service"

for service in $SERVICES; do
    service_name=$(echo $service | sed 's/-service//g')
    target_path="$service/src/main/java/com/sms/$service_name/$TARGET_SUBPATH"

    # Create directory if needed
    mkdir -p "$(dirname $target_path)"

    # Copy template
    cp ".standards/templates/java/$TEMPLATE_FILE" "$target_path"

    # Replace SERVICENAME placeholder
    sed -i '' "s/SERVICENAME/$service_name/g" "$target_path"

    echo "Applied $TEMPLATE_FILE to $service"
done

echo "Template applied to all services"
```

---

## Best Practices

### Before Making Changes

1. **Create feature branch**: Don't work directly on main
   ```bash
   git checkout -b refactor/cross-service-change
   ```

2. **Update template first**: Ensure `.standards/templates/` reflects the change

3. **Test on one service**: Apply to auth-service and verify before rolling out

4. **Document the change**: Update relevant documentation

### During Changes

1. **Use version control**: Commit after each service update
   ```bash
   git add auth-service
   git commit -m "refactor(auth): apply JWT expiration change"
   ```

2. **Run tests frequently**: Catch issues early

3. **Keep changes atomic**: One logical change per commit

4. **Review diffs**: Use `git diff` to verify changes are correct

### After Changes

1. **Run full validation**: Use `.standards/scripts/validate-all-services.sh`

2. **Integration testing**: Test cross-service workflows

3. **Update documentation**: Reflect changes in `.standards/docs/`

4. **Create PR**: Get peer review before merging

5. **Deploy incrementally**: Roll out to development first

---

## Common Pitfalls

### Pitfall 1: Forgetting Docker Configuration

**Problem**: Updated code but forgot to update docker-compose.yml or application-docker.yml

**Solution**: Always update both local and Docker configurations

**Checklist**:
- [ ] application.yml updated
- [ ] application-docker.yml updated
- [ ] docker-compose.yml updated (if env vars changed)
- [ ] .env file updated (if new secrets)

### Pitfall 2: Inconsistent Package Names

**Problem**: Replaced SERVICENAME but used different capitalization

**Solution**: Use consistent service name format (lowercase, no hyphens)

```bash
# Correct
s/SERVICENAME/student/g  # not "Student" or "student-service"
```

### Pitfall 3: Skipping Tests

**Problem**: Applied changes to all services without testing

**Solution**: Always test incrementally

```bash
# Test after each service update
for service in auth-service student-service; do
    cd $service && ./mvnw test && cd .. || exit 1
done
```

### Pitfall 4: Breaking Backward Compatibility

**Problem**: Changed API contract without versioning

**Solution**: Plan breaking changes carefully

- Use deprecation warnings
- Support both old and new patterns temporarily
- Coordinate deployment across services

---

## Rollback Strategy

If cross-service change causes issues:

### Quick Rollback

```bash
# Revert Git changes
git reset --hard HEAD~1

# Or revert specific commit
git revert <commit-hash>

# Rebuild affected services
for service in auth-service student-service; do
    cd $service && ./mvnw clean install && cd ..
done
```

### Selective Rollback

If only one service has issues:

```bash
# Revert changes to specific service only
git checkout HEAD~1 -- student-service/

# Rebuild just that service
cd student-service && ./mvnw clean install
```

---

## Related Documentation

- **Common Locations**: `.standards/docs/common-locations.md` - Find components
- **Refactoring Checklist**: `.standards/docs/refactoring-checklist.md` - Safe refactoring
- **Reusable Components**: `.standards/docs/reusable-components.md` - Template usage
- **Validation Script**: `.standards/scripts/validate-all-services.sh` - Compliance check

---

## Quick Command Reference

```bash
# Find component across all services
.standards/scripts/find-component.sh ComponentName

# Validate all services
.standards/scripts/validate-all-services.sh

# Test all services
for service in auth-service student-service; do
    cd $service && ./mvnw test && cd ..
done

# Find and replace across all Java files
find . -name "*.java" -not -path "*/target/*" \
  -exec sed -i '' 's/old-text/new-text/g' {} +

# Check Git status across all services
git status --short

# View changes
git diff --stat

# Commit changes
git add .
git commit -m "refactor: cross-service change description"
```

---

## Version History

- **v1.0** (2025-11-22): Initial cross-service changes guide created
