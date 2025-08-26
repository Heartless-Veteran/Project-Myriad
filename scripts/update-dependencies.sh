#!/bin/bash

# Project Myriad Android - Dependency Update Script
# This script helps maintain and update project dependencies

set -e

echo "🔄 Project Myriad Android - Dependency Update Script"
echo "====================================================="

# Check if gradlew is available
if [ ! -f "./gradlew" ]; then
    echo "❌ gradle wrapper (gradlew) not found. Please ensure you're in the project root directory."
    exit 1
fi

# Check for dependency updates
echo "📊 Checking for dependency updates..."
./gradlew dependencyUpdates

# Build project to verify dependencies
echo ""
echo "🔄 Building project to verify dependencies..."
./gradlew clean assembleDebug

# Run lint checks
echo ""
echo "🔍 Running lint checks..."
./gradlew lintDebug

# Run unit tests to ensure everything still works
echo ""
echo "🧪 Running tests to verify updates..."
./gradlew testDebugUnitTest

echo ""
echo "✅ Dependency update checks completed successfully!"
echo "📋 Please review any suggested updates and test the application thoroughly."
