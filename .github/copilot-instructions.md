# Project Myriad - Copilot Instructions

This document provides comprehensive instructions for GitHub Copilot coding agents working on **Project Myriad**, a Kotlin Android manga and anime platform application.

## Repository Overview

**Project Myriad** is "The Definitive Manga and Anime Platform" - a comprehensive Kotlin Android application featuring:

- **The Vault**: Local media management with support for .cbz/.cbr manga and .mp4/.mkv/.avi anime files
- **AI Core**: OCR translation, art style matching, and AI-powered recommendations
- **The Browser**: Online content discovery with extensible source system
- **Kotlin**: Full Kotlin implementation with type safety
- **Modern Architecture**: Jetpack Compose UI, Clean Architecture, Hilt DI, Room database

## Critical Build Information & Timings ⚠️

### NEVER CANCEL These Commands - They WILL Complete Successfully

| Command | Expected Time | Status | Critical Notes |
|---------|---------------|--------|----------------|
| `./gradlew build` | **2-5 minutes** | ✅ **WORKS** | **NEVER CANCEL** - Kotlin compilation and Android build |
| `./gradlew assembleDebug` | **1-3 minutes** | ✅ **WORKS** | **NEVER CANCEL** - Builds debug APK |
| `./gradlew test` | **30-60 seconds** | ✅ **WORKS** | **NEVER CANCEL** - Runs unit tests |
| `./gradlew lint` | **30-45 seconds** | ✅ **WORKS** | **NEVER CANCEL** - Android lint checks |
| `./gradlew installDebug` | **15-30 seconds** | ⚠️ **REQUIRES DEVICE** | Needs Android device/emulator connected |

### Known Issues & Workarounds

#### 1. Hilt/KAPT Compatibility Issue
```bash
# Current Status:
# Hilt (Dagger) dependency injection is temporarily disabled due to Kotlin 2.0 KAPT compatibility
# Using manual dependency injection until KAPT is replaced with KSP

# Workaround: Manual DI implementation in place
# Status: Non-critical - core functionality works without Hilt
```

#### 2. Kotlin Version Compatibility
```bash
# Current Setup:
# Kotlin 2.2.10 with Compose Compiler plugin
# Some libraries may have compatibility warnings

# Status: Non-critical - builds successfully with warnings
```

#### 3. Android SDK Dependencies
```bash
# Required:
# - Android SDK 24-36
# - Build Tools 35.0.0
# - Jetpack Compose BOM 2024.02.00

# Status: Standard Android development requirements
```

## Essential Development Commands

### Initial Setup
```bash
# Clone and setup - NO special flags needed for Kotlin/Android
./gradlew build
# Expected: 2-5 minutes, Kotlin compilation and Android build
```

### Development Workflow
```bash
# Build debug APK
./gradlew assembleDebug
# Expected: 1-3 minutes, outputs APK to app/build/outputs/apk/debug/

# Install on connected device/emulator
./gradlew installDebug
# Expected: 15-30 seconds (requires connected Android device)

# Run unit tests
./gradlew test
# Expected: 30-60 seconds, runs Kotlin/Android unit tests

# Run lint checks
./gradlew lint
# Expected: 30-45 seconds, Android-specific lint checks
```

### Build Commands
```bash
# Release build
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk

# Clean build
./gradlew clean build
# Full clean and rebuild
```

## Configuration Files - DO NOT MODIFY UNLESS NECESSARY

