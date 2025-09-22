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