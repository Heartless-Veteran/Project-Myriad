#!/usr/bin/env node

/**
 * Configuration validation and setup script
 * Validates and sets up the automated documentation and code quality workflow
 */

const fs = require('fs');
const path = require('path');

console.log('⚙️  Configuration Validator for Automated Documentation & Code Quality');
console.log('===================================================================\n');

class ConfigValidator {
    constructor() {
        this.issues = [];
        this.fixes = [];
    }

    validate() {
        console.log('1. Validating Gradle configuration...');
        this.validateGradleConfig();
        
        console.log('2. Validating detekt configuration...');
        this.validateDetektConfig();
        
        console.log('3. Validating GitHub Actions workflow...');
        this.validateWorkflow();
        
        console.log('4. Validating documentation structure...');
        this.validateDocs();
        
        this.printResults();
        
        if (this.issues.length > 0) {
            console.log('\n🔧 Auto-fixing issues...');
            this.autoFix();
        }
    }

    validateGradleConfig() {
        const buildFile = 'app/build.gradle.kts';
        if (!fs.existsSync(buildFile)) {
            this.issues.push('Missing app/build.gradle.kts file');
            return;
        }

        const content = fs.readFileSync(buildFile, 'utf8');
        
        if (!content.includes('ktlint')) {
            this.issues.push('ktlint plugin not configured in build.gradle.kts');
        } else {
            console.log('   ✅ ktlint plugin configured');
        }

        if (!content.includes('detekt')) {
            this.issues.push('detekt plugin not configured in build.gradle.kts');
        } else {
            console.log('   ✅ detekt plugin configured');
        }

        if (!content.includes('dokka')) {
            this.issues.push('dokka plugin not configured in build.gradle.kts');
        } else {
            console.log('   ✅ dokka plugin configured');
        }

        if (!content.includes('jacoco')) {
            this.issues.push('jacoco plugin not configured in build.gradle.kts');
        } else {
            console.log('   ✅ jacoco plugin configured');
        }
    }

    validateDetektConfig() {
        const detektConfig = 'config/detekt/detekt.yml';
        if (!fs.existsSync(detektConfig)) {
            this.issues.push('Missing detekt configuration file');
            this.fixes.push(() => this.createMinimalDetektConfig());
        } else {
            console.log('   ✅ detekt configuration exists');
        }
    }

    validateWorkflow() {
        const workflowFile = '.github/workflows/automated-documentation-quality.yml';
        if (!fs.existsSync(workflowFile)) {
            this.issues.push('Missing GitHub Actions workflow file');
        } else {
            console.log('   ✅ GitHub Actions workflow configured');
        }
    }

    validateDocs() {
        const docsDir = 'docs';
        if (!fs.existsSync(docsDir)) {
            fs.mkdirSync(docsDir, { recursive: true });
            this.fixes.push(() => console.log('   🔧 Created docs directory'));
        }

        const packagesFile = 'docs/packages.md';
        if (!fs.existsSync(packagesFile)) {
            this.issues.push('Missing Dokka packages.md file');
        } else {
            console.log('   ✅ Dokka packages.md file exists');
        }
    }

    createMinimalDetektConfig() {
        const minimalConfig = `
build:
  maxIssues: 10
  weights:
    complexity: 2
    style: 1

config:
  validation: true
  warningsAsErrors: false

console-reports:
  active: true

complexity:
  active: true
  CyclomaticComplexMethod:
    active: true
    threshold: 15
  LongMethod:
    active: true
    threshold: 60
  
style:
  active: true
  MaxLineLength:
    active: true
    maxLineLength: 120
    
naming:
  active: true
  ClassNaming:
    active: true
  FunctionNaming:
    active: true
    
potential-bugs:
  active: true
  UnsafeCallOnNullableType:
    active: true
`;
        
        fs.mkdirSync(path.dirname('config/detekt/detekt.yml'), { recursive: true });
        fs.writeFileSync('config/detekt/detekt.yml', minimalConfig.trim());
        console.log('   🔧 Created minimal detekt configuration');
    }

    autoFix() {
        this.fixes.forEach(fix => fix());
    }

    printResults() {
        console.log('\n📋 Validation Results');
        console.log('=====================');
        
        if (this.issues.length === 0) {
            console.log('✅ All configurations are valid!');
            console.log('🚀 Ready to run automated documentation & code quality workflow');
        } else {
            console.log(`❌ Found ${this.issues.length} configuration issue(s):`);
            this.issues.forEach((issue, index) => {
                console.log(`${index + 1}. ${issue}`);
            });
        }
    }
}

// Run validation
if (require.main === module) {
    const validator = new ConfigValidator();
    validator.validate();
}

module.exports = ConfigValidator;