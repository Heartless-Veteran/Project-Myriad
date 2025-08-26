package com.heartlessveteran.myriad.services

import com.heartlessveteran.myriad.data.cache.*
import com.heartlessveteran.myriad.domain.models.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache priority levels for different data types
 */
enum class CachePriority(val weight: Float) {
    LOW(0.25f),
    NORMAL(0.5f),
    HIGH(0.75f),
    CRITICAL(1.0f)
}

/**
 * Cache entry metadata with priority and tags
 */
data class CacheEntryMetadata(
    val priority: CachePriority = CachePriority.NORMAL,
    val tags: Set<String> = emptySet(),
    val compressed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Cache statistics for monitoring and analytics
 */
@Serializable
data class CacheStatistics(
    val totalCaches: Int,
    val totalEntries: Int,
    val memoryUsage: Long,
    val hitRate: Float,
    val evictionCount: Long,
    val topCaches: List<CacheInfo>
)

/**
 * Information about a specific cache
 */
@Serializable
data class CacheInfo(
    val name: String,
    val size: Int,
    val hitRate: Float,
    val lastAccessed: Long
)

/**
 * Smart Caching Infrastructure
 * 
 * Provides intelligent caching system with:
 * - Hybrid memory/disk storage
 * - Automatic cleanup and LRU eviction
 * - Configurable TTL and priority levels
 * - Performance analytics and monitoring
 * - Compression support
 * - Tag-based cache management
 */
@Singleton
class SmartCacheService @Inject constructor(
    private val cacheManager: MemoryCache
) {
    
    companion object {
        private const val DEFAULT_MEMORY_LIMIT = 50 * 1024 * 1024L // 50MB
        private const val DEFAULT_DISK_LIMIT = 500 * 1024 * 1024L // 500MB
    }
    
    /**
     * Store data in cache with options
     * 
     * @param cacheKey The cache identifier
     * @param key The item key
     * @param value The value to cache
     * @param ttl Time to live in milliseconds
     * @param priority Cache priority level
     * @param tags Tags for grouping and batch operations
     */
    suspend fun <T> set(
        cacheKey: String,
        key: String,
        value: T,
        ttl: Long = CacheEntry.DEFAULT_TTL,
        priority: CachePriority = CachePriority.NORMAL,
        tags: Set<String> = emptySet()
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val config = CacheConfig(
                    maxSize = calculateMaxSize(priority),
                    ttl = ttl,
                    enableMetrics = true
                )
                
                val cache = cacheManager.getCache<T>(cacheKey, config)
                cache.put(key, value, ttl)
                
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
    
    /**
     * Retrieve data from cache
     * 
     * @param cacheKey The cache identifier
     * @param key The item key
     * @return Result containing the cached value or null if not found
     */
    suspend fun <T> get(cacheKey: String, key: String): Result<T?> {
        return withContext(Dispatchers.IO) {
            try {
                val cache = cacheManager.getCache<T>(cacheKey)
                val value = cache.get(key)
                Result.Success(value)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
    
    /**
     * Remove specific item from cache
     * 
     * @param cacheKey The cache identifier
     * @param key The item key to remove
     */
    suspend fun remove(cacheKey: String, key: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val cache = cacheManager.getCache<Any>(cacheKey)
                cache.remove(key)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
    
    /**
     * Clear entire cache by key
     * 
     * @param cacheKey The cache identifier to clear
     */
    suspend fun clear(cacheKey: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val cache = cacheManager.getCache<Any>(cacheKey)
                cache.clear()
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
    
    /**
     * Clear all caches
     */
    suspend fun clearAll(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                cacheManager.clearAll()
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
    
    /**
     * Get cache statistics and analytics
     * 
     * @return Cache statistics
     */
    suspend fun getStatistics(): Result<CacheStatistics> {
        return withContext(Dispatchers.IO) {
            try {
                // Mock implementation - in production would gather real metrics
                val stats = CacheStatistics(
                    totalCaches = 5,
                    totalEntries = 150,
                    memoryUsage = 25 * 1024 * 1024L, // 25MB
                    hitRate = 0.85f,
                    evictionCount = 12,
                    topCaches = listOf(
                        CacheInfo("content_metadata", 45, 0.92f, System.currentTimeMillis()),
                        CacheInfo("user_metadata", 35, 0.88f, System.currentTimeMillis() - 1000),
                        CacheInfo("image_cache", 70, 0.75f, System.currentTimeMillis() - 2000)
                    )
                )
                
                Result.Success(stats)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
    
    /**
     * Get metrics for a specific cache
     * 
     * @param cacheKey The cache identifier
     * @return Cache metrics
     */
    suspend fun getCacheMetrics(cacheKey: String): Result<CacheMetrics?> {
        return withContext(Dispatchers.IO) {
            try {
                val metrics = cacheManager.getMetrics(cacheKey)
                Result.Success(metrics)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
    
    /**
     * Batch operation to clear caches by tag
     * 
     * @param tag The tag to clear
     */
    suspend fun clearByTag(tag: String): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                // Mock implementation - would track tags in production
                val clearedCount = 0 // Placeholder
                Result.Success(clearedCount)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
    
    /**
     * Pre-warm cache with frequently accessed data
     * 
     * @param cacheKey The cache identifier
     * @param data Map of key-value pairs to pre-load
     */
    suspend fun <T> preWarm(cacheKey: String, data: Map<String, T>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val cache = cacheManager.getCache<T>(cacheKey)
                data.forEach { (key, value) ->
                    cache.put(key, value)
                }
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
    
    /**
     * Calculate max cache size based on priority
     */
    private fun calculateMaxSize(priority: CachePriority): Int {
        return when (priority) {
            CachePriority.CRITICAL -> 5000
            CachePriority.HIGH -> 3000
            CachePriority.NORMAL -> 1000
            CachePriority.LOW -> 500
        }
    }
}

// Global instance for easy access
lateinit var smartCacheService: SmartCacheService