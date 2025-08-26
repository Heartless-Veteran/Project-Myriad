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
    /**
 * Whether this entry's time-to-live has elapsed.
 *
 * @return true if the current system time is past the entry's timestamp plus its TTL; false otherwise.
 */
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
     * Retrieve an existing named cache or create a new one with the given configuration.
     *
     * If a cache for `cacheKey` already exists it is returned; otherwise a new Cache is created
     * using `config` and stored. This function is suspendable and performs the operation under a mutex
     * so it is safe for concurrent callers.
     *
     * Note: the cache is stored without runtime type information — callers are responsible for
     * using a consistent `T` for a given `cacheKey`; the implementation performs an unchecked cast.
     *
     * @param cacheKey Unique key identifying the cache.
     * @param config Configuration to use when creating a new cache (ignored if the cache already exists).
     * @return The Cache instance associated with `cacheKey`, typed as `Cache<T>`.
     */
    suspend fun <T> getCache(cacheKey: String, config: CacheConfig = CacheConfig()): Cache<T> {
        return mutex.withLock {
            @Suppress("UNCHECKED_CAST")
            caches.getOrPut(cacheKey) { Cache<Any>(config) } as Cache<T>
        }
    }
    
    /**
     * Clears every managed cache and removes them from the global registry.
     *
     * This is a suspending operation that synchronizes access internally; it calls `clear()` on each
     * per-type cache and then removes all cache entries from the manager.
     */
    suspend fun clearAll() {
        mutex.withLock {
            caches.values.forEach { it.clear() }
            caches.clear()
        }
    }
    
    /**
     * Return a snapshot of runtime metrics for the named cache.
     *
     * Looks up the cache identified by `cacheKey` and returns its current CacheMetrics, or null if no cache exists for that key.
     *
     * @param cacheKey The identifier of the cache whose metrics are requested.
     * @return A snapshot of the cache's metrics, or `null` if the cache is not present.
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
    
    private val hits = java.util.concurrent.atomic.AtomicLong(0L)
    private val misses = java.util.concurrent.atomic.AtomicLong(0L)
    private val evictions = java.util.concurrent.atomic.AtomicLong(0L)
    
    /**
     * Retrieves the value for the given cache key if present and not expired.
     *
     * If an entry is expired it is removed and null is returned. When metrics are enabled,
     * a hit is recorded for a successful lookup and a miss is recorded for a missing or expired entry.
     * This operation is coroutine-safe.
     *
     * @param key The cache key to look up.
     * @return The cached value, or null if not present or expired.
     */
    suspend fun get(key: String): T? {
        return mutex.withLock {
            val entry = cache[key]
            
            if (entry == null || entry.isExpired()) {
                if (entry != null) {
                    cache.remove(key) // Remove expired entry
                }
                if (config.enableMetrics) misses.incrementAndGet()
                return null
            }
            
            if (config.enableMetrics) hits.incrementAndGet()
            entry.data
        }
    }
    
    /**
     * Inserts or replaces an entry in the cache.
     *
     * The entry is stored with the current timestamp and the provided TTL. If the cache is at or above
     * its configured max size, oldest entries are evicted (LRU order) until there is space; each eviction
     * increments the eviction counter when metrics are enabled. Operation is coroutine-safe.
     *
     * @param ttl Time-to-live for this entry in milliseconds. Defaults to the cache's configured TTL.
     */
    suspend fun put(key: String, value: T, ttl: Long = config.ttl) {
        mutex.withLock {
            // Remove oldest entries if cache is full
            while (cache.size >= config.maxSize) {
                val oldest = cache.entries.first()
                cache.remove(oldest.key)
                if (config.enableMetrics) evictions.incrementAndGet()
            }
            
            cache[key] = CacheEntry(value, System.currentTimeMillis(), ttl)
        }
    }
    
    /**
     * Removes and returns the value associated with the given key from the cache.
     *
     * This is a suspend function and performs the removal under the cache mutex.
     *
     * @param key The cache key to remove.
     * @return The removed value, or `null` if the key was not present.
     */
    suspend fun remove(key: String): T? {
        return mutex.withLock {
            cache.remove(key)?.data
        }
    }
    
    /**
     * Removes all entries from this cache and resets its metrics.
     *
     * This suspending function acquires the cache's mutex, clears the internal store,
     * and resets hit, miss, and eviction counters to zero.
     */
    suspend fun clear() {
        mutex.withLock {
            cache.clear()
            hits.set(0)
            misses.set(0)
            evictions.set(0)
        }
    }
    
    /**
     * Returns a snapshot of the cache's current metrics.
     *
     * The returned CacheMetrics contains hits, misses, evictions, current size, and the computed `hitRate`.
     *
     * @return a snapshot of current cache metrics
     */
    suspend fun getMetrics(): CacheMetrics {
        return mutex.withLock {
            CacheMetrics(hits.get(), misses.get(), evictions.get(), cache.size)
        }
    }
    
    /**
     * Return a thread-safe snapshot of all keys currently stored in this cache.
     *
     * The returned set is a copy and reflects the cache contents at the time of the call.
     *
     * @return A snapshot Set containing all cache keys.
     */
    suspend fun getKeys(): Set<String> {
        return mutex.withLock {
            cache.keys.toSet()
        }
    }
    
    /**
     * Remove all expired entries from this cache.
     *
     * Scans the internal map under the cache's mutex and deletes any entries whose
     * CacheEntry.isExpired() returns true. This mutates the cache's contents and
     * reduces its reported size.
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