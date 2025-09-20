package com.heartlessveteran.myriad.domain.services

import com.heartlessveteran.myriad.domain.models.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * Service interface for backup and restore functionality.
 *
 * Provides functionality for:
 * - Creating local backups of user data (library, reading progress, settings)
 * - Restoring data from backup files
 * - Managing backup files and storage
 * - Integration with Android Storage Access Framework
 */
interface BackupService {
    /**
     * Create a complete backup of user data.
     *
     * @param includeLibrary Whether to include manga/anime library data
     * @param includeProgress Whether to include reading/watching progress
     * @param includeSettings Whether to include app settings and preferences
     * @param includeCategories Whether to include user-defined categories
     * @param includeTrackingLinks Whether to include external tracking service links
     * @return Result containing backup metadata or error
     */
    suspend fun createBackup(
        includeLibrary: Boolean = true,
        includeProgress: Boolean = true,
        includeSettings: Boolean = true,
        includeCategories: Boolean = true,
        includeTrackingLinks: Boolean = true,
    ): Result<BackupMetadata>

    /**
     * Create a backup and save to a specific file using Storage Access Framework.
     *
     * @param targetUri Android Storage Access Framework URI for the backup file
     * @param options Backup configuration options
     * @return Flow emitting backup progress and final result
     */
    fun createBackupToFile(
        targetUri: String,
        options: BackupOptions = BackupOptions(),
    ): Flow<BackupProgress>

    /**
     * Restore data from a backup file.
     *
     * @param sourceUri Android Storage Access Framework URI for the backup file
     * @param options Restore configuration options
     * @return Flow emitting restore progress and final result
     */
    fun restoreFromFile(
        sourceUri: String,
        options: RestoreOptions = RestoreOptions(),
    ): Flow<RestoreProgress>

    /**
     * Validate a backup file without restoring it.
     *
     * @param sourceUri URI of the backup file to validate
     * @return Result containing backup validation info or error
     */
    suspend fun validateBackup(sourceUri: String): Result<BackupValidation>

    /**
     * Get list of local backup files.
     *
     * @return List of backup files with metadata
     */
    suspend fun getLocalBackups(): Result<List<BackupMetadata>>

    /**
     * Delete a local backup file.
     *
     * @param backupId Backup identifier
     * @return Result indicating success or failure
     */
    suspend fun deleteBackup(backupId: String): Result<Unit>

    /**
     * Get backup service configuration and status.
     *
     * @return Current backup service configuration
     */
    fun getBackupConfiguration(): BackupConfiguration

    /**
     * Update backup service configuration.
     *
     * @param config New backup configuration
     * @return Result indicating success or failure
     */
    suspend fun updateConfiguration(config: BackupConfiguration): Result<Unit>

    /**
     * Create an automatic backup if conditions are met.
     *
     * @return Result containing backup info or reason for skipping
     */
    suspend fun createAutomaticBackup(): Result<AutoBackupResult>

    /**
     * Cleanup old backup files based on retention policy.
     *
     * @return Result with cleanup summary
     */
    suspend fun cleanupOldBackups(): Result<CleanupResult>
}

/**
 * Backup configuration options.
 */
data class BackupOptions(
    val includeLibrary: Boolean = true,
    val includeProgress: Boolean = true,
    val includeSettings: Boolean = true,
    val includeCategories: Boolean = true,
    val includeTrackingLinks: Boolean = true,
    val compressionLevel: CompressionLevel = CompressionLevel.MEDIUM,
    val encryptionEnabled: Boolean = false,
    val encryptionPassword: String? = null,
)

/**
 * Restore configuration options.
 */
data class RestoreOptions(
    val restoreLibrary: Boolean = true,
    val restoreProgress: Boolean = true,
    val restoreSettings: Boolean = true,
    val restoreCategories: Boolean = true,
    val restoreTrackingLinks: Boolean = true,
    val mergeMode: MergeMode = MergeMode.REPLACE,
    val validateIntegrity: Boolean = true,
)

/**
 * Backup service configuration.
 */
data class BackupConfiguration(
    val autoBackupEnabled: Boolean = false,
    val autoBackupFrequency: AutoBackupFrequency = AutoBackupFrequency.WEEKLY,
    val maxBackupFiles: Int = 5,
    val compressionEnabled: Boolean = true,
    val backupLocation: BackupLocation = BackupLocation.INTERNAL,
    val includeCovers: Boolean = false,
    val notificationsEnabled: Boolean = true,
)

/**
 * Backup metadata information.
 */
