package com.heartlessveteran.myriad.security

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SecurityUtils
 * Validates security utilities functionality
 */
class SecurityUtilsTest {
    @Test
    fun `validateGeminiApiKey should return false for null or empty keys`() {
        assertFalse("Null key should be invalid", SecurityUtils.validateGeminiApiKey(null))
        assertFalse("Empty key should be invalid", SecurityUtils.validateGeminiApiKey(""))
        assertFalse("Blank key should be invalid", SecurityUtils.validateGeminiApiKey("   "))
    }

    @Test
    fun `validateGeminiApiKey should return false for short keys`() {
        assertFalse("Short key should be invalid", SecurityUtils.validateGeminiApiKey("AIza123"))
    }

    @Test
    fun `validateGeminiApiKey should return false for invalid format`() {
        assertFalse(
            "Wrong prefix should be invalid",
            SecurityUtils.validateGeminiApiKey("WRONG_PREFIX_1234567890123456789012345678901234"),
        )
        assertFalse(
            "No prefix should be invalid",
            SecurityUtils.validateGeminiApiKey("1234567890123456789012345678901234567890"),
        )
    }

    @Test
    fun `validateGeminiApiKey should return true for valid format`() {
        val validKey = "AIza1234567890123456789012345678901234567"
        assertTrue("Valid key should pass validation", SecurityUtils.validateGeminiApiKey(validKey))
    }

    @Test
    fun `sanitizeInput should handle null and empty inputs`() {
        assertEquals("", SecurityUtils.sanitizeInput(null))
        assertEquals("", SecurityUtils.sanitizeInput(""))
        assertEquals("", SecurityUtils.sanitizeInput("   "))
    }

    @Test
    fun `sanitizeInput should remove control characters`() {
        val input = "Hello\u0000World\u0008Test"
        val expected = "HelloWorldTest"
        assertEquals(expected, SecurityUtils.sanitizeInput(input))
    }

    @Test
    fun `sanitizeInput should remove script tags`() {
        val input = "Hello <script>alert('xss')</script> World"
        val expected = "Hello  World"
        assertEquals(expected, SecurityUtils.sanitizeInput(input))
    }

    @Test
    fun `sanitizeInput should remove javascript protocols`() {
        val input = "javascript:alert('xss')"
        val expected = "alert('xss')"
        assertEquals(expected, SecurityUtils.sanitizeInput(input))
    }

    @Test
    fun `sanitizeInput should limit input length`() {
        val longInput = "a".repeat(20000)
        val result = SecurityUtils.sanitizeInput(longInput)
        assertTrue("Input should be limited to 10000 characters", result.length <= 10000)
    }

    @Test
    fun `generateSecureHash should produce consistent hashes`() {
        val input = "test input"
        val hash1 = SecurityUtils.generateSecureHash(input)
        val hash2 = SecurityUtils.generateSecureHash(input)

        assertNotEquals("Hash should not be empty", "", hash1)
        assertEquals("Hash should be consistent", hash1, hash2)
        assertEquals("Hash should be 64 characters (SHA-256)", 64, hash1.length)
    }

    @Test
    fun `generateSecureHash should produce different hashes for different inputs`() {
        val hash1 = SecurityUtils.generateSecureHash("input1")
        val hash2 = SecurityUtils.generateSecureHash("input2")

        assertNotEquals("Different inputs should produce different hashes", hash1, hash2)
    }

    @Test
    fun `containsSensitiveData should detect passwords`() {
        assertTrue("Should detect password", SecurityUtils.containsSensitiveData("password=secret123"))
        assertTrue("Should detect passwd", SecurityUtils.containsSensitiveData("passwd:mypassword"))
        assertTrue("Should detect pwd", SecurityUtils.containsSensitiveData("pwd = test123"))
    }

    @Test
    fun `containsSensitiveData should detect API keys`() {
        assertTrue("Should detect api_key", SecurityUtils.containsSensitiveData("api_key=AIza1234567890"))
        assertTrue("Should detect apikey", SecurityUtils.containsSensitiveData("apikey:secret_key_123"))
        assertTrue("Should detect api-key", SecurityUtils.containsSensitiveData("api-key = myapikey"))
    }

    @Test
    fun `containsSensitiveData should detect secrets and tokens`() {
        assertTrue("Should detect secret", SecurityUtils.containsSensitiveData("secret=mysecret"))
        assertTrue("Should detect token", SecurityUtils.containsSensitiveData("token:bearer_token_123"))
    }

    @Test
    fun `containsSensitiveData should return false for safe content`() {
        assertFalse("Should not detect in safe content", SecurityUtils.containsSensitiveData("This is safe content"))
        assertFalse("Should not detect in code comments", SecurityUtils.containsSensitiveData("// This is a comment"))
    }
}
