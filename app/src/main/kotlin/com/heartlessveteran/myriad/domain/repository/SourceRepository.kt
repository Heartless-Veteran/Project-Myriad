package com.heartlessveteran.myriad.domain.repository

import com.heartlessveteran.myriad.domain.model.Manga
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining the contract for fetching data from an online source.
 * The domain layer depends on this abstraction, allowing the UI to remain
 * unaware of the specific source (MangaDex, etc.) being used.
 */
interface SourceRepository {

    /**
     * Fetches the latest updated manga from the source.
     *
     * @param page The page number to retrieve.
     * @return A Flow emitting a Result containing a list of [Manga] on success, or an error.
     */
    fun getLatestManga(page: Int): Flow<Result<List<Manga>>>

    /**
     * Searches for manga on the source based on a query.
     *
     * @param query The search term.
     * @param page The page number to retrieve.
     * @return A Flow emitting a Result containing a list of [Manga] on success, or an error.
     */
    fun searchManga(query: String, page: Int): Flow<Result<List<Manga>>>
}