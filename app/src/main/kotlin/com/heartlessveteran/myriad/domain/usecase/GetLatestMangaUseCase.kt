package com.heartlessveteran.myriad.domain.usecase

import com.heartlessveteran.myriad.domain.model.Manga
import com.heartlessveteran.myriad.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for fetching the latest manga.
 * Encapsulates the business logic of retrieving the latest manga list,
 * simplifying the ViewModel's responsibilities.
 */
class GetLatestMangaUseCase(
    private val sourceRepository: SourceRepository,
) {
    /**
     * Invokes the use case.
     * @param page The page number to fetch.
     * @return A Flow emitting the result of the operation.
     */
    operator fun invoke(page: Int): Flow<Result<List<Manga>>> = sourceRepository.getLatestManga(page)
}
