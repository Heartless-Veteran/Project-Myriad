#!/bin/bash

echo "Running unit tests for Project Myriad Android app..."

# Run all unit tests
./gradlew testDebugUnitTest --info

echo ""
echo "Running instrumented tests (requires connected device/emulator)..."
./gradlew connectedDebugAndroidTest

echo ""
echo "✅ All tests completed!"
