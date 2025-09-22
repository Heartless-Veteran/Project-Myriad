#!/usr/bin/env node

/**
 * Security Enhancement and Validation Script for Project Myriad
 * Checks for common security issues and provides recommendations
 */

const fs = require('fs');
const path = require('path');

console.log('🔒 Security Enhancement Analysis for Project Myriad');
console.log('=================================================\n');

class SecurityAnalyzer {
    constructor() {
        this.issues = [];
        this.recommendations = [];
    }

    analyze() {
        console.log('1. Checking for sensitive file exposure...');
        this.checkSensitiveFiles();
        
        console.log('2. Analyzing build configuration security...');
        this.analyzeBuildSecurity();
        
        console.log('3. Checking permissions and configurations...');
        this.checkPermissions();
        
        console.log('4. Validating GitHub configuration...');
        this.validateGitHubSecurity();
        
        this.generateReport();
    }

    checkSensitiveFiles() {
        const sensitiveFiles = [
            'local.properties',
            'keystore.jks',
            'keystore.p12',
            'google-services.json',
            'firebase-config.json',
            '.env',
            'secrets.txt'
        ];

        sensitiveFiles.forEach(file => {
            if (fs.existsSync(file)) {
                // Check if it's properly gitignored
                const gitignore = fs.existsSync('.gitignore') ? fs.readFileSync('.gitignore', 'utf8') : '';
                if (!gitignore.includes(file)) {
                    this.issues.push({
                        type: 'CRITICAL',
                        category: 'File Security',
                        issue: `Sensitive file ${file} not in .gitignore`,
                        solution: `Add "${file}" to .gitignore`
                    });
                }
            }
        });

        // Check for example files
        if (fs.existsSync('local.properties.example')) {
            this.recommendations.push({
                type: 'GOOD',
                category: 'File Security',
                issue: 'Example configuration file exists',
                solution: 'Good practice: local.properties.example provides template'
            });
        }

        console.log('   ✅ Sensitive files checked');
    }

    analyzeBuildSecurity() {
        const appBuildFile = 'app/build.gradle.kts';
        if (fs.existsSync(appBuildFile)) {
            const content = fs.readFileSync(appBuildFile, 'utf8');
            
            // Check for hardcoded secrets
            const suspiciousPatterns = [
                /api[_-]?key\s*=\s*["'][^"']+["']/i,
                /password\s*=\s*["'][^"']+["']/i,
                /secret\s*=\s*["'][^"']+["']/i,
                /token\s*=\s*["'][^"']+["']/i
            ];

            suspiciousPatterns.forEach(pattern => {
                if (pattern.test(content)) {
                    this.issues.push({
                        type: 'HIGH',
                        category: 'Build Security',
                        issue: 'Potential hardcoded secret in build file',
                        solution: 'Move secrets to local.properties or environment variables'
                    });
                }
            });

            // Check for debug signing config in release
            if (content.includes('signingConfig signingConfigs.debug') && content.includes('release')) {
                this.issues.push({
                    type: 'MEDIUM',
                    category: 'Build Security',
                    issue: 'Debug signing config used in release build',
                    solution: 'Use proper release signing configuration'
                });
            }

            // Check for allowBackup setting
            if (!content.includes('allowBackup')) {
                this.recommendations.push({
                    type: 'INFO',
                    category: 'App Security',
                    issue: 'allowBackup not explicitly set',
                    solution: 'Consider setting android:allowBackup="false" in manifest'
                });
            }
        }

        console.log('   ✅ Build security analyzed');
    }

