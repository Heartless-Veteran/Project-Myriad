package com.heartlessveteran.myriad.core.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.heartlessveteran.myriad.core.domain.entities.MangaChapter
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for MangaChapter entities.
 * Provides database operations for manga chapter data using Room persistence library.
 * Follows Clean Architecture by being in the data layer.
 */
@Dao
interface ChapterDao {

    /**
     * Gets all chapters for a specific manga
     * @param mangaId Manga ID to get chapters for
     * @return Flow of chapters ordered by chapter number
     */
    @Query("SELECT * FROM manga_chapters WHERE mangaId = :mangaId ORDER BY chapterNumber ASC")
    fun getChaptersForManga(mangaId: String): Flow<List<MangaChapter>>

    /**
     * Gets a specific chapter by ID
     * @param chapterId Chapter ID to search for
     * @return Single chapter entity or null
     */
    @Query("SELECT * FROM manga_chapters WHERE id = :chapterId")
    suspend fun getChapterById(chapterId: String): MangaChapter?

    /**
     * Gets chapters by manga ID and chapter number
     * @param mangaId Manga ID
     * @param chapterNumber Chapter number
     * @return Chapter entity or null
     */
    @Query("SELECT * FROM manga_chapters WHERE mangaId = :mangaId AND chapterNumber = :chapterNumber")
    suspend fun getChapterByNumber(mangaId: String, chapterNumber: Float): MangaChapter?

    /**
     * Gets downloaded chapters for a manga
     * @param mangaId Manga ID to get downloaded chapters for
     * @return Flow of downloaded chapters
     */
    @Query("SELECT * FROM manga_chapters WHERE mangaId = :mangaId AND isDownloaded = 1 ORDER BY chapterNumber ASC")
    fun getDownloadedChapters(mangaId: String): Flow<List<MangaChapter>>

    /**
     * Gets unread chapters for a manga
     * @param mangaId Manga ID to get unread chapters for
     * @return Flow of unread chapters
     */
    @Query("SELECT * FROM manga_chapters WHERE mangaId = :mangaId AND isRead = 0 ORDER BY chapterNumber ASC")
    fun getUnreadChapters(mangaId: String): Flow<List<MangaChapter>>

    /**
     * Gets the latest chapter for a manga
     * @param mangaId Manga ID to get latest chapter for
     * @return Latest chapter or null
     */
    @Query("SELECT * FROM manga_chapters WHERE mangaId = :mangaId ORDER BY chapterNumber DESC LIMIT 1")
    suspend fun getLatestChapter(mangaId: String): MangaChapter?

    /**
     * Gets the last read chapter for a manga
     * @param mangaId Manga ID to get last read chapter for
     * @return Last read chapter or null
     */
    @Query("""
        SELECT * FROM manga_chapters 
        WHERE mangaId = :mangaId AND isRead = 1 
        ORDER BY chapterNumber DESC 
        LIMIT 1
    """)
    suspend fun getLastReadChapter(mangaId: String): MangaChapter?

    /**
     * Gets next unread chapter after a specific chapter
     * @param mangaId Manga ID
     * @param currentChapterNumber Current chapter number
     * @return Next unread chapter or null
     */
    @Query("""
        SELECT * FROM manga_chapters 
        WHERE mangaId = :mangaId 
        AND chapterNumber > :currentChapterNumber 
        AND isRead = 0 
        ORDER BY chapterNumber ASC 
        LIMIT 1
    """)
    suspend fun getNextUnreadChapter(mangaId: String, currentChapterNumber: Float): MangaChapter?

    /**
     * Inserts or updates a chapter
     * @param chapter Chapter entity to save
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: MangaChapter)

    /**
     * Inserts or updates multiple chapters
     * @param chapters List of chapter entities to save
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<MangaChapter>)

    /**
     * Updates existing chapter
     * @param chapter Chapter entity to update
     */
    @Update
    suspend fun updateChapter(chapter: MangaChapter)

    /**
     * Deletes chapter
     * @param chapter Chapter entity to delete
     */
    @Delete
    suspend fun deleteChapter(chapter: MangaChapter)

    /**
     * Deletes chapter by ID
     * @param chapterId Chapter ID to delete
     */
    @Query("DELETE FROM manga_chapters WHERE id = :chapterId")
    suspend fun deleteChapterById(chapterId: String)

    /**
     * Deletes all chapters for a manga
     * @param mangaId Manga ID to delete chapters for
     */
    @Query("DELETE FROM manga_chapters WHERE mangaId = :mangaId")
    suspend fun deleteChaptersForManga(mangaId: String)

    /**
     * Updates chapter reading progress
     * @param chapterId Chapter ID to update
     * @param isRead Whether chapter is read
     * @param lastReadPage Last read page number
     * @param dateRead Date when chapter was read (nullable)
     */
    @Query("""
        UPDATE manga_chapters 
        SET isRead = :isRead, lastReadPage = :lastReadPage, dateRead = :dateRead 
        WHERE id = :chapterId
    """)
    suspend fun updateChapterProgress(
        chapterId: String,
        isRead: Boolean,
        lastReadPage: Int,
        dateRead: Long?
    )

    /**
     * Marks chapter as downloaded
     * @param chapterId Chapter ID to update
     * @param isDownloaded Download status
     * @param localPath Local file path (nullable)
     */
    @Query("""
        UPDATE manga_chapters 
        SET isDownloaded = :isDownloaded, localPath = :localPath 
        WHERE id = :chapterId
    """)
    suspend fun updateDownloadStatus(
        chapterId: String,
        isDownloaded: Boolean,
        localPath: String?
    )

    /**
     * Gets total chapter count for a manga
     * @param mangaId Manga ID to count chapters for
     * @return Total number of chapters
     */
    @Query("SELECT COUNT(*) FROM manga_chapters WHERE mangaId = :mangaId")
    suspend fun getChapterCount(mangaId: String): Int

    /**
     * Gets read chapter count for a manga
     * @param mangaId Manga ID to count read chapters for
     * @return Number of read chapters
     */
    @Query("SELECT COUNT(*) FROM manga_chapters WHERE mangaId = :mangaId AND isRead = 1")
    suspend fun getReadChapterCount(mangaId: String): Int

    /**
     * Gets downloaded chapter count for a manga
     * @param mangaId Manga ID to count downloaded chapters for
     * @return Number of downloaded chapters
     */
    @Query("SELECT COUNT(*) FROM manga_chapters WHERE mangaId = :mangaId AND isDownloaded = 1")
    suspend fun getDownloadedChapterCount(mangaId: String): Int

    /**
     * Marks all chapters as read up to a specific chapter number
     * @param mangaId Manga ID
     * @param chapterNumber Chapter number to mark read up to
     * @param dateRead Date when chapters were marked as read
     */
    @Query("""
        UPDATE manga_chapters 
        SET isRead = 1, dateRead = :dateRead 
        WHERE mangaId = :mangaId AND chapterNumber <= :chapterNumber
    """)
    suspend fun markChaptersReadUpTo(
        mangaId: String,
        chapterNumber: Float,
        dateRead: Long
    )

    /**
     * Gets chapters from a specific source
     * @param source Source name
     * @return Flow of chapters from the specified source
     */
    @Query("SELECT * FROM manga_chapters WHERE source = :source ORDER BY dateAdded DESC")
    fun getChaptersBySource(source: String): Flow<List<MangaChapter>>
}