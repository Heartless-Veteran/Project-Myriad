# Project Myriad Development Guidelines

This document provides essential information for developers working on Project Myriad, a pure Android manga reader application built with modern Kotlin technologies. Following these guidelines ensures consistency, maintainability, and quality across the codebase.

## Build and Configuration Instructions

### Prerequisites

- **Android Studio Jellyfish | 2023.3.1** or newer
- **JDK 17** or higher (required for latest Kotlin features)
- **Android SDK API 24-36** (full compatibility)
- **Kotlin 2.2.20** with Compose compiler support
- **Gradle 9.1.0** (automatically handled by wrapper)

### Setting Up the Development Environment

1. **Clone the repository**
   ```bash
   git clone https://github.com/Heartless-Veteran/Project-Myriad.git
   cd Project-Myriad
   ```

2. **Configure local properties** (if needed for API keys)
   ```bash
   cp local.properties.example local.properties
   ```

3. **Open in Android Studio and sync project**
   - The project uses version catalogs for dependency management
   - First sync may take several minutes to download dependencies

4. **Build and run on Android device/emulator**
   - Ensure device/emulator runs API 24+ for full compatibility

### Running the Application

**📋 Complete Guide**: For detailed command-line build instructions, see the "Command-Line Build Methods" section below.

#### Quick Development Commands

```bash
# Full clean build (takes 2-5 minutes first time)
./gradlew clean build

# Fast incremental build for development
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run all tests
./gradlew test

# Run quality checks (lint, ktlint, detekt)
./gradlew check
```

#### Building a Release APK

```bash
# Generate a signed release APK
./gradlew assembleRelease
```

The APK will be generated at `app/build/outputs/apk/release/app-release.apk`.

#### Building an Android App Bundle (AAB) - Recommended for Play Store

```bash
# Generate a signed Android App Bundle for Google Play Store
./gradlew bundleRelease
```

The AAB will be generated at `app/build/outputs/bundle/release/app-release.aab`.

**📋 Important**: For production releases, see the complete guide in [`RELEASE_BUILD_GUIDE.md`](RELEASE_BUILD_GUIDE.md) which covers:
- Version management and signing key setup
- Security best practices for release builds  
- Google Play Store publishing process

### Configuration Files

- **Build Config**: `build.gradle.kts` contains project-wide build configuration
- **App Config**: `app/build.gradle.kts` contains app-specific build configuration  
- **Settings**: `settings.gradle.kts` defines project structure
- **Gradle Properties**: `gradle.properties` contains build optimization settings

## Testing Information

### Test Framework

Project Myriad uses JUnit and MockK for unit testing. The test configuration is defined in the app's `build.gradle.kts` file.

### Running Tests

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run instrumented tests (requires device/emulator)
./gradlew connectedDebugAndroidTest
```

### Writing Tests

- Place unit tests in `app/src/test/kotlin/`
- Place instrumented tests in `app/src/androidTest/kotlin/`
- Follow AAA pattern (Arrange, Act, Assert)
- Use MockK for mocking dependencies

## Kotlin Guidelines

### Code Style

- Follow official Kotlin coding conventions
- Use meaningful variable and function names
- Keep functions small and focused on single responsibility
- Prefer immutability when possible

### Architecture Guidelines

Project Myriad follows Clean Architecture principles with MVVM pattern:

```
app/src/main/kotlin/com/heartlessveteran/myriad/
├── data/                   # Data layer
│   ├── database/          # Room database
│   ├── repository/        # Repository implementations
│   └── di/               # Data dependency injection
├── domain/               # Business logic layer
│   ├── entities/         # Core entities
│   ├── repository/       # Repository interfaces
│   └── models/          # Domain models (Result, etc.)
├── ui/                  # Presentation layer
│   ├── screens/         # Compose screens
│   ├── navigation/      # Navigation setup
│   ├── theme/          # Material 3 theming
│   └── viewmodel/      # MVVM ViewModels
└── di/                 # Dependency injection modules
```

### Dependency Injection

- Use Hilt for dependency injection
- Define modules in the `di/` packages
- Use `@HiltAndroidApp` for Application class
- Use `@AndroidEntryPoint` for Activities and ViewModels

### Database Guidelines

- Use Room for local database
- Define entities in `domain/entities/`
- Implement DAOs in `data/database/dao/`
- Use Flow for reactive data streams

### UI Guidelines

- Use Jetpack Compose for UI
- Follow Material 3 design guidelines
- Keep Composables small and focused
- Use ViewModels for state management
- Handle UI state with sealed classes

## File Organization

### Naming Conventions

- Use PascalCase for classes and interfaces
- Use camelCase for functions and variables
- Use UPPER_SNAKE_CASE for constants
- Add descriptive suffixes:
  - `Activity` for activities (if any)
  - `ViewModel` for ViewModels
  - `Repository` for repositories
  - `Dao` for data access objects
  - `Screen` for Compose screens

### Package Structure

- Group by feature, not by type
- Keep related files close together
- Use consistent package naming across features

## Performance Guidelines

### Memory Management

- Avoid memory leaks by properly managing lifecycle
- Use weak references where appropriate
- Profile memory usage regularly

### Database Performance

- Use appropriate database queries
- Implement proper indexing
- Use pagination for large datasets
- Cache frequently accessed data

### UI Performance

- Optimize Compose recomposition
- Use LazyColumn/LazyRow for long lists
- Implement proper image caching with Coil
- Profile UI rendering performance

## Command-Line Build Methods

For developers preferring command-line tools or CI/CD environments:

### Prerequisites
- **JDK 11+** (Java Development Kit)
- **Android SDK** with API levels 24-36  
- **Environment variables**: `ANDROID_HOME` or `ANDROID_SDK_ROOT` set to your Android SDK path

### Build Commands

```bash
# Build debug APK (most common)
./gradlew assembleDebug

