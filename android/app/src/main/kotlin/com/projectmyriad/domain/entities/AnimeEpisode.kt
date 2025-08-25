package com.projectmyriad.domain.entities

import kotlinx.serialization.Serializable

/**
 * Represents an anime episode with video and subtitle data.
 */
@Serializable
data class AnimeEpisode(
    val id: String,
    val animeId: String,
    val episodeNumber: Int,
    val title: String,
    val description: String? = null,
    val duration: Long, // in milliseconds
    val videoUrl: String? = null,
    val localVideoPath: String? = null,
    val thumbnailUrl: String? = null,
    val subtitles: List<SubtitleTrack> = emptyList(),
    val watchProgress: Float = 0f, // 0.0 to 1.0
    val isWatched: Boolean = false,
    val downloadedAt: String? = null,
    val fileSize: Long? = null,
    val quality: VideoQuality? = null,
    val audioTracks: List<AudioTrack> = emptyList()
)

/**
 * Represents subtitle track information.
 */
@Serializable
data class SubtitleTrack(
    val id: String,
    val language: String,
    val label: String,
    val url: String? = null,
    val localPath: String? = null,
    val format: SubtitleFormat = SubtitleFormat.SRT,
    val isDefault: Boolean = false
)

/**
 * Represents audio track information.
 */
@Serializable
data class AudioTrack(
    val id: String,
    val language: String,
    val label: String,
    val codec: String? = null,
    val isDefault: Boolean = false
)

@Serializable
enum class VideoQuality {
    SD_480P,
    HD_720P,
    FHD_1080P,
    UHD_4K
}

@Serializable
enum class SubtitleFormat {
    SRT,
    ASS,
    VTT,
    SSA
}