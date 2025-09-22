package com.heartlessveteran.myriad.core.domain.usecase

import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.repository.Source
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for getting chapter pages from a source.
 * This use case handles the business logic for fetching chapter pages.
 * Following Clean Architecture, it coordinates between the domain and data layers.
 */
@Singleton
class GetChapterPagesUseCase @Inject constructor(
    private val sources: Map<String, @JvmSuppressWildcards Source>
) {
    /**
     * Executes the use case to get chapter pages
     *
     * @param sourceId The ID of the source to use
     * @param chapterUrl The URL or identifier of the chapter
     * @return Result containing list of page URLs or error
     */
    suspend operator fun invoke(sourceId: String, chapterUrl: String): Result<List<String>> {
        return try {
            val source = sources[sourceId]
                ?: return Result.Error(
                    IllegalArgumentException("Source not found: $sourceId"),
                    "Source '$sourceId' is not available"
                )

            source.getChapterPages(chapterUrl)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get chapter pages: ${e.message}")
        }
    }
}