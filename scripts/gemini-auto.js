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

// Configuration
const MAX_FILES_TO_ANALYZE = 20;
const MIN_CONTENT_LENGTH_FOR_FIX = 50;
const MAX_API_CALLS_PER_RUN = 15;
const MAX_FILE_SIZE = 100000; // 100KB limit for security
const GEMINI_API_URL = 'https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent';

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

function findFilesToAnalyze() {
  const files = [];

  // Find JavaScript/TypeScript files
  if (fs.existsSync('src')) {
    const jsFiles = safeExec('find src -name "*.js" -o -name "*.ts" -o -name "*.jsx" -o -name "*.tsx"');
    if (jsFiles) {
      files.push(...jsFiles.trim().split('\n').filter(f => f));
    }
  }

  // Find Kotlin files
  if (fs.existsSync('app/src')) {
    const ktFiles = safeExec('find app/src -name "*.kt"');
    if (ktFiles) {
      files.push(...ktFiles.trim().split('\n').filter(f => f));
    }
  }

  // Add package.json and build files
  const configFiles = ['package.json', 'build.gradle.kts', 'app/build.gradle.kts'].filter(f => fs.existsSync(f));
  files.push(...configFiles);

  return files.slice(0, MAX_FILES_TO_ANALYZE);
}

async function callGeminiAPI(prompt) {
  // Security: Sanitize input before sending to API
  const sanitizedPrompt = sanitizeInput(prompt);
  
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
    const response = await fetch(GEMINI_API_URL, {
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
  
  if (filePath.endsWith('.js') || filePath.endsWith('.ts')) {
    // Basic JavaScript/TypeScript issue detection
    const lines = content.split('\n');
    
    lines.forEach((line, index) => {
      // Detect unused variables (basic pattern)
      if (line.match(/^\s*(var|let|const)\s+\w+.*=.*;\s*$/)) {
        const varName = line.match(/^\s*(var|let|const)\s+(\w+)/)?.[2];
        if (varName && !content.includes(`${varName};`) && !content.includes(`${varName}(`)) {
          issues.push({
            line: index + 1,
            message: `'${varName}' is assigned a value but never used`,
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
          ruleId: 'semi'
        });
      }
      
      // Detect bad spacing
      if (line.match(/if\(/) || line.match(/for\(/)) {
        issues.push({
          line: index + 1,
          message: 'Missing space before opening paren',
          ruleId: 'space-before-function-paren'
        });
      }
    });
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
  let prompt = `You are an expert code fixer. Please analyze the following code and provide ONLY the fixed code with improvements for:

1. Fix all linting/syntax errors
2. Optimize performance where possible
3. Apply security best practices
4. Improve code formatting and style
5. Add missing error handling

File: ${fileName}

`;

  if (lintIssues.length > 0) {
    prompt += `Linting issues to fix:
${lintIssues.map(issue => `- Line ${issue.line}: ${issue.message} (${issue.ruleId})`).join('\n')}

`;
  }

  prompt += `Original code:
\`\`\`
${fileContent}
\`\`\`

Please provide ONLY the fixed code without explanations. The response should be the complete corrected file content that can directly replace the original file.`;

  return prompt;
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
  
  // Skip if no issues found for this file
  if (!lintIssues || lintIssues.messages.length === 0) {
    console.log(`ℹ️ No issues detected for: ${filePath}`);
    return false;
  }

  try {
    const prompt = createFixPrompt(originalContent, filePath, lintIssues.messages || []);
    const fixedContent = await callGeminiAPI(prompt);

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
    console.log(`✅ Fixed: ${filePath}`);
    return true;
    
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
    console.log('🧪 Test mode: Issues detected, would proceed with AI analysis');
    return false;
  }

  console.log('🤖 Issues detected - proceeding with AI analysis...\n');
  
  let totalFixed = 0;
  let apiCallCount = 0;

  for (const file of files) {
    // Security: Limit API calls per run to prevent quota exhaustion
    if (apiCallCount >= MAX_API_CALLS_PER_RUN) {
      console.log(`⚠️ Reached API call limit (${MAX_API_CALLS_PER_RUN}), stopping to conserve quota`);
      break;
    }
    
    try {
      const fixed = await fixFile(file);
      if (fixed) { 
        totalFixed++; 
        apiCallCount++;
      }
    } catch (error) {
      console.log(`❌ Error processing ${file}:`, error.message);
      apiCallCount++; // Count failed attempts too
    }
  }

  console.log(`\n📊 Summary: ${totalFixed}/${files.length} files fixed (${apiCallCount} API calls used)`);

  if (totalFixed > 0) {
    // Run final linter check only if fixes were applied
    console.log('\n🔍 Running final lint check...');
    const finalLintResult = safeExec('npx eslint . --fix --quiet');
    if (finalLintResult) {
      console.log('✅ ESLint auto-fix applied');
    }
  }

  return totalFixed > 0;
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