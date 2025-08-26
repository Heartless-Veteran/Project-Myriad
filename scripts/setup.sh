#!/bin/bash
set -e

# Get the directory of the script
SCRIPT_DIR=$(dirname "$0")

echo "🚀 Starting Project Myriad Android setup..."

# Run the environment check script
"$SCRIPT_DIR/check_env.sh"

# If the check passes, proceed with setup
echo "✅ Environment check passed. Setting up Android project..."

# Create gradle.properties if it doesn't exist
if [ ! -f "gradle.properties" ]; then
    echo "Creating gradle.properties..."
    cat > gradle.properties << 'EOF'
# Project-wide Gradle settings for Project Myriad Android
android.useAndroidX=true
android.enableJetifier=true
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true

# Kotlin compiler options
kotlin.code.style=official
kotlin.incremental=true
kotlin.incremental.android=true
kotlin.incremental.js=true

# Compose compiler options
android.enableR8.fullMode=true
android.defaults.buildfeatures.buildconfig=true
android.nonTransitiveRClass=false
EOF
fi

echo "✅ Setup complete!"
echo ""
echo "🎉 Project Myriad Android is ready for development!"
echo ""
echo "To build and run the application, you can use:"
echo "  ./scripts/run_dev.sh"
echo ""
echo "Or run commands manually:"
echo "1. Build the app:"
echo "   ./gradlew assembleDebug"
echo "2. Run tests:"
echo "   ./gradlew testDebugUnitTest"
echo "3. Install on device/emulator:"
echo "   ./gradlew installDebug"
