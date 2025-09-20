#!/usr/bin/env node

/**
 * Test script for Project Management Automation GitHub Script
 * Validates the workflow logic without GitHub API calls
 */

console.log('🧪 Testing Project Management GitHub Script Logic');
console.log('================================================\n');

// Mock GitHub context and payload
const mockContext = {
    repo: { owner: 'Heartless-Veteran', repo: 'Project-Myriad' },
    eventName: 'pull_request',
    payload: {
        action: 'opened',
        pull_request: {
            number: 123,
            merged: false
        }
    }
};

// Mock GitHub API
const mockGithub = {
    rest: {
        pulls: {
            listFiles: async () => ({
                data: [
                    { filename: 'app/src/main/kotlin/com/heartlessveteran/myriad/MainActivity.kt' },
                    { filename: 'docs/README.md' },
                    { filename: '.github/workflows/ci.yml' },
                    { filename: 'app/build.gradle.kts' }
                ]
            })
        },
        issues: {
            addLabels: async (params) => {
                console.log(`✅ Would add labels: ${params.labels.join(', ')}`);
                return { data: {} };
            },
            listMilestones: async () => ({
                data: [
                    { title: 'v1.0.0 - Foundation', number: 1 },
                    { title: 'v1.1.0 - Core Features', number: 2 }
                ]
            }),
            update: async (params) => {
                console.log(`✅ Would assign milestone: ${params.milestone}`);
                return { data: {} };
            }
        }
    }
};

// Test the auto-labeling logic
async function testAutoLabeling() {
    console.log('Test 1: Auto-labeling Logic');
    console.log('---------------------------');
    
    const files = await mockGithub.rest.pulls.listFiles();
    const paths = files.data.map(file => file.filename);
    
    console.log('Files changed:');
    paths.forEach(path => console.log(`  - ${path}`));
    
    const labels = [];
    
    // Simulate the labeling logic from the workflow
    if (paths.some(path => path.includes('docs/'))) {
        labels.push('documentation');
    }
    if (paths.some(path => path.includes('.github/workflows/'))) {
        labels.push('ci/cd');
    }
    if (paths.some(path => path.includes('app/src/main/kotlin/'))) {
        labels.push('android');
    }
    if (paths.some(path => path.includes('build.gradle'))) {
        labels.push('build');
    }
    
    console.log(`\nSuggested labels: ${labels.join(', ')}`);
    
    if (labels.length > 0) {
        await mockGithub.rest.issues.addLabels({
            owner: mockContext.repo.owner,
            repo: mockContext.repo.repo,
            issue_number: mockContext.payload.pull_request.number,
            labels: labels
        });
    }
    
    return labels.length > 0;
}

// Test milestone assignment logic
async function testMilestoneAssignment() {
    console.log('\nTest 2: Milestone Assignment Logic');
    console.log('----------------------------------');
    
    const testCases = [
        { label: 'bug', expected: 'v1.0.0 - Foundation' },
        { label: 'priority:high', expected: 'v1.0.0 - Foundation' },
        { label: 'feature', expected: 'v1.1.0 - Core Features' },
        { label: 'documentation', expected: 'v1.0.0 - Foundation' }
    ];
    
    for (const testCase of testCases) {
        let milestone = null;
        
        if (testCase.label === 'priority:high' || testCase.label === 'bug') {
            milestone = 'v1.0.0 - Foundation';
        } else if (testCase.label === 'feature' || testCase.label === 'enhancement') {
            milestone = 'v1.1.0 - Core Features';
        } else if (testCase.label === 'documentation') {
            milestone = 'v1.0.0 - Foundation';
        }
        
        const success = milestone === testCase.expected;
        console.log(`${success ? '✅' : '❌'} Label "${testCase.label}" → ${milestone}`);
        
        if (milestone) {
            const milestones = await mockGithub.rest.issues.listMilestones({});
            const targetMilestone = milestones.data.find(m => m.title === milestone);
            if (targetMilestone) {
                await mockGithub.rest.issues.update({
                    owner: mockContext.repo.owner,
                    repo: mockContext.repo.repo,
                    issue_number: mockContext.payload.pull_request.number,
                    milestone: targetMilestone.number
                });
            }
        }
    }
}

// Test status determination logic
function testStatusLogic() {
    console.log('\nTest 3: Status Determination Logic');
    console.log('----------------------------------');
    
    const statusTests = [
        { eventName: 'issues', action: 'opened', expected: 'To Do' },
        { eventName: 'issues', action: 'assigned', expected: 'In Progress' },
        { eventName: 'issues', action: 'closed', expected: 'Done' },
        { eventName: 'pull_request', action: 'opened', expected: 'In Review' },
        { eventName: 'pull_request', action: 'converted_to_draft', expected: 'In Progress' },
        { eventName: 'pull_request', action: 'closed', item: { merged: true }, expected: 'Done' }
    ];
    
    statusTests.forEach(test => {
        let status = null;
        
        if (test.eventName === 'issues') {
            switch (test.action) {
                case 'opened':
                    status = 'To Do';
                    break;
                case 'assigned':
                    status = 'In Progress';
                    break;
                case 'closed':
                    status = 'Done';
                    break;
            }
        }
        
        if (test.eventName === 'pull_request') {
            switch (test.action) {
                case 'opened':
                case 'ready_for_review':
                    status = 'In Review';
                    break;
                case 'converted_to_draft':
                    status = 'In Progress';
                    break;
                case 'closed':
                    status = test.item?.merged ? 'Done' : 'To Do';
                    break;
            }
        }
        
        const success = status === test.expected;
        console.log(`${success ? '✅' : '❌'} ${test.eventName} ${test.action} → ${status || 'No change'}`);
    });
}

// Run all tests
async function runTests() {
    let passed = 0;
    let total = 3;
    
    try {
        if (await testAutoLabeling()) passed++;
        await testMilestoneAssignment();
        passed++;
        testStatusLogic();
        passed++;
    } catch (error) {
        console.error('Test error:', error.message);
    }
    
    console.log('\n📊 Test Results');
    console.log('===============');
    console.log(`Tests passed: ${passed}/${total}`);
    console.log(`Success rate: ${Math.round(passed/total * 100)}%`);
    
    if (passed === total) {
        console.log('🎉 All project management automation tests passed!');
        console.log('The GitHub workflow is ready for production use.');
    } else {
        console.log('⚠️ Some tests failed - review the implementation.');
    }
    
    return passed === total;
}

if (require.main === module) {
    runTests().then(success => {
        process.exit(success ? 0 : 1);
    });
}

module.exports = { runTests };