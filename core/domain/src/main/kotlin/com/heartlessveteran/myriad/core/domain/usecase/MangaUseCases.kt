package com.heartlessveteran.myriad.core.domain.usecase

import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.repository.MangaRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting library manga.
 * Handles business logic for retrieving user's manga library.
 * Returns reactive Flow for automatic UI updates when data changes.
 */
class GetLibraryMangaUseCase(
    private val mangaRepository: MangaRepository
) {
    /**
     * Gets all manga in the user's library as a reactive stream
     *
     * @return Flow of manga list that updates automatically
     */
    operator fun invoke(): Flow<List<Manga>> {
        return mangaRepository.getLibraryManga()
    }
}

/**
 * Use case for getting manga details by ID.
 * Handles business logic for retrieving specific manga information.
 */
class GetMangaDetailsUseCase(
    private val mangaRepository: MangaRepository
) {
    /**
     * Gets manga details by ID
     *
     * @param mangaId The manga identifier
     * @return Result containing manga details or error
     */
    suspend operator fun invoke(mangaId: String): Result<Manga> {
        return if (mangaId.isBlank()) {
            Result.Error(
                IllegalArgumentException("Manga ID cannot be blank"),
                "Invalid manga ID provided"
            )
        } else {
            mangaRepository.getMangaById(mangaId)
        }
    }
}

/**
 * Use case for adding manga to library.
 * Handles business logic for saving manga to the local database.
 */
class AddMangaToLibraryUseCase(
    private val mangaRepository: MangaRepository
) {
    /**
     * Adds manga to the user's library
     *
     * @param manga The manga to add
     * @return Result indicating success or error
     */
    suspend operator fun invoke(manga: Manga): Result<Unit> {
        return try {
            // Ensure manga is marked as in library
            val libraryManga = manga.copy(
                isInLibrary = true,
                dateAdded = java.util.Date()
            )
            mangaRepository.saveManga(libraryManga)
        } catch (e: Exception) {
            Result.Error(e, "Failed to add manga to library: ${e.message}")
        }
    }
}