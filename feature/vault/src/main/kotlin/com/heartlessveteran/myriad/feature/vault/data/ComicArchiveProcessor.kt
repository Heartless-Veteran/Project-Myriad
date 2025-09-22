package com.heartlessveteran.myriad.feature.vault.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * Utility class for processing comic book archives (.cbz/.cbr files).
 * Handles extraction, metadata parsing, and file organization.
 */
class ComicArchiveProcessor(
    private val context: Context
) {
    companion object {
        private const val SUPPORTED_IMAGE_EXTENSIONS = ".jpg,.jpeg,.png,.webp,.gif,.bmp"
        private const val COMIC_INFO_FILE = "ComicInfo.xml"
    }

    /**
     * Process a comic archive file and extract its contents
     */
    suspend fun processArchive(uri: Uri, fileName: String): ArchiveProcessResult = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext ArchiveProcessResult.Error("Cannot open file: $fileName")

            when {
                fileName.endsWith(".cbz", ignoreCase = true) -> {
                    processZipArchive(inputStream, fileName)
                }
                fileName.endsWith(".cbr", ignoreCase = true) -> {
                    // RAR files require special handling - for now return placeholder
                    ArchiveProcessResult.Error("RAR archives not yet supported")
                }
                else -> {
                    ArchiveProcessResult.Error("Unsupported file format: $fileName")
                }
            }
        } catch (e: Exception) {
            ArchiveProcessResult.Error("Error processing archive: ${e.message}")
        }
    }

    /**
     * Process a ZIP-based comic archive (.cbz)
     */
    private suspend fun processZipArchive(inputStream: InputStream, fileName: String): ArchiveProcessResult {
        return withContext(Dispatchers.IO) {
            try {
                val zipInputStream = ZipInputStream(inputStream)
                val pages = mutableListOf<String>()
                var comicInfo: String? = null

                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    val entryName = entry.name
                    
                    when {
                        entryName.equals(COMIC_INFO_FILE, ignoreCase = true) -> {
                            // Read ComicInfo.xml metadata
                            comicInfo = zipInputStream.readBytes().toString(Charsets.UTF_8)
                        }
                        isImageFile(entryName) && !entry.isDirectory -> {
                            // This is an image page - in a real implementation,
                            // we would extract and save it to internal storage
                            pages.add(entryName)
                        }
                    }
                    
                    zipInputStream.closeEntry()
                    entry = zipInputStream.nextEntry
                }

                zipInputStream.close()
                inputStream.close()

                if (pages.isEmpty()) {
                    ArchiveProcessResult.Error("No image pages found in archive")
                } else {
                    ArchiveProcessResult.Success(
                        fileName = fileName,
                        pageCount = pages.size,
                        pages = pages.sorted(), // Sort pages naturally
                        metadata = parseComicInfo(comicInfo)
                    )
                }
            } catch (e: Exception) {
                ArchiveProcessResult.Error("Error reading ZIP archive: ${e.message}")
            }
        }
    }

    /**
     * Check if a file is a supported image format
     */
    private fun isImageFile(fileName: String): Boolean {
        val extension = "." + fileName.substringAfterLast('.', "").lowercase()
        return SUPPORTED_IMAGE_EXTENSIONS.contains(extension)
    }

    /**
     * Parse ComicInfo.xml metadata (simplified implementation)
     */
    private fun parseComicInfo(xmlContent: String?): ComicMetadata? {
        if (xmlContent == null) return null
        
        // This is a simplified parser - in a production app, you'd use a proper XML parser
        return try {
            val title = extractXmlTag(xmlContent, "Title")
            val series = extractXmlTag(xmlContent, "Series")
            val number = extractXmlTag(xmlContent, "Number")
            val summary = extractXmlTag(xmlContent, "Summary")
            val writer = extractXmlTag(xmlContent, "Writer")
            val artist = extractXmlTag(xmlContent, "Penciller")
            
            ComicMetadata(
                title = title ?: series ?: "Unknown",
                series = series,
                number = number,
                summary = summary,
                writer = writer,
                artist = artist
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Simple XML tag extraction (for demo purposes)
     */
    private fun extractXmlTag(xml: String, tagName: String): String? {
        val startTag = "<$tagName>"
        val endTag = "</$tagName>"
        val startIndex = xml.indexOf(startTag)
        if (startIndex == -1) return null
        
        val contentStart = startIndex + startTag.length
        val endIndex = xml.indexOf(endTag, contentStart)
        if (endIndex == -1) return null
        
        return xml.substring(contentStart, endIndex).trim()
    }
}

/**
 * Result of processing a comic archive
 */
sealed class ArchiveProcessResult {
    data class Success(
        val fileName: String,
        val pageCount: Int,
        val pages: List<String>,
        val metadata: ComicMetadata?
    ) : ArchiveProcessResult()
    
    data class Error(
        val message: String
    ) : ArchiveProcessResult()
}

/**
 * Metadata extracted from ComicInfo.xml
 */
data class ComicMetadata(
    val title: String,
    val series: String? = null,
    val number: String? = null,
    val summary: String? = null,
    val writer: String? = null,
    val artist: String? = null
)