#!/usr/bin/env node

/**
 * Performance Optimization Script for Project Myriad
 * Analyzes build performance and suggests optimizations
 */

const fs = require('fs');
const path = require('path');

console.log('⚡ Performance Optimization Analysis for Project Myriad');
console.log('======================================================\n');

class PerformanceAnalyzer {
    constructor() {
        this.recommendations = [];
        this.metrics = {
            gradleProperties: {},
            buildGradleConfig: {},
            moduleCount: 0,
            dependencyCount: 0
        };
    }

    analyze() {
        console.log('1. Analyzing Gradle configuration...');
        this.analyzeGradleProperties();
        
        console.log('2. Analyzing build scripts...');
        this.analyzeBuildScripts();
        
        console.log('3. Analyzing project structure...');
        this.analyzeProjectStructure();
        
        console.log('4. Checking dependency configuration...');
        this.analyzeDependencies();
        
        console.log('5. Analyzing memory and resource usage...');
        this.analyzeMemoryUsage();
        
        console.log('6. Checking build performance optimizations...');
        this.analyzeBuildPerformance();
        
        this.generateReport();
    }

    analyzeGradleProperties() {
        const gradlePropsPath = 'gradle.properties';
        if (!fs.existsSync(gradlePropsPath)) {
            this.recommendations.push({
                type: 'ERROR',
                category: 'Configuration',
                issue: 'Missing gradle.properties file',
                solution: 'Create gradle.properties with performance optimizations'
            });
            return;
        }

        const content = fs.readFileSync(gradlePropsPath, 'utf8');
        const lines = content.split('\n');
        
        // Check for essential performance properties
        const requiredProps = [
            'org.gradle.parallel=true',
            'org.gradle.caching=true',
            'org.gradle.configureondemand=true',
            'org.gradle.configuration-cache=true',
            'android.useAndroidX=true',
            'android.enableJetifier=true'
        ];

        requiredProps.forEach(prop => {
            const [key, expectedValue] = prop.split('=');
            const found = lines.find(line => line.startsWith(key));
            
            if (!found) {
                this.recommendations.push({
                    type: 'WARNING',
                    category: 'Performance',
                    issue: `Missing property: ${key}`,
                    solution: `Add "${prop}" to gradle.properties`
                });
            } else if (found !== prop) {
                const currentValue = found.split('=')[1];
                if (currentValue !== expectedValue) {
                    this.recommendations.push({
                        type: 'INFO',
                        category: 'Performance',
                        issue: `Suboptimal value for ${key}: ${currentValue}`,
                        solution: `Change to "${prop}"`
                    });
                }
            }
        });

        // Check JVM heap size
        const jvmArgs = lines.find(line => line.startsWith('org.gradle.jvmargs'));
        if (!jvmArgs || !jvmArgs.includes('-Xmx')) {
            this.recommendations.push({
                type: 'WARNING',
                category: 'Performance',
                issue: 'No JVM heap size configured',
                solution: 'Add "org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8"'
            });
        }

        console.log('   ✅ Gradle properties analyzed');
    }

    analyzeBuildScripts() {
        const appBuildFile = 'app/build.gradle.kts';
        if (fs.existsSync(appBuildFile)) {
            const content = fs.readFileSync(appBuildFile, 'utf8');
            
            // Check for R8 optimization
            if (!content.includes('android.enableR8.fullMode')) {
                this.recommendations.push({
                    type: 'INFO',
                    category: 'Build Optimization',
                    issue: 'R8 full mode not explicitly enabled',
                    solution: 'Add "android.enableR8.fullMode=true" to gradle.properties'
                });
            }

            // Check for unnecessary dependencies
            const dependencyLines = content.match(/implementation\s*\([^)]+\)/g) || [];
            this.metrics.dependencyCount = dependencyLines.length;

            if (this.metrics.dependencyCount > 50) {
                this.recommendations.push({
                    type: 'WARNING',
                    category: 'Dependencies',
                    issue: `High dependency count: ${this.metrics.dependencyCount}`,
                    solution: 'Review and remove unused dependencies'
                });
            }
        }

