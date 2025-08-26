package com.heartlessveteran.myriad.data.network.dto

import com.heartlessveteran.myriad.domain.model.Manga
import kotlinx.serialization.Serializable

// Simplified DTOs for kotlinx.serialization, focusing only on what we need.

@Serializable
data class MangaListDto(
    val data: List<MangaDataDto>
)

@Serializable
data class MangaDataDto(
    val id: String,
    val attributes: MangaAttributesDto,
    val relationships: List<RelationshipDto>
) {
    fun toDomainModel(): Manga {
        val coverFileName = relationships.firstOrNull { it.type == "cover_art" }?.attributes?.fileName
        val coverUrl = if (coverFileName != null) "https://uploads.mangadex.org/covers/$id/$coverFileName.256.jpg" else ""

        return Manga(
            url = "https://mangadex.org/title/$id",
            title = attributes.title["en"] ?: attributes.title.values.firstOrNull() ?: "No Title",
            description = attributes.description["en"],
            artist = null, // These fields are in different relationships, omitted for simplicity
            author = null,
            status = attributes.status,
            genre = attributes.tags.mapNotNull { it.attributes.name["en"] },
            thumbnailUrl = coverUrl
        )
    }
}

@Serializable
data class MangaAttributesDto(
    val title: Map<String, String>,
    val description: Map<String, String> = emptyMap(),
    val status: String?,
    val tags: List<TagDto>
)

@Serializable
data class RelationshipDto(
    val id: String,
    val type: String,
    val attributes: CoverAttributesDto? = null
)

@Serializable
data class CoverAttributesDto(
    val fileName: String
)

@Serializable
data class TagDto(
    val attributes: TagAttributesDto
)

@Serializable
data class TagAttributesDto(
    val name: Map<String, String>
)