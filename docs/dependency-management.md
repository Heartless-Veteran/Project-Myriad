# Dependency Management Strategy

This document outlines the dependency management strategy for Project Myriad, a Kotlin Android application, addressing deprecated packages and maintaining up-to-date dependencies.

## Recent Updates

### Deprecated Package Replacements

The following deprecated packages have been updated or replaced:

1. **Hilt (Dagger) Dependency Injection** 
   - Temporarily disabled due to Kotlin 2.0 KAPT compatibility issues
   - Using manual dependency injection until KAPT is replaced with KSP

2. **Android Gradle Plugin Updates** 
   - Updated to version 8.12.1 for better Kotlin 2.0 support
   - Improved build performance and compatibility

3. **Kotlin Compiler Updates**
   - Updated to Kotlin 2.2.10 with Compose compiler plugin
   - Improved Jetpack Compose compilation performance

### Core Dependencies Updated

- **Kotlin**: Updated to 2.2.10 (from 2.0.21)
- **Android Gradle Plugin**: Updated to 8.12.1 (from 8.1.x)
- **Jetpack Compose BOM**: Updated to 2024.02.00 (latest stable)
- **Android SDK**: Targeting API 35/36 (Android 15+)

## Renovate Configuration

The project uses Renovate for automated dependency management with the following strategy:

### Package Rules

1. **Kotlin Core**: Manual review required for Kotlin language updates
2. **Android Ecosystem**: Grouped updates for Android and Jetpack related packages
3. **Dev Dependencies**: Auto-merge enabled for development dependencies
4. **Deprecated Package Handling**: Automatic replacement rules for known deprecated packages

### Security

- Vulnerability alerts enabled
- Lock file maintenance enabled (for Gradle)
- Dependency dashboard enabled for visibility

## Manual Dependency Checks

### Before Adding New Dependencies

1. Check if the package is actively maintained
2. Verify compatibility with current Kotlin and Android versions
3. Check for security vulnerabilities
4. Consider APK size impact

### Regular Maintenance

1. Review Renovate dependency dashboard weekly
2. Test major version updates in development environment
3. Monitor for deprecation warnings in build logs
4. Update documentation when dependencies change significantly

## Troubleshooting

### Common Issues

1. **Gradle Plugin Compatibility**: Ensure Android Gradle plugin version is compatible with Kotlin version
2. **Compose Compilation**: Update Compose compiler when Kotlin version changes. For Kotlin 2.2+, use Compose compiler plugin instead of separate version.

### Known Issues

1. **Hilt/KAPT Compatibility**: Hilt is temporarily disabled due to Kotlin 2.0+ KAPT compatibility issues. Using manual dependency injection until KSP migration is complete.
2. **Gradle Dependencies**: Some libraries may show warnings with Kotlin 2.2.10 but still function correctly.
3. **Build Performance**: Large projects may benefit from enabling Gradle build cache and parallel builds.

3. **Android Build**: Clean and rebuild Android project after major dependency updates using `./gradlew clean build`
