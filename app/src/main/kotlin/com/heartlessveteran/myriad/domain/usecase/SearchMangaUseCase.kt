package com.heartlessveteran.myriad.domain.usecase

import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for searching manga by query.
 * Encapsulates the business logic of searching manga,
 * simplifying the ViewModel's responsibilities.
 */
class SearchMangaUseCase(
    private val sourceRepository: SourceRepository,
) {
    /**
     * Invokes the use case.
     * @param query The search query.
     * @param page The page number to fetch.
     * @return A Flow emitting the result of the operation.
     */
    operator fun invoke(
        query: String,
        page: Int,
    ): Flow<Result<List<Manga>>> = sourceRepository.searchManga(query, page)
}
