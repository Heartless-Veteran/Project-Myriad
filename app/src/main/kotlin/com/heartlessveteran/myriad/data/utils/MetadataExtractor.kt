package com.heartlessveteran.myriad.data.utils

import android.util.Log
import com.heartlessveteran.myriad.domain.models.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import java.util.regex.Pattern

/**
 * Utility class for extracting metadata from manga files and filenames.
 *
 * This class provides functionality for:
 * - Parsing manga titles from filenames
 * - Extracting chapter/volume numbers
 * - Detecting series information
 * - Parsing author/artist information
 * - Reading ComicInfo.xml metadata (future implementation)
 */
object MetadataExtractor {
    private const val TAG = "MetadataExtractor"

    // Common patterns for parsing manga filenames
    private val chapterPattern =
        Pattern.compile(
            "(?i)(?:ch|chapter|c)\\s*[\\.-]?\\s*(\\d+(?:\\.\\d+)?)",
            Pattern.CASE_INSENSITIVE,
        )

    private val volumePattern =
        Pattern.compile(
            "(?i)(?:vol|volume|v)\\s*[\\.-]?\\s*(\\d+)",
            Pattern.CASE_INSENSITIVE,
        )

    private val pagePattern =
        Pattern.compile(
            "(?i)(?:pg|page|p)\\s*[\\.-]?\\s*(\\d+)",
            Pattern.CASE_INSENSITIVE,
        )

    // Common bracketed info patterns [Group] (Year) etc.
    private val bracketPattern = Pattern.compile("\\[([^\\]]+)\\]")
    private val parenthesesPattern = Pattern.compile("\\(([^)]+)\\)")

    /**
     * Extract metadata from a manga file.
     *
     * @param filePath Path to the manga file
     * @return Result containing metadata map
     */
    suspend fun extractMetadata(filePath: String): Result<Map<String, Any>> =
        withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (!file.exists()) {
                    return@withContext Result.Error(
                        IllegalArgumentException("File does not exist"),
                        "Could not find the file: $filePath",
                    )
                }

                val metadata = mutableMapOf<String, Any>()

                // Basic file information
                metadata["fileName"] = file.name
                metadata["fileNameWithoutExtension"] = file.nameWithoutExtension
                metadata["fileSize"] = file.length()
                metadata["lastModified"] = file.lastModified()
                metadata["extension"] = file.extension.lowercase()
                metadata["archiveType"] = ArchiveUtils.getArchiveType(filePath)

                // Parse filename for manga information
                val parsedInfo = parseFilename(file.nameWithoutExtension)
                metadata.putAll(parsedInfo)

                // Extract ComicInfo.xml from archive if present
                if (ArchiveUtils.isSupportedArchive(filePath)) {
                    val comicInfoMetadata = extractComicInfoFromArchive(filePath)
                    if (comicInfoMetadata.isNotEmpty()) {
                        // ComicInfo.xml metadata takes precedence over filename parsing
                        comicInfoMetadata.forEach { (key, value) ->
                            metadata[key] = value
                        }
                        metadata["hasComicInfo"] = true
                        Log.d(TAG, "Found ComicInfo.xml in archive: ${file.name}")
                    }
                }

                // TODO: Parse EPUB metadata if applicable
                // TODO: Read embedded metadata from images

