# Project Myriad Release Build Guide

This document provides a comprehensive guide for creating production-ready releases of Project Myriad.

## Overview

Project Myriad now supports complete release build configuration including:
- ✅ **Version Management**: Automatic version code and name handling
- ✅ **Digital Signing**: RSA keystore-based signing for secure releases
- ✅ **Code Optimization**: R8 full mode with ProGuard rules
- ✅ **Android App Bundle**: AAB generation for Google Play Store
- ✅ **Build Splitting**: ABI, density, and language splits for optimized downloads

## Step 1: Update Version Information

Before creating a release, update your app's version in `app/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        versionCode = 2 // Increment for every release (currently: 2)
        versionName = "1.0.1" // User-facing version (currently: "1.0.1")
    }
}
```

**Important**: 
- `versionCode` must be incremented for every new release
- `versionName` should follow semantic versioning (e.g., "1.0.1", "1.1.0")

## Step 2: Generate Release Signing Key

**⚠️ SECURITY NOTE**: Only do this once and keep your keystore secure!

### Generate Keystore

```bash
keytool -genkey -v -keystore app/myriad-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias myriad-key-alias
```

You will be prompted for:
- Keystore password (remember this!)
- Key alias password (remember this!)
- Your name and organization details

### Secure the Keystore

1. **Store safely**: Keep `myriad-release-key.jks` secure and backed up
2. **Never commit**: The keystore is automatically ignored by Git
3. **Document passwords**: Store passwords securely (e.g., password manager)

## Step 3: Configure Local Properties

Create or update `local.properties` with your signing credentials:

```properties
# Google Gemini API Key
geminiApiKey=your_gemini_api_key_here

# Release Signing Configuration
MYRIAD_RELEASE_STORE_PASSWORD=your_keystore_password_here
MYRIAD_RELEASE_KEY_PASSWORD=your_key_alias_password_here
```

**⚠️ SECURITY**: Never commit `local.properties` to version control!

## Step 4: Build Release Outputs

### Build Signed APK

```bash
./gradlew assembleRelease
```

**Output**: `app/build/outputs/apk/release/app-release.apk`

### Build Android App Bundle (Recommended)

```bash
./gradlew bundleRelease
```

**Output**: `app/build/outputs/bundle/release/app-release.aab`

**📱 Google Play Store**: Upload the `.aab` file for optimal delivery

## Step 5: Verify Release Build

### Check Signing

```bash
# Verify APK signature
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# Check AAB signature  
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
```

### Validate Optimizations

The release build includes:
- ✅ **R8 Code Shrinking**: Reduces APK size
- ✅ **Resource Shrinking**: Removes unused resources
- ✅ **ProGuard Rules**: Optimizes and obfuscates code
- ✅ **ABI Splits**: Separate APKs for different architectures

## Build Configuration Details

### Signing Configuration

```kotlin
signingConfigs {
    create("release") {
        val keystoreFile = file("myriad-release-key.jks")
        if (keystoreFile.exists()) {
            storeFile = keystoreFile
            storePassword = localProperties.getProperty("MYRIAD_RELEASE_STORE_PASSWORD")
            keyAlias = "myriad-key-alias"
            keyPassword = localProperties.getProperty("MYRIAD_RELEASE_KEY_PASSWORD")
        }
    }
}
```

### Release Build Type

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro",
        )
        signingConfig = signingConfigs.getByName("release")
    }
}
```

### App Bundle Optimization

```kotlin
bundle {
    abi {
        enableSplit = true
    }
    density {
        enableSplit = true
    }
    language {
        enableSplit = true
    }
}
```

## Release Checklist

Before publishing:

- [ ] **Version Updated**: Increment versionCode and update versionName
- [ ] **Code Quality**: All tests pass, lint checks clean
- [ ] **Keystore Ready**: Signing key configured and secure
- [ ] **Build Success**: Both APK and AAB generate successfully
- [ ] **Signature Verified**: Release builds are properly signed
- [ ] **Testing Complete**: App tested on multiple devices/Android versions
- [ ] **Release Notes**: Documentation prepared for users

## Troubleshooting

### Build Failures

If release build fails:

```bash
# Clean and rebuild
./gradlew clean
./gradlew bundleRelease

# Check for missing keystore
ls -la app/myriad-release-key.jks

# Verify local.properties
cat local.properties
```

### Signing Issues

```bash
# Regenerate keystore if needed
rm app/myriad-release-key.jks
keytool -genkey -v -keystore app/myriad-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias myriad-key-alias
```

## Publishing to Google Play Store

1. **Upload AAB**: Use `app-release.aab` (not APK)
2. **Release Notes**: Document changes and improvements
3. **Testing**: Use internal/alpha testing before production
4. **Staged Rollout**: Consider gradual release percentages

## Security Best Practices

- 🔐 **Never share keystore files publicly**
- 🔐 **Use strong, unique passwords for keystore and key alias**
- 🔐 **Backup keystore securely (you cannot recover if lost)**
- 🔐 **Keep local.properties out of version control**
- 🔐 **Use separate keystores for debug and release**

---

**📱 Project Myriad Release System**: Production-ready Android app releases with security and optimization built-in.