package com.projectmyriad.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.projectmyriad.domain.entities.ContentRating
import com.projectmyriad.domain.entities.MangaStatus

/**
 * Room entity for manga data.
 * This represents the database schema for manga storage.
 */
@Entity(tableName = "manga")
data class MangaEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val description: String,
    val coverImage: String,
    val genres: String, // JSON string
    val status: String, // MangaStatus enum as string
    val rating: Float,
    val tags: String, // JSON string
    val alternativeTitles: String, // JSON string  
    val originalLanguage: String?,
    val publicationYear: Int?,
    val contentRating: String?, // ContentRating enum as string
    val themes: String, // JSON string
    val demographics: String, // JSON string
    val source: String?,
    val sourceUrl: String?,
    val lastUpdated: String?,
    val totalSize: Long?,
    val favorited: Boolean = false,
    val personalRating: Float?,
    val notes: String?,
    val readingProgress: Float = 0f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)