    checkPermissions() {
        const manifestPath = 'app/src/main/AndroidManifest.xml';
        if (fs.existsSync(manifestPath)) {
            const content = fs.readFileSync(manifestPath, 'utf8');
            
            // Check for dangerous permissions
            const dangerousPermissions = [
                'INTERNET',
                'WRITE_EXTERNAL_STORAGE',
                'READ_EXTERNAL_STORAGE',
                'CAMERA',
                'RECORD_AUDIO',
                'ACCESS_FINE_LOCATION',
                'ACCESS_COARSE_LOCATION'
            ];

            dangerousPermissions.forEach(permission => {
                if (content.includes(`android.permission.${permission}`)) {
                    this.recommendations.push({
                        type: 'INFO',
                        category: 'Permissions',
                        issue: `Uses permission: ${permission}`,
                        solution: `Ensure ${permission} permission is necessary and properly handled`
                    });
                }
            });

            // Check for network security config
            if (!content.includes('networkSecurityConfig')) {
                this.recommendations.push({
                    type: 'INFO',
                    category: 'Network Security',
                    issue: 'No network security config specified',
                    solution: 'Consider adding network security configuration for HTTPS enforcement'
                });
            }
        }

        console.log('   ✅ Permissions checked');
    }

    validateGitHubSecurity() {
        // Check for security workflow
        const securityWorkflow = '.github/workflows/security.yml';
        if (fs.existsSync(securityWorkflow)) {
            this.recommendations.push({
                type: 'GOOD',
                category: 'CI/CD Security',
                issue: 'Security workflow exists',
                solution: 'Good practice: Automated security scanning enabled'
            });
        } else {
            this.issues.push({
                type: 'MEDIUM',
                category: 'CI/CD Security',
                issue: 'No security scanning workflow',
                solution: 'Add GitHub Actions workflow for security scanning'
            });
        }

        // Check for Dependabot
        const dependabotConfig = '.github/dependabot.yml';
        if (fs.existsSync(dependabotConfig)) {
            this.recommendations.push({
                type: 'GOOD',
                category: 'Dependency Security',
                issue: 'Dependabot configuration exists',
                solution: 'Good practice: Automated dependency updates enabled'
            });
        }

        // Check for branch protection (can't directly check, but can recommend)
        this.recommendations.push({
            type: 'INFO',
            category: 'Repository Security',
            issue: 'Branch protection recommendations',
            solution: 'Ensure main branch has protection rules: require PR reviews, status checks'
        });

        console.log('   ✅ GitHub security validated');
    }

    generateReport() {
        console.log('\n🔒 Security Analysis Report');
        console.log('===========================');
        
        const allItems = [...this.issues, ...this.recommendations];
        
        if (allItems.length === 0) {
            console.log('\n🎉 No security issues found! Project follows security best practices.');
            return;
        }

        const groupedItems = allItems.reduce((acc, item) => {
            if (!acc[item.type]) acc[item.type] = [];
            acc[item.type].push(item);
            return acc;
        }, {});

        // Define priority order
        const order = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO', 'GOOD'];
        
        order.forEach(type => {
            if (!groupedItems[type]) return;
            
            const icon = {
                'CRITICAL': '🚨',
                'HIGH': '❌',
                'MEDIUM': '⚠️',
                'LOW': '💡',
                'INFO': 'ℹ️',
                'GOOD': '✅'
            }[type];

            console.log(`\n${icon} ${type} PRIORITY:`);
            groupedItems[type].forEach((item, index) => {
                console.log(`${index + 1}. [${item.category}] ${item.issue}`);
                console.log(`   Solution: ${item.solution}`);
            });
        });

        // Security recommendations
        console.log('\n🛡️ Additional Security Recommendations:');
        console.log('• Use ProGuard/R8 obfuscation for release builds');
        console.log('• Implement certificate pinning for API communications');
        console.log('• Use Android KeyStore for sensitive data storage');
        console.log('• Enable code signing verification in CI/CD');
        console.log('• Regular security dependency audits');
        console.log('• Implement proper error handling to avoid information leakage');

        // Quick fixes
        const criticalIssues = this.issues.filter(issue => issue.type === 'CRITICAL');
        if (criticalIssues.length > 0) {
            console.log('\n🚨 IMMEDIATE ACTION REQUIRED:');
            criticalIssues.forEach((issue, index) => {
                console.log(`${index + 1}. ${issue.solution}`);
            });
        }
    }
}

// Run analysis
if (require.main === module) {
    const analyzer = new SecurityAnalyzer();
    analyzer.analyze();
}

module.exports = SecurityAnalyzer;