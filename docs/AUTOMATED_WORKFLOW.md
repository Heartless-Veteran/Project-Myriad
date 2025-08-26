# Automated Documentation & Code Quality Workflow

This document describes the automated documentation and code quality workflow implemented for Project Myriad.

## Overview

The automated workflow provides comprehensive code quality checks, documentation generation, and automated improvements through GitHub Actions. It runs on every push, pull request, and weekly schedule.

## Features

### 🔍 Code Quality Checks
- **ktlint**: Kotlin code style checking and formatting
- **Detekt**: Static code analysis for Kotlin
- **Android Lint**: Android-specific code analysis
- **JaCoCo**: Test coverage reporting

### 📚 Documentation Generation
- **Dokka**: API documentation generation
- **README Statistics**: Automated project statistics updates
- **Dependency Documentation**: Current dependency tracking
- **Code Metrics**: Lines of code, file counts, complexity metrics

### 🏗️ Architecture Validation
- **Clean Architecture**: Validates layer separation and dependencies
- **Package Structure**: Ensures proper package organization
- **Naming Conventions**: Validates naming patterns for ViewModels, Repositories, etc.
- **Dependency Direction**: Prevents improper dependencies between layers

### 🎨 Automated Fixes
- **ktlint Format**: Automatically fixes code style issues
- **Detekt Auto-correct**: Applies safe static analysis fixes
- **Pull Request Creation**: Creates PRs with automated improvements

## Workflow Triggers

The workflow runs on:
- **Push to main/develop branches**
- **Pull requests** to main/develop
- **Weekly schedule** (Sundays at 2 AM UTC)
- **Manual dispatch** from GitHub Actions tab

## Configuration Files

### Gradle Configuration
- `build.gradle.kts`: Added ktlint, detekt, dokka, and jacoco plugins
- `app/build.gradle.kts`: Plugin applications and task configurations

### Code Quality Configuration
- `config/detekt/detekt.yml`: Detekt rules and settings
- `docs/packages.md`: Dokka package documentation

### GitHub Actions
- `.github/workflows/automated-documentation-quality.yml`: Main workflow

## Scripts

### Node.js Scripts
- `scripts/validate-architecture.js`: Architecture validation
- `scripts/update-readme-stats.js`: README statistics updater
- `scripts/validate-config.js`: Configuration validation
- `scripts/test-workflow.js`: Workflow component testing

### NPM Commands
```bash
npm run validate-architecture    # Validate Clean Architecture principles
npm run update-readme-stats     # Update README with current statistics
npm run test-workflow           # Test all workflow components
```

### Gradle Tasks
```bash
./gradlew ktlintCheck           # Check code style
./gradlew ktlintFormat          # Fix code style issues
./gradlew detekt                # Run static analysis
./gradlew dokkaHtml            # Generate API documentation
./gradlew jacocoTestReport     # Generate test coverage report
```

## Workflow Jobs

### 1. Code Quality Analysis
- Runs ktlint, detekt, and Android lint
- Generates test coverage reports
- Comments coverage on pull requests
- Outputs violation status for other jobs

### 2. Architecture Validation
- Validates Clean Architecture principles
- Checks package structure and naming conventions
- Verifies dependency directions
- Generates architecture report

### 3. Documentation Generation
- Generates API documentation with Dokka
- Updates README statistics
- Creates dependency documentation
- Generates code metrics reports

### 4. Code Formatting (conditional)
- Runs only if violations are found
- Applies ktlint formatting
- Applies detekt auto-corrections
- Commits changes directly to the branch

### 5. Improvement PR Creation
- Creates automated improvement PRs
- Includes all documentation updates
- Provides detailed quality metrics
- Runs on schedule or manual trigger

## Quality Metrics

The workflow tracks and reports:
- **Test Coverage Percentage**
- **Code Quality Violations**
- **Architecture Compliance**
- **Lines of Code**
- **File Counts by Layer**
- **Dependency Counts**

## Reports and Artifacts

Generated reports include:
- **ktlint Reports**: Code style violations
- **Detekt Reports**: HTML, XML, MD formats
- **Android Lint Reports**: Android-specific issues
- **JaCoCo Reports**: Test coverage analysis
- **Dokka Documentation**: API documentation
- **Architecture Reports**: Layer validation results

## Usage

### Automatic Execution
The workflow runs automatically on:
- New commits to main/develop
- Pull requests
- Weekly schedule

### Manual Execution
1. Go to GitHub Actions tab
2. Select "Automated Documentation & Code Quality"
3. Click "Run workflow"
4. Optionally force PR creation

### Local Testing
```bash
# Test individual components
npm run validate-architecture
npm run update-readme-stats
./gradlew ktlintCheck detekt

# Test entire workflow
npm run test-workflow
```

## Configuration

### Customizing Rules
- Edit `config/detekt/detekt.yml` for detekt rules
- Modify ktlint settings in `app/build.gradle.kts`
- Update architecture validation in `scripts/validate-architecture.js`

### Workflow Customization
- Modify `.github/workflows/automated-documentation-quality.yml`
- Adjust triggers, add new jobs, or modify existing ones
- Configure environment variables and secrets

## Troubleshooting

### Common Issues
1. **Detekt Configuration Errors**: Check `config/detekt/detekt.yml` syntax
2. **ktlint Parse Errors**: Check Kotlin syntax in source files  
3. **Dokka Generation Failures**: Verify `docs/packages.md` format
4. **Architecture Violations**: Review layer structure and dependencies

### Debug Commands
```bash
# Validate configuration
node scripts/validate-config.js

# Test workflow components
npm run test-workflow

# Check Gradle tasks
./gradlew tasks --group=verification
```

## Benefits

### Development Team
- **Consistent Code Quality**: Automated style and quality checks
- **Up-to-date Documentation**: Always current API docs and README
- **Architecture Compliance**: Enforced Clean Architecture principles
- **Reduced Manual Work**: Automated formatting and documentation

### Project Health
- **Quality Metrics Tracking**: Continuous monitoring of code quality
- **Technical Debt Prevention**: Early detection of issues
- **Knowledge Sharing**: Comprehensive documentation generation
- **Maintainability**: Enforced best practices and conventions

## Contributing

To modify the workflow:
1. Test changes locally with `npm run test-workflow`
2. Validate configuration with `node scripts/validate-config.js`
3. Create a pull request with workflow changes
4. The workflow will validate its own updates

The automated documentation and code quality workflow helps maintain high code standards and keeps project documentation current without manual intervention.