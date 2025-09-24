#!/bin/bash

# Quick YAML formatter for GitHub Actions workflows
echo "🔧 Quick YAML workflow formatter"
echo "================================"

# Function to fix common YAML issues
fix_yaml_file() {
    local file="$1"
    echo "Fixing $file..."
    
    # Create backup
    cp "$file" "$file.bak"
    
    # Remove trailing spaces
    sed -i 's/[[:space:]]*$//' "$file"
    
    # Ensure file ends with newline
    if [ -n "$(tail -c1 "$file")" ]; then
        echo "" >> "$file"
    fi
    
    # Add document start if missing
    if ! head -1 "$file" | grep -q "^---"; then
        # Only add if file doesn't start with 'name:'
        if head -1 "$file" | grep -q "^name:"; then
            echo "File already starts properly with name:"
        else
            sed -i '1i ---' "$file"
        fi
    fi
}

# Fix the problematic workflow files
WORKFLOWS=(
    ".github/workflows/build-apk.yml"
    ".github/workflows/quick-apk-build.yml"
)

for workflow in "${WORKFLOWS[@]}"; do
    if [ -f "$workflow" ]; then
        fix_yaml_file "$workflow"
        echo "✅ Fixed $workflow"
    else
        echo "⚠️  $workflow not found"
    fi
done

echo ""
echo "🎉 YAML formatting completed!"
echo "Note: This fixes basic issues. Complex line length issues may remain."
echo "Run './scripts/validate-ci.sh' to check improvements."