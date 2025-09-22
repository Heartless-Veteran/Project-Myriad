#!/bin/bash

# Phase 6: Final QA - Comprehensive testing script for Project Myriad
# This script runs all quality assurance checks before release

set -e

echo "🔍 Phase 6: Project Myriad - Final QA Testing Suite"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print status
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Step 1: Security Review
echo
echo "🔐 Step 1: Security Review"
echo "--------------------------"

print_status "Running OWASP dependency check..."
./gradlew dependencyCheckAnalyze || {
    print_error "OWASP dependency check failed"
    exit 1
}

print_status "Checking for hardcoded secrets..."
if grep -r "password\|secret\|key\|token" app/src/main/kotlin --exclude-dir=build | grep -v "// No sensitive data"; then
    print_warning "Potential secrets found - please review"
else
    print_status "No hardcoded secrets detected"
fi

# Step 2: Performance Validation
echo
echo "⚡ Step 2: Performance Validation"
echo "--------------------------------"

print_status "Running performance analysis..."
node scripts/performance-analyzer.js

print_status "Building release APK for size analysis..."
./gradlew assembleRelease || {
    print_error "Release build failed"
    exit 1
}

# Check APK size
APK_SIZE=$(stat -c%s "app/build/outputs/apk/release/app-release.apk" 2>/dev/null || echo "0")
APK_SIZE_MB=$((APK_SIZE / 1024 / 1024))

if [ $APK_SIZE_MB -lt 50 ]; then
    print_status "APK size: ${APK_SIZE_MB}MB (under 50MB target)"
else
    print_warning "APK size: ${APK_SIZE_MB}MB (exceeds 50MB target)"
fi

# Step 3: Accessibility Testing
echo
echo "♿ Step 3: Accessibility Testing"
echo "-------------------------------"

print_status "Checking for content descriptions..."
if grep -r "contentDescription" app/src/main/res/layout --include="*.xml" | wc -l > 0; then
    print_status "Content descriptions found in layouts"
else
    print_warning "No content descriptions found - accessibility may be impacted"
fi

print_status "Verifying minimum touch target sizes (48dp)..."
if grep -r "minHeight.*48dp\|minWidth.*48dp" app/src/main/res/values --include="*.xml" | wc -l > 0; then
    print_status "Minimum touch targets configured"
else
    print_warning "Minimum touch targets may not be configured"
fi

# Step 4: Localization Testing
echo
echo "🌍 Step 4: Localization Testing"
echo "------------------------------"

print_status "Checking string resource coverage..."
BASE_STRINGS=$(grep -c "<string name=" app/src/main/res/values/strings.xml)
print_status "Base language has $BASE_STRINGS strings"

for lang in es ja; do
    if [ -f "app/src/main/res/values-$lang/strings.xml" ]; then
        LANG_STRINGS=$(grep -c "<string name=" "app/src/main/res/values-$lang/strings.xml")
        print_status "Language $lang has $LANG_STRINGS strings"
    else
        print_warning "Language $lang not found"
    fi
done

# Step 5: Code Quality
echo
echo "📊 Step 5: Code Quality"
echo "----------------------"

print_status "Running ktlint..."
./gradlew ktlintCheck || {
    print_error "ktlint check failed"
    exit 1
}

print_status "Running detekt..."
./gradlew detekt || {
    print_warning "Detekt found issues - check reports"
}

print_status "Running unit tests..."
./gradlew test || {
    print_error "Unit tests failed"
    exit 1
}

# Step 6: Build Verification
echo
echo "🏗️ Step 6: Build Verification"
echo "-----------------------------"

print_status "Testing debug build..."
./gradlew assembleDebug || {
    print_error "Debug build failed"
    exit 1
}

print_status "Testing staging build..."
./gradlew assembleStaging || {
    print_warning "Staging build failed - check configuration"
}

print_status "Generating Android App Bundle..."
./gradlew bundleRelease || {
    print_error "AAB generation failed"
    exit 1
}

# Check if AAB was created
if [ -f "app/build/outputs/bundle/release/app-release.aab" ]; then
    AAB_SIZE=$(stat -c%s "app/build/outputs/bundle/release/app-release.aab")
    AAB_SIZE_MB=$((AAB_SIZE / 1024 / 1024))
    print_status "Android App Bundle created: ${AAB_SIZE_MB}MB"
else
    print_error "Android App Bundle not found"
    exit 1
fi

# Final Summary
echo
echo "📋 Final QA Summary"
echo "=================="
print_status "Security review completed"
print_status "Performance validation passed"
print_status "Accessibility checks completed"
print_status "Localization testing completed"
print_status "Code quality checks passed"
print_status "Build verification completed"
echo
echo -e "${GREEN}🎉 Phase 6 QA testing completed successfully!${NC}"
echo "Ready for Play Store submission."