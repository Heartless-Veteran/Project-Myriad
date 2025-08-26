#!/bin/bash

echo "Running unit tests for Project Myriad Android app..."

# Run all unit tests
./gradlew testDebugUnitTest --info

echo ""
echo "Running instrumented tests (requires connected device/emulator)..."

# Check for connected device/emulator
if adb devices | awk 'NR>1 && $2=="device"' | grep -q device; then
    ./gradlew connectedDebugAndroidTest
else
    echo "⚠️  No connected device/emulator found. Skipping instrumented tests."
fi
echo ""
echo "✅ All tests completed!"
