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
  const payload = {
    contents: [{
      parts: [{
        text: prompt
      }]
    }],
    generationConfig: {
      maxOutputTokens: 4000,
      temperature: 0.1
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
      throw new Error(`API call failed: ${response.status} - ${response.statusText}`);
    }

    const data = await response.json();
    if (!data.candidates || !data.candidates[0]?.content?.parts?.[0]?.text) {
      throw new Error('Invalid API response structure');
    }

    return data.candidates[0].content.parts[0].text;
  } catch (error) {
    console.error('❌ Gemini API Error:', error.message);
    throw error;
  }
}

function analyzeCodeWithLinters() {
  const issues = [];

  // Run ESLint if available
  const eslintResult = safeExec('npx eslint . --format json --quiet', { stdio: 'pipe' });
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
      // ESLint output might not be JSON if there are no issues
    }
  }

  return issues;
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

  // Get lint issues for this file
  const lintIssues = analyzeCodeWithLinters().find(issue =>
    issue.file.includes(filePath) || filePath.includes(path.basename(issue.file))
  );

  try {
    const prompt = createFixPrompt(originalContent, filePath, lintIssues?.messages || []);
    const fixedContent = await callGeminiAPI(prompt);

    // Extract code from response if it's wrapped in code blocks
    let cleanedContent = fixedContent;
    const codeBlockMatch = fixedContent.match(/```[\w]*\n([\s\S]*?)\n```/);
    if (codeBlockMatch) {
      cleanedContent = codeBlockMatch[1];
    }

    // Only apply fix if content actually changed and is valid
    if (cleanedContent.trim() !== originalContent.trim() && cleanedContent.length > MIN_CONTENT_LENGTH_FOR_FIX) {
      // Basic validation - ensure we haven't broken the file structure
      if (filePath.endsWith('.json')) {
        try {
          JSON.parse(cleanedContent);
        } catch (e) {
          console.log(`⚠️ JSON validation failed for ${filePath}, skipping: ${e.message}`);
          return false;
        }
      }

      fs.writeFileSync(filePath, cleanedContent);
      console.log(`✅ Fixed: ${filePath}`);
      return true;
    } else {
      console.log(`ℹ️ No changes needed: ${filePath}`);
      return false;
    }
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

  if (testMode) {
    console.log('🧪 Test mode: Files discovered successfully');
    return false;
  }

  let totalFixed = 0;

  for (const file of files) {
    try {
      const fixed = await fixFile(file);
      if (fixed) { totalFixed++; }
    } catch (error) {
      console.log(`❌ Error processing ${file}:`, error.message);
    }
  }

  console.log(`\n📊 Summary: ${totalFixed}/${files.length} files fixed`);

  // Run final linter check
  console.log('\n🔍 Running final lint check...');
  const finalLintResult = safeExec('npx eslint . --fix --quiet');
  if (finalLintResult) {
    console.log('✅ ESLint auto-fix applied');
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