# Dependency Management Strategy

This document outlines the dependency management strategy for Project Myriad, a Kotlin Android application, addressing deprecated packages and maintaining up-to-date dependencies.

## Recent Updates

### Deprecated Package Replacements

The following packages have been updated or replaced:

1. **Hilt (Dagger) Dependency Injection** 
   - ✅ **RESOLVED**: Successfully migrated from KAPT to KSP
   - Hilt now fully operational with KSP annotation processing
   - All dependency injection working with @HiltAndroidApp, @AndroidEntryPoint, @HiltViewModel

2. **Annotation Processing Migration**
   - ✅ **COMPLETE**: Migrated from KAPT to KSP (Kotlin Symbol Processing)
   - Room database compiler now uses KSP
   - Hilt compiler now uses KSP
   - KAPT completely removed from the project

3. **Android Gradle Plugin Updates** 
   - Updated to version 8.13.0 for better Kotlin 2.1 support
   - Improved build performance and compatibility

4. **Kotlin Compiler Updates**
   - Updated to Kotlin 2.2.20 with KSP 2.2.20-2.0.3 compatibility
   - Improved Jetpack Compose compilation performance

### Core Dependencies Updated

- **Kotlin**: Updated to 2.2.20 (latest stable with full KSP compatibility)
- **Android Gradle Plugin**: Updated to 8.13.0 (from 8.1.x)
- **Jetpack Compose BOM**: Updated to 2025.09.00 (latest stable)
- **Android SDK**: Targeting API 35/36 (Android 15+)
- **KSP**: Added KSP 2.2.20-2.0.3 for annotation processing
- **Hilt**: Re-enabled with KSP (version 2.57.1)

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

1. **Build Performance**: Large projects may benefit from enabling Gradle build cache and parallel builds.
2. **Gradle Dependencies**: Some libraries may show deprecation warnings with Kotlin 2.2.20 but still function correctly.

**Note**: The previous Hilt/KAPT compatibility issue has been resolved with the successful migration to KSP.

3. **Android Build**: Clean and rebuild Android project after major dependency updates using `./gradlew clean build`
