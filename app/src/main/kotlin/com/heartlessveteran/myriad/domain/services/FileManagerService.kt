package com.heartlessveteran.myriad.domain.services

import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.models.Result

/**
 * File management service for handling local media files.
 *
 * This service provides core functionality for:
 * - Importing manga files (.cbz/.cbr archives)
 * - Scanning directories for media files
 * - Extracting metadata from files
 * - Managing local storage and file organization
 */
interface FileManagerService {
    /**
     * Import a manga from a local file (.cbz or .cbr archive).
     *
     * @param filePath Path to the archive file
     * @return Result containing the imported Manga or an error
     */
    suspend fun importMangaFromFile(filePath: String): Result<Manga>

    /**
     * Scan a directory for manga files and import them.
     *
     * @param directoryPath Path to the directory to scan
     * @param recursive Whether to scan subdirectories
     * @return Result containing list of imported manga or an error
     */
    suspend fun scanDirectoryForManga(
        directoryPath: String,
        recursive: Boolean = true,
    ): Result<List<Manga>>

    /**
     * Extract pages from a manga archive file.
     *
     * @param archivePath Path to the .cbz/.cbr file
     * @param extractPath Directory to extract pages to
     * @return Result containing list of page file paths or an error
     */
    suspend fun extractPagesFromArchive(
        archivePath: String,
        extractPath: String,
    ): Result<List<String>>

    /**
     * Extract metadata from a manga file (title, chapter info, etc).
     *
     * @param filePath Path to the manga file
     * @return Result containing metadata map or an error
     */
    suspend fun extractMetadata(filePath: String): Result<Map<String, Any>>

    /**
     * Validate if a file is a supported manga format.
     *
     * @param filePath Path to the file to validate
     * @return True if the file is supported (.cbz/.cbr)
     */
    fun isSupportedMangaFile(filePath: String): Boolean

    /**
     * Get the total size of files in a directory.
     *
     * @param directoryPath Path to the directory
     * @return Result containing size in bytes or an error
     */
    suspend fun getDirectorySize(directoryPath: String): Result<Long>

    /**
     * Clean up temporary files and cache.
     *
     * @return Result indicating success or failure
     */
    suspend fun cleanupTemporaryFiles(): Result<Unit>
}
