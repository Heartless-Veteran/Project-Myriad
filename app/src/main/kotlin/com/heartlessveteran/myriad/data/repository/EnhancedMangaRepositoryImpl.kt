package com.heartlessveteran.myriad.data.repository

import com.heartlessveteran.myriad.data.cache.CacheConfigs
import com.heartlessveteran.myriad.data.cache.CacheKeys
import com.heartlessveteran.myriad.data.cache.MemoryCache
import com.heartlessveteran.myriad.data.database.dao.MangaDao
import com.heartlessveteran.myriad.data.validation.MangaValidator
import com.heartlessveteran.myriad.data.validation.ValidationException
import com.heartlessveteran.myriad.data.validation.isValid
import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.entities.MangaStatus
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.domain.repository.MangaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

/**
 * Enhanced implementation of MangaRepository with caching, validation, and retry logic
 */
@Singleton
class EnhancedMangaRepositoryImpl @Inject constructor(
    private val mangaDao: MangaDao,
    private val memoryCache: MemoryCache,
    private val validator: MangaValidator = MangaValidator()
    // TODO: Add network API service when implemented
    // TODO: Add file manager service when implemented
) : MangaRepository {
    
    companion object {
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1000L
    }
    
    // Cache instances
    private suspend fun getMangaCache() = memoryCache.getCache<List<Manga>>(CacheKeys.MANGA, CacheConfigs.MANGA)
    private suspend fun getSearchCache() = memoryCache.getCache<List<Manga>>(CacheKeys.SEARCH_RESULTS, CacheConfigs.SEARCH_RESULTS)
    private suspend fun getSingleMangaCache() = memoryCache.getCache<Manga>(CacheKeys.MANGA, CacheConfigs.MANGA)
    
    override fun getAllManga(): Flow<List<Manga>> = 
        mangaDao.getAllManga().map { mangaList ->
            // Cache the result
            getMangaCache().put("all_manga", mangaList)
            mangaList
        }.catch { exception ->
            // Try to get from cache if database fails
            val cached = getMangaCache().get("all_manga")
            if (cached != null) {
                emit(cached)
            } else {
                throw exception
            }
        }
    
    override fun getLibraryManga(): Flow<List<Manga>> = 
        mangaDao.getLibraryManga().map { mangaList ->
            // Cache the result
            getMangaCache().put("library_manga", mangaList)
            mangaList
        }.catch { exception ->
            // Try to get from cache if database fails
            val cached = getMangaCache().get("library_manga")
            if (cached != null) {
                emit(cached)
            } else {
                throw exception
            }
        }
    
    override fun getFavoriteManga(): Flow<List<Manga>> = 
        mangaDao.getFavoriteManga().map { mangaList ->
            // Cache the result
            getMangaCache().put("favorite_manga", mangaList)
            mangaList
        }.catch { exception ->
            // Try to get from cache if database fails
            val cached = getMangaCache().get("favorite_manga")
            if (cached != null) {
                emit(cached)
            } else {
                throw exception
            }
        }
    
    override suspend fun getMangaById(id: String): Result<Manga?> = withRetry {
        // Try cache first
        val cached = getSingleMangaCache().get("manga_$id")
        if (cached != null) {
            return@withRetry Result.Success(cached)
        }
        
        try {
            val manga = mangaDao.getMangaById(id)
            
            // Cache if found
            if (manga != null) {
                getSingleMangaCache().put("manga_$id", manga)
            }
            
            Result.Success(manga)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get manga by ID: ${e.message}")
        }
    }
    
    override fun searchManga(query: String): Flow<List<Manga>> = flow {
        try {
            // Try cache first
            val cacheKey = "search_$query"
            val cached = getSearchCache().get(cacheKey)
            if (cached != null) {
                emit(cached)
                return@flow
            }
            
            // Search in database
            val results = mangaDao.searchManga(query)
            
            // Cache results
            getSearchCache().put(cacheKey, results)
            
            emit(results)
        } catch (e: Exception) {
            throw e
        }
    }
    
    override fun getMangaByStatus(status: MangaStatus): Flow<List<Manga>> = 
        mangaDao.getMangaByStatus(status).map { mangaList ->
            // Cache the result
            getMangaCache().put("status_${status.name}", mangaList)
            mangaList
        }
    
    override fun getMangaByGenre(genre: String): Flow<List<Manga>> = flow {
        try {
            val cacheKey = "genre_$genre"
            val cached = getMangaCache().get(cacheKey)
            if (cached != null) {
                emit(cached)
                return@flow
            }
            
            val results = mangaDao.getMangaByGenre(genre)
            getMangaCache().put(cacheKey, results)
            emit(results)
        } catch (e: Exception) {
            throw e
        }
    }
    
    override suspend fun addToLibrary(manga: Manga): Result<Unit> = withRetry {
        try {
            // Validate manga data
            val validationResult = validator.validate(manga)
            if (!validationResult.isValid()) {
                return@withRetry Result.Error(
                    ValidationException(validationResult as com.heartlessveteran.myriad.data.validation.ValidationResult.Invalid),
                    "Manga data validation failed"
                )
            }
            
            // Add to database
            val updatedManga = manga.copy(
                isInLibrary = true,
                dateAdded = if (manga.dateAdded == Date(0)) Date() else manga.dateAdded
            )
            
            mangaDao.insertManga(updatedManga)
            
            // Invalidate relevant caches
            invalidateLibraryCaches()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to add manga to library: ${e.message}")
        }
    }
    
    override suspend fun removeFromLibrary(mangaId: String): Result<Unit> = withRetry {
        try {
            mangaDao.removeFromLibrary(mangaId)
            
            // Invalidate relevant caches
            invalidateLibraryCaches()
            getSingleMangaCache().remove("manga_$mangaId")
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to remove manga from library: ${e.message}")
        }
    }
    
    override suspend fun toggleFavorite(mangaId: String): Result<Unit> = withRetry {
        try {
            mangaDao.toggleFavorite(mangaId)
            
            // Invalidate relevant caches
            invalidateLibraryCaches()
            getSingleMangaCache().remove("manga_$mangaId")
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to toggle favorite: ${e.message}")
        }
    }
    
    override suspend fun updateReadProgress(mangaId: String, readChapters: Int): Result<Unit> = withRetry {
        try {
            if (readChapters < 0) {
                return@withRetry Result.Error(
                    IllegalArgumentException("Read chapters cannot be negative"),
                    "Invalid read progress value"
                )
            }
            
            mangaDao.updateReadProgress(mangaId, readChapters, Date())
            
            // Invalidate relevant caches
            getSingleMangaCache().remove("manga_$mangaId")
            invalidateLibraryCaches()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update read progress: ${e.message}")
        }
    }
    
    // File operations - Enhanced with validation
    override suspend fun importMangaFromFile(filePath: String): Result<Manga> {
        return Result.Error(
            NotImplementedError("File import not yet implemented"),
            "File import functionality is under development"
        )
    }
    
    override suspend fun scanLocalMangaDirectory(directoryPath: String): Result<List<Manga>> {
        return Result.Error(
            NotImplementedError("Directory scan not yet implemented"),
            "Directory scanning functionality is under development"
        )
    }
    
    // Online source operations - Enhanced with caching
    override suspend fun searchOnlineManga(query: String, source: String): Result<List<Manga>> {
        return Result.Error(
            NotImplementedError("Online search not yet implemented"),
            "Online search functionality is under development"
        )
    }
    
    override suspend fun getMangaFromSource(sourceId: String, source: String): Result<Manga> {
        return Result.Error(
            NotImplementedError("Source manga retrieval not yet implemented"),
            "Source manga retrieval functionality is under development"
        )
    }
    
    override suspend fun downloadManga(manga: Manga): Result<Unit> {
        return Result.Error(
            NotImplementedError("Manga download not yet implemented"),
            "Manga download functionality is under development"
        )
    }
    
    // Metadata operations
    override suspend fun refreshMetadata(mangaId: String): Result<Unit> {
        return Result.Error(
            NotImplementedError("Metadata refresh not yet implemented"),
            "Metadata refresh functionality is under development"
        )
    }
    
    override suspend fun extractMetadataFromCover(coverPath: String): Result<Map<String, Any>> {
        return Result.Error(
            NotImplementedError("Metadata extraction not yet implemented"),
            "Cover metadata extraction functionality is under development"
        )
    }
    
    /**
     * Retry mechanism for database operations
     */
    private suspend fun <T> withRetry(operation: suspend () -> Result<T>): Result<T> {
        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            try {
                val result = operation()
                if (result is Result.Success || attempt == MAX_RETRY_ATTEMPTS - 1) {
                    return result
                }
            } catch (e: Exception) {
                if (attempt == MAX_RETRY_ATTEMPTS - 1) {
                    return Result.Error(e, "Operation failed after $MAX_RETRY_ATTEMPTS attempts: ${e.message}")
                }
            }
            
            // Exponential backoff
            delay(RETRY_DELAY_MS * (attempt + 1))
        }
        
        return Result.Error(
            RuntimeException("Retry mechanism failed unexpectedly"),
            "Unexpected error in retry mechanism"
        )
    }
    
    /**
     * Invalidate library-related caches
     */
    private suspend fun invalidateLibraryCaches() {
        val mangaCache = getMangaCache()
        mangaCache.remove("all_manga")
        mangaCache.remove("library_manga")
        mangaCache.remove("favorite_manga")
    }
    
    /**
     * Get cache statistics for monitoring
     */
    suspend fun getCacheStatistics(): Map<String, com.heartlessveteran.myriad.data.cache.CacheMetrics?> {
        return mapOf(
            CacheKeys.MANGA to memoryCache.getMetrics(CacheKeys.MANGA),
            CacheKeys.SEARCH_RESULTS to memoryCache.getMetrics(CacheKeys.SEARCH_RESULTS)
        )
    }
    
    /**
     * Clear all caches (for debugging or maintenance)
     */
    suspend fun clearAllCaches() {
        memoryCache.clearAll()
    }
}