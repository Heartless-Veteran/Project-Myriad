package com.heartlessveteran.myriad.di

import android.content.Context
import com.heartlessveteran.myriad.data.services.DownloadServiceImpl
import com.heartlessveteran.myriad.data.services.SourceServiceImpl
import com.heartlessveteran.myriad.domain.services.DownloadService
import com.heartlessveteran.myriad.domain.services.SourceService

/**
 * Manual dependency injection container for Download features.
 * Temporary solution until Hilt/KSP is fully enabled.
 *
 * This container provides:
 * - DownloadService for background downloads
 * - SourceService for online content discovery with real MangaDx integration
 * - Integration between download and source services
 */
object DownloadDiContainer {
    @Volatile
    private var downloadService: DownloadService? = null

    @Volatile
    private var sourceService: SourceService? = null

    /**
     * Get DownloadService instance.
     */
    fun getDownloadService(context: Context): DownloadService =
        downloadService ?: synchronized(this) {
            downloadService ?: DownloadServiceImpl(context).also { downloadService = it }
        }

    /**
     * Get SourceService instance with real MangaDx integration.
     */
    fun getSourceService(): SourceService =
        sourceService ?: synchronized(this) {
            sourceService ?: SourceServiceImpl(
                mangaDxSourceRepository = BrowseDiContainer.sourceRepository
            ).also { sourceService = it }
        }

    /**
     * Clear and reset the cached service instances.
     *
     * Thread-safe: if the cached downloadService is a DownloadServiceImpl, its
     * cleanup() method is invoked before both downloadService and sourceService
     * are set to null. Intended for testing or resetting DI state.
     */
    fun clearInstances() {
        synchronized(this) {
            (downloadService as? DownloadServiceImpl)?.cleanup()
            downloadService = null
            sourceService = null
        }
    }
}
