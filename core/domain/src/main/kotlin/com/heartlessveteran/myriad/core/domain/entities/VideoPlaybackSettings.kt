package com.heartlessveteran.myriad.core.domain.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Video playback preferences and settings for enhanced anime viewing experience
 */
@Entity(tableName = "video_playback_settings")
data class VideoPlaybackSettings(
    @PrimaryKey val id: String = "default",
    
    // Audio/Subtitle Presets
    val preferredAudioLanguage: String = "ja", // Japanese by default for anime
    val preferredSubtitleLanguage: String = "en", // English subtitles by default
    val enableSubtitlesByDefault: Boolean = true,
    val preferDualAudio: Boolean = false,
    
    // Frame Rate Settings
    val enableFrameRateMatching: Boolean = true,
    val preferredFrameRate: Float = 24.0f, // Standard anime frame rate
    val enableFrameRateDetection: Boolean = true,
    
    // Playback Speed Control
    val defaultPlaybackSpeed: Float = 1.0f,
    val enablePitchCorrection: Boolean = true,
    val availableSpeedOptions: List<Float> = listOf(0.5f, 0.75f, 0.9f, 1.0f, 1.1f, 1.25f, 1.5f, 2.0f),
    
    // Subtitle Styling
    val subtitleFontSize: Float = 16.0f,
    val subtitleFontFamily: String = "default",
    val subtitleTextColor: String = "#FFFFFF",
    val subtitleBackgroundColor: String = "#80000000",
    val subtitleOutlineEnabled: Boolean = true,
    val subtitleOutlineColor: String = "#000000",
    val subtitleOutlineWidth: Float = 2.0f,
    val subtitleShadowEnabled: Boolean = true,
    val subtitleShadowColor: String = "#80000000",
    val subtitleShadowOffset: Float = 2.0f,
    
    // Subtitle Position
    val subtitleVerticalPosition: Float = 0.9f, // 0.0 = top, 1.0 = bottom
    val subtitleHorizontalAlignment: SubtitleAlignment = SubtitleAlignment.CENTER,
    val subtitleMargin: Float = 16.0f,
    
    // Multi-track Support
    val enableMultipleSubtitleTracks: Boolean = false,
    val enableKaraokeSubtitles: Boolean = true,
    
    // Chapter and Scene Navigation
    val enableChapterNavigation: Boolean = true,
    val enableAutoSkipIntro: Boolean = false,
    val enableAutoSkipOutro: Boolean = false,
    val autoSkipIntroDelay: Long = 3000L, // 3 seconds delay
    val autoSkipOutroDelay: Long = 5000L, // 5 seconds delay
    val enableSceneMarkers: Boolean = true,
    
    // General Player Settings
    val enableFullscreenByDefault: Boolean = false,
    val rememberVolume: Boolean = true,
    val rememberBrightness: Boolean = true,
    val enableGestureControls: Boolean = true,
    val showPlayerControlsTimeout: Long = 3000L, // Hide controls after 3 seconds
)

/**
 * Subtitle alignment options
 */
enum class SubtitleAlignment {
    LEFT,
    CENTER,
    RIGHT
}

/**
 * Audio track information for multi-language support
 */
data class AudioTrack(
    val trackId: Int,
    val language: String,
    val languageCode: String,
    val name: String,
    val isDefault: Boolean = false,
    val channels: Int = 2,
    val bitrate: Int = 0
)

/**
 * Subtitle track information for multi-language support
 */
data class SubtitleTrack(
    val trackId: Int,
    val language: String,
    val languageCode: String,
    val name: String,
    val isDefault: Boolean = false,
    val isForced: Boolean = false,
    val isKaraoke: Boolean = false,
    val format: SubtitleFormat = SubtitleFormat.SRT
)

/**
 * Supported subtitle formats
 */
enum class SubtitleFormat {
    SRT,
    VTT,
    ASS,
    SSA,
    TTML
}

/**
 * Chapter information for episode navigation
 */
data class Chapter(
    val startTimeMs: Long,
    val endTimeMs: Long,
    val title: String,
    val type: ChapterType = ChapterType.CONTENT
)

/**
 * Types of chapters/scenes in anime episodes
 */
enum class ChapterType {
    INTRO,
    CONTENT,
    OUTRO,
    PREVIEW,
    CREDITS,
    EYECATCH
}

/**
 * Video quality settings
 */
data class VideoQuality(
    val width: Int,
    val height: Int,
    val frameRate: Float,
    val bitrate: Long,
    val codec: String
)

/**
 * Playback position with additional metadata
 */
data class PlaybackPosition(
    val positionMs: Long,
    val durationMs: Long,
    val playbackSpeed: Float = 1.0f,
    val audioTrackId: Int? = null,
    val subtitleTrackIds: List<Int> = emptyList(),
    val volume: Float = 1.0f,
    val brightness: Float = 1.0f
)