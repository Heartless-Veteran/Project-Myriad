package com.heartlessveteran.myriad.data.network.dto

import kotlinx.serialization.Serializable

// Simplified DTOs for kotlinx.serialization, focusing only on what we need.

@Serializable
data class MangaListDto(
    val data: List<MangaDataDto>,
)

@Serializable
data class MangaDataDto(
    val id: String,
    val attributes: MangaAttributesDto,
    val relationships: List<RelationshipDto>,
)

@Serializable
data class MangaAttributesDto(
    val title: Map<String, String>,
    val description: Map<String, String> = emptyMap(),
    val status: String?,
    val tags: List<TagDto>,
)

@Serializable
data class RelationshipDto(
    val id: String,
    val type: String,
    val attributes: CoverAttributesDto? = null,
)

@Serializable
data class CoverAttributesDto(
    val fileName: String,
)

@Serializable
data class TagDto(
    val attributes: TagAttributesDto,
)

@Serializable
data class TagAttributesDto(
    val name: Map<String, String>,
)