                Log.d(TAG, "Extracted metadata for: ${file.name}")
                Result.Success(metadata.toMap())
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting metadata from: $filePath", e)
                Result.Error(e, "Failed to extract metadata: ${e.localizedMessage}")
            }
        }

    /**
     * Parse manga information from filename.
     *
     * @param filename Filename without extension
     * @return Map containing parsed information
     */
    private fun parseFilename(filename: String): Map<String, Any> {
        val info = mutableMapOf<String, Any>()

        // Extract title by removing common patterns
        val title = extractTitle(filename)
        info["title"] = title

        // Extract chapter number
        val chapterMatch = chapterPattern.matcher(filename)
        if (chapterMatch.find()) {
            val chapterStr = chapterMatch.group(1)
            info["chapter"] = chapterStr?.toFloatOrNull() ?: chapterStr
        }

        // Extract volume number
        val volumeMatch = volumePattern.matcher(filename)
        if (volumeMatch.find()) {
            val volumeStr = volumeMatch.group(1)
            info["volume"] = volumeStr?.toIntOrNull() ?: volumeStr
        }

        // Extract page number (for single pages)
        val pageMatch = pagePattern.matcher(filename)
        if (pageMatch.find()) {
            val pageStr = pageMatch.group(1)
            info["page"] = pageStr?.toIntOrNull() ?: pageStr
        }

        // Extract bracketed information (usually groups/translators)
        val brackets = bracketPattern.matcher(filename)
        val bracketedInfo = mutableListOf<String>()
        while (brackets.find()) {
            brackets.group(1)?.let { bracketedInfo.add(it) }
        }
        if (bracketedInfo.isNotEmpty()) {
            info["groups"] = bracketedInfo
        }

        // Extract parentheses information (usually years, artists, etc)
        val parentheses = parenthesesPattern.matcher(filename)
        val parenthesesInfo = mutableListOf<String>()
        while (parentheses.find()) {
            parentheses.group(1)?.let { parenthesesInfo.add(it) }
        }
        if (parenthesesInfo.isNotEmpty()) {
            info["additionalInfo"] = parenthesesInfo
        }

        // Try to detect year
        val yearPattern = Pattern.compile("(\\b(?:19|20)\\d{2}\\b)")
        val yearMatch = yearPattern.matcher(filename)
        if (yearMatch.find()) {
            val year = yearMatch.group(1)?.toIntOrNull()
            if (year != null) {
                info["year"] = year
            }
        }

        // Detect if it's a oneshot (no chapter/volume info)
        val hasChapter = info.containsKey("chapter")
        val hasVolume = info.containsKey("volume")
        info["isOneshot"] = !hasChapter && !hasVolume

        return info
    }

    /**
     * Extract clean title from filename by removing common patterns.
     *
     * @param filename Original filename
     * @return Cleaned title
     */
    private fun extractTitle(filename: String): String {
        var title = filename

        // Remove bracketed information
        title = bracketPattern.matcher(title).replaceAll("")

        // Remove parentheses information
        title = parenthesesPattern.matcher(title).replaceAll("")

        // Remove chapter information
        title = chapterPattern.matcher(title).replaceAll("")

        // Remove volume information
        title = volumePattern.matcher(title).replaceAll("")

        // Remove page information
        title = pagePattern.matcher(title).replaceAll("")

        // Clean up common separators and formatting
        title =
            title
                .replace(Regex("[_\\-\\.]+"), " ") // Replace underscores, hyphens, dots with spaces
                .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
                .trim()

        // Return original filename if title becomes empty
        return title.ifBlank { filename }
    }

    /**
     * Generate a standardized filename based on metadata.
     *
     * @param metadata Metadata map
     * @return Standardized filename
     */
    fun generateStandardizedFilename(metadata: Map<String, Any>): String {
        val title = metadata["title"]?.toString() ?: "Unknown"
        val volume = metadata["volume"]?.toString()
        val chapter = metadata["chapter"]?.toString()
        val extension = metadata["extension"]?.toString() ?: "cbz"

        val parts = mutableListOf<String>()
        parts.add(title)

        if (volume != null) {
            parts.add("Vol $volume")
        }

        if (chapter != null) {
            parts.add("Ch $chapter")
        }

        return "${parts.joinToString(" - ")}.$extension"
    }

    /**
     * Validate extracted metadata for completeness.
     *
     * @param metadata Metadata to validate
     * @return List of missing or invalid fields
     */
    fun validateMetadata(metadata: Map<String, Any>): List<String> {
        val issues = mutableListOf<String>()

        // Check for required fields
        if (metadata["title"]?.toString().isNullOrBlank()) {
            issues.add("Title is missing or empty")
        }

        if (metadata["fileSize"] as? Long ?: 0L <= 0) {
            issues.add("Invalid file size")
        }

        // Check for logical issues
        val chapter = metadata["chapter"]?.toString()?.toFloatOrNull()
        if (chapter != null && chapter < 0) {
            issues.add("Chapter number cannot be negative")
        }

        val volume = metadata["volume"]?.toString()?.toIntOrNull()
        if (volume != null && volume < 0) {
            issues.add("Volume number cannot be negative")
        }

        return issues
    }

    /**
     * Create a summary string from metadata for display purposes.
     *
     * @param metadata Metadata map
     * @return Human-readable summary
     */
    fun createMetadataSummary(metadata: Map<String, Any>): String {
        val title = metadata["title"]?.toString() ?: "Unknown Title"
        val volume = metadata["volume"]?.toString()
        val chapter = metadata["chapter"]?.toString()
        val fileSize = metadata["fileSize"] as? Long

        val parts = mutableListOf<String>()
        parts.add(title)

        if (volume != null) {
            parts.add("Volume $volume")
        }

        if (chapter != null) {
            parts.add("Chapter $chapter")
        }

        if (fileSize != null) {
            parts.add(formatFileSize(fileSize))
        }

        return parts.joinToString(" • ")
    }

    /**
     * Extract ComicInfo.xml metadata from archive.
     * ComicInfo.xml is a standard format for comic/manga metadata.
     */
    private suspend fun extractComicInfoFromArchive(archivePath: String): Map<String, Any> =
        withContext(Dispatchers.IO) {
            val metadata = mutableMapOf<String, Any>()

            try {
                // TODO: Implement ComicInfo.xml extraction when zip4j is properly integrated
                Log.d(TAG, "ComicInfo.xml extraction from $archivePath not yet implemented")
            } catch (e: Exception) {
                Log.w(TAG, "Could not extract ComicInfo.xml from archive: $archivePath", e)
            }

            metadata
        }

    /**
     * Parse ComicInfo.xml content and extract relevant metadata.
     * ComicInfo.xml format: https://anansi-project.github.io/docs/comicinfo/documentation
     */
    private fun parseComicInfoXml(xmlContent: String): Map<String, Any> {
        val metadata = mutableMapOf<String, Any>()

        try {
            // TODO: Implement proper ComicInfo.xml parsing
            // This is a placeholder to fix compilation issues
            Log.d(TAG, "ComicInfo.xml parsing will be implemented in phase 1")
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing ComicInfo.xml content", e)
        }

        return metadata
    }

    /**
     * Extract text content from an XML tag (placeholder implementation).
     * TODO: Implement proper XML tag extraction in phase 1
     */
    private fun extractXmlTag(
        xmlContent: String,
        tagName: String,
    ): String? = null

    /**
     * Extract text content for multiple XML tags in a single pass.
     * Returns a map of tag names to their text content (or null if not found).
     * TODO: Implement proper XML parsing in phase 1
     */
    private fun extractXmlTags(
        xmlContent: String,
        tagNames: Set<String>,
    ): Map<String, String?> {
        val results = mutableMapOf<String, String?>()
        // TODO: Implement XML parsing when needed
        for (tag in tagNames) {
            results[tag] = null
        }
        return results
    }

    /**
     * Format file size in human-readable format.
     */
    private fun formatFileSize(bytes: Long): String =
        when {
            bytes >= 1024 * 1024 * 1024 -> "%.1f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
            bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
}
