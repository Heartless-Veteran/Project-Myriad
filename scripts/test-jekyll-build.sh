#!/bin/bash
# Local Jekyll build test script for GitHub Pages debugging

set -e

echo "🔧 Testing Jekyll build for Project Myriad GitHub Pages"
echo "=================================================="

# Check if we're in the right directory
if [ ! -f "_config.yml" ]; then
    echo "❌ Error: _config.yml not found. Run this script from the repository root."
    exit 1
fi

# Check if Jekyll/Bundle is available
echo "📋 Checking dependencies..."
if ! command -v bundle &> /dev/null; then
    echo "❌ Bundle not found. Please install Ruby and Bundler first."
    echo "   On Ubuntu/Debian: sudo apt-get install ruby-full build-essential zlib1g-dev"
    echo "   Then: gem install bundler"
    exit 1
fi

# Install dependencies
echo "📦 Installing Jekyll dependencies..."
bundle config set --local path 'vendor/bundle'
bundle install

# Create API directory if it doesn't exist
echo "📁 Creating API documentation placeholder..."
mkdir -p api
if [ ! -f "api/index.md" ]; then
    cat > api/index.md << EOF
---
layout: default
title: API Documentation
---

# Kotlin API Documentation

API documentation will be generated automatically from the source code using Dokka.

To generate it locally, run:
\`\`\`bash
./gradlew app:dokkaGenerateModuleHtml
\`\`\`
EOF
fi

# Test Jekyll build
echo "🏗️ Building site with Jekyll..."
bundle exec jekyll build --verbose

# Check if build was successful
if [ -d "_site" ]; then
    echo "✅ Jekyll build successful!"
    echo "📊 Site statistics:"
    echo "   - HTML files: $(find _site -name '*.html' | wc -l)"
    echo "   - Total files: $(find _site -type f | wc -l)"
    echo "   - Site size: $(du -sh _site | cut -f1)"
    
    # Check for critical files
    echo "🔍 Checking critical files..."
    for file in _site/index.html _site/docs/index.html; do
        if [ -f "$file" ]; then
            echo "   ✅ $file"
        else
            echo "   ❌ $file (missing)"
        fi
    done
    
    echo ""
    echo "🌐 To serve locally for testing:"
    echo "   bundle exec jekyll serve"
    echo "   Then visit: http://localhost:4000"
    
else
    echo "❌ Jekyll build failed!"
    exit 1
fi

echo ""
echo "✅ Local Jekyll build test completed successfully!"