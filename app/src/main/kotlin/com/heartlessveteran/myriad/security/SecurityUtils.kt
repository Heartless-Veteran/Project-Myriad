package com.heartlessveteran.myriad.security

import android.content.Context
import android.util.Log
import java.security.MessageDigest
import java.util.regex.Pattern

/**
 * Security utility class for Project Myriad
 * Provides validation, sanitization, and security checks for the application
 */
object SecurityUtils {
    
    private const val TAG = "SecurityUtils"
    
    // API Key validation patterns
    private val GEMINI_API_KEY_PATTERN = Pattern.compile("^AIza[0-9A-Za-z\\-_]{35}$")
    
    // Common security patterns to detect
    private val SECURITY_THREATS = listOf(
        "(?i)(password|passwd|pwd)\\s*[:=]\\s*[^\\s]+",
        "(?i)(api[_-]?key|apikey)\\s*[:=]\\s*[^\\s]+",
        "(?i)(secret|token)\\s*[:=]\\s*[^\\s]+"
    )
    
    /**
     * Validates if the provided API key has the correct format for Gemini API
     */
    fun validateGeminiApiKey(apiKey: String?): Boolean {
        if (apiKey.isNullOrBlank()) {
            Log.w(TAG, "API key is null or empty")
            return false
        }
        
        if (apiKey.length < 39) {
            Log.w(TAG, "API key is too short")
            return false
        }
        
        if (!GEMINI_API_KEY_PATTERN.matcher(apiKey).matches()) {
            Log.w(TAG, "API key format is invalid")
            return false
        }
        
        return true
    }
    
    /**
     * Sanitizes input strings to prevent injection attacks
     */
    fun sanitizeInput(input: String?): String {
        if (input.isNullOrBlank()) return ""
        
        return input
            .replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]"), "") // Remove control characters
            .replace(Regex("<script[^>]*>.*?</script>", RegexOption.IGNORE_CASE), "") // Remove script tags
            .replace(Regex("javascript:", RegexOption.IGNORE_CASE), "") // Remove javascript protocols
            .trim()
            .take(10000) // Limit length to prevent memory issues
    }
    
    /**
     * Checks if the current build is a debug build
     * This can be used to enable/disable certain security features
     */
    fun isDebugBuild(): Boolean {
        return try {
            val debugField = Class.forName("com.heartlessveteran.myriad.BuildConfig")
                .getDeclaredField("DEBUG")
            debugField.getBoolean(null)
        } catch (e: Exception) {
            Log.w(TAG, "Could not determine debug status", e)
            false
        }
    }
    
    /**
     * Generates a secure hash of the input string
     */
    fun generateSecureHash(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating hash", e)
            ""
        }
    }
    
    /**
     * Checks for potentially sensitive information in strings
     */
    fun containsSensitiveData(content: String): Boolean {
        return SECURITY_THREATS.any { pattern ->
            Pattern.compile(pattern).matcher(content).find()
        }
    }
    
    /**
     * Validates that the application is running in a secure environment
     */
    fun validateSecureEnvironment(context: Context): SecurityValidationResult {
        val issues = mutableListOf<String>()
        
        // Check if debugging is enabled
        if (isDebugBuild()) {
            issues.add("Debug build detected")
        }
        
        // Check for emulator (basic check)
        if (isRunningOnEmulator()) {
            issues.add("Running on emulator")
        }
        
        return SecurityValidationResult(
            isSecure = issues.isEmpty(),
            issues = issues
        )
    }
    
    /**
     * Basic emulator detection
     */
    private fun isRunningOnEmulator(): Boolean {
        return (android.os.Build.FINGERPRINT.startsWith("generic") ||
                android.os.Build.FINGERPRINT.lowercase().contains("vbox") ||
                android.os.Build.FINGERPRINT.lowercase().contains("test-keys") ||
                android.os.Build.MODEL.contains("google_sdk") ||
                android.os.Build.MODEL.contains("Emulator") ||
                android.os.Build.MODEL.contains("Android SDK built for x86") ||
                android.os.Build.MANUFACTURER.contains("Genymotion"))
    }
}

/**
 * Result of security validation checks
 */
data class SecurityValidationResult(
    val isSecure: Boolean,
    val issues: List<String>
)