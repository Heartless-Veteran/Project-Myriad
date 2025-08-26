#!/usr/bin/env node

/**
 * Gemini API Key Validation Script
 * 
 * This script helps validate your GEMINI_API_KEY configuration locally
 * before committing to the repository workflow.
 * 
 * Usage:
 *   GEMINI_API_KEY=your_api_key_here node scripts/validate-gemini-api.js
 */

const { execFileSync } = require('child_process');

console.log('🤖 Gemini API Key Validation Script');
console.log('=====================================\n');

// Check if API key is provided
const apiKey = process.env.GEMINI_API_KEY;

if (!apiKey) {
  console.log('❌ **Error**: GEMINI_API_KEY environment variable is not set.\n');
  console.log('📝 **How to use this script**:');
  console.log('   GEMINI_API_KEY=your_api_key_here node scripts/validate-gemini-api.js\n');
  console.log('🔑 **How to get a Gemini API Key**:');
  console.log('   1. Visit https://makersuite.google.com/app/apikey');
  console.log('   2. Sign in with your Google account');
  console.log('   3. Click "Create API Key"');
  console.log('   4. Copy the generated key\n');
  process.exit(1);
}

// Validate API key format
console.log('🔍 Validating API key format...');
if (apiKey.length < 30) {
  console.log('⚠️  **Warning**: API key appears to be too short. Expected length is typically 40+ characters.');
  console.log('   Please verify your API key from Google AI Studio.\n');
}

// Test API connectivity
console.log('🌐 Testing API connectivity...');

const testPayload = JSON.stringify({
  "contents": [{
    "parts": [{
      "text": "Hello, this is a test message to validate the API key."
    }]
  }]
});

try {
  const curlArgs = [
    '-s',
    '-w', '%{http_code}',
    'https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent',
    '-H', 'Content-Type: application/json',
    '-H', `x-api-key: ${apiKey}`,
    '-d', testPayload,
    '-o', '/tmp/gemini-test-response.json'
  ];

  const httpCode = execFileSync('curl', curlArgs, { encoding: 'utf8' }).trim();
  
    '-o', '/tmp/gemini-test-response.json'
  ];
  const httpCode = execFileSync('curl', curlArgs, { encoding: 'utf8' }).trim();
  
  console.log('📡 HTTP Response Code:', httpCode);
  
  if (httpCode === '200') {
    console.log('✅ **Success**: Your GEMINI_API_KEY is valid and working!\n');
    
    // Try to read and display a sample of the response
    try {
      const response = require('fs').readFileSync('/tmp/gemini-test-response.json', 'utf8');
      const responseJson = JSON.parse(response);
      
      if (responseJson.candidates && responseJson.candidates[0]) {
        const responseText = responseJson.candidates[0].content.parts[0].text;
        console.log('📝 **Sample API Response**:');
        console.log('  ', responseText.substring(0, 100) + (responseText.length > 100 ? '...' : ''));
      }
    } catch (parseError) {
      console.log('📝 **Note**: Response received but could not parse content (this is normal for validation).');
    }
    
    console.log('\n🎉 **Your API key is ready for use in GitHub Actions!**');
    console.log('   Next steps:');
    console.log('   1. Go to your repository Settings > Secrets and variables > Actions');
    console.log('   2. Click "New repository secret"');
    console.log('   3. Name: GEMINI_API_KEY');
    console.log('   4. Value: Your API key');
    
  } else if (httpCode === '403') {
    console.log('❌ **Authentication Failed (HTTP 403)**\n');
    console.log('🔧 **Troubleshooting**:');
    console.log('   - Your API key is invalid, expired, or lacks proper permissions');
    console.log('   - Try regenerating your API key at https://makersuite.google.com/app/apikey');
    console.log('   - Ensure you copied the entire API key without extra spaces');
    
  } else if (httpCode === '429') {
    console.log('⏱️  **API Quota Exceeded (HTTP 429)**\n');
    console.log('🔧 **Solutions**:');
    console.log('   - You have reached your Gemini API usage limits');
    console.log('   - Wait for quota reset (usually 24 hours)');
    console.log('   - Check your quota at Google AI Studio');
    console.log('   - Consider upgrading your Google Cloud billing if needed');
    
  } else {
    console.log(`❌ **API Error (HTTP ${httpCode})**\n`);
    
    // Try to read error details
    try {
      const errorResponse = require('fs').readFileSync('/tmp/gemini-test-response.json', 'utf8');
      console.log('📄 **Error Response**:');
      console.log(JSON.stringify(JSON.parse(errorResponse), null, 2));
    } catch (readError) {
      console.log('📄 **Error**: Could not read detailed error response');
    }
    
    console.log('\n🔧 **General Troubleshooting**:');
    console.log('   1. Check Google Cloud Status: https://status.cloud.google.com/');
    console.log('   2. Verify your API key at https://makersuite.google.com/app/apikey');
    console.log('   3. Ensure your Google account has access to Gemini API');
  }
  
} catch (error) {
  console.log('❌ **Network Error**: Failed to connect to Gemini API\n');
  console.log('🔧 **Possible Causes**:');
  console.log('   - Internet connectivity issues');
  console.log('   - API endpoint unavailable');
  console.log('   - curl command not available');
  console.log('\n📝 **Technical Details**:', error.message);
  process.exit(1);
  require('fs').unlinkSync(tempResponsePath);

// Clean up temporary files
try {
  require('fs').unlinkSync('/tmp/gemini-test-response.json');
} catch (cleanupError) {
  // Ignore cleanup errors
}

console.log('\n---\n📚 For more help, see the README.md file in the repository.');