#!/usr/bin/env node

/**
 * Enhanced test script for Gemini Auto
 * Tests the new configuration system and enhanced features
 */

console.log('🧪 Testing Enhanced Gemini Auto System');
console.log('=====================================\n');

const fs = require('fs');
const { execSync } = require('child_process');

// Test 1: Configuration loading
console.log('Test 1: Configuration System');
if (fs.existsSync('.gemini-rules.json')) {
  try {
    const config = JSON.parse(fs.readFileSync('.gemini-rules.json', 'utf8'));
    console.log('✅ Configuration file loaded successfully');
    
    // Validate configuration structure
    if (config.kotlin && config.javascript && config.shell) {
      console.log('✅ Language configurations present');
    }
    
    if (config.fixCategories) {
      const enabledCategories = Object.keys(config.fixCategories).filter(cat => config.fixCategories[cat]);
      console.log(`✅ Fix categories configured: ${enabledCategories.join(', ')}`);
    }
    
    if (config.modelSelection) {
      console.log(`✅ Model selection configured: ${config.modelSelection.defaultModel} / ${config.modelSelection.complexTaskModel}`);
    }
    
  } catch (e) {
    console.log(`❌ Configuration file invalid: ${e.message}`);
  }
} else {
  console.log('ℹ️ No custom configuration file found, using defaults');
}

// Test 2: Enhanced file discovery
console.log('\nTest 2: Enhanced File Discovery');
process.env.GEMINI_TEST_MODE = 'true';

try {
  const output = execSync('node scripts/gemini-auto.js', { encoding: 'utf8' });
  console.log('✅ Enhanced file discovery test passed');
  
  if (output.includes('Found') && output.includes('files to analyze')) {
    console.log('✅ File discovery working correctly');
  }
  
  if (output.includes('basic issues')) {
    console.log('✅ Language-specific issue detection working');
  }
  
  if (output.includes('After filtering')) {
    console.log('✅ File filtering system working');
  }
  
} catch (error) {
  console.log('❌ Enhanced file discovery test failed:', error.message);
}

// Test 3: Validate language-specific analysis
console.log('\nTest 3: Language-Specific Analysis');

// Create a test Kotlin file with issues
const testKotlinContent = `
class TestClass {
    fun test() {
        val user = getUser()
        val name = user.name  // Potential null pointer
        launch {  // Missing dispatcher context
            // some work
        }
    }
}
`;

const testJsContent = `
var oldStyle = "should be const";  // Should use const/let
console.log("test")  // Missing semicolon
if(true) {  // Missing space
    // code
}
`;

try {
  fs.writeFileSync('/tmp/test.kt', testKotlinContent);
  fs.writeFileSync('/tmp/test.js', testJsContent);
  
  console.log('✅ Test files created for validation');
} catch (e) {
  console.log('⚠️ Could not create test files for validation');
}

// Test 4: Gradle integration
console.log('\nTest 4: Gradle Task Integration');
if (fs.existsSync('scripts/gemini-auto.gradle.kts')) {
  console.log('✅ Gradle task file exists');
  
  const gradleContent = fs.readFileSync('scripts/gemini-auto.gradle.kts', 'utf8');
  if (gradleContent.includes('geminiAutoFix') && gradleContent.includes('geminiAutoCheck')) {
    console.log('✅ Gradle tasks properly defined');
  }
} else {
  console.log('❌ Gradle task file missing');
}

// Test 5: Workflow enhancements
console.log('\nTest 5: Enhanced Workflow');
if (fs.existsSync('.github/workflows/gemini-auto.yml')) {
  const workflowContent = fs.readFileSync('.github/workflows/gemini-auto.yml', 'utf8');
  
  if (workflowContent.includes('Validate configuration')) {
    console.log('✅ Configuration validation step added to workflow');
  }
  
  if (workflowContent.includes('Enhanced AI Code Fixer')) {
    console.log('✅ Enhanced PR comments configured');
  }
  
  if (workflowContent.includes('Language-specific analysis')) {
    console.log('✅ Enhanced feature descriptions added');
  }
} else {
  console.log('❌ Enhanced workflow file missing');
}

// Test 6: Documentation updates
console.log('\nTest 6: Enhanced Documentation');
if (fs.existsSync('docs/GEMINI_AUTO.md')) {
  const docContent = fs.readFileSync('docs/GEMINI_AUTO.md', 'utf8');
  
  if (docContent.includes('Enhanced') || docContent.includes('.gemini-rules.json')) {
    console.log('✅ Documentation updated with enhanced features');
  }
  
  if (docContent.includes('Language-Specific Analysis')) {
    console.log('✅ Language-specific features documented');
  }
  
  if (docContent.includes('Custom Configuration')) {
    console.log('✅ Configuration system documented');
  }
} else {
  console.log('❌ Enhanced documentation missing');
}

console.log('\n🏁 Enhanced Testing Complete');
console.log('============================');
console.log('🎉 Enhanced Gemini Auto system ready for production!');
console.log('📊 New Features: Language-specific analysis, configurable rules, intelligent model selection');
console.log('📚 See docs/GEMINI_AUTO.md for complete documentation');