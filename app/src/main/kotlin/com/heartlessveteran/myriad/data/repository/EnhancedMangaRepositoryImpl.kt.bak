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
    private val validator: MangaValidator
    // TODO: Add network API service when implemented
    // TODO: Add file manager service when implemented
) : MangaRepository {
    
    companion object {
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1000L
    }
    
    /**
 * Returns the MemoryCache instance used to store the list of all manga.
 *
 * The cache is retrieved from `memoryCache` with key `CacheKeys.MANGA` and configuration `CacheConfigs.MANGA`.
 *
 * @return MemoryCache<List<Manga>> for storing/retrieving all-manga lists.
 */
    private suspend fun getMangaCache() = memoryCache.getCache<List<Manga>>(CacheKeys.MANGA, CacheConfigs.MANGA)
    /**
 * Returns the memory cache used for storing search result lists.
 *
 * This retrieves (or creates) a MemoryCache keyed for search results using the
 * SEARCH_RESULTS cache key and its configured CacheConfig.
 *
 * @return MemoryCache<List<Manga>> configured for search result entries.
 */
private suspend fun getSearchCache() = memoryCache.getCache<List<Manga>>(CacheKeys.SEARCH_RESULTS, CacheConfigs.SEARCH_RESULTS)
    /**
 * Returns the memory cache instance used for individual (online) Manga entries.
 *
 * This obtains a MemoryCache<Manga> keyed by CacheKeys.ONLINE_MANGA using the MANGA cache configuration.
 *
 * @return MemoryCache<Manga> for single-manga lookups and storage.
 */
private suspend fun getSingleMangaCache() = memoryCache.getCache<Manga>(CacheKeys.ONLINE_MANGA, CacheConfigs.MANGA)
    
    /**
         * Emits a flow of all Manga records from the database, caching results for offline fallback.
         *
         * Queries the DAO for all manga and caches each emitted list under the key `"all_manga"`.
         * If the DAO stream fails, the flow will attempt to emit a cached list (if present); otherwise
         * the original exception is rethrown.
         *
         * @return A [Flow] that emits the current list of all [Manga].
         */
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
    
    /**
         * Streams the user's library manga as a cold Flow.
         *
         * Emits the list of library manga retrieved from the DAO and caches the latest successful list under the key `"library_manga"`.
         * If the DAO emits an error, attempts to emit the cached list instead; if no cached value is available the original exception is rethrown.
         *
         * @return A Flow that emits the current list of library manga.
         */
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
    
    /**
         * Streams the user's favorite manga, caching results and falling back to cache on errors.
         *
         * Emits lists from the database as a Flow and stores each result under the cache key `"favorite_manga"`.
         * If the database Flow throws, attempts to emit the cached list (if present); otherwise the exception is rethrown.
         *
         * @return A Flow that emits the current list of favorite Manga.
         */
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
    
    /**
     * Retrieves a manga by its ID, returning a Result that contains the manga if found.
     *
     * Looks up a single-manga cache first and returns the cached value when available. If not cached,
     * queries the DAO, caches the retrieved manga (when non-null), and returns it. Database/IO
     * operations are executed with the repository's retry mechanism; on failure a Result.Error is
     * returned containing the underlying exception and message.
     *
     * @param id The unique identifier of the manga to retrieve (used as part of the cache key `manga_$id`).
     * @return Result.Success containing the Manga when found, Result.Success(null) when no entry exists,
     *         or Result.Error when an exception occurs.
     */
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
    
    /**
     * Searches for manga matching the provided query, emitting results as a Flow.
     *
     * Performs a cache-first lookup using a query-specific key ("search_<query>"); if a cached
     * result is present it is emitted immediately. Otherwise the DAO is queried, the results are
     * cached under the same key, and then emitted. Exceptions from the DAO or cache access are
     * propagated to the caller.
     *
     * @param query The search string used to match manga titles/metadata.
     * @return A Flow that emits the list of matching Manga (cached or freshly fetched).
     */
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
    
    /**
         * Returns a flow that emits all manga matching the given reading `status`.
         *
         * The DAO query results are cached under the key `status_{STATUS_NAME}` (e.g. `status_COMPLETED`)
         * each time the flow emits.
         *
         * @param status The manga status to filter by.
         * @return A [Flow] that emits lists of [Manga] matching the provided status. Cache side-effect occurs on each emission.
         */
        override fun getMangaByStatus(status: MangaStatus): Flow<List<Manga>> = 
        mangaDao.getMangaByStatus(status).map { mangaList ->
            // Cache the result
            getMangaCache().put("status_${status.name}", mangaList)
            mangaList
        }
    
    /**
     * Returns a Flow that emits the list of manga matching the given genre.
     *
     * The function first attempts to return cached results keyed by `"genre_<genre>"`.
     * If no cache entry exists it queries the DAO, caches the retrieved list, and emits it.
     * Any exception thrown by the DAO or cache is propagated through the Flow.
     *
     * @param genre The genre name to filter manga by.
     * @return A Flow emitting the list of matching Manga objects.
     */
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
    
    /**
     * Validates and adds the given manga to the user's library.
     *
     * The manga is validated; if valid, a copy is persisted with `isInLibrary = true`
     * and `dateAdded` set to the current time when it was previously unset. Library-related
     * caches are invalidated after a successful insert. The operation is executed with
     * the repository's retry policy.
     *
     * @param manga The manga to add to the library. If `manga.dateAdded` is the epoch
     *               (Date(0)), it will be replaced with the current date.
     * @return Result.Success(Unit) on success; Result.Error contains the originating exception
     *         or a ValidationException when validation fails.
     */
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
    
    /**
     * Removes the specified manga from the user's library.
     *
     * This performs a database update to mark the manga as removed from the library, invalidates library-related caches,
     * and removes the single-manga cache entry for the given id. The operation is executed with the repository's retry
     * policy and returns a Result indicating success or failure.
     *
     * @param mangaId The unique identifier of the manga to remove from the library.
     * @return Result.Success(Unit) on success, or Result.Error with the underlying exception and message on failure.
     */
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
    
    /**
     * Toggles the favorite status of the manga with the given ID.
     *
     * Performs the operation inside the repository's retry wrapper. On success it invalidates
     * library-related caches and removes the single-manga cache entry for this manga.
     *
     * @param mangaId The identifier of the manga whose favorite status will be toggled.
     * @return Result.Success(Unit) on success, or Result.Error containing the underlying exception and a brief message on failure.
     */
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
    
    /**
     * Updates the read chapter count for a manga and invalidates related caches.
     *
     * Updates the stored read progress for the manga identified by [mangaId] to [readChapters], records the update timestamp,
     * and clears the single-manga cache entry plus library-related caches so callers will observe fresh data.
     * This operation is executed with an internal retry policy for transient failures.
     *
     * @param mangaId The unique identifier of the manga to update.
     * @param readChapters The new absolute read chapter count; must be >= 0.
     * @return A [Result.Success] on success or [Result.Error] on failure. Returns a [Result.Error] with an
     *         IllegalArgumentException if [readChapters] is negative; other exceptions from the DAO are returned inside [Result.Error].
     */
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
    
    /**
     * Imports a manga from a local file into the repository.
     *
     * @param filePath Filesystem path to the manga file (archive or package) to import.
     * @return A [Result] wrapping the imported [Manga] on success. Currently this function is not implemented and returns a [Result.Error] containing a [NotImplementedError] with a development message.
     */
    override suspend fun importMangaFromFile(filePath: String): Result<Manga> {
        return Result.Error(
            NotImplementedError("File import not yet implemented"),
            "File import functionality is under development"
        )
    }
    
    /**
     * Scans a local filesystem directory for manga and returns the discovered manga entries.
     *
     * This operation is currently not implemented and will return a Result.Error containing a
     * NotImplementedError while the directory scanning functionality is under development.
     *
     * @param directoryPath Path to the local directory to scan.
     * @return A Result containing a list of discovered Manga on success; currently always returns
     *         Result.Error(NotImplementedError, ...) indicating the feature is not implemented.
     */
    override suspend fun scanLocalMangaDirectory(directoryPath: String): Result<List<Manga>> {
        return Result.Error(
            NotImplementedError("Directory scan not yet implemented"),
            "Directory scanning functionality is under development"
        )
    }
    
    /**
     * Searches for manga from an online source by query.
     *
     * Performs an online search against the specified source and returns matching manga entries.
     *
     * @param query The search query string (title, author, or keywords).
     * @param source Identifier of the online source to search (e.g., provider key or base URL).
     * @return A [Result] containing a list of matching [Manga] on success.
     *         Currently always returns a [Result.Error] wrapping a [NotImplementedError] because
     *         online search functionality is under development.
     */
    override suspend fun searchOnlineManga(query: String, source: String): Result<List<Manga>> {
        return Result.Error(
            NotImplementedError("Online search not yet implemented"),
            "Online search functionality is under development"
        )
    }
    
    /**
     * Retrieves a manga's metadata from an external source by source-specific ID.
     *
     * Currently not implemented — this function returns a `Result.Error` containing a
     * `NotImplementedError` and a developer-facing message indicating the feature is under development.
     *
     * @param sourceId The identifier of the manga in the external source.
     * @param source The identifier or name of the external source (e.g., provider key).
     * @return A Result containing the manga on success; presently always a `Result.Error(NotImplementedError, ...)`.
     */
    override suspend fun getMangaFromSource(sourceId: String, source: String): Result<Manga> {
        return Result.Error(
            NotImplementedError("Source manga retrieval not yet implemented"),
            "Source manga retrieval functionality is under development"
        )
    }
    
    /**
     * Attempts to download the given manga for offline storage.
     *
     * This function is a stub: it currently returns a Result.Error containing a NotImplementedError
     * indicating the download functionality is under development.
     *
     * @param manga The manga to download.
     * @return A Result<Unit> which is currently always an error wrapping NotImplementedError.
     */
    override suspend fun downloadManga(manga: Manga): Result<Unit> {
        return Result.Error(
            NotImplementedError("Manga download not yet implemented"),
            "Manga download functionality is under development"
        )
    }
    
    /**
     * Refreshes metadata for the manga identified by [mangaId].
     *
     * Currently unimplemented; callers will receive a `Result.Error` containing a `NotImplementedError`
     * indicating the metadata refresh functionality is under development.
     *
     * @param mangaId The unique identifier of the manga whose metadata should be refreshed.
     * @return A [Result] that would indicate success when implemented; currently always an error
     *         with `NotImplementedError`.
     */
    override suspend fun refreshMetadata(mangaId: String): Result<Unit> {
        return Result.Error(
            NotImplementedError("Metadata refresh not yet implemented"),
            "Metadata refresh functionality is under development"
        )
    }
    
    /**
     * Extracts metadata from a manga cover image file.
     *
     * Intended to analyze the cover at the given file path and return a map of discovered
     * metadata (e.g., "width", "height", "dominantColor", "format", "dpi", "embeddedTags").
     *
     * @param coverPath Path to the cover image file (local filesystem). Supported formats and parsing
     *     behavior are implementation details.
     * @return On success, a Result.Success containing a Map with metadata keys and their values.
     *     Currently this operation is not implemented and will return Result.Error with a
     *     NotImplementedError indicating the feature is under development.
     */
    override suspend fun extractMetadataFromCover(coverPath: String): Result<Map<String, Any>> {
        return Result.Error(
            NotImplementedError("Metadata extraction not yet implemented"),
            "Cover metadata extraction functionality is under development"
        )
    }
    
    /**
     * Executes the provided suspendable operation with retry semantics and exponential backoff.
     *
     * Repeats the operation up to MAX_RETRY_ATTEMPTS. If the operation returns a Result.Success
     * it is returned immediately. If the operation returns a non-success Result, it will be
     * retried until the last attempt, in which case that final Result is returned. If the
     * operation throws an exception it is retried; on the final failed attempt the exception
     * is wrapped in a Result.Error with a descriptive message. Between attempts the function
     * delays using an exponential backoff based on RETRY_DELAY_MS.
     *
     * @param operation The suspendable database operation to execute; should return a Result<T>.
     * @return The successful Result<T> or a Result.Error describing the final failure.
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
     * Removes cached entries for library-related manga so subsequent reads reflect recent changes.
     *
     * This suspending helper clears the "all_manga", "library_manga", and "favorite_manga" keys from the manga cache.
     */
    private suspend fun invalidateLibraryCaches() {
        val mangaCache = getMangaCache()
        mangaCache.remove("all_manga")
        mangaCache.remove("library_manga")
        mangaCache.remove("favorite_manga")
    }
    
    /**
     * Returns runtime metrics for the repository's caches.
     *
     * Provides the current CacheMetrics (may be null) for the MANGA and SEARCH_RESULTS cache keys.
     *
     * @return A map keyed by cache name (CacheKeys.MANGA and CacheKeys.SEARCH_RESULTS) to their
     *         corresponding CacheMetrics or null if metrics are unavailable.
     */
    suspend fun getCacheStatistics(): Map<String, com.heartlessveteran.myriad.data.cache.CacheMetrics?> {
        return mapOf(
            CacheKeys.MANGA to memoryCache.getMetrics(CacheKeys.MANGA),
            CacheKeys.SEARCH_RESULTS to memoryCache.getMetrics(CacheKeys.SEARCH_RESULTS)
        )
    }
    
    /**
     * Clears every entry from all configured in-memory caches.
     *
     * Intended for debugging and maintenance; invokes the cache provider to remove all stored items.
     */
    suspend fun clearAllCaches() {
        memoryCache.clearAll()
    }
}