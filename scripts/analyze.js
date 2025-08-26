// scripts/analyze.js
const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

console.log("--- GitAuto-AI Analysis Report ---");
console.log(`Report generated on: ${new Date().toISOString()}`);
console.log("-------------------------------------\n");

// Helper function to safely execute commands
function safeExec(command, options = {}) {
  try {
    return execSync(command, { encoding: 'utf8', ...options });
  } catch (error) {
    return error.stdout || error.stderr || `Error executing: ${command}`;
  }
}

// Helper function to find Kotlin files
function findKotlinFiles(dir, kotlinFiles = []) {
  try {
    const items = fs.readdirSync(dir);
    for (const item of items) {
      const fullPath = path.join(dir, item);
      if (fs.statSync(fullPath).isDirectory() && !item.startsWith('.') && item !== 'build') {
        findKotlinFiles(fullPath, kotlinFiles);
      } else if (item.endsWith('.kt')) {
        kotlinFiles.push(fullPath);
      }
    }
  } catch (error) {
    // Ignore permission errors
  }
  return kotlinFiles;
}

// Helper function to count files by extension
function countFiles(dir, fileTypes = {}) {
  try {
    const items = fs.readdirSync(dir);
    for (const item of items) {
      const fullPath = path.join(dir, item);
      if (fs.statSync(fullPath).isDirectory()) {
        if (!item.startsWith('.') && item !== 'node_modules' && item !== 'build') {
          countFiles(fullPath, fileTypes);
        }
      } else {
        const ext = path.extname(item);
        fileTypes[ext] = (fileTypes[ext] || 0) + 1;
      }
    }
  } catch (error) {
    // Ignore permission errors
  }
  return fileTypes;
}

// Check if we're in a Node.js project or Android project
const hasPackageJson = fs.existsSync('package.json');
const hasGradleBuild = fs.existsSync('build.gradle.kts') || fs.existsSync('app/build.gradle.kts');

console.log('Project Detection:');
console.log(`- Node.js project: ${hasPackageJson ? 'Yes' : 'No'}`);
console.log(`- Android/Gradle project: ${hasGradleBuild ? 'Yes' : 'No'}`);
console.log();

if (hasPackageJson) {
  console.log('Node.js/JavaScript Analysis:');
  console.log('=============================');
  
  // Run ESLint if available
  try {
    const eslintOutput = safeExec('npx eslint . --format compact');
    console.log('ESLint Analysis:');
    console.log(eslintOutput);
  } catch (error) {
    console.log('ESLint Analysis (with findings):');
    console.log(error.stdout || 'ESLint not configured or no issues found');
  }
  console.log();
}

if (hasGradleBuild) {
  console.log('Android/Kotlin Analysis:');
  console.log('========================');
  
  // Analyze Gradle build files
  console.log('Gradle Build File Analysis:');
  if (fs.existsSync('build.gradle.kts')) {
    const buildContent = fs.readFileSync('build.gradle.kts', 'utf8');
    console.log('- Root build.gradle.kts found');
    if (buildContent.includes('kotlin')) {
      console.log('- Kotlin plugin detected');
    }
  }
  
  if (fs.existsSync('app/build.gradle.kts')) {
    const appBuildContent = fs.readFileSync('app/build.gradle.kts', 'utf8');
    console.log('- App build.gradle.kts found');
    
    // Extract key information without running Gradle
    const targetSdk = appBuildContent.match(/targetSdk = (\d+)/)?.[1];
    const minSdk = appBuildContent.match(/minSdk = (\d+)/)?.[1];
    const versionName = appBuildContent.match(/versionName = "([^"]+)"/)?.[1];
    
    if (targetSdk) console.log(`- Target SDK: ${targetSdk}`);
    if (minSdk) console.log(`- Min SDK: ${minSdk}`);
    if (versionName) console.log(`- Version: ${versionName}`);
  }
  
  // Analyze Kotlin source structure
  console.log('\nKotlin Source Analysis:');
  const kotlinFiles = findKotlinFiles('app/src');
  console.log(`- Found ${kotlinFiles.length} Kotlin source files`);
  
  // Basic static analysis of Kotlin files
  let totalLines = 0;
  let classCount = 0;
  let functionCount = 0;
  
  kotlinFiles.slice(0, 10).forEach(file => { // Analyze first 10 files to avoid timeout
    try {
      const content = fs.readFileSync(file, 'utf8');
      totalLines += content.split('\n').length;
      classCount += (content.match(/class\s+\w+/g) || []).length;
      functionCount += (content.match(/fun\s+\w+/g) || []).length;
    } catch (error) {
      // Skip files that can't be read
    }
  });
  
  console.log(`- Sample analysis (first 10 files):`);
  console.log(`  - Total lines: ${totalLines}`);
  console.log(`  - Classes found: ${classCount}`);
  console.log(`  - Functions found: ${functionCount}`);
  console.log();
}

// General project structure analysis
console.log('Project Structure Analysis:');
console.log('===========================');

// Count files by extension
const fileTypes = countFiles('.');

console.log('File type distribution:');
Object.entries(fileTypes)
  .sort(([,a], [,b]) => b - a)
  .slice(0, 10)
  .forEach(([ext, count]) => {
    console.log(`  ${ext || '(no extension)'}: ${count} files`);
  });

console.log('\n--- Analysis Complete ---');