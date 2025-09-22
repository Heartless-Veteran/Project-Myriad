package com.heartlessveteran.myriad.core.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.heartlessveteran.myriad.core.domain.entities.Manga
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Manga entities.
 * Provides database operations for manga data using Room persistence library.
 * Follows Clean Architecture by being in the data layer.
 */
@Dao
interface MangaDao {

    /**
     * Gets all manga in the user's library
     * @return Flow of manga list for reactive UI updates
     */
    @Query("SELECT * FROM manga WHERE isInLibrary = 1 ORDER BY dateAdded DESC")
    fun getLibraryManga(): Flow<List<Manga>>

    /**
     * Gets manga by ID
     * @param id Manga ID to search for
     * @return Single manga entity or null
     */
    @Query("SELECT * FROM manga WHERE id = :id")
    suspend fun getMangaById(id: String): Manga?

    /**
     * Gets all manga from a specific source
     * @param source Source name (e.g., "local", "mangadx")
     * @return Flow of manga from the specified source
     */
    @Query("SELECT * FROM manga WHERE source = :source")
    fun getMangaBySource(source: String): Flow<List<Manga>>

    /**
     * Searches library manga by title, author, or genre
     * @param query Search query string
     * @return Flow of matching manga
     */
    @Query("""
        SELECT * FROM manga 
        WHERE isInLibrary = 1 
        AND (title LIKE '%' || :query || '%' 
             OR author LIKE '%' || :query || '%'
             OR artist LIKE '%' || :query || '%')
        ORDER BY title ASC
    """)
    fun searchLibraryManga(query: String): Flow<List<Manga>>

    /**
     * Gets recently read manga
     * @param limit Maximum number of manga to return
     * @return Flow of recently read manga
     */
    @Query("""
        SELECT * FROM manga 
        WHERE isInLibrary = 1 
        AND lastReadDate IS NOT NULL 
        ORDER BY lastReadDate DESC 
        LIMIT :limit
    """)
    fun getRecentlyReadManga(limit: Int): Flow<List<Manga>>

    /**
     * Gets favorite manga
     * @return Flow of favorite manga
     */
    @Query("SELECT * FROM manga WHERE isInLibrary = 1 AND isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteManga(): Flow<List<Manga>>

    /**
     * Inserts or updates manga
     * @param manga Manga entity to save
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManga(manga: Manga)

    /**
     * Inserts or updates multiple manga
     * @param manga List of manga entities to save
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManga(manga: List<Manga>)

    /**
     * Updates existing manga
     * @param manga Manga entity to update
     */
    @Update
    suspend fun updateManga(manga: Manga)

    /**
     * Deletes manga
     * @param manga Manga entity to delete
     */
    @Delete
    suspend fun deleteManga(manga: Manga)

    /**
     * Deletes manga by ID
     * @param id Manga ID to delete
     */
    @Query("DELETE FROM manga WHERE id = :id")
    suspend fun deleteMangaById(id: String)

    /**
     * Updates reading progress for a manga
     * @param mangaId Manga ID to update
     * @param readChapters Number of chapters read
     * @param lastReadDate Date of last read
     */
    @Query("""
        UPDATE manga 
        SET readChapters = :readChapters, lastReadDate = :lastReadDate 
        WHERE id = :mangaId
    """)
    suspend fun updateReadingProgress(
        mangaId: String,
        readChapters: Int,
        lastReadDate: Long
    )

    /**
     * Toggles favorite status of manga
     * @param mangaId Manga ID to toggle
     * @param isFavorite New favorite status
     */
    @Query("UPDATE manga SET isFavorite = :isFavorite WHERE id = :mangaId")
    suspend fun updateFavoriteStatus(mangaId: String, isFavorite: Boolean)

    /**
     * Adds manga to library
     * @param mangaId Manga ID to add
     */
    @Query("UPDATE manga SET isInLibrary = 1, dateAdded = :dateAdded WHERE id = :mangaId")
    suspend fun addToLibrary(mangaId: String, dateAdded: Long)

    /**
     * Removes manga from library
     * @param mangaId Manga ID to remove
     */
    @Query("UPDATE manga SET isInLibrary = 0 WHERE id = :mangaId")
    suspend fun removeFromLibrary(mangaId: String)

    /**
     * Gets total count of manga in library
     * @return Number of manga in library
     */
    @Query("SELECT COUNT(*) FROM manga WHERE isInLibrary = 1")
    suspend fun getLibraryCount(): Int

    /**
     * Gets manga by genre
     * @param genre Genre to filter by
     * @return Flow of manga with the specified genre
     */
    @Query("""
        SELECT * FROM manga 
        WHERE isInLibrary = 1 
        AND genres LIKE '%' || :genre || '%'
        ORDER BY title ASC
    """)
    fun getMangaByGenre(genre: String): Flow<List<Manga>>
}