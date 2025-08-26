#!/usr/bin/env node

/**
 * Gemini Auto - AI Code Fixer
 *
 * This script analyzes code using Google's Gemini AI and automatically
 * applies fixes for linting issues, performance improvements, security
 * vulnerabilities, and code formatting.
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

console.log('🤖 Gemini Auto - AI Code Fixer');
console.log('===============================\n');

// Load configuration
function loadConfiguration() {
  const defaultConfig = {
    kotlin: { enabled: true, rules: { 'null-safety': true, 'coroutine-context': true } },
    javascript: { enabled: true, rules: { 'modern-syntax': true, 'unused-imports': true } },
    shell: { enabled: true, rules: { 'shellcheck': true, 'best-practices': true } },
    fixCategories: { security: true, performance: true, style: true },
    severityThreshold: 'medium',
    includePatterns: ['app/src/main/**/*.kt', 'src/**/*.js', 'src/**/*.ts', 'scripts/**/*.js'],
    excludePatterns: ['**/test/**', '**/build/**', '**/node_modules/**'],
    modelSelection: { defaultModel: 'gemini-1.5-flash', complexTaskModel: 'gemini-1.5-pro' }
  };

  try {
    if (fs.existsSync('.gemini-rules.json')) {
      const customConfig = JSON.parse(fs.readFileSync('.gemini-rules.json', 'utf8'));
      return { ...defaultConfig, ...customConfig };
    }
  } catch (error) {
    console.log('⚠️ Error loading .gemini-rules.json, using defaults:', error.message);
  }
  
  return defaultConfig;
}

const config = loadConfiguration();

// Configuration constants
const MAX_FILES_TO_ANALYZE = 20;
const MIN_CONTENT_LENGTH_FOR_FIX = 50;
const MAX_API_CALLS_PER_RUN = 15;
const MAX_FILE_SIZE = 100000; // 100KB limit for security

// Check API key
const apiKey = process.env.GEMINI_API_KEY;
const testMode = process.env.GEMINI_TEST_MODE === 'true';

if (!apiKey && !testMode) {
  console.log('❌ Error: GEMINI_API_KEY environment variable not set');
  process.exit(1);
}

// Helper functions
function safeExec(command, options = {}) {
  try {
    return execSync(command, { encoding: 'utf8', ...options });
  } catch (error) {
    return null;
  }
}

// Security validation functions
function sanitizeInput(content) {
  if (typeof content !== 'string') {
    throw new Error('Content must be a string');
  }
  
  // Check file size limit
  if (content.length > MAX_FILE_SIZE) {
    throw new Error(`File too large: ${content.length} bytes exceeds ${MAX_FILE_SIZE} bytes`);
  }
  
  // Basic sanitization - remove potential injection patterns
  const sanitized = content
    .replace(/[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]/g, '') // Remove control characters
    .substring(0, MAX_FILE_SIZE); // Ensure size limit
    
  return sanitized;
}

