package com.heartlessveteran.myriad.domain.usecase

import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for fetching the latest manga.
 * Encapsulates the business logic of retrieving the latest manga list,
 * simplifying the ViewModel's responsibilities.
 */
class GetLatestMangaUseCase
    @Inject
    constructor(
        private val sourceRepository: SourceRepository,
    ) {
        /**
         * Invokes the use case.
         * @param page The page number to fetch.
         * @return A Flow emitting the result of the operation.
         */
        operator fun invoke(page: Int): Flow<Result<List<Manga>>> = sourceRepository.getLatestManga(page)
    }
