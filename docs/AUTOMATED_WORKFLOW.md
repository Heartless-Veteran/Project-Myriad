# Automated Documentation & Code Quality Workflow

This document describes the automated workflow system for Project Myriad that ensures code quality, generates documentation, and maintains project consistency.

## Overview

Project Myriad uses a **streamlined automated workflow system** that runs on every push, pull request, and on a daily schedule to maintain high code quality and up-to-date documentation. 

**Key improvements in the consolidated workflow:**
- ✅ **Eliminated redundancy** - Single CI workflow instead of 3 overlapping workflows
- ✅ **Faster execution** - Optimized job dependencies and parallel execution
- ✅ **Consistent environment** - Unified JDK and Gradle setup across all checks
- ✅ **Clear separation** - CI, Security, and Release workflows have distinct purposes

## Workflow Components

### 🔍 Code Quality Checks

**Automated Tools:**
- **ktlint** - Kotlin code formatting and style enforcement
- **Detekt** - Static code analysis for Kotlin
- **Android Lint** - Android-specific code analysis
- **JaCoCo** - Test coverage reporting

**Triggers:**
- Every push to `main` and `develop` branches
- All pull requests
- Daily scheduled runs at 2 AM UTC

### 📚 Documentation Generation

**Automated Documentation:**
- **Dokka** - Kotlin API documentation generation
- **Package Documentation** - Automatic package.md updates
- **README Statistics** - Project metrics and badges
- **Architecture Validation** - Structural consistency checks

### 🚀 Consolidated CI Pipeline

**Single CI Workflow (`ci.yml`):**
- **Build & Quality Job** - Unified build, test, and quality checks
- **Documentation Job** - Automated documentation generation (conditional)
- **Release Job** - Release APK generation (main branch only)

**Optimizations:**
- Shared Gradle caching and setup
- Parallel-safe quality checks with `--continue` flag
- Conditional job execution based on event type
- Comprehensive artifact collection

## Workflow Files

### `.github/workflows/ci.yml`
**Main CI workflow** - Consolidated build, test, and quality checks:

```yaml
# Runs on: push, PR, daily schedule
# Jobs:
# - build-and-quality: Build APK, ktlint, detekt, Android lint, unit tests, test coverage reporting
# - documentation: Dokka generation, architecture validation, README stats, Node.js validation scripts
# - release: Release APK generation (main branch only)
```

### `.github/workflows/security.yml`
**Security scanning** - Dedicated security and vulnerability analysis:

```yaml
# Runs on: push, PR, weekly schedule
# Jobs:
# - codeql: CodeQL static analysis
# - dependency-check: OWASP dependency vulnerability scanning
# - secret-scan: TruffleHog secret detection
```

### `.github/workflows/release.yml`
**Release management** - Automated release creation and publishing:

```yaml
# Runs on: version tags, manual dispatch
# Jobs:
# - release: Version extraction, APK signing, GitHub release creation
```

## Local Development Integration

### Setup Commands

```bash
# Install Git hooks for automated checks
./scripts/setup.sh

# Run full quality check locally
./gradlew check

# Generate documentation locally
./gradlew dokkaHtml

# Update README statistics
node scripts/update-readme-stats.js

# Validate project configuration
node scripts/validate-config.js
```

### Pre-commit Validation

The workflow automatically runs these checks before allowing commits:

1. **Kotlin formatting** - ktlint fixes formatting issues
2. **Code analysis** - Detekt identifies potential issues
3. **Test execution** - Ensures tests pass
4. **Documentation** - Updates package documentation

## Configuration Files

### `config/detekt/detekt.yml`
Detekt static analysis configuration with project-specific rules.

### `gradle.properties`
Gradle configuration optimized for CI/CD performance:

```properties
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
org.gradle.parallel=true
org.gradle.caching=true
```

### `.editorconfig`
Consistent code formatting across editors and IDEs.

## Quality Gates

### Pull Request Requirements

Before merging, PRs must pass:

- [x] **Build Success** - All modules compile successfully
- [x] **Test Coverage** - Minimum 70% coverage (target)
- [x] **Code Quality** - Detekt analysis passes
- [x] **Formatting** - ktlint formatting enforced
- [x] **Documentation** - API docs updated for public APIs

### Release Requirements

Releases require additional validation:

- [x] **Security Scan** - No critical vulnerabilities
- [x] **Performance Tests** - No regression in key metrics
- [x] **Integration Tests** - All flows working correctly
- [x] **Documentation Review** - Release notes and API docs current

## Metrics and Reporting

### Automated Reports

The workflow generates:

- **Test Coverage Reports** - JaCoCo HTML and XML
- **Code Quality Reports** - Detekt findings and metrics
- **API Documentation** - Dokka HTML documentation
- **Dependency Reports** - License and vulnerability scans

### Badge Updates

README badges are automatically updated with:

- Build status (passing/failing)
- Test coverage percentage
- Code quality score
- Latest release version
- Supported Android API levels

## Troubleshooting

### Common Issues

**Build Failures:**
```bash
# Clear caches and rebuild
./gradlew clean build

# Check dependency issues
./gradlew dependencies
```

**Quality Gate Failures:**
```bash
# Fix formatting issues
./gradlew ktlintFormat

# Address Detekt findings
./gradlew detekt

# Update test coverage
./gradlew jacocoTestReport
```

**Documentation Issues:**
```bash
# Regenerate documentation
./gradlew dokkaHtml

# Update README stats
node scripts/update-readme-stats.js

# Validate configuration
node scripts/validate-config.js
```

## Future Enhancements

### Planned Improvements

- **Performance Monitoring** - Automated performance regression detection
- **Visual Testing** - Automated UI screenshot comparisons
- **Accessibility Testing** - Automated accessibility compliance checks
- **Multi-locale Testing** - Internationalization validation

### Integration Opportunities

- **Sonar Cloud** - Enhanced code quality analysis
- **Codecov** - Advanced coverage reporting
- **Renovate** - Automated dependency updates
- **Security Scanning** - Enhanced vulnerability detection

---

## Related Documentation

- [Development Guide](../DEVELOPMENT.md) - Local development setup
- [Contributing Guide](../CONTRIBUTING.md) - Contribution guidelines
- [Code Quality Configuration](../config/detekt/detekt.yml) - Quality rules
- [CI/CD Configuration](../.github/workflows/) - Workflow definitions

---

*This workflow system ensures Project Myriad maintains high quality standards while enabling rapid development and reliable releases.*

*Last updated: December 2024*