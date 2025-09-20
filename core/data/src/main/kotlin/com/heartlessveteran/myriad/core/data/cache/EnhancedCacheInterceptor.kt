package com.heartlessveteran.myriad.core.data.cache

import okhttp3.*
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Enhanced HTTP caching interceptor for Project Myriad.
 * Implements aggressive caching strategy for network efficiency and offline capabilities.
 */
class EnhancedCacheInterceptor(
    private val cacheDir: File,
    private val maxCacheSize: Long = 50L * 1024 * 1024 // 50MB
) {
    
    private val cache = Cache(cacheDir, maxCacheSize)
    
    /**
     * OkHttp client with enhanced caching configuration
     */
    fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(onlineInterceptor())
            .addNetworkInterceptor(offlineInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Online caching strategy - cache responses for short duration
     * to prevent unnecessary network requests on rapid navigation
     */
    private fun onlineInterceptor(): Interceptor {
        return Interceptor { chain ->
            val response = chain.proceed(chain.request())
            
            // Cache popular manga lists and search results for 5 minutes
            val maxAge = when {
                chain.request().url.encodedPath.contains("/manga") -> 300 // 5 minutes
                chain.request().url.encodedPath.contains("/search") -> 180 // 3 minutes
                else -> 60 // 1 minute for other requests
            }
            
            response.newBuilder()
                .header("Cache-Control", "public, max-age=$maxAge")
                .removeHeader("Pragma")
                .build()
        }
    }
    
    /**
     * Offline caching strategy - serve cached content when offline
     * Enables offline-first experience for previously loaded content
     */
    private fun offlineInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            
            // Serve cached content when offline for up to 7 days
            val modifiedRequest = request.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=${60 * 60 * 24 * 7}")
                .build()
            
            try {
                chain.proceed(request)
            } catch (e: Exception) {
                // If network fails, try to serve from cache
                chain.proceed(modifiedRequest)
            }
        }
    }
    
    /**
     * Clear cache manually when needed
     */
    suspend fun clearCache() {
        cache.evictAll()
    }
    
    /**
     * Get cache statistics for monitoring
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            hitCount = cache.hitCount(),
            missCount = cache.requestCount() - cache.hitCount(),
            cacheSize = cache.size(),
            maxCacheSize = cache.maxSize()
        )
    }
}

/**
 * Cache statistics for monitoring cache performance
 */
data class CacheStats(
    val hitCount: Int,
    val missCount: Int,
    val cacheSize: Long,
    val maxCacheSize: Long
) {
    val hitRate: Float = if (hitCount + missCount > 0) {
        hitCount.toFloat() / (hitCount + missCount)
    } else 0f
}