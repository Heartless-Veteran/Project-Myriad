package com.heartlessveteran.myriad.domain.services

import com.heartlessveteran.myriad.domain.models.Result
import kotlinx.coroutines.flow.Flow

/**
 * Service interface for manga/anime tracking integration with external services.
 *
 * Provides functionality for:
 * - Authentication with tracking services (MyAnimeList, AniList, etc.)
 * - Syncing read/watch progress
 * - Managing tracking status (reading, completed, on-hold, etc.)
 * - Retrieving user's tracking data
 */
interface TrackingService {
    /**
     * Get all available tracking services.
     *
     * @return List of available tracking service providers
     */
    fun getAvailableServices(): List<TrackingServiceProvider>

    /**
     * Get enabled/authenticated tracking services.
     *
     * @return List of authenticated tracking services
     */
    fun getAuthenticatedServices(): List<TrackingServiceProvider>

    /**
     * Start OAuth2 authentication for a tracking service.
     *
     * @param serviceId The tracking service identifier (e.g., "myanimelist", "anilist")
     * @return Result containing authentication URL or error
     */
    suspend fun startAuthentication(serviceId: String): Result<AuthenticationSession>

    /**
     * Complete OAuth2 authentication flow.
     *
     * @param serviceId The tracking service identifier
     * @param authCode The authorization code from OAuth callback
     * @param state The state parameter for security verification
     * @return Result indicating success or failure
     */
    suspend fun completeAuthentication(
        serviceId: String,
        authCode: String,
        state: String,
    ): Result<UserProfile>

    /**
     * Disconnect/logout from a tracking service.
     *
     * @param serviceId The tracking service identifier
     * @return Result indicating success or failure
     */
    suspend fun disconnect(serviceId: String): Result<Unit>

    /**
     * Search for manga/anime on a tracking service.
     *
     * @param serviceId The tracking service identifier
     * @param query Search query
     * @param type Content type (manga/anime)
     * @return Result containing search results
     */
    suspend fun search(
        serviceId: String,
        query: String,
        type: TrackingContentType,
    ): Result<List<TrackingEntry>>

    /**
     * Link local manga/anime to tracking service entry.
     *
     * @param serviceId The tracking service identifier
     * @param localId Local manga/anime ID
     * @param trackingId Remote tracking ID
     * @param type Content type
     * @return Result indicating success or failure
     */
    suspend fun linkContent(
        serviceId: String,
        localId: String,
        trackingId: String,
        type: TrackingContentType,
    ): Result<TrackingLink>

    /**
     * Update progress for linked content.
     *
     * @param serviceId The tracking service identifier
     * @param trackingId Remote tracking ID
     * @param progress Current progress (chapter/episode number)
     * @param status Reading/watching status
     * @param score Optional user score/rating
     * @return Result indicating success or failure
     */
    suspend fun updateProgress(
        serviceId: String,
        trackingId: String,
        progress: Int,
        status: TrackingStatus? = null,
        score: Float? = null,
    ): Result<Unit>

    /**
     * Get user's tracked manga/anime list.
     *
     * @param serviceId The tracking service identifier
     * @param type Content type filter
     * @param status Status filter (optional)
     * @return Flow emitting user's tracking list
     */
    fun getUserList(
        serviceId: String,
        type: TrackingContentType,
        status: TrackingStatus? = null,
    ): Flow<Result<List<TrackingEntry>>>

    /**
     * Get tracking information for specific content.
     *
     * @param serviceId The tracking service identifier
     * @param trackingId Remote tracking ID
     * @return Result containing tracking details
     */
    suspend fun getTrackingInfo(
        serviceId: String,
        trackingId: String,
    ): Result<TrackingEntry>

    /**
     * Sync local progress with tracking service.
     * This automatically updates progress based on local reading/watching data.
     *
     * @param serviceId The tracking service identifier
     * @param localId Local content ID
     * @return Result indicating sync success or failure
     */
    suspend fun syncProgress(
        serviceId: String,
        localId: String,
    ): Result<Unit>

    /**
     * Bulk sync all linked content for a service.
     *
     * @param serviceId The tracking service identifier
     * @return Flow emitting sync progress for each item
     */
    fun bulkSync(serviceId: String): Flow<SyncResult>
}

/**
 * Represents a tracking service provider (MyAnimeList, AniList, etc.).
 */
data class TrackingServiceProvider(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String? = null,
    val websiteUrl: String,
    val isAuthenticated: Boolean = false,
    val supportedTypes: Set<TrackingContentType> = setOf(TrackingContentType.MANGA, TrackingContentType.ANIME),
    val features: Set<TrackingFeature> = emptySet(),
)

/**
 * OAuth2 authentication session data.
 */
data class AuthenticationSession(
    val serviceId: String,
    val authUrl: String,
    val state: String,
    val expiresAt: Long,
)

/**
 * User profile information from tracking service.
 */
data class UserProfile(
    val serviceId: String,
    val userId: String,
    val username: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val profileUrl: String? = null,
)

/**
 * Tracking entry (manga/anime with tracking information).
 */
data class TrackingEntry(
    val trackingId: String,
    val serviceId: String,
    val title: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val type: TrackingContentType,
    val status: TrackingStatus,
    val progress: Int = 0,
    val totalChapters: Int? = null,
    val totalEpisodes: Int? = null,
    val score: Float? = null,
    val maxScore: Float = 10f,
    val startDate: String? = null,
    val endDate: String? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
)

/**
 * Link between local content and tracking service.
 */
data class TrackingLink(
    val localId: String,
    val serviceId: String,
    val trackingId: String,
    val type: TrackingContentType,
    val title: String,
    val lastSynced: Long = System.currentTimeMillis(),
)

/**
 * Sync operation result.
 */
data class SyncResult(
    val serviceId: String,
    val localId: String,
    val trackingId: String,
    val success: Boolean,
    val error: String? = null,
    val updatedProgress: Int? = null,
)

/**
 * Content type for tracking.
 */
enum class TrackingContentType {
    MANGA,
    ANIME,
}

/**
 * Reading/watching status.
 */
enum class TrackingStatus {
    PLANNING,
    CURRENT,
    COMPLETED,
    PAUSED,
    DROPPED,
    REPEATING,
}

/**
 * Features supported by tracking services.
 */
enum class TrackingFeature {
    OAUTH2_AUTH,
    PROGRESS_SYNC,
    SCORE_SYNC,
    STATUS_SYNC,
    LIST_IMPORT,
    LIST_EXPORT,
    BULK_SYNC,
    REAL_TIME_SYNC,
}
