package com.heartlessveteran.myriad.domain.vault

import com.heartlessveteran.myriad.domain.models.Result
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * The Vault - Local Media Engine for offline-first content management
 * 
 * This provides the core interface for managing local media files with support for:
 * - .cbz/.cbr manga archives and .mp4/.mkv/.avi anime files
 * - Smart organization and metadata extraction
 * - Offline-first access with intelligent caching
 * - Collections and advanced search/filtering
 * - File system abstraction for managed storage
 */
interface VaultService {
    
    /**
     * Import media from a local file
     */
    suspend fun importMedia(filePath: String): Result<VaultItem>
    
    /**
     * Import media from a directory (recursive scan)
     */
    suspend fun importFromDirectory(
        directoryPath: String,
        recursive: Boolean = true,
        mediaTypes: Set<MediaType> = setOf(MediaType.MANGA, MediaType.ANIME)
    ): Result<List<VaultItem>>
    
    /**
     * Get all items in the vault
     */
    fun getAllItems(): Flow<List<VaultItem>>
    
    /**
     * Get items by type
     */
    fun getItemsByType(type: MediaType): Flow<List<VaultItem>>
    
    /**
     * Search vault items
     */
    suspend fun searchItems(
        query: String,
        filters: VaultFilters = VaultFilters()
    ): Result<List<VaultItem>>
    
    /**
     * Get item details by ID
     */
    suspend fun getItemDetails(itemId: String): Result<VaultItemDetail>
    
    /**
     * Get content for an item (pages for manga, episodes for anime)
     */
    suspend fun getItemContent(itemId: String): Result<List<VaultContent>>
    
    /**
     * Create a collection
     */
    suspend fun createCollection(
        name: String,
        description: String? = null,
        tags: List<String> = emptyList()
    ): Result<VaultCollection>
    
    /**
     * Add item to collection
     */
    suspend fun addToCollection(collectionId: String, itemId: String): Result<Unit>
    
    /**
     * Remove item from collection
     */
    suspend fun removeFromCollection(collectionId: String, itemId: String): Result<Unit>
    
    /**
     * Get all collections
     */
    fun getCollections(): Flow<List<VaultCollection>>
    
    /**
     * Delete an item from the vault
     */
    suspend fun deleteItem(itemId: String, deleteFile: Boolean = false): Result<Unit>
    
    /**
     * Get vault statistics
     */
    suspend fun getVaultStatistics(): Result<VaultStatistics>
    
    /**
     * Rescan the vault for changes
     */
    suspend fun rescanVault(): Result<Unit>
    
    /**
     * Backup vault metadata
     */
    suspend fun backupMetadata(backupPath: String): Result<String>
    
    /**
     * Restore vault metadata
     */
    suspend fun restoreMetadata(backupPath: String): Result<Unit>
}

/**
 * Media types supported by the Vault
 */
enum class MediaType(val displayName: String, val extensions: List<String>) {
    MANGA("Manga", listOf("cbz", "cbr", "zip", "rar")),
    ANIME("Anime", listOf("mp4", "mkv", "avi", "m4v", "mov", "webm")),
    NOVEL("Novel", listOf("epub", "pdf", "txt", "mobi")),
    AUDIO("Audio", listOf("mp3", "m4a", "flac", "wav", "ogg"))
}

/**
 * Vault item representing local media
 */
data class VaultItem(
    val id: String,
    val title: String,
    val type: MediaType,
    val filePath: String,
    val coverPath: String? = null,
    val fileSize: Long,
    val dateAdded: Long,
    val lastAccessed: Long? = null,
    val metadata: VaultMetadata,
    val tags: List<String> = emptyList(),
    val collectionIds: List<String> = emptyList(),
    val isFavorite: Boolean = false
)

/**
 * Detailed vault item information
 */
data class VaultItemDetail(
    val item: VaultItem,
    val contentCount: Int, // Number of chapters/episodes
    val totalDuration: Long? = null, // For anime in milliseconds
    val readingProgress: ReadingProgress? = null,
    val extractedMetadata: Map<String, Any> = emptyMap()
)

/**
 * Vault content (chapter/episode)
 */
data class VaultContent(
    val id: String,
    val itemId: String,
    val title: String,
    val number: Float,
    val filePath: String? = null, // For extracted content
    val pages: List<String> = emptyList(), // For manga pages
    val duration: Long? = null, // For anime episodes
    val fileSize: Long? = null,
    val lastRead: Long? = null
)

/**
 * Metadata extracted from media files
 */
