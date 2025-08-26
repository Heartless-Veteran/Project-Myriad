package com.projectmyriad.data.mappers

import com.projectmyriad.data.database.entities.MangaEntity
import com.projectmyriad.domain.entities.Manga
import com.projectmyriad.domain.entities.MangaStatus
import com.projectmyriad.domain.entities.ContentRating
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Extension functions to map between domain entities and data entities.
 * Handles JSON serialization/deserialization for complex fields.
 */

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * Convert MangaEntity (database) to Manga (domain)
 */
/**
 * Convert MangaEntity (database) to Manga (domain)
 * @param chapters List of chapters for this manga. Must be provided by the caller.
 * If chapters are not available, pass emptyList().
 */
fun MangaEntity.toDomain(chapters: List<Chapter> = emptyList()): Manga {
    return Manga(
        id = id,
        title = title,
        author = author,
        description = description,
        coverImage = coverImage,
        chapters = chapters,
        genres = parseStringList(genres),
        status = MangaStatus.valueOf(status),
        rating = rating,
        tags = parseStringList(tags),
        alternativeTitles = parseStringList(alternativeTitles),
        originalLanguage = originalLanguage,
        publicationYear = publicationYear,
        contentRating = contentRating?.let { ContentRating.valueOf(it) },
        themes = parseStringList(themes),
        demographics = parseStringList(demographics),
        source = source,
        sourceUrl = sourceUrl,
        lastUpdated = lastUpdated,
        totalSize = totalSize,
        favorited = favorited,
        personalRating = personalRating,
        notes = notes,
        readingProgress = readingProgress
    )
}

/**
 * Convert Manga (domain) to MangaEntity (database)
 */
fun Manga.toEntity(): MangaEntity {
    return MangaEntity(
        id = id,
        title = title,
        author = author,
        description = description,
        coverImage = coverImage,
        genres = encodeStringList(genres),
        status = status.name,
        rating = rating,
        tags = encodeStringList(tags),
        alternativeTitles = encodeStringList(alternativeTitles),
        originalLanguage = originalLanguage,
        publicationYear = publicationYear,
        contentRating = contentRating?.name,
        themes = encodeStringList(themes),
        demographics = encodeStringList(demographics),
        source = source,
        sourceUrl = sourceUrl,
        lastUpdated = lastUpdated,
        totalSize = totalSize,
        favorited = favorited,
        personalRating = personalRating,
        notes = notes,
        readingProgress = readingProgress
    )
}

/**
 * Helper function to parse JSON string list
 */
private fun parseStringList(jsonString: String): List<String> {
    return if (jsonString.isEmpty()) {
        emptyList()
    } else {
        try {
            json.decodeFromString<List<String>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/**
 * Helper function to encode string list to JSON
 */
private fun encodeStringList(list: List<String>): String {
    return try {
        json.encodeToString(list)
    } catch (e: Exception) {
        "[]"
    }
}