package com.heartlessveteran.myriad.data.repository

import com.heartlessveteran.myriad.data.database.dao.MangaDao
import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.entities.MangaStatus
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.domain.repository.MangaRepository
import com.heartlessveteran.myriad.domain.services.FileManagerService
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Implementation of MangaRepository using Room database and various services
 */
class MangaRepositoryImpl(
    private val mangaDao: MangaDao,
    private val fileManagerService: FileManagerService,
    // Service dependencies - will be implemented in next phase
    // private val sourceService: SourceService,
    // private val downloadService: DownloadService,
) : MangaRepository {
    override fun getAllManga(): Flow<List<Manga>> = mangaDao.getAllManga()

    override fun getLibraryManga(): Flow<List<Manga>> = mangaDao.getLibraryManga()

    override fun getFavoriteManga(): Flow<List<Manga>> = mangaDao.getFavoriteManga()

    override suspend fun getMangaById(id: String): Result<Manga?> =
        try {
            val manga = mangaDao.getMangaById(id)
            Result.Success(manga)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get manga by id: ${e.message}")
        }

    override fun searchManga(query: String): Flow<List<Manga>> = mangaDao.searchManga(query)

    override fun getMangaByStatus(status: MangaStatus): Flow<List<Manga>> = mangaDao.getMangaByStatus(status)

    override fun getMangaByGenre(genre: String): Flow<List<Manga>> = mangaDao.getMangaByGenre(genre)

    override suspend fun addToLibrary(manga: Manga): Result<Unit> =
        try {
            val updatedManga =
                manga.copy(
                    isInLibrary = true,
                    dateAdded = Date(),
                )
            mangaDao.insertManga(updatedManga)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to add manga to library: ${e.message}")
        }

    override suspend fun removeFromLibrary(mangaId: String): Result<Unit> =
        try {
            mangaDao.updateLibraryStatus(mangaId, false)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to remove manga from library: ${e.message}")
        }

    override suspend fun toggleFavorite(mangaId: String): Result<Unit> =
        try {
            val manga = mangaDao.getMangaById(mangaId)
            manga?.let {
                mangaDao.updateFavoriteStatus(mangaId, !it.isFavorite)
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to toggle favorite: ${e.message}")
        }

    override suspend fun updateReadProgress(
        mangaId: String,
        readChapters: Int,
    ): Result<Unit> =
        try {
            mangaDao.updateReadProgress(mangaId, readChapters, Date())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update read progress: ${e.message}")
        }

    // File operations - Delegate to FileManagerService
    override suspend fun importMangaFromFile(filePath: String): Result<Manga> =
        when (val result = fileManagerService.importMangaFromFile(filePath)) {
            is Result.Success -> {
                // Save the imported manga to database
                try {
                    mangaDao.insertManga(result.data)
                    Result.Success(result.data)
                } catch (e: Exception) {
                    Result.Error(e, "Failed to save imported manga to database: ${e.message}")
                }
            }
            is Result.Error -> result
            is Result.Loading -> result
        }

    override suspend fun scanLocalMangaDirectory(directoryPath: String): Result<List<Manga>> =
        when (val result = fileManagerService.scanDirectoryForManga(directoryPath)) {
            is Result.Success -> {
                // Save all imported manga to database
                try {
                    val mangaList = result.data
                    mangaDao.insertMangaList(mangaList)
                    Result.Success(mangaList)
                } catch (e: Exception) {
                    Result.Error(e, "Failed to save scanned manga to database: ${e.message}")
                }
            }
            is Result.Error -> result
            is Result.Loading -> result
        }

    // Online source operations - Delegate to SourceService
    override suspend fun searchOnlineManga(
        query: String,
        source: String,
    ): Result<List<Manga>> {
        // TODO: Implement with SourceService integration
        // return sourceService.searchMangaAcrossSources(query, listOf(source)).first().results
        return Result.Error(
            NotImplementedError("Online search not yet implemented - SourceService integration needed"),
            "Online manga search functionality is under development. Next phase: implement SourceService",
        )
    }

    override suspend fun getMangaFromSource(
        sourceId: String,
        source: String,
    ): Result<Manga> {
        // TODO: Implement with SourceService integration
        // return sourceService.getMangaDetails(source, sourceId)
        return Result.Error(
            NotImplementedError("Source manga fetch not yet implemented - SourceService integration needed"),
            "Source manga fetching functionality is under development. Next phase: implement SourceService",
        )
    }

    // Download operations - Delegate to DownloadService
    override suspend fun downloadManga(manga: Manga): Result<Unit> {
        // TODO: Implement with DownloadService integration
        // return downloadService.enqueueMangaDownload(manga).map { Unit }
        return Result.Error(
            NotImplementedError("Manga download not yet implemented - DownloadService integration needed"),
            "Manga download functionality is under development. Next phase: implement DownloadService",
        )
    }

    // Metadata operations
    override suspend fun refreshMetadata(mangaId: String): Result<Unit> {
        // TODO: Implement metadata refresh logic
        // This could involve re-reading file metadata or fetching from online sources
        return Result.Error(
            NotImplementedError("Metadata refresh not yet implemented"),
            "Metadata refresh functionality is under development. Next phase: integrate with FileManagerService and SourceService",
        )
    }

    override suspend fun extractMetadataFromCover(coverPath: String): Result<Map<String, Any>> {
        // TODO: Implement with FileManagerService integration
        // return fileManagerService.extractMetadata(coverPath)
        return Result.Error(
            NotImplementedError("Metadata extraction not yet implemented - FileManagerService integration needed"),
            "Cover metadata extraction functionality is under development. Next phase: implement FileManagerService",
        )
    }
}
