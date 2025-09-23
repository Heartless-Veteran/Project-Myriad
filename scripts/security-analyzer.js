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
        
        console.log('4. Validating source code security...');
        this.checkSourceCodeSecurity();
        
        console.log('5. Checking ProGuard/R8 configuration...');
        this.checkObfuscationConfig();
        
        console.log('6. Validating GitHub configuration...');
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

            // Check for security best practices
            if (content.includes('android:allowBackup="false"')) {
                this.recommendations.push({
                    type: 'GOOD',
                    category: 'App Security',
                    issue: 'Backup disabled for security',
                    solution: 'Good practice: android:allowBackup="false" prevents data extraction'
                });
            } else if (content.includes('android:allowBackup="true"')) {
                this.issues.push({
                    type: 'MEDIUM',
                    category: 'App Security',
                    issue: 'App backup is enabled',
                    solution: 'Consider setting android:allowBackup="false" to prevent data extraction'
                });
            }

            // Check for network security config
            if (content.includes('networkSecurityConfig')) {
                this.recommendations.push({
                    type: 'GOOD',
                    category: 'Network Security',
                    issue: 'Network security config configured',
                    solution: 'Good practice: Network security configuration helps enforce HTTPS'
                });
            } else {
                this.recommendations.push({
                    type: 'INFO',
                    category: 'Network Security',
                    issue: 'No network security config specified',
                    solution: 'Consider adding network security configuration for HTTPS enforcement'
                });
            }

            // Check for cleartext traffic
            if (content.includes('android:usesCleartextTraffic="false"')) {
                this.recommendations.push({
                    type: 'GOOD',
                    category: 'Network Security',
                    issue: 'Cleartext traffic disabled',
                    solution: 'Good practice: usesCleartextTraffic="false" enforces encrypted connections'
                });
            } else if (content.includes('android:usesCleartextTraffic="true"')) {
                this.issues.push({
                    type: 'MEDIUM',
                    category: 'Network Security',
                    issue: 'Cleartext traffic is allowed',
                    solution: 'Consider setting android:usesCleartextTraffic="false" for better security'
                });
            }

            // Check for exported activities without proper protection
            const exportedActivities = content.match(/<activity[^>]*android:exported="true"[^>]*>/g);
            if (exportedActivities) {
                exportedActivities.forEach(() => {
                    this.recommendations.push({
                        type: 'INFO',
                        category: 'Component Security',
                        issue: 'Exported activity found',
                        solution: 'Ensure exported activities have proper intent filters and security checks'
                    });
                });
            }

            // Check for debuggable flag in manifest (shouldn't be there for release)
            if (content.includes('android:debuggable="true"')) {
                this.issues.push({
                    type: 'HIGH',
                    category: 'App Security',
                    issue: 'Debuggable flag set in manifest',
                    solution: 'Remove android:debuggable="true" for production releases'
                });
            }
        }

        console.log('   ✅ Permissions checked');
    }

    checkSourceCodeSecurity() {
        const kotlinSourceDirs = ['app/src/main/kotlin', 'core', 'feature'];
        let totalFilesScanned = 0;
        
        kotlinSourceDirs.forEach(dir => {
            if (fs.existsSync(dir)) {
                this.scanDirectoryForSecurityIssues(dir);
            }
        });
        
        console.log('   ✅ Source code security checked');
    }

    scanDirectoryForSecurityIssues(directory) {
        const files = fs.readdirSync(directory, { withFileTypes: true });
        
        files.forEach(file => {
            const fullPath = path.join(directory, file.name);
            
            if (file.isDirectory()) {
                this.scanDirectoryForSecurityIssues(fullPath);
            } else if (file.name.endsWith('.kt') || file.name.endsWith('.java')) {
                const content = fs.readFileSync(fullPath, 'utf8');
                
                // Check for potential security issues in code
                const securityPatterns = [
                    {
                        pattern: /Log\.[div]\s*\([^)]*password[^)]*\)/i,
                        type: 'HIGH',
                        issue: 'Password logging detected',
                        solution: 'Remove password logging or use proper redaction'
                    },
                    {
                        pattern: /System\.out\.print[ln]*\s*\([^)]*password[^)]*\)/i,
                        type: 'MEDIUM',
                        issue: 'Password in console output',
                        solution: 'Remove password from console output'
                    },
                    {
                        pattern: /val\s+\w*(?:[Pp]assword)\s*=\s*"[^"]+"/,
                        type: 'HIGH',
                        issue: 'Hardcoded password in source code',
                        solution: 'Move password to secure storage or configuration'
                    },
                    {
                        pattern: /(?:(?:const\s+)?val|var)\s+\w*[Aa][Pp][Ii][_]?[Kk][Ee][Yy]\s*=\s*"[^"]+"/,
                        type: 'CRITICAL',
                        issue: 'Hardcoded API key in source code',
                        solution: 'Move API key to BuildConfig or secure storage'
                    },
                    {
                        pattern: /\.setHostnameVerifier\s*\{\s*_,\s*_\s*->\s*true\s*\}/,
                        type: 'CRITICAL',
                        issue: 'Disabled hostname verification',
                        solution: 'Use proper certificate validation'
                    },
                    {
                        pattern: /TrustManager.*checkServerTrusted\s*\([^)]*\)\s*\{\s*\}/,
                        type: 'CRITICAL',
                        issue: 'Custom TrustManager that accepts all certificates',
                        solution: 'Implement proper certificate validation'
                    }
                ];
                
                securityPatterns.forEach(({ pattern, type, issue, solution }) => {
                    if (pattern.test(content)) {
                        this.issues.push({
                            type,
                            category: 'Source Code Security',
                            issue: `${issue} in ${file.name}`,
                            solution
                        });
                    }
                });
            }
        });
    }

    checkObfuscationConfig() {
        const proguardRules = 'app/proguard-rules.pro';
        const buildGradle = 'app/build.gradle.kts';
        
        if (fs.existsSync(buildGradle)) {
            const content = fs.readFileSync(buildGradle, 'utf8');
            
            // Check if obfuscation is enabled for release
            if (content.includes('isMinifyEnabled = true')) {
                this.recommendations.push({
                    type: 'GOOD',
                    category: 'Code Protection',
                    issue: 'Code obfuscation enabled',
                    solution: 'Good practice: minifyEnabled protects against reverse engineering'
                });
            } else {
                this.issues.push({
                    type: 'MEDIUM',
                    category: 'Code Protection',
                    issue: 'Code obfuscation not enabled',
                    solution: 'Enable isMinifyEnabled = true for release builds'
                });
            }
            
            // Check if resource shrinking is enabled
            if (content.includes('isShrinkResources = true')) {
                this.recommendations.push({
                    type: 'GOOD',
                    category: 'Code Protection',
                    issue: 'Resource shrinking enabled',
                    solution: 'Good practice: shrinkResources reduces APK size and attack surface'
                });
            }
        }
        
        if (fs.existsSync(proguardRules)) {
            const content = fs.readFileSync(proguardRules, 'utf8');
            
            // Check for overly permissive rules
            if (content.includes('-dontwarn **') || content.includes('-ignorewarnings')) {
                this.issues.push({
                    type: 'MEDIUM',
                    category: 'Code Protection',
                    issue: 'Overly permissive ProGuard rules',
                    solution: 'Review and tighten ProGuard rules to avoid hiding important warnings'
                });
            }
            
            // Check for keep rules that might expose sensitive code
            if (content.includes('-keep class * { *; }')) {
                this.issues.push({
                    type: 'HIGH',
                    category: 'Code Protection',
                    issue: 'Overly broad keep rules in ProGuard',
                    solution: 'Use specific keep rules instead of keeping all classes'
                });
            }
        } else {
            this.recommendations.push({
                type: 'INFO',
                category: 'Code Protection',
                issue: 'No custom ProGuard rules file',
                solution: 'Consider adding proguard-rules.pro for custom obfuscation rules'
            });
        }
        
        console.log('   ✅ Obfuscation configuration checked');
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