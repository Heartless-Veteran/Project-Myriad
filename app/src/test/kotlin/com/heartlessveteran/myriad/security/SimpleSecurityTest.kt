package com.heartlessveteran.myriad.security

import org.junit.Assert.*
import org.junit.Test

/**
 * Simple unit tests for SecurityUtils that don't require Android dependencies
 */
class SimpleSecurityTest {
    @Test
    fun `basic hash test`() {
        // This is a basic test that doesn't use Android dependencies
        val input = "test"
        val result = SecurityUtils.generateSecureHash(input)

        assertNotNull("Hash should not be null", result)
        assertTrue("Hash should not be empty", result.isNotEmpty())
    }

    @Test
    fun `hash consistency test`() {
        val input = "test"
        val hash1 = SecurityUtils.generateSecureHash(input)
        val hash2 = SecurityUtils.generateSecureHash(input)

        assertEquals("Same input should produce same hash", hash1, hash2)
    }

    @Test
    fun `different inputs produce different hashes`() {
        val hash1 = SecurityUtils.generateSecureHash("input1")
        val hash2 = SecurityUtils.generateSecureHash("input2")

        assertNotEquals("Different inputs should produce different hashes", hash1, hash2)
    }
}
