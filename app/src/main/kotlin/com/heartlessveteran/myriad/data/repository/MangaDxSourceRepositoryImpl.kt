package com.heartlessveteran.myriad.data.repository

import android.util.Log
import com.heartlessveteran.myriad.data.network.MangaDxApi
import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.entities.MangaStatus
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date
import java.util.UUID

/**
 * A concrete implementation of the [SourceRepository] for MangaDx.
 * It uses the [MangaDxApi] to fetch data and maps it to the domain entities.
 */
class MangaDxSourceRepositoryImpl(
    private val api: MangaDxApi,
) : SourceRepository {
    companion object {
        private const val TAG = "MangaDxSourceRepositoryImpl"
    }

    /**
     * Streams the latest manga from MangaDx for the given 1-based page.
     *
     * Emits Result.Loading, then Result.Success with a list of Manga on success or Result.Error on failure.
     * Each page contains 20 items; an internal offset is computed as (page - 1) * 20. DTOs from the API
     * are mapped to domain Manga objects (cover URL resolved when cover art is present, titles prefer
     * English then any available title, status mapped to MangaStatus, tags → genres, etc.).
     *
     * @param page 1-based page number used to compute the API offset (20 items per page).
     * @return Flow that emits the loading state and then either a success with a list of Manga or an error.
     */
    override fun getLatestManga(page: Int): Flow<Result<List<Manga>>> =
        flow {
            try {
                emit(Result.Loading)
                val offset = (page - 1) * 20
                Log.d(TAG, "Fetching latest manga: page=$page, offset=$offset")

                val response = api.getLatestUpdates(offset = offset)
                val mangaList =
                    response.data.map { dto ->
                        // Convert DTO to domain entity
                        val coverFileName =
                            dto.relationships
                                .firstOrNull { it.type == "cover_art" }
                                ?.attributes
                                ?.fileName
                        val coverUrl =
                            if (coverFileName != null) {
                                "https://uploads.mangadx.org/covers/${dto.id}/$coverFileName.256.jpg"
                            } else {
                                null
                            }

                        Manga(
                            id = UUID.randomUUID().toString(),
                            title =
                                dto.attributes.title["en"] ?: dto.attributes.title.values
                                    .firstOrNull() ?: "No Title",
                            description = dto.attributes.description["en"] ?: "",
                            author = "Unknown", // TODO: Extract from relationships
                            artist = "Unknown", // TODO: Extract from relationships
                            status =
                                when (dto.attributes.status?.lowercase()) {
                                    "ongoing" -> MangaStatus.ONGOING
                                    "completed" -> MangaStatus.COMPLETED
                                    "hiatus" -> MangaStatus.HIATUS
                                    "cancelled" -> MangaStatus.CANCELLED
                                    else -> MangaStatus.UNKNOWN
                                },
                            genres = dto.attributes.tags.mapNotNull { it.attributes.name["en"] },
                            coverImageUrl = coverUrl,
                            lastUpdated = Date(),
                            source = "mangadx",
                            sourceId = dto.id,
                            isLocal = false,
                            isInLibrary = false,
                        )
                    }

                Log.d(TAG, "Successfully fetched ${mangaList.size} manga from MangaDx")
                emit(Result.Success(mangaList))
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching latest manga from MangaDx", e)
                emit(Result.Error(e, "Failed to fetch latest manga: ${e.localizedMessage}"))
            }
        }

    /**
     * Searches MangaDex for manga matching the given query and emits paginated results.
     *
     * Performs a search request with 20 items per page (offset = (page - 1) * 20), maps each returned DTO
     * to a Manga domain entity (including title resolution, genre extraction, cover URL construction,
     * status mapping, and generated id), and emits the results as a Flow of Result.
     *
     * @param query Search text to send to the remote API.
     * @param page  1-based page number. Pages contain 20 items each.
     * @return A Flow that first emits Result.Loading and then either Result.Success with a list of Manga
     *         or Result.Error with failure details if the request or mapping fails.
     */
    override fun searchManga(
        query: String,
        page: Int,
    ): Flow<Result<List<Manga>>> =
        flow {
            try {
                emit(Result.Loading)
                val offset = (page - 1) * 20
                Log.d(TAG, "Searching manga: query='$query', page=$page, offset=$offset")

                val response = api.search(query = query, offset = offset)
                val mangaList =
                    response.data.map { dto ->
                        // Convert DTO to domain entity
                        val coverFileName =
                            dto.relationships
                                .firstOrNull { it.type == "cover_art" }
                                ?.attributes
                                ?.fileName
                        val coverUrl =
                            if (coverFileName != null) {
                                "https://uploads.mangadx.org/covers/${dto.id}/$coverFileName.256.jpg"
                            } else {
                                null
                            }

                        Manga(
                            id = UUID.randomUUID().toString(),
                            title =
                                dto.attributes.title["en"] ?: dto.attributes.title.values
                                    .firstOrNull() ?: "No Title",
                            description = dto.attributes.description["en"] ?: "",
                            author = "Unknown", // TODO: Extract from relationships
                            artist = "Unknown", // TODO: Extract from relationships
                            status =
                                when (dto.attributes.status?.lowercase()) {
                                    "ongoing" -> MangaStatus.ONGOING
                                    "completed" -> MangaStatus.COMPLETED
                                    "hiatus" -> MangaStatus.HIATUS
                                    "cancelled" -> MangaStatus.CANCELLED
                                    else -> MangaStatus.UNKNOWN
                                },
                            genres = dto.attributes.tags.mapNotNull { it.attributes.name["en"] },
                            coverImageUrl = coverUrl,
                            lastUpdated = Date(),
                            source = "mangadx",
                            sourceId = dto.id,
                            isLocal = false,
                            isInLibrary = false,
                        )
                    }

                Log.d(TAG, "Successfully found ${mangaList.size} manga matching '$query'")
                emit(Result.Success(mangaList))
            } catch (e: Exception) {
                Log.e(TAG, "Error searching manga on MangaDx for query: $query", e)
                emit(Result.Error(e, "Search failed: ${e.localizedMessage}"))
            }
        }
}