        console.log('   ✅ Build scripts analyzed');
    }

    analyzeProjectStructure() {
        const moduleDirectories = [
            'app', 'core', 'feature', 'baselineprofile'
        ].filter(dir => fs.existsSync(dir));
        
        this.metrics.moduleCount = moduleDirectories.length;

        // Count source files
        let totalFiles = 0;
        moduleDirectories.forEach(dir => {
            const files = this.countKotlinFiles(dir);
            totalFiles += files;
        });

        this.metrics.totalSourceFiles = totalFiles;

        if (totalFiles > 200) {
            this.recommendations.push({
                type: 'INFO',
                category: 'Architecture',
                issue: `Large codebase: ${totalFiles} source files`,
                solution: 'Consider using incremental compilation and parallel builds'
            });
        }

        console.log('   ✅ Project structure analyzed');
    }

    countKotlinFiles(directory) {
        let count = 0;
        try {
            const walk = (dir) => {
                const files = fs.readdirSync(dir);
                files.forEach(file => {
                    const filePath = path.join(dir, file);
                    const stat = fs.statSync(filePath);
                    if (stat.isDirectory()) {
                        walk(filePath);
                    } else if (file.endsWith('.kt')) {
                        count++;
                    }
                });
            };
            walk(directory);
        } catch (error) {
            // Directory doesn't exist or not accessible
        }
        return count;
    }

    analyzeDependencies() {
        // Check for common performance issues in dependencies
        const catalogFile = 'gradle/libs.versions.toml';
        if (fs.existsSync(catalogFile)) {
            const content = fs.readFileSync(catalogFile, 'utf8');
            
            // Check for version catalog usage (good practice)
            this.recommendations.push({
                type: 'SUCCESS',
                category: 'Dependencies',
                issue: 'Using version catalog',
                solution: 'Continue using version catalog for dependency management'
            });
        }

        console.log('   ✅ Dependencies analyzed');
    }

    analyzeMemoryUsage() {
        // Analyze resource files for potential memory issues
        const resourceDirs = [
            'app/src/main/res/drawable',
            'app/src/main/res/drawable-xxhdpi',
            'app/src/main/res/drawable-xxxhdpi',
            'app/src/main/res/raw'
        ];

        let largeResourceCount = 0;
        resourceDirs.forEach(dir => {
            if (fs.existsSync(dir)) {
                const files = fs.readdirSync(dir);
                files.forEach(file => {
                    const filePath = path.join(dir, file);
                    const stats = fs.statSync(filePath);
                    const sizeInMB = stats.size / (1024 * 1024);
                    
                    if (sizeInMB > 1) { // Files larger than 1MB
                        largeResourceCount++;
                        this.recommendations.push({
                            type: 'WARNING',
                            category: 'Memory Optimization',
                            issue: `Large resource file: ${file} (${sizeInMB.toFixed(2)}MB)`,
                            solution: 'Consider compressing or using vector drawables'
                        });
                    }
                });
            }
        });

        // Check for vector drawable usage
        const drawableDir = 'app/src/main/res/drawable';
        if (fs.existsSync(drawableDir)) {
            const files = fs.readdirSync(drawableDir);
            const vectorFiles = files.filter(f => f.endsWith('.xml'));
            const bitmapFiles = files.filter(f => f.endsWith('.png') || f.endsWith('.jpg') || f.endsWith('.jpeg'));
            
            if (vectorFiles.length > bitmapFiles.length) {
                this.recommendations.push({
                    type: 'SUCCESS',
                    category: 'Memory Optimization',
                    issue: 'Good use of vector drawables',
                    solution: 'Continue using vector drawables for scalable graphics'
                });
            } else if (bitmapFiles.length > 10) {
                this.recommendations.push({
                    type: 'INFO',
                    category: 'Memory Optimization',
                    issue: `${bitmapFiles.length} bitmap files found`,
                    solution: 'Consider converting suitable bitmaps to vector drawables'
                });
            }
        }

        // Check Kotlin source files for potential memory leaks
        this.checkForMemoryLeakPatterns('app/src/main/kotlin');
        if (fs.existsSync('core')) this.checkForMemoryLeakPatterns('core');
        if (fs.existsSync('feature')) this.checkForMemoryLeakPatterns('feature');

        console.log('   ✅ Memory usage analyzed');
    }

    checkForMemoryLeakPatterns(directory) {
        if (!fs.existsSync(directory)) return;
        
        const files = fs.readdirSync(directory, { withFileTypes: true });
        files.forEach(file => {
            const fullPath = path.join(directory, file.name);
            
            if (file.isDirectory()) {
                this.checkForMemoryLeakPatterns(fullPath);
            } else if (file.name.endsWith('.kt')) {
                const content = fs.readFileSync(fullPath, 'utf8');
                
                // Check for potential memory leak patterns
                // Kotlin: Check for companion object or top-level Context references
                if (
                    (content.includes('companion object') && content.match(/companion object[^{]*\{[^}]*Context/)) ||
                    content.match(/^(?:\s*)?(var|val)\s+\w+\s*:\s*Context/m)
                ) {
                    this.recommendations.push({
                        type: 'WARNING',
                        category: 'Memory Optimization',
                        issue: `Potential Context leak in ${file.name}`,
                        solution: 'Avoid storing Context in companion objects or top-level variables - use Application context or weak references'
                    });
                }
                
                if (content.includes('Handler(') && !content.includes('WeakReference')) {
                    this.recommendations.push({
                        type: 'INFO',
                        category: 'Memory Optimization',
                        issue: `Handler usage in ${file.name}`,
                        solution: 'Consider using WeakReference or lifecycle-aware components'
                    });
                }
                
                if (content.includes('ArrayList') && content.includes('clear()')) {
                    this.recommendations.push({
                        type: 'INFO',
                        category: 'Memory Optimization',
                        issue: `Manual collection clearing in ${file.name}`,
                        solution: 'Good practice: Properly clearing collections helps prevent memory leaks'
                    });
                }
            }
        });
    }

    analyzeBuildPerformance() {
        const gradleProps = 'gradle.properties';
        if (fs.existsSync(gradleProps)) {
            const content = fs.readFileSync(gradleProps, 'utf8');
            
            // Check for build performance optimizations
            const performanceChecks = [
                {
                    setting: 'org.gradle.parallel=true',
                    present: content.includes('org.gradle.parallel=true'),
                    category: 'Build Performance',
                    issue: 'Parallel builds enabled',
                    solution: 'Parallel execution improves build times on multi-core systems'
                },
                {
                    setting: 'org.gradle.caching=true',
                    present: content.includes('org.gradle.caching=true'),
                    category: 'Build Performance',
                    issue: 'Build caching enabled',
                    solution: 'Build cache reuses outputs from previous builds'
                },
                {
                    setting: 'org.gradle.configuration-cache=true',
                    present: content.includes('org.gradle.configuration-cache=true'),
                    category: 'Build Performance',
                    issue: 'Configuration cache enabled',
                    solution: 'Configuration cache speeds up subsequent builds'
                },
                {
                    setting: 'kotlin.incremental=true',
                    present: content.includes('kotlin.incremental=true'),
                    category: 'Build Performance',
                    issue: 'Kotlin incremental compilation enabled',
                    solution: 'Incremental compilation speeds up Kotlin builds'
                }
            ];

            performanceChecks.forEach(check => {
                if (check.present) {
                    this.recommendations.push({
                        type: 'SUCCESS',
                        category: check.category,
                        issue: check.issue,
                        solution: check.solution
                    });
                } else {
                    this.recommendations.push({
                        type: 'WARNING',
                        category: check.category,
                        issue: `Missing: ${check.setting}`,
                        solution: `Add "${check.setting}" to gradle.properties`
                    });
                }
            });
        }

        // Check for APK size optimizations in build.gradle
        const buildFile = 'app/build.gradle.kts';
        if (fs.existsSync(buildFile)) {
            const content = fs.readFileSync(buildFile, 'utf8');
            
            if (content.includes('splits {')) {
                this.recommendations.push({
                    type: 'SUCCESS',
                    category: 'APK Optimization',
                    issue: 'APK splits configured',
                    solution: 'APK splits reduce download size for users'
                });
            }
            
            if (content.includes('bundle {')) {
                this.recommendations.push({
                    type: 'SUCCESS',
                    category: 'APK Optimization',
                    issue: 'Android App Bundle configuration found',
                    solution: 'AAB enables dynamic delivery and reduces app size'
                });
            }
            
            if (content.includes('isShrinkResources = true')) {
                this.recommendations.push({
                    type: 'SUCCESS',
                    category: 'APK Optimization',
                    issue: 'Resource shrinking enabled',
                    solution: 'Resource shrinking removes unused resources from APK'
                });
            }
        }

        console.log('   ✅ Build performance analyzed');
    }

    generateReport() {
        console.log('\n📊 Performance Analysis Report');
        console.log('==============================');
        
        console.log('\n📈 Metrics:');
        console.log(`• Project modules: ${this.metrics.moduleCount}`);
        console.log(`• Source files: ${this.metrics.totalSourceFiles || 'N/A'}`);
        console.log(`• Dependencies: ${this.metrics.dependencyCount}`);

        if (this.recommendations.length === 0) {
            console.log('\n🎉 No performance issues found! Project is well optimized.');
            return;
        }

        const groupedRecommendations = this.recommendations.reduce((acc, rec) => {
            if (!acc[rec.type]) acc[rec.type] = [];
            acc[rec.type].push(rec);
            return acc;
        }, {});

        Object.entries(groupedRecommendations).forEach(([type, recs]) => {
            const icon = {
                'ERROR': '❌',
                'WARNING': '⚠️',
                'INFO': 'ℹ️',
                'SUCCESS': '✅'
            }[type];

            console.log(`\n${icon} ${type}S:`);
            recs.forEach((rec, index) => {
                console.log(`${index + 1}. [${rec.category}] ${rec.issue}`);
                console.log(`   Solution: ${rec.solution}`);
            });
        });

        console.log('\n💡 Quick Performance Tips:');
        console.log('• Use "./gradlew --build-cache" for faster builds');
        console.log('• Run "./gradlew --profile" to analyze build performance');
        console.log('• Consider using "./gradlew assemble --parallel" for parallel builds');
        console.log('• Use incremental builds: avoid "clean" unless necessary');
    }
}

// Run analysis
if (require.main === module) {
    const analyzer = new PerformanceAnalyzer();
    analyzer.analyze();
}

module.exports = PerformanceAnalyzer;