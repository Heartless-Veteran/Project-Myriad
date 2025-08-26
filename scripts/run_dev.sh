#!/bin/bash
set -e

echo "🚀 Starting development environment for Project Myriad Android app..."

# Build and run the Android app in debug mode
echo "📱 Building and starting the Android application..."
echo "This can take a while, especially on the first run."

./gradlew assembleDebug

echo ""
echo "✅ Build completed successfully!"
echo ""
echo "To install and run on a connected device or emulator:"
echo "  ./gradlew installDebug"
echo ""  
echo "To run tests:"
echo "  ./gradlew testDebugUnitTest"
echo ""
echo "👋 Happy coding!"
