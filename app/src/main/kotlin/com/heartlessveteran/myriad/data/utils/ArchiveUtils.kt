package com.heartlessveteran.myriad.data.utils

import android.util.Log
import com.heartlessveteran.myriad.domain.models.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import java.io.File

/**
 * Utility class for handling archive file operations (.cbz/.cbr).
 *
 * This class provides functionality for:
 * - Extracting .cbz (ZIP) archives
 * - Extracting .cbr (RAR) archives (future implementation)
 * - Validating archive formats
 * - Organizing extracted pages
 */
object ArchiveUtils {
    private const val TAG = "ArchiveUtils"

    // Supported image extensions in manga archives
    private val supportedImageExtensions =
        setOf(
            "jpg",
            "jpeg",
            "png",
            "webp",
            "gif",
            "bmp",
        )

    /**
     * Extract pages from a .cbz/.cbr archive to a directory.
     *
     * @param archivePath Path to the archive file
     * @param extractPath Directory to extract pages to
     * @return Result containing list of extracted image file paths
     */
    suspend fun extractArchive(
        archivePath: String,
        extractPath: String,
    ): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val archiveFile = File(archivePath)
                if (!archiveFile.exists()) {
                    return@withContext Result.Error(
                        IllegalArgumentException("Archive file does not exist"),
                        "Could not find the archive file: $archivePath",
                    )
                }

                val extractDir = File(extractPath)
                if (!extractDir.exists()) {
                    extractDir.mkdirs()
                }

                // Determine archive type and extract
                when (archiveFile.extension.lowercase()) {
                    "cbz", "zip" -> extractZipArchive(archivePath, extractPath)
                    "cbr", "rar" -> extractRarArchive(archivePath, extractPath)
                    else ->
                        Result.Error(
                            IllegalArgumentException("Unsupported archive format"),
                            "Only .cbz/.cbr files are supported",
                        )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting archive: $archivePath", e)
                Result.Error(e, "Failed to extract archive: ${e.localizedMessage}")
            }
        }

    /**
     * Extract a .cbz (ZIP) archive.
     */
    private suspend fun extractZipArchive(
        archivePath: String,
        extractPath: String,
    ): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val extractedFiles = mutableListOf<String>()

                // Use zip4j library for better ZIP handling
                val zipFile = ZipFile(archivePath)

                if (!zipFile.isValidZipFile) {
                    return@withContext Result.Error(
                        IllegalArgumentException("Invalid ZIP file"),
                        "The selected file is not a valid ZIP archive",
                    )
                }

                // Extract all entries
                zipFile.fileHeaders.forEach { fileHeader ->
                    val fileName = fileHeader.fileName

                    // Only extract image files
                    if (isImageFile(fileName) && !fileHeader.isDirectory) {
                        val extractedFile = File(extractPath, fileName)

                        // Create parent directories if needed
                        extractedFile.parentFile?.mkdirs()

                        // Extract the file
                        zipFile.extractFile(fileHeader, extractPath)

                        if (extractedFile.exists()) {
                            extractedFiles.add(extractedFile.absolutePath)
                            Log.d(TAG, "Extracted: ${extractedFile.name}")
                        }
                    }
                }

                // Sort files naturally (1.jpg, 2.jpg, 10.jpg instead of 1.jpg, 10.jpg, 2.jpg)
                val sortedFiles = extractedFiles.sortedWith(naturalOrder())

                Log.i(TAG, "Successfully extracted ${sortedFiles.size} images from $archivePath")
                Result.Success(sortedFiles)
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting ZIP archive", e)
                Result.Error(e, "Failed to extract ZIP archive: ${e.localizedMessage}")
            }
        }

    /**
     * Extract a .cbr (RAR) archive.
     * TODO: Implement RAR support using appropriate library
     */
    private suspend fun extractRarArchive(
        archivePath: String,
        extractPath: String,
    ): Result<List<String>> =
        withContext(Dispatchers.IO) {
            // RAR support would require additional library like junrar
            // For now, return not implemented
            Result.Error(
                NotImplementedError("RAR extraction not yet implemented"),
                "RAR archive support will be added in a future update",
            )
        }

    /**
     * Check if a file is a supported image format.
     */
    private fun isImageFile(fileName: String): Boolean {
        val extension = File(fileName).extension.lowercase()
        return extension in supportedImageExtensions
    }

    /**
     * Natural order comparator for sorting file names.
     * This handles numeric sorting properly (1, 2, 10 instead of 1, 10, 2).
     */
    private fun naturalOrder(): Comparator<String> =
        Comparator { a, b ->
            val aFile = File(a).nameWithoutExtension
            val bFile = File(b).nameWithoutExtension

            // Try to extract numbers from filenames for proper sorting
            val aNum = extractNumber(aFile)
            val bNum = extractNumber(bFile)

            when {
                aNum != null && bNum != null -> aNum.compareTo(bNum)
                else -> a.compareTo(b, ignoreCase = true)
            }
        }

    /**
     * Extract number from filename for natural sorting.
     */
    private fun extractNumber(filename: String): Int? {
        val regex = Regex("\\d+")
        val matchResult = regex.find(filename)
        return matchResult?.value?.toIntOrNull()
    }

    /**
     * Validate if file is a supported archive format.
     */
    fun isSupportedArchive(filePath: String): Boolean {
        val extension = File(filePath).extension.lowercase()
        return extension in setOf("cbz", "zip", "cbr", "rar")
    }

    /**
     * Get archive type from file extension.
     */
    fun getArchiveType(filePath: String): String =
        when (File(filePath).extension.lowercase()) {
            "cbz", "zip" -> "ZIP"
            "cbr", "rar" -> "RAR"
            else -> "UNKNOWN"
        }

    /**
     * Clean up extracted files in a directory.
     */
    suspend fun cleanupExtractedFiles(extractPath: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val extractDir = File(extractPath)
                if (extractDir.exists() && extractDir.isDirectory) {
                    extractDir.deleteRecursively()
                    Log.i(TAG, "Cleaned up extracted files in: $extractPath")
                }
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up extracted files", e)
                Result.Error(e, "Failed to cleanup extracted files: ${e.localizedMessage}")
            }
        }
}
