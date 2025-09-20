package com.heartlessveteran.myriad.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.heartlessveteran.myriad.domain.entities.Anime
import com.heartlessveteran.myriad.domain.entities.AnimeStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Anime operations
 */
@Dao
interface AnimeDao {
    @Query("SELECT * FROM anime ORDER BY lastUpdated DESC")
    fun getAllAnime(): Flow<List<Anime>>

    @Query("SELECT * FROM anime WHERE isInLibrary = 1 ORDER BY lastUpdated DESC")
    fun getLibraryAnime(): Flow<List<Anime>>

    @Query("SELECT * FROM anime WHERE isFavorite = 1 ORDER BY lastUpdated DESC")
    fun getFavoriteAnime(): Flow<List<Anime>>

    @Query("SELECT * FROM anime WHERE id = :id")
    suspend fun getAnimeById(id: String): Anime?

    @Query("SELECT * FROM anime WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchAnime(query: String): Flow<List<Anime>>

    @Query("SELECT * FROM anime WHERE status = :status")
    fun getAnimeByStatus(status: AnimeStatus): Flow<List<Anime>>

    /**
     * Returns a flow that emits lists of anime whose `genres` column contains the given genre.
     *
     * The match is performed using a SQL `LIKE` substring match (i.e., any row where `genres` contains `genre`).
     *
     * @param genre The genre substring to search for (matched anywhere within the `genres` column).
     * @return A Flow that emits lists of matching Anime entities.
     */
    @Query("SELECT * FROM anime WHERE genres LIKE '%' || :genre || '%'")
    fun getAnimeByGenre(genre: String): Flow<List<Anime>>

    /**
     * Update the watch progress for a specific anime record.
     *
     * Suspends and updates the stored `watchedEpisodes` count and `lastWatchedDate` for the anime with the given `animeId`.
     * If no record matches `animeId`, the call completes without affecting any rows.
     *
     * @param animeId The unique identifier of the anime to update.
     * @param watchedEpisodes The new number of watched episodes to store.
     * @param lastWatchedDate The timestamp of the last watched episode.
     */
    @Query(
        "UPDATE anime SET watchedEpisodes = :watchedEpisodes, lastWatchedDate = :lastWatchedDate WHERE id = :animeId",
    )
    suspend fun updateWatchProgress(
        animeId: String,
        watchedEpisodes: Int,
        lastWatchedDate: java.util.Date,
    )

    @Query("UPDATE anime SET isFavorite = :isFavorite WHERE id = :animeId")
    suspend fun updateFavoriteStatus(
        animeId: String,
        isFavorite: Boolean,
    )

    @Query("UPDATE anime SET isInLibrary = :isInLibrary WHERE id = :animeId")
    suspend fun updateLibraryStatus(
        animeId: String,
        isInLibrary: Boolean,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnime(anime: Anime)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimeList(animeList: List<Anime>)

    @Update
    suspend fun updateAnime(anime: Anime)

    @Delete
    suspend fun deleteAnime(anime: Anime)

    @Query("DELETE FROM anime WHERE id = :animeId")
    suspend fun deleteAnimeById(animeId: String)
}
