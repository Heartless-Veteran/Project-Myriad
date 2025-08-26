# Gemini Auto - AI Code Fixer

🤖 **Gemini Auto** is an AI-powered code fixing system that automatically identifies and resolves code issues in your pull requests. Unlike traditional AI review tools that only provide suggestions, Gemini Auto actually fixes the code and commits the changes directly to your PR.

**🔒 Enhanced Security & Efficiency**: Now runs only when issues are detected, with comprehensive security validations and rate limiting.

## Features

### 🔧 Auto-Fix Capabilities
- **Lint Issues**: Automatically fixes ESLint errors and warnings
- **Performance**: Optimizes code for better performance
- **Security**: Applies security best practices and fixes vulnerabilities  
- **Formatting**: Standardizes code style and formatting
- **Error Handling**: Adds missing error handling patterns

### 🛡️ Security Enhancements
- **Conditional Execution**: Only runs when actual issues are detected
- **Input Sanitization**: Validates all content sent to AI API
- **Response Validation**: Checks AI responses for malicious content
- **Rate Limiting**: Limits API calls per run to prevent quota exhaustion
- **Size Validation**: Enforces file size limits for security
- **Change Validation**: Ensures fixes are actual improvements

### 🚀 Seamless Integration
- **Direct Commits**: Fixes are committed directly to your PR
- **Smart Analysis**: Uses Google's Gemini AI for intelligent code understanding
- **Multi-Language**: Supports JavaScript, TypeScript, Kotlin, and configuration files
- **GitHub Integration**: Works seamlessly with GitHub Actions
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
- **JavaScript/TypeScript**: `.js`, `.ts`, `.jsx`, `.tsx` files in `src/`
- **Kotlin**: `.kt` files in `app/src/`
- **Config Files**: `package.json`, `build.gradle.kts`

### Limitations
- Maximum 20 files per run (configurable)
- Maximum 15 API calls per run for quota conservation
- Files must be under 100KB for security
- Only applies safe, conservative fixes
- Validates all changes before applying

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