### Build Configuration (Correctly Setup)
```kotlin
// build.gradle.kts (project level)
plugins {
    id("com.android.application") version "8.12.1"
    id("org.jetbrains.kotlin.android") version "2.2.10"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
}

// app/build.gradle.kts
android {
    compileSdk = 36
    defaultConfig {
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

### Gradle Configuration
```properties
# gradle.properties - CRITICAL for build performance
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
```

## Architecture & Structure

```
app/src/main/kotlin/com/heartlessveteran/myriad/
├── ui/                     # Jetpack Compose UI components
│   ├── screens/           # Application screens
│   ├── components/        # Reusable UI components
│   └── theme/            # Material Design 3 theming
├── domain/               # Domain layer (Clean Architecture)
│   ├── entities/         # Business entities
│   ├── repositories/     # Repository interfaces
│   └── usecases/         # Business logic use cases
├── data/                 # Data layer implementation
│   ├── database/         # Room database (DAOs, entities)
│   ├── repository/       # Repository implementations
│   └── network/          # API services (future)
├── di/                   # Dependency injection (manual for now)
└── utils/               # Utility functions and extensions
```

## Validation Scenarios

### Quick Health Check (2-3 minutes total)
```bash
# 1. Test Gradle sync and build (2-3 minutes)
./gradlew build

# 2. Test unit tests (30s)
./gradlew test

# 3. Check lint (30s)
./gradlew lint
```

### Full Validation (5-10 minutes)
```bash
# Build debug APK
./gradlew assembleDebug

# Run all tests
./gradlew test

# Generate lint report
./gradlew lint

# Verify key files exist
ls -la app/src/main/kotlin/com/heartlessveteran/myriad/  # Should show domain, data, ui dirs
```

## Troubleshooting Guide

### If Gradle build fails:
```bash
# Clean and rebuild
./gradlew clean build

# Clear Gradle caches
rm -rf ~/.gradle/caches
./gradlew build

# Check Android SDK setup
./gradlew dependencies
```

### If app won't install:
```bash
# Check connected devices
adb devices

# Force reinstall
./gradlew uninstallDebug installDebug

# Clear app data
adb shell pm clear com.heartlessveteran.myriad
```

### If Compose preview fails:
```bash
# Invalidate caches in Android Studio
# File > Invalidate Caches and Restart

# Check Compose compiler version
./gradlew dependencies | grep compose
```

## Performance Expectations

- **Clean Build**: 2-5 minutes (Kotlin compilation + Android build)
- **Incremental Build**: 30-90 seconds (changed files only)
- **Unit Tests**: 30-60 seconds (Kotlin test execution)
- **Lint Checks**: 30-45 seconds (Android lint analysis)
- **APK Install**: 15-30 seconds (to connected device)
- **First Launch**: 5-10 seconds (cold start with Room DB init)

## Critical Warnings for Copilot Agents

1. **NEVER CANCEL** ./gradlew build during Kotlin compilation phase
2. **NEVER CANCEL** ./gradlew test during test execution
3. **ALWAYS ENSURE** Android SDK is properly configured
4. **DO NOT MODIFY** gradle.properties without understanding impact
5. **DO NOT ENABLE** Hilt until KAPT/KSP migration is complete
6. **DO NOT WORRY** about some dependency warnings - they're non-critical
7. **DO NOT "FIX"** temporarily disabled features unless specifically requested

## Success Indicators

✅ **./gradlew build completes** successfully with Kotlin compilation
✅ **APK generates** in app/build/outputs/apk/debug/
✅ **Unit tests pass** without critical failures
✅ **Lint completes** with acceptable warnings
✅ **App installs and launches** on Android device/emulator

## Common Pitfalls to Avoid

❌ Canceling Gradle builds that appear slow but are actually compiling
❌ Trying to "fix" Hilt without understanding KAPT migration needs
❌ Modifying Kotlin or Compose versions without compatibility testing
❌ Ignoring Android SDK requirements
❌ Treating build warnings as critical errors (most are informational)
❌ Modifying manual DI without understanding the current architecture

## Final Notes

This is a **modern Kotlin Android project** using Jetpack Compose and Clean Architecture. Some features like Hilt are temporarily disabled due to Kotlin 2.0 compatibility, but the core application builds and runs successfully. Focus on Kotlin/Android best practices rather than React Native patterns.

**When in doubt**: Use the Gradle commands and Android development practices specified in this document. They have been tested and verified to work correctly with the current Kotlin/Android setup.