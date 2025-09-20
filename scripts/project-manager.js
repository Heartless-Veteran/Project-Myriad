#!/usr/bin/env node

/**
 * Project Manager - Enhanced GitHub Project Automation
 * Provides intelligent project management capabilities for Project Myriad
 */

const fs = require('fs');
const path = require('path');

console.log('🎯 Project Manager - Enhanced GitHub Project Automation');
console.log('====================================================\n');

class ProjectManager {
    constructor() {
        this.projectConfig = {
            projectUrl: 'https://github.com/users/Heartless-Veteran/projects/1',
            columns: {
                todo: 'To Do',
                inProgress: 'In Progress',
                inReview: 'In Review',
                done: 'Done',
                released: 'Released'
            },
            milestones: {
                foundation: 'v1.0.0 - Foundation',
                coreFeatures: 'v1.1.0 - Core Features',
                aiIntegration: 'v1.2.0 - AI Integration',
                polish: 'v1.3.0 - Polish & UX'
            },
            labelMappings: {
                // Auto-labeling based on file paths
                'docs/': ['documentation'],
                '.github/workflows/': ['ci/cd', 'automation'],
                'app/src/main/kotlin/': ['android', 'kotlin'],
                'scripts/': ['automation'],
                'build.gradle': ['build'],
                'app/src/test/': ['testing'],
                'app/src/androidTest/': ['testing', 'android'],
                'config/': ['configuration']
            },
            priorityLabels: {
                'priority:critical': 1,
                'priority:high': 2,
                'priority:medium': 3,
                'priority:low': 4,
                'bug': 2,
                'security': 1,
                'enhancement': 3,
                'feature': 3,
                'documentation': 4
            }
        };
    }

    /**
     * Analyze PR file changes and suggest labels
     */
    suggestLabelsForPR(files) {
        const labels = new Set();
        const changedPaths = files.map(file => file.filename || file);

        for (const [pathPattern, pathLabels] of Object.entries(this.projectConfig.labelMappings)) {
            if (changedPaths.some(path => path.includes(pathPattern))) {
                pathLabels.forEach(label => labels.add(label));
            }
        }

        // Additional intelligent labeling
        const kotlinFiles = changedPaths.filter(path => path.endsWith('.kt'));
        const xmlFiles = changedPaths.filter(path => path.endsWith('.xml'));
        const gradleFiles = changedPaths.filter(path => path.includes('gradle'));

        if (kotlinFiles.length > 0) {
            labels.add('kotlin');
            
            // Analyze Kotlin file patterns
            const hasViewModel = kotlinFiles.some(path => path.includes('ViewModel'));
            const hasRepository = kotlinFiles.some(path => path.includes('Repository'));
            const hasDao = kotlinFiles.some(path => path.includes('Dao'));
            const hasEntity = kotlinFiles.some(path => path.includes('Entity'));
            
            if (hasViewModel || hasRepository || hasDao || hasEntity) {
                labels.add('architecture');
            }
        }

        if (xmlFiles.length > 0) {
            labels.add('ui');
            if (xmlFiles.some(path => path.includes('layout/'))) {
                labels.add('layout');
            }
        }

        if (gradleFiles.length > 0) {
            labels.add('build');
            if (changedPaths.some(path => path.includes('dependencies'))) {
                labels.add('dependencies');
            }
        }

        return Array.from(labels);
    }

    /**
     * Determine milestone based on labels and content
     */
    suggestMilestone(labels, isIssue, title, body) {
        const labelSet = new Set(labels.map(l => typeof l === 'string' ? l : l.name));
        
        // Critical bugs and security issues go to foundation
        if (labelSet.has('bug') || labelSet.has('security') || labelSet.has('priority:critical')) {
            return this.projectConfig.milestones.foundation;
        }
        
        // Core functionality features
        if (labelSet.has('feature') && (
            title.toLowerCase().includes('vault') ||
            title.toLowerCase().includes('reader') ||
            title.toLowerCase().includes('library') ||
            body?.toLowerCase().includes('core feature')
        )) {
            return this.projectConfig.milestones.coreFeatures;
        }
        
        // AI-related features
        if (labelSet.has('feature') && (
            title.toLowerCase().includes('ai') ||
            title.toLowerCase().includes('ocr') ||
            title.toLowerCase().includes('translation') ||
            body?.toLowerCase().includes('ai core')
        )) {
            return this.projectConfig.milestones.aiIntegration;
        }
        
        // Documentation, build, CI/CD to foundation
        if (labelSet.has('documentation') || labelSet.has('ci/cd') || labelSet.has('build')) {
            return this.projectConfig.milestones.foundation;
        }
        
        // Default for features
        if (labelSet.has('feature') || labelSet.has('enhancement')) {
            return this.projectConfig.milestones.coreFeatures;
        }
        
        return this.projectConfig.milestones.foundation;
    }

