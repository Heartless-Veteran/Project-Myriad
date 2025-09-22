#!/bin/bash

# Comprehensive Testing Script for Project Myriad
# Runs all quality checks, tests, and validations

set -e

echo "🧪 Running Comprehensive Tests for Project Myriad"
echo "================================================="

# Function to run command with error handling
run_command() {
    local cmd="$1"
    local desc="$2"
    echo "🔄 $desc..."
    
    if eval "$cmd"; then
        echo "✅ $desc - PASSED"
        return 0
    else
        echo "❌ $desc - FAILED"
        return 1
    fi
}

# Track results
TOTAL_TESTS=0
PASSED_TESTS=0

# 1. Architecture Validation
TOTAL_TESTS=$((TOTAL_TESTS + 1))
if run_command "node scripts/validate-architecture.js" "Architecture Validation"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
fi

# 2. Configuration Validation
TOTAL_TESTS=$((TOTAL_TESTS + 1))
if run_command "node scripts/validate-config.js" "Configuration Validation"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
fi

# 3. Code Formatting (ktlint)
TOTAL_TESTS=$((TOTAL_TESTS + 1))
if run_command "./gradlew app:ktlintCheck --no-daemon" "Code Formatting Check"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
fi

# 4. Static Analysis (Detekt)
TOTAL_TESTS=$((TOTAL_TESTS + 1))
if run_command "./gradlew app:detekt --no-daemon" "Static Analysis"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
fi

# 5. Compilation Test
TOTAL_TESTS=$((TOTAL_TESTS + 1))
if run_command "./gradlew app:compileDebugKotlin --no-daemon" "Compilation Test"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
fi

# 6. Unit Tests
TOTAL_TESTS=$((TOTAL_TESTS + 1))
if run_command "./gradlew app:testDebugUnitTest --no-daemon" "Unit Tests"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
fi

# 7. Documentation Generation
TOTAL_TESTS=$((TOTAL_TESTS + 1))
if run_command "./gradlew app:dokkaGenerateModuleHtml --no-daemon" "Documentation Generation"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
fi

# 8. README Statistics Update
TOTAL_TESTS=$((TOTAL_TESTS + 1))
if run_command "node scripts/update-readme-stats.js" "README Statistics Update"; then
    PASSED_TESTS=$((PASSED_TESTS + 1))
fi

# Summary
echo ""
echo "📊 Test Results Summary"
echo "======================"
echo "Total Tests: $TOTAL_TESTS"
echo "Passed: $PASSED_TESTS"
echo "Failed: $((TOTAL_TESTS - PASSED_TESTS))"
echo "Success Rate: $(echo "scale=1; $PASSED_TESTS * 100 / $TOTAL_TESTS" | bc)%"

if [ $PASSED_TESTS -eq $TOTAL_TESTS ]; then
    echo "🎉 All tests passed! Project is in excellent condition."
    exit 0
else
    echo "⚠️ Some tests failed. Check the output above for details."
    echo "💡 Run individual commands to fix specific issues:"
    echo "   - ./gradlew app:ktlintFormat (fix formatting)"
    echo "   - ./gradlew app:detekt (check static analysis)"
    echo "   - node scripts/validate-config.js (validate config)"
    exit 1
fi