# Enhanced Anime Video Player Integration Guide

## Overview

This guide explains how to integrate and use the enhanced anime video playback features implemented for Project Myriad. The enhancements provide a comprehensive anime-focused viewing experience with advanced controls, subtitle management, and scene navigation.

## Architecture

### Core Components

1. **VideoPlaybackSettings** - Configuration entity for all video preferences
2. **AnimePlayerViewModel** - Enhanced ViewModel with video feature state management
3. **EnhancedVideoPlayerControls** - Custom UI controls overlay
4. **VideoPlaybackSettingsScreen** - User preferences configuration screen

### Entity Structure

```kotlin
// Video configuration
VideoPlaybackSettings(
    preferredAudioLanguage = "ja",        // Japanese audio by default
    preferredSubtitleLanguage = "en",     // English subtitles by default
    enableFrameRateMatching = true,       // 24fps anime optimization
    currentPlaybackSpeed = 1.0f,          // Speed control with pitch correction
    subtitleStyling = /* customizable */,  // Font, colors, positioning
    chapterNavigation = /* enabled */,     // Intro/outro skip functionality
)

// Track information
AudioTrack(trackId, language, name, isDefault, channels, bitrate)
SubtitleTrack(trackId, language, name, isKaraoke, format)
Chapter(startTimeMs, endTimeMs, title, type) // INTRO, CONTENT, OUTRO, PREVIEW
VideoQuality(width, height, frameRate, bitrate, codec)
```

## Implementation Guide

### 1. Basic Integration

```kotlin
// In your video player screen
@Composable
fun AnimePlayerScreen(
    animeId: String,
    episodeId: String,
    viewModel: AnimePlayerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // ExoPlayer view (disable built-in controls)
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false // Use custom controls
                }
            }
        )
        
        // Enhanced custom controls overlay
        EnhancedVideoPlayerControls(
            uiState = uiState,
            onEvent = viewModel::onEvent
        )
    }
}
```

### 2. ExoPlayer Integration Points

The enhanced features integrate with ExoPlayer at these key points:

```kotlin
// Audio track selection
fun selectAudioTrack(trackId: Int) {
    val trackSelector = exoPlayer.trackSelector as DefaultTrackSelector
    val trackGroups = /* get audio track groups */
    val builder = trackSelector.parameters.buildUpon()
    builder.setRendererDisabled(/* audio renderer */, false)
    builder.setSelectionOverride(/* audio renderer */, trackGroups, override)
    trackSelector.setParameters(builder)
}

// Subtitle track selection
fun selectSubtitleTrack(trackId: Int, enabled: Boolean) {
    val trackSelector = exoPlayer.trackSelector as DefaultTrackSelector
    val builder = trackSelector.parameters.buildUpon()
    if (enabled) {
        builder.setRendererDisabled(/* subtitle renderer */, false)
        // Set subtitle track selection
    } else {
        builder.setRendererDisabled(/* subtitle renderer */, true)
    }
    trackSelector.setParameters(builder)
}

// Playback speed with pitch correction
fun setPlaybackSpeed(speed: Float, pitchCorrection: Boolean) {
    val playbackParams = PlaybackParameters(speed, if (pitchCorrection) 1.0f else speed)
    exoPlayer.setPlaybackParameters(playbackParams)
}

// Frame rate detection
fun detectFrameRate(): Float? {
    val videoFormat = exoPlayer.videoFormat
    return videoFormat?.frameRate
}

// Seeking to chapters
fun seekToChapter(chapter: Chapter) {
    exoPlayer.seekTo(chapter.startTimeMs)
}
```

### 3. Settings Persistence

```kotlin
// Save/load video settings
class VideoSettingsRepository {
    suspend fun saveSettings(settings: VideoPlaybackSettings) {
        // Save to Room database or DataStore
        settingsDao.insertOrUpdate(settings)
    }
    
    suspend fun getSettings(): VideoPlaybackSettings {
        return settingsDao.getSettings() ?: VideoPlaybackSettings()
    }
}

// In ViewModel initialization
class AnimePlayerViewModel {
    init {
        viewModelScope.launch {
            val settings = settingsRepository.getSettings()
            _uiState.value = _uiState.value.copy(playbackSettings = settings)
        }
    }
}
```

## Feature Details

### 1. Audio/Subtitle Presets

**Default Configuration:**
- Audio: Japanese (ja) for authentic anime experience
- Subtitles: English (en) for accessibility
- Multiple subtitle tracks supported (full, signs/songs, karaoke)

**Usage:**
```kotlin
// Event handling
AnimePlayerEvent.SelectAudioTrack(trackId = 0) // Japanese
AnimePlayerEvent.SelectSubtitleTrack(trackId = 0, enabled = true) // English
```

### 2. Frame Rate Detection & Matching

**Anime Optimization:**
- Detects 24fps content automatically
- Matches display refresh rate when possible
- Provides visual feedback for frame rate status

**Implementation:**
```kotlin
// Automatic detection on video load
AnimePlayerEvent.DetectFrameRate
AnimePlayerEvent.LoadEpisode(animeId, episodeId) // Triggers detection
```

### 3. Playback Speed Control

**Features:**
- Speed range: 0.5x to 2.0x
- Pitch correction toggle
- Common speeds: 0.9x, 1.0x, 1.1x, 1.25x

