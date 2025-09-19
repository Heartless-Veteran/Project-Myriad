package com.heartlessveteran.myriad.data.cache

import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MemoryCache
 */
class MemoryCacheTest {
    private lateinit var memoryCache: MemoryCache

    @Before
    fun setUp() {
        memoryCache = MemoryCache()
    }

    @Test
    fun `should create and retrieve cache`() =
        runTest {
            val cache = memoryCache.getCache<String>("test_cache")

            assertNotNull(cache)
        }

    @Test
    fun `should return same cache instance for same key`() =
        runTest {
            val cache1 = memoryCache.getCache<String>("test_cache")
            val cache2 = memoryCache.getCache<String>("test_cache")

            assertSame(cache1, cache2)
        }

    @Test
    fun `should clear all caches`() =
        runTest {
            val cache = memoryCache.getCache<String>("test_cache")
            cache.put("key1", "value1")

            memoryCache.clearAll()

            val newCache = memoryCache.getCache<String>("test_cache")
            assertNull(newCache.get("key1"))
        }
}

/**
 * Unit tests for individual Cache implementation
 */
class CacheTest {
    private lateinit var cache: Cache<String>

    @Before
    fun setUp() {
        cache = Cache(CacheConfig(maxSize = 3, ttl = 1000L))
    }

    @Test
    fun `should store and retrieve values`() =
        runTest {
            cache.put("key1", "value1")

            val retrieved = cache.get("key1")

            assertEquals("value1", retrieved)
        }

    @Test
    fun `should return null for non-existent key`() =
        runTest {
            val retrieved = cache.get("non-existent")

            assertNull(retrieved)
        }

    @Test
    fun `should return null for expired entries`() =
        runTest {
            cache.put("key1", "value1", ttl = 1L) // 1ms TTL

            advanceTimeBy(10) // Simulate time passing for expiration

            val retrieved = cache.get("key1")
            assertNull(retrieved)
        }

    @Test
    fun `should evict oldest entries when cache is full`() =
        runTest {
            cache.put("key1", "value1")
            cache.put("key2", "value2")
            cache.put("key3", "value3")
            cache.put("key4", "value4") // This should evict key1

            assertNull(cache.get("key1"))
            assertEquals("value2", cache.get("key2"))
            assertEquals("value3", cache.get("key3"))
            assertEquals("value4", cache.get("key4"))
        }

    @Test
    fun `should update LRU order on access`() =
        runTest {
            cache.put("key1", "value1")
            cache.put("key2", "value2")
            cache.put("key3", "value3")

            // Access key1 to make it most recently used
            cache.get("key1")

            // Add key4, which should evict key2 (least recently used)
            cache.put("key4", "value4")

            assertEquals("value1", cache.get("key1"))
            assertNull(cache.get("key2"))
            assertEquals("value3", cache.get("key3"))
            assertEquals("value4", cache.get("key4"))
        }

    @Test
    fun `should remove specific key`() =
        runTest {
            cache.put("key1", "value1")
            cache.put("key2", "value2")

            val removed = cache.remove("key1")

            assertEquals("value1", removed)
            assertNull(cache.get("key1"))
            assertEquals("value2", cache.get("key2"))
        }

    @Test
    fun `should clear all entries`() =
        runTest {
            cache.put("key1", "value1")
            cache.put("key2", "value2")

            cache.clear()

            assertNull(cache.get("key1"))
            assertNull(cache.get("key2"))
        }

    @Test
    fun `should track cache metrics`() =
        runTest {
            val cacheWithMetrics = Cache<String>(CacheConfig(enableMetrics = true))

            // Generate some hits and misses
            cacheWithMetrics.put("key1", "value1")
            cacheWithMetrics.get("key1") // Hit
            cacheWithMetrics.get("key1") // Hit
            cacheWithMetrics.get("key2") // Miss
            cacheWithMetrics.get("key3") // Miss

            val metrics = cacheWithMetrics.getMetrics()

            assertEquals(2, metrics.hits)
            assertEquals(2, metrics.misses)
            assertEquals(0.5f, metrics.hitRate)
            assertEquals(1, metrics.size)
        }

    @Test
    fun `should track evictions in metrics`() =
        runTest {
            val smallCache = Cache<String>(CacheConfig(maxSize = 2, enableMetrics = true))

            smallCache.put("key1", "value1")
            smallCache.put("key2", "value2")
            smallCache.put("key3", "value3") // Should cause eviction

            val metrics = smallCache.getMetrics()

            assertEquals(1, metrics.evictions)
            assertEquals(2, metrics.size)
        }

    @Test
    fun `should return all keys`() =
        runTest {
            cache.put("key1", "value1")
            cache.put("key2", "value2")

            val keys = cache.getKeys()

            assertEquals(2, keys.size)
            assertTrue(keys.contains("key1"))
            assertTrue(keys.contains("key2"))
        }

    @Test
    fun `should cleanup expired entries`() =
        runTest {
            cache.put("key1", "value1", ttl = 1L) // Will expire
            cache.put("key2", "value2", ttl = 10000L) // Won't expire

            advanceTimeBy(10) // Simulate time passing for first entry to expire

            cache.cleanupExpired()

            val keys = cache.getKeys()
            assertEquals(1, keys.size)
            assertTrue(keys.contains("key2"))
            assertFalse(keys.contains("key1"))
        }

    @Test
    fun `CacheEntry should detect expiration correctly`() {
        val entry = CacheEntry("data", System.currentTimeMillis() - 2000L, 1000L)

        assertTrue(entry.isExpired())
    }

    @Test
    fun `CacheEntry should not be expired when within TTL`() {
        val entry = CacheEntry("data", System.currentTimeMillis(), 10000L)

        assertFalse(entry.isExpired())
    }

    @Test
    fun `CacheMetrics should calculate hit rate correctly`() {
        val metrics = CacheMetrics(hits = 8, misses = 2, evictions = 0, size = 5)

        assertEquals(0.8f, metrics.hitRate)
    }

    @Test
    fun `CacheMetrics should handle zero hits and misses`() {
        val metrics = CacheMetrics(hits = 0, misses = 0, evictions = 0, size = 0)

        assertEquals(0f, metrics.hitRate)
    }
}