data class BackupMetadata(
    val id: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val createdAt: Long,
    val version: String,
    val appVersion: String,
    val itemCounts: BackupItemCounts,
    val options: BackupOptions,
    val checksum: String? = null,
)

/**
 * Count of items in backup.
 */
@Serializable
data class BackupItemCounts(
    val mangaCount: Int = 0,
    val animeCount: Int = 0,
    val categoryCount: Int = 0,
    val trackingLinkCount: Int = 0,
    val settingsCount: Int = 0,
)

/**
 * Backup progress information.
 */
data class BackupProgress(
    val phase: BackupPhase,
    val progress: Float, // 0.0 to 1.0
    val currentItem: String? = null,
    val itemsProcessed: Int = 0,
    val totalItems: Int = 0,
    val error: String? = null,
    val completed: Boolean = false,
    val result: BackupMetadata? = null,
)

/**
 * Restore progress information.
 */
data class RestoreProgress(
    val phase: RestorePhase,
    val progress: Float, // 0.0 to 1.0
    val currentItem: String? = null,
    val itemsProcessed: Int = 0,
    val totalItems: Int = 0,
    val error: String? = null,
    val completed: Boolean = false,
    val result: RestoreResult? = null,
)

/**
 * Backup validation result.
 */
data class BackupValidation(
    val isValid: Boolean,
    val version: String,
    val createdAt: Long,
    val itemCounts: BackupItemCounts,
    val compatibility: CompatibilityStatus,
    val issues: List<ValidationIssue> = emptyList(),
)

/**
 * Restore operation result.
 */
data class RestoreResult(
    val success: Boolean,
    val itemsRestored: RestoreItemCounts,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
)

/**
 * Count of restored items.
 */
data class RestoreItemCounts(
    val mangaRestored: Int = 0,
    val animeRestored: Int = 0,
    val categoriesRestored: Int = 0,
    val trackingLinksRestored: Int = 0,
    val settingsRestored: Int = 0,
)

/**
 * Automatic backup result.
 */
data class AutoBackupResult(
    val created: Boolean,
    val backupMetadata: BackupMetadata? = null,
    val reason: String? = null, // Reason if backup was skipped
)

/**
 * Cleanup operation result.
 */
data class CleanupResult(
    val filesDeleted: Int,
    val spaceFreed: Long,
    val errors: List<String> = emptyList(),
)

/**
 * Validation issue found in backup.
 */
data class ValidationIssue(
    val type: IssueType,
    val description: String,
    val severity: IssueSeverity,
)

/**
 * Backup phases.
 */
enum class BackupPhase {
    INITIALIZING,
    COLLECTING_DATA,
    PROCESSING_LIBRARY,
    PROCESSING_PROGRESS,
    PROCESSING_SETTINGS,
    PROCESSING_CATEGORIES,
    PROCESSING_TRACKING,
    COMPRESSING,
    ENCRYPTING,
    WRITING_FILE,
    FINALIZING,
    COMPLETED,
}

/**
 * Restore phases.
 */
enum class RestorePhase {
    INITIALIZING,
    VALIDATING,
    READING_FILE,
    DECRYPTING,
    DECOMPRESSING,
    PARSING_DATA,
    RESTORING_SETTINGS,
    RESTORING_CATEGORIES,
    RESTORING_LIBRARY,
    RESTORING_PROGRESS,
    RESTORING_TRACKING,
    FINALIZING,
    COMPLETED,
}

/**
 * Compression levels.
 */
enum class CompressionLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
    MAXIMUM,
}

/**
 * Data merge modes during restore.
 */
enum class MergeMode {
    REPLACE, // Replace existing data
    MERGE, // Merge with existing data
    SKIP, // Skip if data exists
}

/**
 * Auto backup frequency.
 */
enum class AutoBackupFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    NEVER,
}

/**
 * Backup storage location.
 */
enum class BackupLocation {
    INTERNAL,
    EXTERNAL,
    DOCUMENTS,
    DOWNLOADS,
}

/**
 * Backup compatibility status.
 */
enum class CompatibilityStatus {
    COMPATIBLE,
    PARTIALLY_COMPATIBLE,
    INCOMPATIBLE,
    UNKNOWN,
}

/**
 * Validation issue types.
 */
enum class IssueType {
    CORRUPTION,
    VERSION_MISMATCH,
    MISSING_DATA,
    INVALID_FORMAT,
    CHECKSUM_MISMATCH,
}

/**
 * Issue severity levels.
 */
enum class IssueSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL,
}
