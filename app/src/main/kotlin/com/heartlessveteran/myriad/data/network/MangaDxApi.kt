package com.heartlessveteran.myriad.data.network

import com.heartlessveteran.myriad.data.network.dto.MangaListDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the MangaDex API.
 * Defines the endpoints and parameters for fetching manga data.
 */
interface MangaDxApi {

    @GET("manga")
    suspend fun getLatestUpdates(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int,
        @Query("order[latestUploadedChapter]") order: String = "desc",
        @Query("includes[]") includes: String = "cover_art"
    ): MangaListDto

    @GET("manga")
    suspend fun search(
        @Query("title") query: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int,
        @Query("includes[]") includes: String = "cover_art"
    ): MangaListDto

    companion object {
        const val BASE_URL = "https://api.mangadex.org/"
    }
}