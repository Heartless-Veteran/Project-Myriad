package com.heartlessveteran.myriad.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache entry with expiration time
 */
data class CacheEntry<T>(
    val data: T,
    val timestamp: Long,
    val ttl: Long = DEFAULT_TTL
) {
    fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > ttl
    
    companion object {
        const val DEFAULT_TTL = 5 * 60 * 1000L // 5 minutes
    }
}

/**
 * Cache configuration for different data types
 */
data class CacheConfig(
    val maxSize: Int = 1000,
    val ttl: Long = CacheEntry.DEFAULT_TTL,
    val enableMetrics: Boolean = true
)

/**
 * Cache metrics for monitoring
 */
data class CacheMetrics(
    val hits: Long = 0,
    val misses: Long = 0,
    val evictions: Long = 0,
    val size: Int = 0
) {
    val hitRate: Float = if (hits + misses > 0) hits.toFloat() / (hits + misses) else 0f
}

/**
 * Generic memory cache with LRU eviction and TTL support
 */
@Singleton
class MemoryCache @Inject constructor() {
    
    private val caches = ConcurrentHashMap<String, Cache<Any>>()
    private val mutex = Mutex()
    
    /**
     * Get or create a cache for a specific key type
     */
    suspend fun <T> getCache(cacheKey: String, config: CacheConfig = CacheConfig()): Cache<T> {
        return mutex.withLock {
            @Suppress("UNCHECKED_CAST")
            caches.getOrPut(cacheKey) { Cache<Any>(config) } as Cache<T>
        }
    }
    
    /**
     * Clear all caches
     */
    suspend fun clearAll() {
        mutex.withLock {
            caches.values.forEach { it.clear() }
            caches.clear()
        }
    }
    
    /**
     * Get metrics for a specific cache
     */
    suspend fun getMetrics(cacheKey: String): CacheMetrics? {
        return mutex.withLock {
            caches[cacheKey]?.getMetrics()
        }
    }
}

/**
 * Individual cache implementation with LRU eviction
 */
class Cache<T>(private val config: CacheConfig) {
    
    private val cache = LinkedHashMap<String, CacheEntry<T>>(config.maxSize + 1, 0.75f, true)
    private val mutex = Mutex()
    
    private var hits = 0L
    private var misses = 0L
    private var evictions = 0L
    
    /**
     * Get value from cache
     */
    suspend fun get(key: String): T? {
        return mutex.withLock {
            val entry = cache[key]
            
            if (entry == null || entry.isExpired()) {
                if (entry != null) {
                    cache.remove(key) // Remove expired entry
                }
                if (config.enableMetrics) misses++
                return null
            }
            
            if (config.enableMetrics) hits++
            entry.data
        }
    }
    
    /**
     * Put value in cache
     */
    suspend fun put(key: String, value: T, ttl: Long = config.ttl) {
        mutex.withLock {
            // Remove oldest entries if cache is full
            while (cache.size >= config.maxSize) {
                val oldest = cache.entries.first()
                cache.remove(oldest.key)
                if (config.enableMetrics) evictions++
            }
            
            cache[key] = CacheEntry(value, System.currentTimeMillis(), ttl)
        }
    }
    
    /**
     * Remove specific key from cache
     */
    suspend fun remove(key: String): T? {
        return mutex.withLock {
            cache.remove(key)?.data
        }
    }
    
    /**
     * Clear cache
     */
    suspend fun clear() {
        mutex.withLock {
            cache.clear()
            hits = 0
            misses = 0
            evictions = 0
        }
    }
    
    /**
     * Get cache metrics
     */
    suspend fun getMetrics(): CacheMetrics {
        return mutex.withLock {
            CacheMetrics(hits, misses, evictions, cache.size)
        }
    }
    
    /**
     * Get all keys (for debugging)
     */
    suspend fun getKeys(): Set<String> {
        return mutex.withLock {
            cache.keys.toSet()
        }
    }
    
    /**
     * Clean up expired entries
     */
    suspend fun cleanupExpired() {
        mutex.withLock {
            val iterator = cache.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.value.isExpired()) {
                    iterator.remove()
                }
            }
        }
    }
}

/**
 * Cache key constants for different data types
 */
object CacheKeys {
    const val MANGA = "manga"
    const val MANGA_CHAPTERS = "manga_chapters"
    const val SEARCH_RESULTS = "search_results"
    const val METADATA = "metadata"
    const val ONLINE_MANGA = "online_manga"
}

/**
 * Predefined cache configurations
 */
object CacheConfigs {
    val MANGA = CacheConfig(
        maxSize = 500,
        ttl = 10 * 60 * 1000L // 10 minutes
    )
    
    val SEARCH_RESULTS = CacheConfig(
        maxSize = 100,
        ttl = 5 * 60 * 1000L // 5 minutes
    )
    
    val METADATA = CacheConfig(
        maxSize = 1000,
        ttl = 60 * 60 * 1000L // 1 hour
    )
    
    val ONLINE_CONTENT = CacheConfig(
        maxSize = 200,
        ttl = 30 * 60 * 1000L // 30 minutes
    )
}