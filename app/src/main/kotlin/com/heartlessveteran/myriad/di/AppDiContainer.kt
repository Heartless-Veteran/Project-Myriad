package com.heartlessveteran.myriad.di

import android.content.Context
import com.heartlessveteran.myriad.data.services.OCRService
import com.heartlessveteran.myriad.domain.repository.MangaRepository
import com.heartlessveteran.myriad.domain.repository.SourceRepository
import com.heartlessveteran.myriad.domain.services.DownloadService
import com.heartlessveteran.myriad.domain.services.FileManagerService
import com.heartlessveteran.myriad.domain.services.SourceService
import com.heartlessveteran.myriad.domain.services.TrackingService
import com.heartlessveteran.myriad.domain.services.BackupService
import com.heartlessveteran.myriad.domain.usecase.DownloadMangaUseCase
import com.heartlessveteran.myriad.domain.usecase.GetLatestMangaUseCase
import com.heartlessveteran.myriad.domain.usecase.ImportMangaFromFileUseCase
import com.heartlessveteran.myriad.domain.usecase.SearchMangaUseCase
import com.heartlessveteran.myriad.services.EnhancedAIService

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
     * Returns a DownloadService configured for background downloads.
     *
     * @param context Android Context used to obtain/create the service (preferably the application context).
     * @return A DownloadService instance for performing background download operations.
     */
    fun getDownloadService(context: Context): DownloadService = DownloadDiContainer.getDownloadService(context)

    /**
     * Returns the shared SourceService used for online content discovery.
     *
     * @return The SourceService instance.
     */
    fun getSourceService(): SourceService = DownloadDiContainer.getSourceService()

    /**
     * Returns the TrackingService for progress tracking with external services.
     *
     * @param context Android Context used to obtain the service.
     * @return The TrackingService instance.
     */
    fun getTrackingService(context: Context): TrackingService = DownloadDiContainer.getTrackingService(context)

    /**
     * Returns the BackupService for data backup and restore operations.
     *
     * @param context Android Context used to obtain the service.
     * @return The BackupService instance.
     */
    fun getBackupService(context: Context): BackupService = DownloadDiContainer.getBackupService(context)

    /**
     * Get MangaRepository for manga data operations.
     */
    fun getMangaRepository(context: Context): MangaRepository = LibraryDiContainer.getMangaRepository(context)

    /**
     * Returns the shared SourceRepository used to access online manga source data.
     *
     * This returns the singleton instance provided by BrowseDiContainer.
     *
     * @return The SourceRepository for online manga sources.
     */
    fun getSourceRepository(): SourceRepository = BrowseDiContainer.sourceRepository

    /**
     * Creates an [ImportMangaFromFileUseCase] configured to perform manga imports from files.
     *
     * @param context Android Context used to obtain the file manager service required by the use case.
     * @return A configured [ImportMangaFromFileUseCase].
     */
    fun getImportMangaFromFileUseCase(context: Context): ImportMangaFromFileUseCase =
        ImportMangaFromFileUseCase(
            fileManagerService = getFileManagerService(context),
        )

    /**
     * Create a DownloadMangaUseCase configured with the DownloadService resolved from the given Android context.
     *
     * @param context Android Context used to obtain the DownloadService.
     * @return A DownloadMangaUseCase instance ready for download operations.
     */
    fun getDownloadMangaUseCase(context: Context): DownloadMangaUseCase =
        DownloadMangaUseCase(
            downloadService = getDownloadService(context),
        )

    /**
     * Returns the shared GetLatestMangaUseCase used to fetch the latest manga.
     *
     * This provides the singleton instance supplied by BrowseDiContainer.
     *
     * @return The GetLatestMangaUseCase instance.
     */
    fun getGetLatestMangaUseCase(): GetLatestMangaUseCase = BrowseDiContainer.getLatestMangaUseCase

    /**
     * Returns the application's shared SearchMangaUseCase.
     *
     * The instance is sourced from BrowseDiContainer (singleton).
     *
     * @return The SearchMangaUseCase used for searching manga.
     */
    fun getSearchMangaUseCase(): SearchMangaUseCase = BrowseDiContainer.searchMangaUseCase

    /**
     * Get OCRService for text recognition and translation.
     */
    fun getOCRService(): OCRService = AIDiContainer.getOCRService()

    /**
     * Get EnhancedAIService for coordinated AI operations.
     */
    fun getEnhancedAIService(): EnhancedAIService = AIDiContainer.getEnhancedAIService()

    /**
     * Clears cached instances in DI containers.
     *
     * Resets LibraryDiContainer, DownloadDiContainer, and AIDiContainer (useful for tests or resetting app state).
     * Note: BrowseDiContainer is not cleared because it exposes singleton objects.
     */
    fun clearInstances() {
        LibraryDiContainer.clearInstances()
        DownloadDiContainer.clearInstances()
        AIDiContainer.clearInstances()
        // BrowseDiContainer doesn't need clearing as it uses singleton objects
    }
}
