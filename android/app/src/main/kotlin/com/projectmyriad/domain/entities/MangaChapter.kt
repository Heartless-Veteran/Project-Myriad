package com.projectmyriad.domain.entities

import kotlinx.serialization.Serializable

/**
 * Represents a manga chapter with pages and metadata.
 */
@Serializable
data class MangaChapter(
    val id: String,
    val mangaId: String,
    val chapterNumber: Float,
    val title: String,
    val pages: List<MangaPage>,
    val readProgress: Float = 0f,
    val isRead: Boolean = false,
    val downloadedAt: String? = null,
    val fileSize: Long? = null,
    val filePath: String? = null
)

/**
 * Represents a single page in a manga chapter.
 */
@Serializable
data class MangaPage(
    val id: String,
    val pageNumber: Int,
    val imageUrl: String,
    val localPath: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val hasTranslation: Boolean = false,
    val translationData: List<TextBubble> = emptyList()
)

/**
 * Represents translated text bubbles on a manga page.
 */
@Serializable
data class TextBubble(
    val id: String,
    val originalText: String,
    val translatedText: String,
    val language: String,
    val confidence: Float,
    val boundingBox: BoundingBox
)

/**
 * Represents a bounding box for text regions.
 */
@Serializable
data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)