package com.heartlessveteran.myriad.di

import android.content.Context
import com.heartlessveteran.myriad.data.services.DownloadServiceImpl
import com.heartlessveteran.myriad.data.services.SourceServiceImpl
import com.heartlessveteran.myriad.data.services.TrackingServiceImpl
import com.heartlessveteran.myriad.domain.services.DownloadService
import com.heartlessveteran.myriad.domain.services.SourceService
import com.heartlessveteran.myriad.domain.services.TrackingService

/**
 * Manual dependency injection container for Download features.
 * Temporary solution until Hilt/KSP is fully enabled.
 *
 * This container provides:
 * - DownloadService for background downloads
 * - SourceService for online content discovery with real MangaDx integration
 * - TrackingService for progress tracking with external services
 * - Integration between download and source services
 */
object DownloadDiContainer {
    @Volatile
    private var downloadService: DownloadService? = null

    @Volatile
    private var sourceService: SourceService? = null

    @Volatile
    private var trackingService: TrackingService? = null

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
     * Get TrackingService instance.
     */
    fun getTrackingService(context: Context): TrackingService =
        trackingService ?: synchronized(this) {
            trackingService ?: TrackingServiceImpl(context).also { trackingService = it }
        }

    /**
     * Clear and reset the cached service instances.
     *
     * Thread-safe: if the cached downloadService is a DownloadServiceImpl, its
     * cleanup() method is invoked before all services are set to null. 
     * Intended for testing or resetting DI state.
     */
    fun clearInstances() {
        synchronized(this) {
            (downloadService as? DownloadServiceImpl)?.cleanup()
            downloadService = null
            sourceService = null
            trackingService = null
        }
    }
}
