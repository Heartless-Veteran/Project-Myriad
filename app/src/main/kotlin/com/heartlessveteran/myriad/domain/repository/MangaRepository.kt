package com.heartlessveteran.myriad.domain.repository

import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.entities.MangaStatus
import com.heartlessveteran.myriad.domain.models.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for manga-related operations
 * Provides abstraction for data access from various sources
 */
interface MangaRepository {
    // Local library operations
    fun getAllManga(): Flow<List<Manga>>

    fun getLibraryManga(): Flow<List<Manga>>

    fun getFavoriteManga(): Flow<List<Manga>>

    suspend fun getMangaById(id: String): Result<Manga?>

    // Search and filtering
    fun searchManga(query: String): Flow<List<Manga>>

    fun getMangaByStatus(status: MangaStatus): Flow<List<Manga>>

    fun getMangaByGenre(genre: String): Flow<List<Manga>>

    // Library management
    suspend fun addToLibrary(manga: Manga): Result<Unit>

    suspend fun removeFromLibrary(mangaId: String): Result<Unit>

    suspend fun toggleFavorite(mangaId: String): Result<Unit>

    suspend fun updateReadProgress(
        mangaId: String,
        readChapters: Int,
    ): Result<Unit>

    // File operations - NOW IMPLEMENTED via FileManagerService
    suspend fun importMangaFromFile(filePath: String): Result<Manga>

    suspend fun scanLocalMangaDirectory(directoryPath: String): Result<List<Manga>>

    // Online source operations - NOW IMPLEMENTED via SourceService
    suspend fun searchOnlineManga(
        query: String,
        source: String,
    ): Result<List<Manga>>

    suspend fun getMangaFromSource(
        sourceId: String,
        source: String,
    ): Result<Manga>

    // Download operations - NOW IMPLEMENTED via DownloadService
    suspend fun downloadManga(manga: Manga): Result<Unit>

    // Metadata operations
    suspend fun refreshMetadata(mangaId: String): Result<Unit>

    suspend fun extractMetadataFromCover(coverPath: String): Result<Map<String, Any>>
}
