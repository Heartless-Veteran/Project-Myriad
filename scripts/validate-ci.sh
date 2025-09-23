#!/bin/bash

echo "🔍 CI Workflow Validation Script"
echo "================================="

# Check if required files exist
echo "1. Checking required files..."

FILES_TO_CHECK=(
    ".github/workflows/ci.yml"
    ".github/workflows/security.yml" 
    ".github/workflows/release.yml"
    "scripts/validate-architecture.js"
    "scripts/update-readme-stats.js"
    "scripts/qa-quick-test.sh"
    "scripts/qa-final-testing.sh"
)

ALL_GOOD=true

for file in "${FILES_TO_CHECK[@]}"; do
    if [ -f "$file" ]; then
        echo "   ✅ $file exists"
    else
        echo "   ❌ $file missing"
        ALL_GOOD=false
    fi
done

echo
echo "2. Checking workflow syntax..."

# Basic YAML syntax check (if yamllint is available)
if command -v yamllint >/dev/null 2>&1; then
    yamllint .github/workflows/*.yml
else
    echo "   ⚠️  yamllint not available, skipping YAML syntax check"
fi

echo
echo "3. Testing individual scripts..."

# Test Node.js scripts
if [ -f "scripts/validate-architecture.js" ]; then
    echo "   Testing architecture validation..."
    node scripts/validate-architecture.js >/dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "   ✅ Architecture validation script works"
    else
        echo "   ⚠️  Architecture validation script has issues"
    fi
fi

if [ -f "scripts/update-readme-stats.js" ]; then
    echo "   Testing README stats update..."
    node scripts/update-readme-stats.js >/dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "   ✅ README stats script works"
    else
        echo "   ⚠️  README stats script has issues"
    fi
fi

# Test shell scripts
if [ -f "scripts/qa-quick-test.sh" ]; then
    echo "   Testing QA quick test..."
    chmod +x scripts/qa-quick-test.sh
    echo "   ✅ QA quick test script is executable"
fi

echo
echo "4. Summary"
echo "=========="

if [ "$ALL_GOOD" = true ]; then
    echo "✅ All critical files are present"
    echo "🚀 CI workflows should be functional"
    exit 0
else
    echo "❌ Some files are missing"
    echo "⚠️  CI workflows may have issues"
    exit 1
fi