# Build release APK (requires signing configuration)
./gradlew assembleRelease

# Install debug APK on connected device
./gradlew installDebug

# Clean and rebuild
./gradlew clean build

# Run all tests
./gradlew test

# Generate test coverage report
./gradlew jacocoTestReport
```

**Output locations**:
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`

### Environment Verification

```bash
# Check Android SDK
echo $ANDROID_HOME
adb version

# Check Java version
java -version

# Verify Gradle
./gradlew --version
```

## Testing Guidelines

### Unit Testing

- Test business logic in isolation
- Mock external dependencies
- Aim for high code coverage
- Test edge cases and error conditions

### Integration Testing

- Test component interactions
- Use test databases for data layer tests
- Verify proper dependency injection

### UI Testing

- Test user interactions
- Verify UI state changes
- Use Compose testing APIs
- Test accessibility features

## Code Quality

### Linting and Static Analysis

```bash
# Run lint checks
./gradlew lintDebug
```

### Code Review Guidelines

- Review for architectural consistency
- Check performance implications
- Verify proper error handling
- Ensure adequate test coverage
- Review security implications

## Troubleshooting

### Common Build Issues

1. **Gradle sync fails**: Check Android Studio version and SDK installation
2. **Compose version conflicts**: Verify Kotlin and Compose compiler versions
3. **Memory issues**: Increase Gradle JVM heap size in `gradle.properties`

### Debugging

- Use Android Studio debugger
- Add logging with proper log levels
- Use network inspection tools
- Profile app performance regularly

## Architecture Overview

Project Myriad follows **Clean Architecture** principles with MVVM pattern. For complete architectural details, design patterns, and implementation guidelines, see:

📖 **[Complete Architecture Documentation](ARCHITECTURE.md)**

### Quick Reference

**Module Structure:**
```
app/src/main/kotlin/com/heartlessveteran/myriad/
├── ui/                  # Presentation Layer (Compose UI, ViewModels)
├── domain/             # Business Logic Layer (Use Cases, Entities)
├── data/               # Data Layer (Room, Repositories, Network)
└── di/                 # Dependency Injection Setup
```

**Key Principles:**
- Single Source of Truth (Room database)
- Unidirectional Data Flow (UI → ViewModel → Use Case → Repository)
- Separation of Concerns (Clear layer boundaries)
- Dependency Inversion (Abstractions over concretions)

## Related Documentation

For additional technical information:

- **[Architecture Documentation](ARCHITECTURE.md)** - Complete architecture details and patterns
- **[Requirements Specification](docs/requirements.md)** - Detailed technical requirements  
- **[Contributing Guidelines](CONTRIBUTING.md)** - Development standards and contribution process
- **[Documentation Index](docs/INDEX.md)** - Complete documentation navigation guide

## Contributing Guidelines

- Follow the established architecture patterns
- Write tests for new features
- Update documentation for significant changes
- Use meaningful commit messages
- Create focused pull requests

## Additional Resources

- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Android Developer Guides](https://developer.android.com/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)