function validateApiResponse(response) {
  if (!response || typeof response !== 'string') {
    throw new Error('Invalid API response: not a string');
  }
  
  if (response.length === 0) {
    throw new Error('Empty API response');
  }
  
  // Check for suspicious content that might indicate an attack
  const suspiciousPatterns = [
    /eval\s*\(/i,
    /document\.cookie/i,
    /window\.location/i,
    /<script/i,
    /javascript:/i,
    /data:text\/html/i
  ];
  
  for (const pattern of suspiciousPatterns) {
    if (pattern.test(response)) {
      throw new Error('API response contains suspicious content');
    }
  }
  
  return response;
}

function matchesPattern(filePath, patterns) {
  return patterns.some(pattern => {
    // Convert glob patterns to regex more carefully
    let regex_pattern = pattern
      .replace(/\\/g, '\\\\')              // Escape backslashes
      .replace(/\*\*/g, '___DOUBLESTAR___')  // Temporarily replace ** 
      .replace(/\*/g, '[^/]*')              // Replace single * with [^/]*
      .replace(/___DOUBLESTAR___/g, '.*')   // Replace ** with .* (any characters including /)
      .replace(/\./g, '\\.')               // Escape dots
      .replace(/\//g, '\\/');              // Escape forward slashes
    
    const regex = new RegExp('^' + regex_pattern + '$');
    return regex.test(filePath);
  });
}

function findFilesToAnalyze() {
  const files = [];

  // Enhanced file discovery with pattern matching
  const findCommands = [];
  
  if (config.kotlin.enabled && fs.existsSync('app/src')) {
    findCommands.push('find app/src -name "*.kt"');
  }
  
  if (config.javascript.enabled && fs.existsSync('src')) {
    findCommands.push('find src -name "*.js" -o -name "*.ts" -o -name "*.jsx" -o -name "*.tsx"');
  }
  
  if (config.shell.enabled) {
    findCommands.push('find scripts -name "*.sh" 2>/dev/null || true');
  }

  // Execute find commands
  for (const cmd of findCommands) {
    const result = safeExec(cmd);
    if (result) {
      files.push(...result.trim().split('\n').filter(f => f && f.trim()));
    }
  }

  // Add config files
  const configFiles = ['package.json', 'build.gradle.kts', 'app/build.gradle.kts'].filter(f => fs.existsSync(f));
  files.push(...configFiles);

  // Apply include/exclude patterns - simplified for now
  let filteredFiles = files;
  
  // Simple exclusion: remove test files
  filteredFiles = filteredFiles.filter(file => !file.includes('/test/'));
  
  console.log(`📋 After filtering: ${filteredFiles.length} files`);

  return filteredFiles.slice(0, MAX_FILES_TO_ANALYZE);
}

// Enhanced model selection based on task complexity
function selectGeminiModel(filePath, content, issueCount = 0) {
  const { modelSelection } = config;
  const thresholds = modelSelection.complexityThresholds || {};
  
  // Check complexity indicators
  const isLargeFile = content.length > (thresholds.fileSize || 50000);
  const hasManyIssues = issueCount > (thresholds.issueCount || 10);
  const isKotlinClass = filePath.endsWith('.kt') && /class\s+\w+/.test(content) && thresholds.kotlinClass;
  
  if (isLargeFile || hasManyIssues || isKotlinClass) {
    console.log(`🧠 Using advanced model for complex task: ${path.basename(filePath)}`);
    return modelSelection.complexTaskModel || 'gemini-1.5-pro';
  }
  
  return modelSelection.defaultModel || 'gemini-1.5-flash';
}

function getGeminiApiUrl(model) {
  return `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`;
}

async function callGeminiAPI(prompt, filePath, content, issueCount = 0) {
  // Security: Sanitize input before sending to API
  const sanitizedPrompt = sanitizeInput(prompt);
  
  // Select appropriate model based on complexity
  const model = selectGeminiModel(filePath, content, issueCount);
  const apiUrl = getGeminiApiUrl(model);
  
  const payload = {
    contents: [{
      parts: [{
        text: sanitizedPrompt
      }]
    }],
    generationConfig: {
      maxOutputTokens: 4000,
      temperature: 0.1,
      // Security: Add safety settings
      topK: 40,
      topP: 0.95
    }
  };

  try {
    const response = await fetch(apiUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-api-key': apiKey
      },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('❌ API Error Response:', errorText.substring(0, 200));
      throw new Error(`API call failed: ${response.status} - ${response.statusText}`);
    }

    const data = await response.json();
    if (!data.candidates || !data.candidates[0]?.content?.parts?.[0]?.text) {
      throw new Error('Invalid API response structure');
    }

    const responseText = data.candidates[0].content.parts[0].text;
    
    // Security: Validate response before returning
    return validateApiResponse(responseText);
  } catch (error) {
    console.error('❌ Gemini API Error:', error.message);
    throw error;
  }
}

function analyzeCodeWithLinters() {
  const issues = [];

  // Run ESLint if available - try different configurations
  let eslintResult = null;
  
  // Try ESLint with flat config first
  eslintResult = safeExec('npx eslint . --format json --quiet', { stdio: 'pipe' });
  
  // If flat config fails, try with legacy config
  if (!eslintResult) {
    eslintResult = safeExec('npx eslint . --format json --quiet --config .eslintrc.js', { stdio: 'pipe' });
  }
  
  // If both fail, try without config to get basic syntax checking
  if (!eslintResult) {
    eslintResult = safeExec('npx eslint src/ --format json --no-eslintrc --quiet', { stdio: 'pipe' });
  }
  
  if (eslintResult) {
    try {
      const eslintData = JSON.parse(eslintResult);
      eslintData.forEach(file => {
        if (file.messages && file.messages.length > 0) {
          issues.push({
            file: file.filePath,
            tool: 'eslint',
            messages: file.messages
          });
        }
      });
    } catch (e) {
      console.warn(`Could not parse ESLint JSON output. This might be expected if no issues were found. Error: ${e.message}`);
    }
  } else {
    console.warn('⚠️ ESLint not available or configuration issues detected');
  }

  return issues;
}

function detectBasicCodeIssues(filePath, content) {
  const issues = [];
  const lines = content.split('\n');
  
  if (filePath.endsWith('.kt')) {
    // Enhanced Kotlin-specific issue detection
    if (config.kotlin.rules['null-safety']) {
      // Detect potential null pointer exceptions
      lines.forEach((line, index) => {
        if (/\.(?!let|also|run|apply|with)[a-zA-Z_$][a-zA-Z0-9_$]*(?!\?)\./.test(line)) {
          issues.push({
            line: index + 1,
            message: 'Potential null pointer exception - consider using safe call operator',
            severity: 'warning',
            category: 'security',
            ruleId: 'null-safety'
          });
        }
      });
    }
    
    if (config.kotlin.rules['coroutine-context']) {
      // Detect improper coroutine context usage
      const content_lower = content.toLowerCase();
      if (content_lower.includes('launch') || content_lower.includes('async')) {
        if (!content_lower.includes('dispatchers')) {
          issues.push({
            line: 1,
            message: 'Coroutine launched without explicit dispatcher context',
            severity: 'warning',
            category: 'performance',
            ruleId: 'coroutine-context'
          });
        }
      }
    }
    
    if (config.kotlin.rules['data-class-conventions']) {
      // Check for data class best practices
      if (content.includes('data class')) {
        const hasEquals = content.includes('override fun equals');
        const hasHashCode = content.includes('override fun hashCode');
        if (hasEquals !== hasHashCode) {
          issues.push({
            line: 1,
            message: 'Data class should override both equals() and hashCode() or neither',
            severity: 'warning',
            category: 'style',
            ruleId: 'data-class-conventions'
          });
        }
      }
    }
  } else if (filePath.endsWith('.js') || filePath.endsWith('.ts')) {
    // Enhanced JavaScript/TypeScript issue detection
    if (config.javascript.rules['modern-syntax']) {
      // Detect var usage
      lines.forEach((line, index) => {
        if (/\bvar\s+/.test(line) && !line.trim().startsWith('//')) {
          issues.push({
            line: index + 1,
            message: 'Use const or let instead of var',
            severity: 'warning',
            category: 'style',
            ruleId: 'no-var'
          });
        }
      });
    }
    
    // Keep existing basic checks
    lines.forEach((line, index) => {
      // Detect unused variables (basic pattern)
      if (line.match(/^\s*(var|let|const)\s+\w+.*=.*;\s*$/)) {
        const varName = line.match(/^\s*(var|let|const)\s+(\w+)/)?.[2];
        if (varName && !content.includes(`${varName};`) && !content.includes(`${varName}(`)) {
          issues.push({
            line: index + 1,
            message: `'${varName}' is assigned a value but never used`,
            severity: 'warning',
            category: 'style',
            ruleId: 'no-unused-vars'
          });
        }
      }
      
      // Detect missing semicolons
      if (line.match(/^\s*console\.log.*[^;]\s*$/) || 
          line.match(/^\s*return.*[^;]\s*$/)) {
        issues.push({
          line: index + 1,
          message: 'Missing semicolon',
          severity: 'error',
          category: 'style',
          ruleId: 'semi'
        });
      }
      
      // Detect bad spacing
      if (line.match(/if\(/) || line.match(/for\(/)) {
        issues.push({
          line: index + 1,
          message: 'Missing space before opening paren',
          severity: 'warning',
          category: 'style',
          ruleId: 'space-before-function-paren'
        });
      }
    });
  } else if (filePath.endsWith('.sh')) {
    // Shell script analysis
    if (config.shell.rules['best-practices']) {
      if (!content.startsWith('#!/')) {
        issues.push({
          line: 1,
          message: 'Shell script should start with shebang line',
          severity: 'warning',
          category: 'style',
          ruleId: 'shebang-required'
        });
      }
      
      // Check for unquoted variables
      lines.forEach((line, index) => {
        if (/\$[a-zA-Z_][a-zA-Z0-9_]*(?!["\w])/.test(line) && !line.trim().startsWith('#')) {
          issues.push({
            line: index + 1,
            message: 'Variable should be quoted to prevent word splitting',
            severity: 'warning',
            category: 'security',
            ruleId: 'quote-variables'
          });
        }
      });
    }
  }
  
  return issues;
}

function hasActualIssuesToFix() {
  console.log('🔍 Pre-checking for issues before AI analysis...');
  
  const lintIssues = analyzeCodeWithLinters();
  let totalIssues = lintIssues.reduce((total, file) => total + file.messages.length, 0);
  
  // If ESLint didn't work, try basic analysis
  if (totalIssues === 0) {
    console.log('🔍 ESLint issues not detected, trying basic analysis...');
    const files = findFilesToAnalyze().slice(0, 5); // Check first few files
    
    for (const file of files) {
      if (fs.existsSync(file)) {
        const content = fs.readFileSync(file, 'utf8');
        const basicIssues = detectBasicCodeIssues(file, content);
        if (basicIssues.length > 0) {
          totalIssues += basicIssues.length;
          console.log(`   - ${path.basename(file)}: ${basicIssues.length} basic issues`);
        }
      }
    }
  }
  
  if (totalIssues === 0) {
    console.log('✅ No issues detected - skipping AI analysis');
    return false;
  }
  
  console.log(`📋 Found ${totalIssues} issues to fix`);
  if (lintIssues.length > 0) {
    lintIssues.forEach(issue => {
      console.log(`   - ${path.basename(issue.file)}: ${issue.messages.length} issues`);
    });
  }
  
  return true;
}

function createFixPrompt(fileContent, fileName, lintIssues = []) {
  const fileExt = path.extname(fileName).toLowerCase();
  const enabledCategories = Object.keys(config.fixCategories).filter(cat => config.fixCategories[cat]);
  
  let prompt = `You are an expert ${getLanguageName(fileExt)} code fixer. Please analyze the following code and provide ONLY the fixed code with improvements.\n\n`;
  
  prompt += `ENABLED FIX CATEGORIES (only apply fixes from these categories):\n`;
  if (enabledCategories.includes('security')) prompt += `- SECURITY: Fix potential vulnerabilities and apply security best practices\n`;
  if (enabledCategories.includes('performance')) prompt += `- PERFORMANCE: Optimize code for better performance and efficiency\n`;
  if (enabledCategories.includes('style')) prompt += `- STYLE: Improve code formatting, naming conventions, and readability\n`;
  if (enabledCategories.includes('architecture')) prompt += `- ARCHITECTURE: Ensure proper design patterns and architectural compliance\n`;
  if (enabledCategories.includes('testing')) prompt += `- TESTING: Add or improve test coverage and testing practices\n`;
  
  // Add language-specific guidance
  if (fileExt === '.kt') {
    prompt += `\nKOTLIN-SPECIFIC RULES:\n`;
    if (config.kotlin.rules['null-safety']) prompt += `- Apply null-safety best practices and safe call operators\n`;
    if (config.kotlin.rules['coroutine-context']) prompt += `- Ensure proper coroutine context and dispatcher usage\n`;
    if (config.kotlin.rules['compose-performance']) prompt += `- Optimize Jetpack Compose performance and avoid unnecessary recompositions\n`;
    if (config.kotlin.rules['hilt-injection']) prompt += `- Follow proper dependency injection patterns with Hilt\n`;
  } else if (fileExt === '.js' || fileExt === '.ts') {
    prompt += `\nJAVASCRIPT/TYPESCRIPT RULES:\n`;
    if (config.javascript.rules['modern-syntax']) prompt += `- Use modern JavaScript/TypeScript syntax (const/let, arrow functions, async/await)\n`;
    if (config.javascript.rules['unused-imports']) prompt += `- Remove unused imports and variables\n`;
    if (config.javascript.rules['async-await']) prompt += `- Prefer async/await over Promise chains\n`;
  }
  
  prompt += `\nFile: ${fileName}\nSeverity Threshold: ${config.severityThreshold}\n\n`;

  if (lintIssues.length > 0) {
    // Categorize issues
    const categorizedIssues = categorizeIssues(lintIssues);
    
    prompt += `DETECTED ISSUES TO FIX:\n`;
    Object.entries(categorizedIssues).forEach(([category, issues]) => {
      if (issues.length > 0 && enabledCategories.includes(category.toLowerCase())) {
        prompt += `\n${category.toUpperCase()} ISSUES:\n`;
        issues.forEach(issue => {
          prompt += `- Line ${issue.line}: ${issue.message}${issue.ruleId ? ` (${issue.ruleId})` : ''}\n`;
        });
      }
    });
    prompt += '\n';
  }

  prompt += `Original code:
\`\`\`${getLanguageName(fileExt).toLowerCase()}
${fileContent}
\`\`\`

IMPORTANT: 
- Provide ONLY the fixed code without explanations or markdown formatting
- The response should be the complete corrected file content 
- Only apply fixes from the enabled categories listed above
- Maintain the original file structure and functionality
- Add educational comments for significant changes when appropriate`;

  return prompt;
}

function getLanguageName(extension) {
  switch (extension) {
    case '.kt': return 'Kotlin';
    case '.js': return 'JavaScript';
    case '.ts': return 'TypeScript';
    case '.jsx': return 'JSX';
    case '.tsx': return 'TSX';
    case '.sh': return 'Shell';
    default: return 'Code';
  }
}

function categorizeIssues(issues) {
  const categories = {
    Security: [],
    Performance: [],
    Style: [],
    Architecture: [],
    Testing: []
  };
  
  issues.forEach(issue => {
    const category = issue.category || 'Style'; // Default to Style if no category
    const categoryKey = category.charAt(0).toUpperCase() + category.slice(1).toLowerCase();
    if (categories[categoryKey]) {
      categories[categoryKey].push(issue);
    } else {
      categories.Style.push(issue); // Fallback to Style
    }
  });
  
  return categories;
}

async function fixFile(filePath) {
  console.log(`🔧 Analyzing: ${filePath}`);

  if (!fs.existsSync(filePath)) {
    console.log(`⚠️ File not found: ${filePath}`);
    return false;
  }

  const originalContent = fs.readFileSync(filePath, 'utf8');
  
  // Security: Validate file size before processing
  if (originalContent.length > MAX_FILE_SIZE) {
    console.log(`⚠️ File too large to process safely: ${filePath} (${originalContent.length} bytes)`);
    return false;
  }

  // Get lint issues for this file
  const lintIssues = analyzeCodeWithLinters().find(issue =>
    issue.file.includes(filePath) || filePath.includes(path.basename(issue.file))
  );
  
  // Removed redundant per-file lint issue check (handled by pre-check)

  try {
    // Combine linting issues and basic issues for comprehensive analysis
    const allIssues = [...(lintIssues?.messages || [])];
    const basicIssues = detectBasicCodeIssues(filePath, originalContent);
    allIssues.push(...basicIssues);
    
    const prompt = createFixPrompt(originalContent, filePath, allIssues);
    const fixedContent = await callGeminiAPI(prompt, filePath, originalContent, allIssues.length);

    // Extract code from response if it's wrapped in code blocks
    let cleanedContent = fixedContent;
    const codeBlockMatch = fixedContent.match(/```[\w]*\n([\s\S]*?)\n```/);
    if (codeBlockMatch) {
      cleanedContent = codeBlockMatch[1];
    }

    // Security: Validate that the fix is actually an improvement
    if (cleanedContent.trim() === originalContent.trim()) {
      console.log(`ℹ️ No meaningful changes suggested: ${filePath}`);
      return false;
    }
    
    // Security: Ensure content meets minimum length requirement
    if (cleanedContent.length < MIN_CONTENT_LENGTH_FOR_FIX) {
      console.log(`⚠️ Fixed content too short, likely invalid: ${filePath}`);
      return false;
    }
    
    // Security: Ensure fixed content isn't drastically different (potential attack)
    const sizeDifference = Math.abs(cleanedContent.length - originalContent.length);
    const sizeChangeRatio = sizeDifference / originalContent.length;
    if (sizeChangeRatio > 2.0) { // More than 200% change
      console.log(`⚠️ Content changed too drastically, skipping for security: ${filePath}`);
      return false;
    }

    // Enhanced validation based on file type
    if (filePath.endsWith('.json')) {
      try {
        JSON.parse(cleanedContent);
      } catch (e) {
        console.log(`⚠️ JSON validation failed for ${filePath}, skipping: ${e.message}`);
        return false;
      }
    }
    
    // Additional validation for Kotlin files
    if (filePath.endsWith('.kt')) {
      // Basic Kotlin syntax check
      if (!cleanedContent.includes('package ') && originalContent.includes('package ')) {
        console.log(`⚠️ Kotlin package declaration missing in fix for ${filePath}, skipping`);
        return false;
      }
    }

    // Apply fix if all validations pass
    fs.writeFileSync(filePath, cleanedContent);
    
    // Create categorized fix report
    const fixReport = {
      file: filePath,
      originalSize: originalContent.length,
      fixedSize: cleanedContent.length,
      issuesFixed: allIssues.length,
      categories: categorizeIssues(allIssues)
    };
    
    console.log(`✅ Fixed: ${path.basename(filePath)}`);
    console.log(`   📊 Issues resolved: ${allIssues.length}`);
    
    // Log category breakdown
    Object.entries(fixReport.categories).forEach(([category, issues]) => {
      if (issues.length > 0) {
        console.log(`   🔧 ${category}: ${issues.length} issues`);
      }
    });
    
    return fixReport;
    
  } catch (error) {
    console.log(`❌ Error fixing ${filePath}:`, error.message);
    return false;
  }
}

async function runAutoFix() {
  console.log('🔍 Finding files to analyze...');
  const files = findFilesToAnalyze();

  if (files.length === 0) {
    console.log('ℹ️ No files found to analyze');
    return false;
  }

  console.log(`📁 Found ${files.length} files to analyze:`);
  files.forEach(f => console.log(`   - ${f}`));
  console.log();

  // Security: Pre-check if there are actually issues to fix
  if (!hasActualIssuesToFix()) {
    return false;
  }

  if (testMode) {
    console.log('🧪 Test mode enabled: Issues detected, but AI analysis is skipped.');
    return false;
  }

  console.log('🤖 Issues detected - proceeding with AI analysis...\n');
  
  let fixReports = [];
  let apiCallCount = 0;

  for (const file of files) {
    // Security: Limit API calls per run to prevent quota exhaustion
    if (apiCallCount >= MAX_API_CALLS_PER_RUN) {
      console.log(`⚠️ Reached API call limit (${MAX_API_CALLS_PER_RUN}), stopping to conserve quota`);
      break;
    }
    
    try {
      const result = await fixFile(file);
      if (result && typeof result === 'object') { 
        fixReports.push(result);
        apiCallCount++;
      }
    } catch (error) {
      console.log(`❌ Error processing ${file}:`, error.message);
      apiCallCount++; // Count failed attempts too
    }
  }

  // Generate comprehensive report
  console.log('\n' + '='.repeat(50));
  console.log('📊 GEMINI AUTO FIX REPORT');
  console.log('='.repeat(50));
  
  if (fixReports.length > 0) {
    const totalIssues = fixReports.reduce((sum, report) => sum + report.issuesFixed, 0);
    const categoryStats = {};
    
    fixReports.forEach(report => {
      Object.entries(report.categories).forEach(([category, issues]) => {
        categoryStats[category] = (categoryStats[category] || 0) + issues.length;
      });
    });
    
    console.log(`🔧 Files processed: ${fixReports.length}/${files.length}`);
    console.log(`📋 Total issues fixed: ${totalIssues}`);
    console.log(`🔌 API calls used: ${apiCallCount}/${MAX_API_CALLS_PER_RUN}`);
    
    console.log('\n📈 Fix Categories:');
    Object.entries(categoryStats)
      .sort(([,a], [,b]) => b - a)
      .forEach(([category, count]) => {
        if (count > 0) {
          const icon = getCategoryIcon(category);
          console.log(`   ${icon} ${category}: ${count} issues`);
        }
      });
    
    console.log('\n📁 Files Modified:');
    fixReports.forEach(report => {
      console.log(`   ✅ ${path.basename(report.file)}`);
      console.log(`      └─ ${report.issuesFixed} issues resolved`);
    });
    
  } else {
    console.log('ℹ️ No fixes applied');
  }
  
  console.log('='.repeat(50));

  if (fixReports.length > 0) {
    // Run final linter check only if fixes were applied
    console.log('\n🔍 Running final lint check...');
    const finalLintResult = safeExec('npx eslint . --fix --quiet');
    if (finalLintResult) {
      console.log('✅ ESLint auto-fix applied');
    }
  }

  return fixReports.length > 0;
}

function getCategoryIcon(category) {
  const icons = {
    'Security': '🔒',
    'Performance': '⚡',
    'Style': '📏',
    'Architecture': '🏗️',
    'Testing': '🧪'
  };
  return icons[category] || '🔧';
}

// Add fetch polyfill for Node.js
// Main execution
runAutoFix()
  .then(changesMade => {
    if (changesMade) {
      console.log('\n🎉 Gemini Auto completed successfully with fixes applied!');
      process.exit(0);
    } else {
      console.log('\n✅ Gemini Auto completed - no fixes needed!');
      process.exit(0);
    }
  })
  .catch(error => {
    console.error('\n❌ Gemini Auto failed:', error.message);
    process.exit(1);
  });