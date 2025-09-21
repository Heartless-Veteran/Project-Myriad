# Gemini Auto Enhanced - AI-Powered Code Fixing System

**Gemini Auto Enhanced** is an AI-powered code fixing system that leverages Google's Gemini AI to automatically detect and fix code issues across multiple programming languages with language-specific analysis and automated issue resolution.

## Overview

Gemini Auto provides intelligent code analysis and automated fixes for:

- **Kotlin** - Android development best practices, null safety, coroutine management
- **JavaScript/TypeScript** - Modern syntax, performance optimization, security
- **Shell Scripts** - Security, best practices, cross-platform compatibility
- **Build Scripts** - Gradle optimization, dependency management

## Features

### 🧠 **Intelligent Analysis**

- **Language-Specific Rules** - Tailored analysis for each programming language
- **Context-Aware Fixes** - Understanding of project structure and dependencies  
- **Security-First** - Prioritizes security vulnerabilities and best practices
- **Performance Optimization** - Identifies and fixes performance bottlenecks

### 🔧 **Automated Fixing**

- **Safe Transformations** - Non-breaking changes that preserve functionality
- **Batch Processing** - Efficient handling of multiple files and issues
- **Rollback Support** - Easy reversal of applied changes
- **Incremental Updates** - Smart detection of changed files

### 📊 **Quality Metrics**

- **Before/After Analysis** - Quantified improvement metrics
- **Issue Classification** - Categorized by severity and type
- **Coverage Reporting** - Analysis of code coverage and completeness
- **Trend Analysis** - Historical improvement tracking

## Setup and Configuration

### Prerequisites

```bash
# Required tools
node --version    # Node.js 18+ required
npm --version     # npm 8+ required

# Gemini API access
export GEMINI_API_KEY="your-api-key-here"
```

### Installation

```bash
# Clone and setup
git clone https://github.com/Heartless-Veteran/Project-Myriad
cd Project-Myriad

# Install dependencies
npm install

# Configure Gemini Auto
cp .gemini-rules.example.json .gemini-rules.json
```

### Configuration File (`.gemini-rules.json`)

```json
{
  "kotlin": {
    "enabled": true,
    "rules": {
      "null-safety": true,
      "coroutine-context": true,
      "android-best-practices": true,
      "performance-optimization": true
    }
  },
  "javascript": {
    "enabled": true,
    "rules": {
      "modern-syntax": true,
      "unused-imports": true,
      "security-fixes": true,
      "performance-optimization": true
    }
  },
  "shell": {
    "enabled": true,
    "rules": {
      "shellcheck": true,
      "best-practices": true,
      "security-hardening": true
    }
  },
  "fixCategories": {
    "security": true,
    "performance": true,
    "style": true,
    "maintainability": true
  },
  "severityThreshold": "medium",
  "includePatterns": [
    "app/src/main/**/*.kt",
    "core/**/*.kt",
    "feature/**/*.kt",
    "scripts/**/*.js",
    "scripts/**/*.sh"
  ],
  "excludePatterns": [
    "**/test/**",
    "**/build/**",
    "**/node_modules/**",
    "**/.git/**"
  ],
  "modelSelection": {
    "defaultModel": "gemini-1.5-flash",
    "complexTaskModel": "gemini-1.5-pro"
  }
}
```

## Usage

### Basic Operations

```bash
# Run full analysis and auto-fix
node scripts/gemini-auto.js

# Analyze specific files
node scripts/gemini-auto.js --files "app/src/main/kotlin/**/*.kt"

# Dry run (analysis only, no changes)
node scripts/gemini-auto.js --dry-run

# Focus on specific issue types
node scripts/gemini-auto.js --categories security,performance

# Test mode (use mock API responses)
GEMINI_TEST_MODE=true node scripts/gemini-auto.js
```

### Advanced Usage

```bash
# Language-specific analysis
node scripts/gemini-auto.js --language kotlin
node scripts/gemini-auto.js --language javascript

# Severity-based filtering
node scripts/gemini-auto.js --severity high
node scripts/gemini-auto.js --severity medium,high

# Custom configuration
node scripts/gemini-auto.js --config custom-rules.json

# Integration with CI/CD
node scripts/gemini-auto.js --ci-mode --report-format json
```

## Language-Specific Features

### Kotlin Analysis

**Specializations:**
- **Null Safety** - Proper nullable type usage and null checks
- **Coroutines** - Context management and structured concurrency
- **Android Patterns** - Activity lifecycle, fragment management
- **Performance** - Collection usage, memory management

**Example Fixes:**
```kotlin
// Before: Potential null pointer exception
val result = data?.let { it.value }

// After: Safe null handling with proper context
val result = data?.value ?: return defaultValue
```

### JavaScript/TypeScript Analysis

**Specializations:**
- **Modern Syntax** - ES6+ features, async/await patterns
- **Security** - XSS prevention, input validation
- **Performance** - Efficient DOM manipulation, memory leaks
- **Node.js** - File system security, dependency management

