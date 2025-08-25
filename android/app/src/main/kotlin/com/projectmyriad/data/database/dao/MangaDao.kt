package com.projectmyriad.data.database.dao

import androidx.room.*
import com.projectmyriad.data.database.entities.MangaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for manga operations.
 * Provides reactive queries using Flow for real-time UI updates.
 */
@Dao
interface MangaDao {
    
    @Query("SELECT * FROM manga ORDER BY updatedAt DESC")
    fun getAllManga(): Flow<List<MangaEntity>>
    
    @Query("SELECT * FROM manga WHERE id = :id")
    suspend fun getMangaById(id: String): MangaEntity?
    
    @Query("SELECT * FROM manga WHERE favorited = 1 ORDER BY updatedAt DESC")
    fun getFavoriteManga(): Flow<List<MangaEntity>>
    
    @Query("SELECT * FROM manga WHERE readingProgress > 0 AND readingProgress < 1 ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentlyReadManga(limit: Int = 10): Flow<List<MangaEntity>>
    
    @Query("SELECT * FROM manga WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchManga(query: String): Flow<List<MangaEntity>>
    
    @Query("SELECT * FROM manga WHERE genres LIKE '%' || :genre || '%'")
    fun getMangaByGenre(genre: String): Flow<List<MangaEntity>>
    
    @Query("SELECT * FROM manga WHERE status = :status")
    fun getMangaByStatus(status: String): Flow<List<MangaEntity>>
    
    @Query("SELECT * FROM manga WHERE rating >= :minRating ORDER BY rating DESC")
    fun getMangaByMinRating(minRating: Float): Flow<List<MangaEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManga(manga: MangaEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)  
    suspend fun insertMangaList(mangaList: List<MangaEntity>)
    
    @Update
    suspend fun updateManga(manga: MangaEntity)
    
    @Query("UPDATE manga SET readingProgress = :progress, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateReadingProgress(id: String, progress: Float, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE manga SET favorited = :favorited, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, favorited: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteManga(manga: MangaEntity)
    
    @Query("DELETE FROM manga WHERE id = :id")
    suspend fun deleteMangaById(id: String)
    
    @Query("DELETE FROM manga")
    suspend fun deleteAllManga()
    
    @Query("SELECT COUNT(*) FROM manga")
    suspend fun getMangaCount(): Int
    
    @Query("SELECT COUNT(*) FROM manga WHERE favorited = 1") 
    suspend fun getFavoriteMangaCount(): Int
}