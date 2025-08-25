#!/bin/bash
set -e

echo "🔎 Checking development environment..."

# Check for Node.js
echo -n "Checking for Node.js (version 18+)... "
if ! command -v node &> /dev/null; then
    echo "❌ Node.js not found. Please install Node.js version 18 or higher."
    exit 1
fi

NODE_VERSION=$(node -v)
NODE_MAJOR_VERSION=$(echo "$NODE_VERSION" | cut -d. -f1 | sed 's/v//')

if [ "$NODE_MAJOR_VERSION" -lt 18 ]; then
    echo "❌ Node.js version is $NODE_VERSION. Version 18 or higher is required."
    exit 1
fi
echo "✅ Node.js $NODE_VERSION found."

# Check for JDK
echo -n "Checking for JDK (version 11+)... "
if ! command -v java &> /dev/null; then
    echo "❌ Java (JDK) not found. Please install JDK version 11 or higher."
    exit 1
fi

JAVA_VERSION_OUTPUT=$(java -version 2>&1)
JAVA_VERSION_LINE=$(echo "$JAVA_VERSION_OUTPUT" | head -n 1)
# Regex to extract version number like "11.0.12" or "1.8.0_292"
if [[ "$JAVA_VERSION_LINE" =~ \"(1\.)?([0-9]+) ]]; then
    JAVA_MAJOR_VERSION=${BASH_REMATCH[2]}
else
    echo "❌ Could not determine Java version from: $JAVA_VERSION_LINE"
    exit 1
fi

if [ "$JAVA_MAJOR_VERSION" -lt 11 ]; then
    echo "❌ JDK version is $JAVA_MAJOR_VERSION. Version 11 or higher is required."
    exit 1
fi
echo "✅ JDK version $JAVA_MAJOR_VERSION found."

# Check for Android SDK
echo -n "Checking for Android SDK... "
if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
    echo "❌ ANDROID_HOME or ANDROID_SDK_ROOT environment variable not set."
    echo "Please set one of these variables to point to your Android SDK directory."
    exit 1
fi

if [ -n "$ANDROID_HOME" ]; then
    echo "✅ Android SDK found at ANDROID_HOME: $ANDROID_HOME"
elif [ -n "$ANDROID_SDK_ROOT" ]; then
    echo "✅ Android SDK found at ANDROID_SDK_ROOT: $ANDROID_SDK_ROOT"
fi

echo "✅ Environment check passed!"
