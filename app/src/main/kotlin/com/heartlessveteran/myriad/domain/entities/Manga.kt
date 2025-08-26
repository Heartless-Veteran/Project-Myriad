package com.heartlessveteran.myriad.domain.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Core manga entity representing a manga series in the database
 */
@Entity(tableName = "manga")
data class Manga(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val alternativeTitles: List<String> = emptyList(),
    val description: String = "",
    val author: String = "",
    val artist: String = "",
    val status: MangaStatus = MangaStatus.UNKNOWN,
    val genres: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val coverImageUrl: String? = null,
    val localCoverPath: String? = null,
    val rating: Float = 0f,
    val totalChapters: Int = 0,
    val readChapters: Int = 0,
    val isInLibrary: Boolean = false,
    val isFavorite: Boolean = false,
    val dateAdded: Date = Date(),
    val lastReadDate: Date? = null,
    val lastUpdated: Date = Date(),
    val source: String = "local", // local, mangadex, etc.
    val sourceId: String? = null,
    val isLocal: Boolean = true,
    val localPath: String? = null
)

/**
 * Status of a manga series
 */
enum class MangaStatus {
    ONGOING,
    COMPLETED,
    HIATUS,
    CANCELLED,
    UNKNOWN
}

/**
 * Individual manga chapter entity
 */
@Entity(tableName = "manga_chapters")
data class MangaChapter(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val mangaId: String,
    val chapterNumber: Float,
    val title: String = "",
    val pages: List<String> = emptyList(), // List of page file paths/URLs
    val isRead: Boolean = false,
    val lastReadPage: Int = 0,
    val isDownloaded: Boolean = false,
    val localPath: String? = null,
    val dateAdded: Date = Date(),
    val dateRead: Date? = null,
    val source: String = "local",
    val sourceChapterId: String? = null
)