data class VaultMetadata(
    val title: String? = null,
    val author: String? = null,
    val artist: String? = null,
    val publisher: String? = null,
    val year: Int? = null,
    val genre: List<String> = emptyList(),
    val language: String? = null,
    val description: String? = null,
    val series: String? = null,
    val volume: Int? = null,
    val chapter: Float? = null,
    val pageCount: Int? = null,
    val duration: Long? = null,
    val resolution: String? = null,
    val format: String? = null
)

/**
 * Reading/viewing progress
 */
data class ReadingProgress(
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val currentTime: Long = 0, // For anime
    val totalTime: Long = 0,
    val isCompleted: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Vault collection for organizing items
 */
data class VaultCollection(
    val id: String,
    val name: String,
    val description: String? = null,
    val coverPath: String? = null,
    val tags: List<String> = emptyList(),
    val itemIds: List<String> = emptyList(),
    val dateCreated: Long,
    val lastModified: Long,
    val sortOrder: CollectionSortOrder = CollectionSortOrder.DATE_ADDED_DESC
)

/**
 * Collection sort orders
 */
enum class CollectionSortOrder {
    TITLE_ASC,
    TITLE_DESC,
    DATE_ADDED_ASC,
    DATE_ADDED_DESC,
    LAST_READ_ASC,
    LAST_READ_DESC,
    FILE_SIZE_ASC,
    FILE_SIZE_DESC,
    CUSTOM
}

/**
 * Filters for vault search
 */
data class VaultFilters(
    val mediaTypes: Set<MediaType> = emptySet(),
    val tags: Set<String> = emptySet(),
    val genres: Set<String> = emptySet(),
    val dateAddedRange: LongRange? = null,
    val fileSizeRange: LongRange? = null,
    val isFavorite: Boolean? = null,
    val hasProgress: Boolean? = null,
    val isCompleted: Boolean? = null,
    val language: String? = null,
    val sortBy: VaultSortBy = VaultSortBy.DATE_ADDED_DESC
)

/**
 * Sort options for vault items
 */
enum class VaultSortBy {
    TITLE_ASC,
    TITLE_DESC,
    DATE_ADDED_ASC,
    DATE_ADDED_DESC,
    LAST_ACCESSED_ASC,
    LAST_ACCESSED_DESC,
    FILE_SIZE_ASC,
    FILE_SIZE_DESC,
    PROGRESS_ASC,
    PROGRESS_DESC
}

/**
 * Vault statistics
 */
data class VaultStatistics(
    val totalItems: Int,
    val totalSize: Long,
    val mangaCount: Int,
    val animeCount: Int,
    val novelCount: Int,
    val audioCount: Int,
    val totalCollections: Int,
    val totalTags: Int,
    val diskSpaceUsed: Long,
    val averageFileSize: Long,
    val mostAccessedItem: VaultItem? = null,
    val recentlyAdded: List<VaultItem> = emptyList()
)

/**
 * File system manager for the Vault
 */
interface VaultFileSystem {
    
    /**
     * Get the vault root directory
     */
    fun getVaultRoot(): File
    
    /**
     * Create managed storage structure
     */
    suspend fun initializeStorage(): Result<Unit>
    
    /**
     * Import file to managed storage
     */
    suspend fun importFile(
        sourceFile: File,
        mediaType: MediaType,
        preserveStructure: Boolean = true
    ): Result<File>
    
    /**
     * Extract archive to managed storage
     */
    suspend fun extractArchive(
        archiveFile: File,
        extractionPath: String
    ): Result<List<File>>
    
    /**
     * Generate thumbnail for media
     */
    suspend fun generateThumbnail(
        mediaFile: File,
        mediaType: MediaType
    ): Result<File>
    
    /**
     * Clean up orphaned files
     */
    suspend fun cleanupOrphanedFiles(): Result<Int>
    
    /**
     * Get available disk space
     */
    fun getAvailableSpace(): Long
    
    /**
     * Get used disk space
     */
    fun getUsedSpace(): Long
}

/**
 * Metadata extractor for various media types
 */
interface VaultMetadataExtractor {
    
    /**
     * Extract metadata from a file
     */
    suspend fun extractMetadata(file: File, mediaType: MediaType): Result<VaultMetadata>
    
    /**
     * Extract cover image from media
     */
    suspend fun extractCover(file: File, mediaType: MediaType): Result<File?>
    
    /**
     * Validate media file format
     */
    suspend fun validateFormat(file: File, expectedType: MediaType): Result<Boolean>
    
    /**
     * Get supported formats
     */
    fun getSupportedFormats(): Map<MediaType, List<String>>
}