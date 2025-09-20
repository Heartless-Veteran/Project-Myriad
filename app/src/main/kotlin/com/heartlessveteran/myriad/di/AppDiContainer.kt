package com.heartlessveteran.myriad.di

import android.content.Context
import com.heartlessveteran.myriad.domain.repository.MangaRepository
import com.heartlessveteran.myriad.domain.repository.SourceRepository
import com.heartlessveteran.myriad.domain.services.DownloadService
import com.heartlessveteran.myriad.domain.services.FileManagerService
import com.heartlessveteran.myriad.domain.services.SourceService
import com.heartlessveteran.myriad.domain.usecase.DownloadMangaUseCase
import com.heartlessveteran.myriad.domain.usecase.GetLatestMangaUseCase
import com.heartlessveteran.myriad.domain.usecase.ImportMangaFromFileUseCase
import com.heartlessveteran.myriad.domain.usecase.SearchMangaUseCase

/**
 * Master dependency injection container that provides access to all services and use cases.
 * This centralized container simplifies dependency access across the app.
 */
object AppDiContainer {
    /**
     * Get FileManagerService for local file operations.
     */
    fun getFileManagerService(context: Context): FileManagerService = LibraryDiContainer.getFileManagerService(context)

    /**
     * Get DownloadService for background downloads.
     */
    fun getDownloadService(context: Context): DownloadService = DownloadDiContainer.getDownloadService(context)

    /**
     * Get SourceService for online content discovery.
     */
    fun getSourceService(): SourceService = DownloadDiContainer.getSourceService()

    /**
     * Get MangaRepository for manga data operations.
     */
    fun getMangaRepository(context: Context): MangaRepository = LibraryDiContainer.getMangaRepository(context)

    /**
     * Get SourceRepository for online manga data.
     */
    fun getSourceRepository(): SourceRepository = BrowseDiContainer.sourceRepository

    /**
     * Get ImportMangaFromFileUseCase for file import operations.
     */
    fun getImportMangaFromFileUseCase(context: Context): ImportMangaFromFileUseCase =
        ImportMangaFromFileUseCase(
            fileManagerService = getFileManagerService(context),
        )

    /**
     * Get DownloadMangaUseCase for download operations.
     */
    fun getDownloadMangaUseCase(context: Context): DownloadMangaUseCase =
        DownloadMangaUseCase(
            downloadService = getDownloadService(context),
        )

    /**
     * Get GetLatestMangaUseCase for fetching latest manga.
     */
    fun getGetLatestMangaUseCase(): GetLatestMangaUseCase = BrowseDiContainer.getLatestMangaUseCase

    /**
     * Get SearchMangaUseCase for searching manga.
     */
    fun getSearchMangaUseCase(): SearchMangaUseCase = BrowseDiContainer.searchMangaUseCase

    /**
     * Clear all cached instances (useful for testing).
     */
    fun clearInstances() {
        LibraryDiContainer.clearInstances()
        DownloadDiContainer.clearInstances()
        // BrowseDiContainer doesn't need clearing as it uses singleton objects
    }
}
