# Gemini Auto Enhanced - AI Code Fixer

🤖 **Gemini Auto Enhanced** is an advanced AI-powered code fixing system that automatically identifies and resolves code issues in your pull requests with language-specific intelligence. Unlike traditional AI review tools that only provide suggestions, Gemini Auto actually fixes the code and commits the changes directly to your PR.

**🚀 New Enhanced Features**: Language-specific analyzers, intelligent model selection, configurable rules, and rich categorized reporting.

## Features

### 🔧 Enhanced Auto-Fix Capabilities
- **Language-Specific Analysis**: Specialized rules for Kotlin, JavaScript/TypeScript, and Shell
- **Kotlin Optimizations**: Null-safety, coroutine context, data classes, Compose performance
- **JavaScript/TypeScript**: Modern syntax, unused imports, async/await patterns
- **Shell Scripts**: Best practices, security patterns, shebang validation
- **Security**: Applies security best practices and fixes vulnerabilities  
- **Performance**: Optimizes code for better performance with language-specific patterns
- **Style**: Standardizes code formatting with language conventions

### 🧠 Intelligent AI Selection
- **Smart Model Selection**: Uses Gemini 1.5 Pro for complex tasks (large files, many issues, Kotlin classes)
- **Efficient Processing**: Falls back to Gemini 1.5 Flash for simpler tasks
- **Task Complexity Analysis**: Automatically determines optimal model based on file size and issue count

### ⚙️ Configurable Rule System
- **Custom Configuration**: Support for `.gemini-rules.json` project-specific rules
- **Fix Categories**: Enable/disable specific types of fixes (Security, Performance, Style, Architecture, Testing)
- **Severity Thresholds**: Configurable minimum severity for fixes
- **Include/Exclude Patterns**: Granular control over which files to analyze

### 📊 Rich Reporting & Analytics
- **Categorized Issue Reports**: Issues grouped by type (Security, Performance, Style)
- **Detailed Statistics**: Comprehensive analysis with file counts and fix breakdowns
- **Educational Explanations**: Context-aware explanations for applied fixes
- **Progress Tracking**: Real-time feedback during analysis process

### 🛡️ Security Enhancements
- **Conditional Execution**: Only runs when actual issues are detected
- **Input Sanitization**: Validates all content sent to AI API
- **Response Validation**: Checks AI responses for malicious content
- **Rate Limiting**: Limits API calls per run to prevent quota exhaustion
- **Size Validation**: Enforces file size limits for security
- **Change Validation**: Ensures fixes are actual improvements

### 🚀 Seamless Integration
- **Direct Commits**: Fixes are committed directly to your PR
- **Gradle Task Integration**: Run locally with `./gradlew geminiAutoFix`
- **GitHub Integration**: Enhanced workflow with detailed PR comments
- **Multi-Language Support**: Kotlin (primary), JavaScript, TypeScript, Shell scripts
- **Efficient**: Pre-checks prevent unnecessary API calls

## Setup

### 1. Get a Gemini API Key
1. Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Sign in with your Google account
3. Click **Create API Key**
4. Copy the generated API key

### 2. Configure GitHub Secret
1. Go to your repository **Settings > Secrets and variables > Actions**
2. Click **New repository secret**
3. Name: `GEMINI_API_KEY`
4. Value: Paste your API key from step 1

### 3. Usage

Gemini Auto will automatically run on:
- **New PRs**: Automatically analyzes and fixes code in new pull requests
- **New commits**: Re-analyzes when you push new commits to a PR
- **Manual trigger**: Add the `gemini-auto` label to any PR to trigger analysis
- **Workflow dispatch**: Manually run the workflow from GitHub Actions tab

## How It Works

1. **Pre-Check Analysis**: Before using AI, scans your code to detect if fixes are actually needed:
   - ESLint for JavaScript/TypeScript linting  
   - Basic syntax analysis as fallback
   - File-by-file issue detection

2. **Conditional Execution**: Only proceeds with AI analysis if issues are detected:
   - Saves API quota on clean code
   - Reduces unnecessary workflow runs
   - Provides clear feedback when no fixes needed

3. **Secure AI Processing**: When issues are found, sends code to Google's Gemini AI with security safeguards:
   - Input sanitization and size limits
   - Response validation for malicious content
   - Rate limiting to prevent quota exhaustion
   - Enhanced error handling

4. **Validated Auto-Fix**: Applies AI-suggested fixes with comprehensive validation:
   - Ensures fixes are actual improvements
   - Validates file structure integrity
   - Checks for reasonable change magnitude
   - Language-specific syntax validation

5. **Commit & Push**: Creates a commit with validated fixes and pushes to your PR:
   ```
   🤖 Gemini Auto: Apply AI-suggested fixes

   Auto-generated fixes by Gemini AI to improve code quality,
   performance, and adherence to best practices.
   ```

## Configuration

### File Types Analyzed
- **Kotlin**: `.kt` files in `app/src/main/` (primary focus)
- **JavaScript/TypeScript**: `.js`, `.ts`, `.jsx`, `.tsx` files in `src/`
- **Shell Scripts**: `.sh` files in `scripts/`
- **Config Files**: `package.json`, `build.gradle.kts`

### Custom Configuration

Create a `.gemini-rules.json` file in your project root to customize Gemini Auto behavior:

```json
{
  "kotlin": {
    "enabled": true,
    "rules": {
      "null-safety": true,
      "coroutine-context": true,
      "compose-performance": true,
      "hilt-injection": true,
      "data-class-conventions": true
    }
  },
  "javascript": {
    "enabled": true,
    "rules": {
      "modern-syntax": true,
      "unused-imports": true,
      "async-await": true,
      "const-let": true
    }
  },
  "shell": {
    "enabled": true,
    "rules": {
      "shellcheck": true,
      "best-practices": true
    }
  },
  "fixCategories": {
    "security": true,
    "performance": true,
    "style": true,
    "architecture": false,
    "testing": false
  },
  "severityThreshold": "medium",
  "includePatterns": [
    "app/src/main/**/*.kt",
    "src/**/*.js",
    "src/**/*.ts"
  ],
  "excludePatterns": [
    "**/test/**",
    "**/build/**"
  ],
  "modelSelection": {
    "defaultModel": "gemini-1.5-flash",
    "complexTaskModel": "gemini-1.5-pro",
    "complexityThresholds": {
      "fileSize": 50000,
      "issueCount": 10,
      "kotlinClass": true
    }
  }
}
```

### Configuration Options

#### Language Rules
- **kotlin.rules.null-safety**: Detect and fix potential null pointer exceptions
- **kotlin.rules.coroutine-context**: Ensure proper coroutine dispatcher usage
- **kotlin.rules.compose-performance**: Optimize Jetpack Compose performance
- **kotlin.rules.data-class-conventions**: Enforce data class best practices
- **javascript.rules.modern-syntax**: Promote modern ES6+ syntax
- **javascript.rules.unused-imports**: Remove unused imports and variables
- **shell.rules.best-practices**: Apply shell scripting best practices

#### Fix Categories
- **security**: Security vulnerabilities and best practices
- **performance**: Performance optimizations and bottlenecks
- **style**: Code formatting and style conventions
- **architecture**: Design patterns and architectural improvements
- **testing**: Test coverage and testing best practices

#### Model Selection
- **defaultModel**: Model for simple tasks (default: `gemini-1.5-flash`)
- **complexTaskModel**: Model for complex analysis (default: `gemini-1.5-pro`)
- **complexityThresholds**: Criteria for using the complex model

### Limitations
- Maximum 20 files per run (configurable)
- Maximum 15 API calls per run for quota conservation
- Files must be under 100KB for security
- Only applies safe, conservative fixes
- Validates all changes before applying

### Local Usage

Run Gemini Auto locally using the Gradle task:

```bash
# Run Gemini Auto locally
./gradlew geminiAutoFix

# Check Gemini Auto configuration
./gradlew geminiAutoCheck
```

## Security Features

### 🛡️ Input Validation
- **File Size Limits**: Enforces 100KB maximum per file
- **Content Sanitization**: Removes control characters and potential injection patterns
- **Type Validation**: Ensures all inputs are properly formatted

### 🔒 API Security
- **Response Validation**: Checks AI responses for suspicious patterns
- **Rate Limiting**: Maximum 15 API calls per run
- **Error Handling**: Secure error messages without exposing sensitive data
- **Timeout Protection**: 10-minute maximum execution time

### ✅ Change Validation
- **Improvement Verification**: Ensures fixes are actual improvements
- **Size Change Limits**: Rejects changes over 200% size difference
- **Structure Integrity**: Validates JSON, Kotlin package declarations, etc.
- **Content Analysis**: Checks for malicious code patterns

## Manual Usage

You can also run Gemini Auto locally:

```bash
# Set your API key
export GEMINI_API_KEY=your_api_key_here

# Run Gemini Auto
npm run gemini-auto
```

## Troubleshooting

### Common Issues

1. **Missing API Key**: Ensure `GEMINI_API_KEY` is set in repository secrets
2. **API Quota Exceeded**: Check your Gemini API usage at [Google AI Studio](https://makersuite.google.com/app/apikey)
3. **No Changes Made**: Your code might already be optimized!

### Error Messages

- `GEMINI_API_KEY environment variable not set`: Add the secret to GitHub
- `API call failed: 403`: API key is invalid or expired - regenerate it
- `API call failed: 429`: You've exceeded your API quota

## Comparison with Old System

| Feature | Old GitAuto-AI | New Gemini Auto |
|---------|---------------|-----------------|
| **Action** | Comments with suggestions | Direct code fixes |
| **Integration** | Manual copy-paste needed | Automatic commits |
| **Coverage** | Basic analysis | Advanced AI fixing |
| **Efficiency** | Time-consuming | Instant application |
| **Reliability** | Authentication issues | Improved error handling |

## Migration

If you were using the old "GitAuto-AI" system:
- The old workflow is now deprecated and disabled
- Use label `gemini-auto` instead of `gemini-ai`
- No other changes needed - API key setup remains the same

## Privacy & Security

- Code is sent to Google's Gemini API for analysis (standard terms apply)
- API keys are securely stored in GitHub Secrets
- All communication uses HTTPS encryption
- No code is permanently stored by the AI service
- Input sanitization prevents injection attacks
- Response validation blocks malicious content
- Rate limiting prevents quota abuse

## Performance & Efficiency

- **Pre-Check**: Skips AI analysis when no issues detected
- **Conditional Workflow**: Only runs when fixes are actually needed
- **API Quota Conservation**: Limits calls to prevent exhaustion
- **Smart Caching**: Reuses dependency installations where possible

---

**Powered by Google's Gemini AI** 🤖  
*Enhanced with Security & Efficiency* 🛡️