**Usage:**
```kotlin
// Speed adjustment
AnimePlayerEvent.ChangePlaybackSpeed(1.25f) // 25% faster
```

### 4. Advanced Subtitle Styling

**Customization Options:**
- Font size (12sp - 32sp)
- Colors (text, background, outline, shadow)
- Position (vertical: 10% - 100%)
- Alignment (left, center, right)
- Karaoke highlighting support

**Configuration:**
```kotlin
val customSettings = VideoPlaybackSettings(
    subtitleFontSize = 18.0f,
    subtitleTextColor = "#FFFFFF",
    subtitleBackgroundColor = "#80000000",
    subtitleOutlineEnabled = true,
    subtitleVerticalPosition = 0.85f, // 85% down from top
    subtitleHorizontalAlignment = SubtitleAlignment.CENTER
)
```

### 5. Chapter Navigation

**Scene Types:**
- **INTRO** - Opening sequence (auto-skip available)
- **CONTENT** - Main episode content
- **OUTRO** - Ending sequence (auto-skip available)
- **PREVIEW** - Next episode preview
- **EYECATCH** - Mid-episode transition

**Features:**
- Visual chapter markers on timeline
- Color-coded chapter types
- Skip buttons with 3-5 second delays
- Auto-skip preferences per user

**Implementation:**
```kotlin
// Chapter data structure
val chapters = listOf(
    Chapter(0L, 90000L, "Opening", ChapterType.INTRO),
    Chapter(90000L, 1320000L, "Episode Content", ChapterType.CONTENT),
    Chapter(1320000L, 1410000L, "Ending", ChapterType.OUTRO)
)

// Skip functionality
AnimePlayerEvent.SkipIntro // Jumps to content start
AnimePlayerEvent.SkipOutro // Next episode or video end
AnimePlayerEvent.SeekToChapter(chapter) // Manual chapter navigation
```

## UI Components

### 1. Enhanced Player Controls Layout

```
┌─────────────────────────────────────────┐
│ [Episode Info]           [Skip Buttons] │ ← Top Bar
├─────────────────────────────────────────┤
│                                         │
│            [Play Controls]              │ ← Center Controls
│         ◀◀    ▶/⏸    ▶▶               │
│                                         │
├─────────────────────────────────────────┤
│ [Progress Bar with Chapter Markers]     │ ← Bottom Bar
│ [Time Info] [Speed] [Quality] [⛶]      │
└─────────────────────────────────────────┘
                                      ↑
                              Side Quick Controls
                              (Speed, Volume, Brightness)
```

### 2. Settings Screen Organization

1. **Audio & Subtitle Presets** - Language preferences and track options
2. **Frame Rate** - Detection and matching settings
3. **Playback Speed** - Default speed and pitch correction
4. **Subtitle Styling** - Visual customization options
5. **Chapter Navigation** - Skip preferences and timing
6. **General Player** - Fullscreen, gesture controls, memory settings

## Testing & Validation

### 1. Mock Data Testing

The implementation includes comprehensive mock data for testing:

```kotlin
// Test with various content types
generateMockAudioTracks() // Japanese + English audio
generateMockSubtitleTracks() // Full, Signs/Songs, Karaoke
generateMockChapters() // Complete episode structure
generateMockQualities() // 480p, 720p, 1080p options
```

### 2. Preview Components

Use the provided preview composables for UI development:

```kotlin
@Preview
@Composable
fun EnhancedVideoPlayerControlsPreview() // Player controls overlay
@Composable
fun VideoPlaybackSettingsScreenPreview() // Settings configuration
```

## Best Practices

### 1. Performance Considerations

- Load chapter data asynchronously
- Cache video quality options
- Debounce subtitle style updates
- Use efficient subtitle rendering

### 2. User Experience

- Provide visual feedback for all actions
- Remember user preferences across sessions
- Show frame rate detection results
- Auto-hide controls after 3 seconds

### 3. Accessibility

- Include content descriptions for all controls
- Support keyboard/remote navigation
- Provide high contrast subtitle options
- Ensure text scaling compatibility

## Migration from Basic Player

### Step 1: Update Dependencies
```kotlin
// Add to existing AnimePlayerViewModel constructor
class AnimePlayerViewModel(
    // ... existing dependencies
    private val videoSettingsRepository: VideoSettingsRepository
)
```

### Step 2: Replace Player View
```kotlin
// Replace basic PlayerView with enhanced controls
- useController = true
+ useController = false

+ EnhancedVideoPlayerControls(uiState, onEvent)
```

### Step 3: Add Settings Screen
```kotlin
// Navigation to settings
VideoPlaybackSettingsScreen(
    settings = uiState.playbackSettings,
    onSettingsChange = { settings ->
        viewModel.onEvent(AnimePlayerEvent.UpdateSubtitleStyle(settings))
    }
)
```

## Future Enhancements

The architecture supports these upcoming features:

1. **AI Scene Detection** - Automatic intro/outro detection
2. **Gesture Controls** - Touch gestures for volume/brightness
3. **Online Integration** - Remote subtitle and chapter sources
4. **Advanced Analytics** - Watch pattern analysis
5. **Custom Themes** - Player UI theming options

This enhanced video player provides a solid foundation for premium anime viewing experiences while maintaining clean architecture and extensibility.