package com.projectmyriad.data.repositories

import com.projectmyriad.data.database.dao.MangaDao
import com.projectmyriad.data.database.entities.MangaEntity
import com.projectmyriad.domain.entities.Manga
import com.projectmyriad.domain.entities.MangaChapter
import com.projectmyriad.domain.repositories.MangaRepository
import com.projectmyriad.data.mappers.toDomain
import com.projectmyriad.data.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of MangaRepository using Room database.
 * Handles data access and business logic for manga operations.
 */
@Singleton
class MangaRepositoryImpl @Inject constructor(
    private val mangaDao: MangaDao
) : MangaRepository {
    
    override fun getAllManga(): Flow<List<Manga>> {
        return mangaDao.getAllManga().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getMangaById(id: String): Result<Manga?> {
        return try {
            val entity = mangaDao.getMangaById(id)
            Result.success(entity?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getChaptersForManga(mangaId: String): Flow<List<MangaChapter>> {
        // TODO: Implement chapter DAO and logic
        // For now return empty flow
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }
    
    override suspend fun importMangaFromFile(filePath: String): Result<Manga> {
        return try {
            // TODO: Implement file parsing logic (.cbz/.cbr)
            // This is a placeholder implementation
            val manga = Manga(
                id = java.util.UUID.randomUUID().toString(),
                title = "Imported Manga",
                author = "Unknown",
                description = "Imported from $filePath",
                coverImage = "",
                chapters = emptyList(),
                genres = emptyList(),
                status = com.projectmyriad.domain.entities.MangaStatus.ONGOING,
                rating = 0f,
                tags = emptyList()
            )
            
            mangaDao.insertManga(manga.toEntity())
            Result.success(manga)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteManga(mangaId: String): Result<Unit> {
        return try {
            mangaDao.deleteMangaById(mangaId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateReadingProgress(mangaId: String, progress: Float): Result<Unit> {
        return try {
            val clampedProgress = progress.coerceIn(0f, 1f)
            mangaDao.updateReadingProgress(mangaId, clampedProgress)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateChapterProgress(chapterId: String, progress: Float): Result<Unit> {
        return try {
            val clampedProgress = progress.coerceIn(0f, 1f)
            mangaDao.updateChapterProgress(chapterId, clampedProgress)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun searchManga(query: String): Flow<List<Manga>> {
        return mangaDao.searchManga(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getMangaFiltered(
        genres: List<String>,
        status: List<String>,
        minRating: Float
    ): Flow<List<Manga>> {
        // Use all filter parameters: genres, status, and minRating
        // Assumes mangaDao.getMangaFiltered is implemented to handle these filters
        return mangaDao.getMangaFiltered(genres, status, minRating).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun toggleFavorite(mangaId: String): Result<Unit> {
        return try {
            val manga = mangaDao.getMangaById(mangaId)
            if (manga != null) {
                mangaDao.updateFavoriteStatus(mangaId, !manga.favorited)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getFavoriteManga(): Flow<List<Manga>> {
        return mangaDao.getFavoriteManga().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getRecentlyReadManga(limit: Int): Flow<List<Manga>> {
        return mangaDao.getRecentlyReadManga(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}