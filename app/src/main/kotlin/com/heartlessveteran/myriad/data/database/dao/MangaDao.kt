package com.heartlessveteran.myriad.data.database.dao

import androidx.room.*
import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.entities.MangaStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Manga operations
 */
@Dao
interface MangaDao {
    @Query("SELECT * FROM manga ORDER BY lastUpdated DESC")
    fun getAllManga(): Flow<List<Manga>>

    @Query("SELECT * FROM manga WHERE isInLibrary = 1 ORDER BY lastUpdated DESC")
    fun getLibraryManga(): Flow<List<Manga>>

    @Query("SELECT * FROM manga WHERE isFavorite = 1 ORDER BY lastUpdated DESC")
    fun getFavoriteManga(): Flow<List<Manga>>

    @Query("SELECT * FROM manga WHERE id = :id")
    suspend fun getMangaById(id: String): Manga?

    @Query("SELECT * FROM manga WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchManga(query: String): Flow<List<Manga>>

    @Query("SELECT * FROM manga WHERE status = :status")
    fun getMangaByStatus(status: MangaStatus): Flow<List<Manga>>

    @Query("SELECT * FROM manga WHERE genres LIKE '%' || :genre || '%'")
    fun getMangaByGenre(genre: String): Flow<List<Manga>>

    @Query("UPDATE manga SET readChapters = :readChapters, lastReadDate = :lastReadDate WHERE id = :mangaId")
    suspend fun updateReadProgress(
        mangaId: String,
        readChapters: Int,
        lastReadDate: java.util.Date,
    )

    @Query("UPDATE manga SET isFavorite = :isFavorite WHERE id = :mangaId")
    suspend fun updateFavoriteStatus(
        mangaId: String,
        isFavorite: Boolean,
    )

    @Query("UPDATE manga SET isInLibrary = :isInLibrary WHERE id = :mangaId")
    suspend fun updateLibraryStatus(
        mangaId: String,
        isInLibrary: Boolean,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManga(manga: Manga)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMangaList(mangaList: List<Manga>)

    @Update
    suspend fun updateManga(manga: Manga)

    @Delete
    suspend fun deleteManga(manga: Manga)

    @Query("DELETE FROM manga WHERE id = :mangaId")
    suspend fun deleteMangaById(mangaId: String)
}
