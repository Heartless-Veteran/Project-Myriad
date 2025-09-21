package com.heartlessveteran.myriad.core.domain.repository

import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.core.domain.entities.MangaChapter
import com.heartlessveteran.myriad.core.domain.model.Result

/**
 * Interface for manga sources (e.g., MangaDex, local files)
 * This is the core interface that all manga sources must implement
 */
interface Source {
    /**
     * Unique identifier for this source
     */
    val id: String

    /**
     * Display name of the source
     */
    val name: String

    /**
     * Language code supported by this source
     */
    val lang: String

    /**
     * Base URL for the source (if applicable)
     */
    val baseUrl: String

    /**
     * Fetches the latest manga from the source
     *
     * @param page Page number to fetch
     * @return Result containing list of manga or error
     */
    suspend fun getLatestManga(page: Int): Result<List<Manga>>

    /**
     * Fetches detailed information about a specific manga
     *
     * @param url URL or identifier of the manga
     * @return Result containing manga details or error
     */
    suspend fun getMangaDetails(url: String): Result<Manga>

    /**
     * Fetches pages for a specific chapter
     *
     * @param url URL or identifier of the chapter
     * @return Result containing list of page URLs or error
     */
    suspend fun getChapterPages(url: String): Result<List<String>>

    /**
     * Searches for manga based on query
     *
     * @param query Search query
     * @param page Page number
     * @return Result containing list of matching manga or error
     */
    suspend fun searchManga(query: String, page: Int): Result<List<Manga>>

    /**
     * Fetches popular manga from the source
     *
     * @param page Page number to fetch
     * @return Result containing list of popular manga or error
     */
    suspend fun getPopularManga(page: Int): Result<List<Manga>>

    /**
     * Fetches chapters for a specific manga
     *
     * @param manga The manga to get chapters for
     * @return Result containing list of chapters or error
     */
    suspend fun getChapterList(manga: Manga): Result<List<MangaChapter>>
}