**Example Fixes:**
```javascript
// Before: Potential security vulnerability
eval(userInput);

// After: Safe alternative
const safeResult = JSON.parse(userInput);
```

### Shell Script Analysis

**Specializations:**
- **Security** - Injection prevention, privilege escalation
- **Portability** - Cross-platform compatibility
- **Error Handling** - Proper exit codes and error checking
- **Best Practices** - Quoting, variable usage

**Example Fixes:**
```bash
# Before: Unquoted variable expansion
rm $file

# After: Properly quoted for safety
rm "$file"
```

## Integration with Development Workflow

### Git Hooks Integration

```bash
# Pre-commit hook
#!/bin/sh
node scripts/gemini-auto.js --pre-commit --quick

# Pre-push hook
#!/bin/sh
node scripts/gemini-auto.js --pre-push --security-focus
```

### CI/CD Integration

```yaml
# GitHub Actions workflow
- name: Gemini Auto Analysis
  run: |
    node scripts/gemini-auto.js --ci-mode
    git diff --exit-code || echo "Code improvements applied"
```

### IDE Integration

**VS Code Extension:**
```json
{
  "gemini-auto.enabled": true,
  "gemini-auto.autoFixOnSave": true,
  "gemini-auto.languages": ["kotlin", "javascript", "typescript"]
}
```

## Safety and Limitations

### Safety Measures

- **File Size Limits** - Maximum 100KB per file for analysis
- **API Rate Limiting** - Maximum 15 API calls per run
- **Backup Creation** - Automatic backup before applying fixes
- **Rollback Support** - Easy reversal of applied changes

### Known Limitations

- **Complex Logic** - May not understand complex business logic
- **Context Sensitivity** - Limited understanding of project-specific patterns
- **API Dependencies** - Requires internet connectivity and valid API key
- **Language Support** - Currently supports Kotlin, JavaScript, and Shell

### Best Practices

1. **Start Small** - Begin with dry-run mode to understand changes
2. **Review Changes** - Always review applied fixes before committing
3. **Incremental Adoption** - Enable features gradually
4. **Backup Strategy** - Maintain version control and backups
5. **Team Training** - Ensure team understands the tool's capabilities

## Troubleshooting

### Common Issues

**API Key Issues:**
```bash
# Verify API key is set
echo $GEMINI_API_KEY

# Test API connectivity
node scripts/validate-gemini-api.js
```

**Permission Errors:**
```bash
# Make scripts executable
chmod +x scripts/gemini-auto.js

# Check file permissions
ls -la scripts/
```

**Analysis Failures:**
```bash
# Run in debug mode
DEBUG=true node scripts/gemini-auto.js

# Check file patterns
node scripts/gemini-auto.js --list-files
```

### Performance Optimization

**Large Projects:**
```bash
# Process files in batches
node scripts/gemini-auto.js --batch-size 5

# Focus on changed files only
node scripts/gemini-auto.js --changed-only

# Use faster model for simple fixes
node scripts/gemini-auto.js --model gemini-1.5-flash
```

## Testing

### Test Suite

```bash
# Run Gemini Auto tests
node scripts/test-gemini-auto.js

# Test specific components
npm test -- --grep "gemini-auto"

# Integration tests
npm run test:integration
```

### Mock Testing

```bash
# Test without API calls
GEMINI_TEST_MODE=true node scripts/gemini-auto.js

# Validate configuration
node scripts/validate-config.js
```

## Reporting and Analytics

### Generated Reports

- **Fix Summary** - Overview of applied changes
- **Quality Metrics** - Before/after code quality scores
- **Security Analysis** - Vulnerability detection and remediation
- **Performance Impact** - Optimization results and improvements

### Export Formats

```bash
# JSON report for CI/CD
node scripts/gemini-auto.js --report-format json

# HTML report for review
node scripts/gemini-auto.js --report-format html

# Markdown summary
node scripts/gemini-auto.js --report-format markdown
```

## Contributing

### Adding Language Support

1. Create language analyzer in `analyzers/`
2. Add language-specific rules to configuration
3. Implement fix patterns for common issues
4. Add comprehensive tests
5. Update documentation

### Improving Analysis

1. Enhance rule definitions
2. Add context-aware analysis
3. Improve fix accuracy and safety
4. Optimize performance for large codebases

---

## Related Documentation

- [Automated Workflow](AUTOMATED_WORKFLOW.md) - CI/CD integration
- [Development Guide](../DEVELOPMENT.md) - Local development setup
- [Contributing Guide](../CONTRIBUTING.md) - Contribution guidelines
- [Security Policy](../SECURITY.md) - Security best practices

---

*Gemini Auto Enhanced provides intelligent code improvement while maintaining safety and code quality standards.*

*For support and feature requests, please use the GitHub Issues system.*

*Last updated: December 2024*