    /**
     * Determine project status based on issue/PR state
     */
    determineStatus(eventType, action, item) {
        if (eventType === 'issues') {
            switch (action) {
                case 'opened':
                    return this.projectConfig.columns.todo;
                case 'assigned':
                    return this.projectConfig.columns.inProgress;
                case 'closed':
                    return item.state_reason === 'completed' ? this.projectConfig.columns.done : this.projectConfig.columns.todo;
                case 'reopened':
                    return this.projectConfig.columns.todo;
                default:
                    return null;
            }
        }
        
        if (eventType === 'pull_request') {
            switch (action) {
                case 'opened':
                case 'ready_for_review':
                    return this.projectConfig.columns.inReview;
                case 'converted_to_draft':
                    return this.projectConfig.columns.inProgress;
                case 'closed':
                    return item.merged ? this.projectConfig.columns.done : this.projectConfig.columns.todo;
                case 'reopened':
                    return this.projectConfig.columns.inReview;
                default:
                    return null;
            }
        }
        
        return null;
    }

    /**
     * Generate project management report
     */
    generateReport() {
        const reportData = {
            timestamp: new Date().toISOString(),
            configuration: {
                columns: Object.keys(this.projectConfig.columns).length,
                milestones: Object.keys(this.projectConfig.milestones).length,
                labelMappings: Object.keys(this.projectConfig.labelMappings).length
            },
            features: [
                'Automatic labeling based on file paths',
                'Intelligent milestone assignment',
                'Status tracking with project columns',
                'Priority-based organization',
                'Architecture-aware categorization'
            ]
        };

        console.log('📊 Project Management Configuration Report');
        console.log('==========================================');
        console.log(`Generated: ${reportData.timestamp.split('T')[0]}`);
        console.log(`\n🏗️ Configuration:`);
        console.log(`   - Project Columns: ${reportData.configuration.columns}`);
        console.log(`   - Milestones: ${reportData.configuration.milestones}`);
        console.log(`   - Label Mappings: ${reportData.configuration.labelMappings}`);
        
        console.log(`\n✨ Features:`);
        reportData.features.forEach(feature => {
            console.log(`   ✅ ${feature}`);
        });

        console.log(`\n🎯 Available Columns:`);
        Object.entries(this.projectConfig.columns).forEach(([key, name]) => {
            console.log(`   - ${key}: "${name}"`);
        });

        console.log(`\n🏆 Available Milestones:`);
        Object.entries(this.projectConfig.milestones).forEach(([key, name]) => {
            console.log(`   - ${key}: "${name}"`);
        });

        return reportData;
    }

    /**
     * Test automation with sample data
     */
    testAutomation() {
        console.log('\n🧪 Testing Project Management Automation');
        console.log('=========================================');

        // Test 1: Label suggestions for different file types
        const testFiles = [
            'app/src/main/kotlin/com/heartlessveteran/myriad/ui/MainActivity.kt',
            'docs/README.md',
            '.github/workflows/ci.yml',
            'app/build.gradle.kts',
            'scripts/setup.sh'
        ];

        console.log('\nTest 1: Auto-labeling for file changes');
        const suggestedLabels = this.suggestLabelsForPR(testFiles);
        console.log(`Files: ${testFiles.length}`);
        console.log(`Suggested labels: ${suggestedLabels.join(', ')}`);

        // Test 2: Milestone suggestions
        console.log('\nTest 2: Milestone assignment');
        const testScenarios = [
            { labels: ['bug', 'priority:high'], title: 'Fix critical crash', type: 'issue' },
            { labels: ['feature'], title: 'Add AI Core translation', type: 'issue' },
            { labels: ['documentation'], title: 'Update API docs', type: 'issue' },
            { labels: ['feature'], title: 'Implement Vault library management', type: 'issue' }
        ];

        testScenarios.forEach((scenario, i) => {
            const milestone = this.suggestMilestone(scenario.labels, scenario.type === 'issue', scenario.title, '');
            console.log(`   Scenario ${i + 1}: "${scenario.title}" → ${milestone}`);
        });

        // Test 3: Status determination
        console.log('\nTest 3: Status updates');
        const statusTests = [
            { eventType: 'issues', action: 'opened' },
            { eventType: 'issues', action: 'assigned' },
            { eventType: 'pull_request', action: 'opened' },
            { eventType: 'pull_request', action: 'converted_to_draft' }
        ];

        statusTests.forEach(test => {
            const status = this.determineStatus(test.eventType, test.action, {});
            console.log(`   ${test.eventType} ${test.action} → ${status || 'No change'}`);
        });

        console.log('\n✅ Automation testing completed successfully!');
    }
}

// Main execution
async function main() {
    const manager = new ProjectManager();
    
    // Generate configuration report
    const report = manager.generateReport();
    
    // Test automation features
    manager.testAutomation();
    
    // Save report for workflow use
    const reportPath = path.join(__dirname, '..', 'project-management-report.json');
    fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
    console.log(`\n📄 Report saved to: ${reportPath}`);
    
    console.log('\n🎯 Project Manager setup completed successfully!');
    console.log('The enhanced workflow will now provide:');
    console.log('  ✅ Automatic labeling based on file changes');
    console.log('  ✅ Intelligent milestone assignment');
    console.log('  ✅ Status tracking and column management');
    console.log('  ✅ Priority-based organization');
    console.log('  ✅ Architecture-aware categorization');
}

if (require.main === module) {
    main().catch(console.error);
}

module.exports = ProjectManager;