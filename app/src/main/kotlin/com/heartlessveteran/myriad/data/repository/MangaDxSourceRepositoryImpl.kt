package com.heartlessveteran.myriad.data.repository

import com.heartlessveteran.myriad.data.network.MangaDxApi
import com.heartlessveteran.myriad.domain.model.Manga
import com.heartlessveteran.myriad.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A concrete implementation of the [SourceRepository] for MangaDx.
 * It uses the [MangaDxApi] to fetch data and maps it to the domain models.
 */
class MangaDxSourceRepositoryImpl(
    private val api: MangaDxApi,
) : SourceRepository {
    override fun getLatestManga(page: Int): Flow<Result<List<Manga>>> =
        flow {
            try {
                val offset = (page - 1) * 20
                val response = api.getLatestUpdates(offset = offset)
                val mangaList = response.data.map { it.toDomainModel() }
                emit(Result.success(mangaList))
            } catch (e: Exception) {
                // In a real app, log the exception and provide a more specific error message
                emit(Result.failure(e))
            }
        }

    override fun searchManga(
        query: String,
        page: Int,
    ): Flow<Result<List<Manga>>> =
        flow {
            try {
                val offset = (page - 1) * 20
                val response = api.search(query = query, offset = offset)
                val mangaList = response.data.map { it.toDomainModel() }
                emit(Result.success(mangaList))
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        }
}
