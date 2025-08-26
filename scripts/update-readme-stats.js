#!/usr/bin/env node

/**
 * README Statistics Updater for Project Myriad
 * Updates project statistics in README.md
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

console.log('📊 README Statistics Updater for Project Myriad');
console.log('===============================================\n');

class ReadmeUpdater {
    constructor() {
        this.stats = {};
        this.readmePath = 'README.md';
        this.srcPath = 'app/src/main/kotlin/com/heartlessveteran/myriad';
        this.testPath = 'app/src/test/kotlin/com/heartlessveteran/myriad';
    }

    async updateStats() {
        console.log('1. Gathering project statistics...');
        await this.gatherStats();
        
        console.log('2. Updating README.md...');
        this.updateReadme();
        
        console.log('✅ README.md updated with current statistics');
    }

    async gatherStats() {
        // Count files
        this.stats.kotlinFiles = this.countKotlinFiles(this.srcPath);
        this.stats.testFiles = this.countKotlinFiles(this.testPath);
        
        // Count lines of code (excluding empty lines and comments)
        this.stats.linesOfCode = this.countLinesOfCode(this.srcPath);
        
        // Count dependencies
        this.stats.dependencies = this.countDependencies();
        
        // Get build info
        this.stats.buildInfo = this.getBuildInfo();
        
        // Git statistics
        this.stats.gitStats = this.getGitStats();
        
        // Package information
        this.stats.packageInfo = this.getPackageInfo();

        console.log(`   📁 Kotlin files: ${this.stats.kotlinFiles}`);
        console.log(`   🧪 Test files: ${this.stats.testFiles}`);
        console.log(`   📝 Lines of code: ${this.stats.linesOfCode}`);
        console.log(`   📦 Dependencies: ${this.stats.dependencies.total}`);
        console.log(`   🏗️  Target SDK: ${this.stats.buildInfo.targetSdk}`);
    }

    countKotlinFiles(dir) {
        if (!fs.existsSync(dir)) return 0;
        
        let count = 0;
        const items = fs.readdirSync(dir);
        
        items.forEach(item => {
            const itemPath = path.join(dir, item);
            const stat = fs.statSync(itemPath);
            
            if (stat.isDirectory()) {
                count += this.countKotlinFiles(itemPath);
            } else if (item.endsWith('.kt')) {
                count++;
            }
        });
        
        return count;
    }

    countLinesOfCode(dir) {
        if (!fs.existsSync(dir)) return 0;
        
        let lines = 0;
        const kotlinFiles = this.getKotlinFiles(dir);
        
        kotlinFiles.forEach(file => {
            const content = fs.readFileSync(file, 'utf8');
            const codeLines = content
                .split('\n')
                .filter(line => {
                    const trimmed = line.trim();
                    return trimmed && !trimmed.startsWith('//') && !trimmed.startsWith('/*') && !trimmed.startsWith('*');
                }).length;
            lines += codeLines;
        });
        
        return lines;
    }

    getKotlinFiles(dir) {
        if (!fs.existsSync(dir)) return [];
        
        let files = [];
        const items = fs.readdirSync(dir);
        
        items.forEach(item => {
            const itemPath = path.join(dir, item);
            const stat = fs.statSync(itemPath);
            
            if (stat.isDirectory()) {
                files = files.concat(this.getKotlinFiles(itemPath));
            } else if (item.endsWith('.kt')) {
                files.push(itemPath);
            }
        });
        
        return files;
    }

    countDependencies() {
        try {
            const buildFile = fs.readFileSync('app/build.gradle.kts', 'utf8');
            
            const implementationCount = (buildFile.match(/implementation\(/g) || []).length;
            const testCount = (buildFile.match(/testImplementation\(/g) || []).length;
            const androidTestCount = (buildFile.match(/androidTestImplementation\(/g) || []).length;
            
            return {
                total: implementationCount + testCount + androidTestCount,
                implementation: implementationCount,
                test: testCount,
                androidTest: androidTestCount
            };
        } catch (error) {
            console.warn('Could not count dependencies:', error.message);
            return { total: 0, implementation: 0, test: 0, androidTest: 0 };
        }
    }

    getBuildInfo() {
        try {
            const buildFile = fs.readFileSync('app/build.gradle.kts', 'utf8');
            
            const targetSdk = buildFile.match(/targetSdk = (\d+)/)?.[1] || 'N/A';
            const minSdk = buildFile.match(/minSdk = (\d+)/)?.[1] || 'N/A';
            const compileSdk = buildFile.match(/compileSdk = (\d+)/)?.[1] || 'N/A';
            const versionName = buildFile.match(/versionName = "([^"]+)"/)?.[1] || 'N/A';
            
            return {
                targetSdk,
                minSdk,
                compileSdk,
                versionName
            };
        } catch (error) {
            console.warn('Could not get build info:', error.message);
            return { targetSdk: 'N/A', minSdk: 'N/A', compileSdk: 'N/A', versionName: 'N/A' };
        }
    }

    getGitStats() {
        try {
            const commitCount = execSync('git rev-list --count HEAD', { encoding: 'utf8' }).trim();
            const lastCommit = execSync('git log -1 --format="%h %s"', { encoding: 'utf8' }).trim();
            const contributors = execSync('git shortlog -sn | wc -l', { encoding: 'utf8' }).trim();
            
            return {
                commits: commitCount,
                lastCommit,
                contributors
            };
        } catch (error) {
            console.warn('Could not get git stats:', error.message);
            return { commits: 'N/A', lastCommit: 'N/A', contributors: 'N/A' };
        }
    }

    getPackageInfo() {
        try {
            const packageJson = JSON.parse(fs.readFileSync('package.json', 'utf8'));
            return {
                version: packageJson.version,
                name: packageJson.name,
                description: packageJson.description
            };
        } catch (error) {
            console.warn('Could not get package info:', error.message);
            return { version: 'N/A', name: 'N/A', description: 'N/A' };
        }
    }

    updateReadme() {
        if (!fs.existsSync(this.readmePath)) {
            console.error('README.md not found!');
            return;
        }

        let readme = fs.readFileSync(this.readmePath, 'utf8');
        const timestamp = new Date().toISOString().split('T')[0];

        // Create statistics section
        const statsSection = this.createStatsSection(timestamp);

        // Find and replace existing stats section or add new one
        const statsMarker = '<!-- PROJECT_STATS -->';
        const endMarker = '<!-- /PROJECT_STATS -->';
        
        if (readme.includes(statsMarker)) {
            // Replace existing stats section
            const startIndex = readme.indexOf(statsMarker);
            const endIndex = readme.indexOf(endMarker) + endMarker.length;
            
            if (endIndex > startIndex) {
                readme = readme.substring(0, startIndex) + statsSection + readme.substring(endIndex);
            } else {
                readme += '\n\n' + statsSection;
            }
        } else {
            // Add stats section before the first heading or at the end
            const firstHeading = readme.indexOf('\n## ');
            if (firstHeading !== -1) {
                readme = readme.substring(0, firstHeading) + '\n\n' + statsSection + readme.substring(firstHeading);
            } else {
                readme += '\n\n' + statsSection;
            }
        }

        // Update other dynamic content
        readme = this.updateBadges(readme);

        fs.writeFileSync(this.readmePath, readme);
    }

    createStatsSection(timestamp) {
        return `<!-- PROJECT_STATS -->
## 📊 Project Statistics

| Metric | Value |
|--------|-------|
| **Kotlin Files** | ${this.stats.kotlinFiles} |
| **Test Files** | ${this.stats.testFiles} |
| **Lines of Code** | ${this.stats.linesOfCode.toLocaleString()} |
| **Dependencies** | ${this.stats.dependencies.total} |
| **Target SDK** | ${this.stats.buildInfo.targetSdk} |
| **Min SDK** | ${this.stats.buildInfo.minSdk} |
| **Version** | ${this.stats.buildInfo.versionName} |
| **Commits** | ${this.stats.gitStats.commits} |
| **Contributors** | ${this.stats.gitStats.contributors} |

*Last updated: ${timestamp}*

### Dependency Breakdown
- **Implementation**: ${this.stats.dependencies.implementation} dependencies
- **Test**: ${this.stats.dependencies.test} dependencies  
- **Android Test**: ${this.stats.dependencies.androidTest} dependencies

### Recent Activity
- Latest commit: ${this.stats.gitStats.lastCommit}

<!-- /PROJECT_STATS -->`};

    updateBadges(readme) {
        // Update version badge if it exists
        const versionBadge = `![Version](https://img.shields.io/badge/version-${this.stats.buildInfo.versionName}-blue)`;
        const sdkBadge = `![SDK](https://img.shields.io/badge/SDK-${this.stats.buildInfo.minSdk}+-green)`;
        const kotlinBadge = `![Kotlin](https://img.shields.io/badge/kotlin-${this.stats.kotlinFiles}%20files-purple)`;
        
        // If there's a badges section, update it
        if (readme.includes('![Version]')) {
            readme = readme.replace(/!\[Version\][^\n]*/g, versionBadge);
        }
        if (readme.includes('![SDK]')) {
            readme = readme.replace(/!\[SDK\][^\n]*/g, sdkBadge);
        }
        if (readme.includes('![Kotlin]')) {
            readme = readme.replace(/!\[Kotlin\][^\n]*/g, kotlinBadge);
        }

        return readme;
    }
}

// Run updater
if (require.main === module) {
    const updater = new ReadmeUpdater();
    updater.updateStats().catch(console.error);
}

module.exports = ReadmeUpdater;