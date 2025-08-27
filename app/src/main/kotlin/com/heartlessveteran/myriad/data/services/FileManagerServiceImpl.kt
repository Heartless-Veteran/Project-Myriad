package com.heartlessveteran.myriad.data.services

import android.content.Context
import android.util.Log
import com.heartlessveteran.myriad.data.utils.ArchiveUtils
import com.heartlessveteran.myriad.data.utils.MetadataExtractor
import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.entities.MangaStatus
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.domain.services.FileManagerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

/**
 * Implementation of FileManagerService for handling local manga files.
 * 
 * This implementation provides:
 * - .cbz/.cbr archive extraction and processing
 * - Metadata extraction from filenames and archives
 * - Directory scanning and batch import
 * - File validation and error handling
 * - Cache management for extracted pages
 */
class FileManagerServiceImpl(
    private val context: Context
    // Future dependencies when DI is fully implemented:
    // private val mangaDao: MangaDao,
    // private val cacheManager: CacheManager
) : FileManagerService {
    
    companion object {
        private const val TAG = "FileManagerServiceImpl"
        private const val EXTRACT_DIR_NAME = "extracted_pages"
        private const val MAX_EXTRACTION_SIZE = 500 * 1024 * 1024L // 500MB limit
    }
    
    // Get application cache directory for extracted pages
    private val extractionBaseDir: File
        get() = File(context.cacheDir, EXTRACT_DIR_NAME).apply {
            if (!exists()) mkdirs()
        }
    
    override suspend fun importMangaFromFile(filePath: String): Result<Manga> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                
                // Validate file exists and is supported
                val validationResult = validateMangaFile(file)
                if (validationResult.isError) {
                    return@withContext Result.Error(
                        (validationResult as Result.Error).exception,
                        validationResult.message
                    )
                }
                
                Log.i(TAG, "Starting import of: ${file.name}")
                
                // Extract metadata from file
                val metadataResult = MetadataExtractor.extractMetadata(filePath)
                val metadata = when (metadataResult) {
                    is Result.Success -> metadataResult.data
                    is Result.Error -> {
                        Log.w(TAG, "Failed to extract metadata: ${metadataResult.message}")
                        mapOf("title" to file.nameWithoutExtension)
                    }
                    is Result.Loading -> mapOf("title" to file.nameWithoutExtension)
                }
                
                // Create extraction directory for this manga
                val extractDir = File(extractionBaseDir, UUID.randomUUID().toString())
                
                // Extract archive to get page count and validate content
                val extractionResult = ArchiveUtils.extractArchive(filePath, extractDir.absolutePath)
                val pageCount = when (extractionResult) {
                    is Result.Success -> {
                        Log.i(TAG, "Successfully extracted ${extractionResult.data.size} pages")
                        extractionResult.data.size
                    }
                    is Result.Error -> {
                        Log.e(TAG, "Failed to extract archive: ${extractionResult.message}")
                        // Still create manga entry but mark as problematic
                        0
                    }
                    is Result.Loading -> 0
                }
                
                // Create manga entity
                val manga = createMangaFromMetadata(file, metadata, pageCount)
                
                // Note: Database persistence is handled by the repository layer
                // This service focuses on file processing and metadata extraction
                
                // Clean up extracted files (we only needed them for validation)
                cleanupExtractionDirectory(extractDir)
                
                Log.i(TAG, "Successfully imported manga: ${manga.title}")
                Result.Success(manga)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error importing manga from file: $filePath", e)
                Result.Error(e, "Failed to import manga: ${e.localizedMessage}")
            }
        }
    }
    
    override suspend fun scanDirectoryForManga(
        directoryPath: String, 
        recursive: Boolean
    ): Result<List<Manga>> {
        return withContext(Dispatchers.IO) {
            try {
                val directory = File(directoryPath)
                
                if (!directory.exists() || !directory.isDirectory) {
                    return@withContext Result.Error(
                        IllegalArgumentException("Invalid directory: $directoryPath"),
                        "The selected path is not a valid directory"
                    )
                }
                
                Log.i(TAG, "Scanning directory: $directoryPath (recursive: $recursive)")
                
                val mangaFiles = findMangaFiles(directory, recursive)
                Log.i(TAG, "Found ${mangaFiles.size} manga files")
                
                val importedManga = mutableListOf<Manga>()
                val errors = mutableListOf<String>()
                
                // Import each file
                mangaFiles.forEach { file ->
                    when (val result = importMangaFromFile(file.absolutePath)) {
                        is Result.Success -> importedManga.add(result.data)
                        is Result.Error -> {
                            errors.add("${file.name}: ${result.message}")
                            Log.w(TAG, "Failed to import: ${file.name}")
                        }
                        is Result.Loading -> {
                            // This shouldn't happen in this context, but handle it
                            Log.w(TAG, "Unexpected loading state for: ${file.name}")
                        }
                    }
                }
                
                Log.i(TAG, "Successfully imported ${importedManga.size}/${mangaFiles.size} files")
                
                if (errors.isNotEmpty()) {
                    Log.w(TAG, "Import errors: ${errors.joinToString(", ")}")
                }
                
                Result.Success(importedManga)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error scanning directory: $directoryPath", e)
                Result.Error(e, "Failed to scan directory: ${e.localizedMessage}")
            }
        }
    }
    
    override suspend fun extractPagesFromArchive(
        archivePath: String, 
        extractPath: String
    ): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                // Validate archive size before extraction
                val archiveFile = File(archivePath)
                if (archiveFile.length() > MAX_EXTRACTION_SIZE) {
                    return@withContext Result.Error(
                        IllegalArgumentException("Archive too large"),
                        "Archive exceeds maximum size limit of ${MAX_EXTRACTION_SIZE / (1024 * 1024)}MB"
                    )
                }
                
                Log.i(TAG, "Extracting pages from: ${archiveFile.name}")
                ArchiveUtils.extractArchive(archivePath, extractPath)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting pages from archive: $archivePath", e)
                Result.Error(e, "Failed to extract pages: ${e.localizedMessage}")
            }
        }
    }
    
    override suspend fun extractMetadata(filePath: String): Result<Map<String, Any>> {
        return MetadataExtractor.extractMetadata(filePath)
    }
    
    override fun isSupportedMangaFile(filePath: String): Boolean {
        return ArchiveUtils.isSupportedArchive(filePath)
    }
    
    override suspend fun getDirectorySize(directoryPath: String): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val directory = File(directoryPath)
                
                if (!directory.exists() || !directory.isDirectory) {
                    return@withContext Result.Error(
                        IllegalArgumentException("Invalid directory"),
                        "Directory does not exist or is not accessible"
                    )
                }
                
                val size = calculateDirectorySize(directory)
                Log.d(TAG, "Directory size for $directoryPath: ${formatFileSize(size)}")
                
                Result.Success(size)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error calculating directory size: $directoryPath", e)
                Result.Error(e, "Failed to calculate directory size: ${e.localizedMessage}")
            }
        }
    }
    
    override suspend fun cleanupTemporaryFiles(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                var cleanedFiles = 0
                var freedSpace = 0L
                
                // Clean up extraction base directory
                if (extractionBaseDir.exists()) {
                    extractionBaseDir.walkTopDown().forEach { file ->
                        if (file.isFile) {
                            freedSpace += file.length()
                            cleanedFiles++
                            file.delete()
                        }
                    }
                    extractionBaseDir.deleteRecursively()
                    extractionBaseDir.mkdirs()
                }
                
                Log.i(TAG, "Cleaned up $cleanedFiles temporary files, freed ${formatFileSize(freedSpace)}")
                Result.Success(Unit)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up temporary files", e)
                Result.Error(e, "Failed to cleanup temporary files: ${e.localizedMessage}")
            }
        }
    }
    
    // Private helper methods
    
    /**
     * Validate manga file before processing.
     */
    private fun validateMangaFile(file: File): Result<Unit> {
        when {
            !file.exists() -> return Result.Error(
                IllegalArgumentException("File does not exist"),
                "The selected file could not be found: ${file.name}"
            )
            !file.canRead() -> return Result.Error(
                IllegalArgumentException("File is not readable"),
                "Cannot read the selected file: ${file.name}"
            )
            !isSupportedMangaFile(file.absolutePath) -> return Result.Error(
                IllegalArgumentException("Unsupported file format"),
                "Only .cbz and .cbr files are supported"
            )
            file.length() == 0L -> return Result.Error(
                IllegalArgumentException("File is empty"),
                "The selected file is empty: ${file.name}"
            )
            file.length() > MAX_EXTRACTION_SIZE -> return Result.Error(
                IllegalArgumentException("File too large"),
                "File exceeds maximum size limit of ${MAX_EXTRACTION_SIZE / (1024 * 1024)}MB"
            )
        }
        return Result.Success(Unit)
    }
    
    /**
     * Find manga files in directory.
     */
    private fun findMangaFiles(directory: File, recursive: Boolean): List<File> {
        val files = mutableListOf<File>()
        
        if (recursive) {
            directory.walkTopDown()
                .filter { it.isFile && isSupportedMangaFile(it.absolutePath) }
                .toCollection(files)
        } else {
            directory.listFiles()
                ?.filter { it.isFile && isSupportedMangaFile(it.absolutePath) }
                ?.let { files.addAll(it) }
        }
        
        return files.sortedBy { it.name.lowercase() }
    }
    
    /**
     * Create Manga entity from file and metadata.
     */
    private fun createMangaFromMetadata(file: File, metadata: Map<String, Any>, pageCount: Int): Manga {
        val title = metadata["title"]?.toString() ?: file.nameWithoutExtension
        val chapter = metadata["chapter"]?.toString()?.toFloatOrNull()
        val volume = metadata["volume"]?.toString()?.toIntOrNull()
        
        return Manga(
            id = UUID.randomUUID().toString(),
            title = title,
            description = createDescription(metadata, pageCount),
            author = extractAuthorFromMetadata(metadata),
            status = MangaStatus.UNKNOWN,
            localPath = file.absolutePath,
            isLocal = true,
            isInLibrary = true,
            dateAdded = Date(),
            lastUpdated = Date(file.lastModified())
        )
    }
    
    /**
     * Create description from metadata.
     */
    private fun createDescription(metadata: Map<String, Any>, pageCount: Int): String {
        val parts = mutableListOf<String>()
        parts.add("Imported from local file")
        
        if (pageCount > 0) {
            parts.add("$pageCount pages")
        }
        
        val fileSize = metadata["fileSize"] as? Long
        if (fileSize != null) {
            parts.add(formatFileSize(fileSize))
        }
        
        val groups = metadata["groups"] as? List<*>
        if (!groups.isNullOrEmpty()) {
            parts.add("by ${groups.joinToString(", ")}")
        }
        
        return parts.joinToString(" • ")
    }
    
    /**
     * Extract author information from metadata.
     */
    private fun extractAuthorFromMetadata(metadata: Map<String, Any>): String {
        // Try to find author in various metadata fields
        val groups = metadata["groups"] as? List<*>
        if (!groups.isNullOrEmpty()) {
            return groups.first().toString()
        }
        
        val additionalInfo = metadata["additionalInfo"] as? List<*>
        if (!additionalInfo.isNullOrEmpty()) {
            return additionalInfo.first().toString()
        }
        
        return "Unknown"
    }
    
    /**
     * Calculate total size of directory recursively.
     */
    private fun calculateDirectorySize(directory: File): Long {
        return directory.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
    
    /**
     * Clean up extraction directory.
     */
    private fun cleanupExtractionDirectory(extractDir: File) {
        try {
            if (extractDir.exists()) {
                extractDir.deleteRecursively()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cleanup extraction directory: ${extractDir.path}", e)
        }
    }
    
    /**
     * Format file size for display.
     */
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "%.1f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
            bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}