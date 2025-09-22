package com.heartlessveteran.myriad.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heartlessveteran.myriad.core.domain.entities.SubtitleAlignment
import com.heartlessveteran.myriad.core.domain.entities.VideoPlaybackSettings

/**
 * Video playback settings screen for configuring anime player preferences
 */
@Composable
fun VideoPlaybackSettingsScreen(
    settings: VideoPlaybackSettings,
    onSettingsChange: (VideoPlaybackSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Video Playback Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        // Audio & Subtitle Presets Section
        item {
            SettingsSection(
                title = "Audio & Subtitle Presets",
                icon = Icons.Default.VolumeUp,
            ) {
                AudioSubtitlePreferences(
                    settings = settings,
                    onSettingsChange = onSettingsChange,
                )
            }
        }

        // Frame Rate Settings Section
        item {
            SettingsSection(
                title = "Frame Rate",
                icon = Icons.Default.VideoSettings,
            ) {
                FrameRateSettings(
                    settings = settings,
                    onSettingsChange = onSettingsChange,
                )
            }
        }

        // Playback Speed Section
        item {
            SettingsSection(
                title = "Playback Speed",
                icon = Icons.Default.Speed,
            ) {
                PlaybackSpeedSettings(
                    settings = settings,
                    onSettingsChange = onSettingsChange,
                )
            }
        }

        // Subtitle Styling Section
        item {
            SettingsSection(
                title = "Subtitle Styling",
                icon = Icons.Default.Subtitles,
            ) {
                SubtitleStylingSettings(
                    settings = settings,
                    onSettingsChange = onSettingsChange,
                )
            }
        }

        // Chapter Navigation Section
        item {
            SettingsSection(
                title = "Chapter & Scene Navigation",
                icon = Icons.Default.PlaylistPlay,
            ) {
                ChapterNavigationSettings(
                    settings = settings,
                    onSettingsChange = onSettingsChange,
                )
            }
        }

        // General Player Settings Section
        item {
            SettingsSection(
                title = "General Player",
                icon = Icons.Default.Settings,
            ) {
                GeneralPlayerSettings(
                    settings = settings,
                    onSettingsChange = onSettingsChange,
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            content()
        }
    }
}

@Composable
private fun AudioSubtitlePreferences(
    settings: VideoPlaybackSettings,
    onSettingsChange: (VideoPlaybackSettings) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Preferred Audio Language
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Preferred Audio Language")
            var expanded by remember { mutableStateOf(false) }

            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(if (settings.preferredAudioLanguage == "ja") "Japanese" else "English")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Japanese") },
                        onClick = {
                            onSettingsChange(settings.copy(preferredAudioLanguage = "ja"))
                            expanded = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("English") },
                        onClick = {
                            onSettingsChange(settings.copy(preferredAudioLanguage = "en"))
                            expanded = false
                        },
                    )
                }
            }
        }

        // Preferred Subtitle Language
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Preferred Subtitle Language")
            var expanded by remember { mutableStateOf(false) }

            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(if (settings.preferredSubtitleLanguage == "en") "English" else "Japanese")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("English") },
                        onClick = {
                            onSettingsChange(settings.copy(preferredSubtitleLanguage = "en"))
                            expanded = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Japanese") },
                        onClick = {
                            onSettingsChange(settings.copy(preferredSubtitleLanguage = "ja"))
                            expanded = false
                        },
                    )
                }
            }
        }

        // Enable Subtitles by Default
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Enable Subtitles by Default")
            Switch(
                checked = settings.enableSubtitlesByDefault,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(enableSubtitlesByDefault = enabled))
                },
            )
        }

        // Enable Multiple Subtitle Tracks
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Multiple Subtitle Tracks")
            Switch(
                checked = settings.enableMultipleSubtitleTracks,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(enableMultipleSubtitleTracks = enabled))
                },
            )
        }

        // Enable Karaoke Subtitles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Karaoke Subtitle Highlighting")
            Switch(
                checked = settings.enableKaraokeSubtitles,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(enableKaraokeSubtitles = enabled))
                },
            )
        }
    }
}

@Composable
private fun FrameRateSettings(
    settings: VideoPlaybackSettings,
    onSettingsChange: (VideoPlaybackSettings) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Enable Frame Rate Matching
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Frame Rate Matching")
            Switch(
                checked = settings.enableFrameRateMatching,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(enableFrameRateMatching = enabled))
                },
            )
        }

        // Enable Frame Rate Detection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Automatic Frame Rate Detection")
            Switch(
                checked = settings.enableFrameRateDetection,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(enableFrameRateDetection = enabled))
                },
            )
        }

        // Preferred Frame Rate
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Preferred Frame Rate")
            Text("${settings.preferredFrameRate} fps")
        }
    }
}

@Composable
private fun PlaybackSpeedSettings(
    settings: VideoPlaybackSettings,
    onSettingsChange: (VideoPlaybackSettings) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Default Playback Speed
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Default Playback Speed")
            var expanded by remember { mutableStateOf(false) }

            Box {
                TextButton(onClick = { expanded = true }) {
                    Text("${settings.defaultPlaybackSpeed}x")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    settings.availableSpeedOptions.forEach { speed ->
                        DropdownMenuItem(
                            text = { Text("${speed}x") },
                            onClick = {
                                onSettingsChange(settings.copy(defaultPlaybackSpeed = speed))
                                expanded = false
                            },
                        )
                    }
                }
            }
        }

        // Enable Pitch Correction
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Pitch Correction")
            Switch(
                checked = settings.enablePitchCorrection,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(enablePitchCorrection = enabled))
                },
            )
        }
    }
}

@Composable
private fun SubtitleStylingSettings(
    settings: VideoPlaybackSettings,
    onSettingsChange: (VideoPlaybackSettings) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Subtitle Font Size
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Font Size")
            Text("${settings.subtitleFontSize.toInt()}sp")
        }

        Slider(
            value = settings.subtitleFontSize,
            onValueChange = { size ->
                onSettingsChange(settings.copy(subtitleFontSize = size))
            },
            valueRange = 12f..32f,
            steps = 20,
        )

        // Subtitle Position
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Vertical Position")
            Text("${(settings.subtitleVerticalPosition * 100).toInt()}%")
        }

        Slider(
            value = settings.subtitleVerticalPosition,
            onValueChange = { position ->
                onSettingsChange(settings.copy(subtitleVerticalPosition = position))
            },
            valueRange = 0.1f..1.0f,
        )

        // Subtitle Alignment
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Text Alignment")
            var expanded by remember { mutableStateOf(false) }

            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(settings.subtitleHorizontalAlignment.name)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    SubtitleAlignment.values().forEach { alignment ->
                        DropdownMenuItem(
                            text = { Text(alignment.name) },
                            onClick = {
                                onSettingsChange(settings.copy(subtitleHorizontalAlignment = alignment))
                                expanded = false
                            },
                        )
                    }
                }
            }
        }

        // Subtitle Outline
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Text Outline")
            Switch(
                checked = settings.subtitleOutlineEnabled,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(subtitleOutlineEnabled = enabled))
                },
            )
        }

        // Subtitle Shadow
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Text Shadow")
            Switch(
                checked = settings.subtitleShadowEnabled,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(subtitleShadowEnabled = enabled))
                },
            )
        }
    }
}

@Composable
private fun ChapterNavigationSettings(
    settings: VideoPlaybackSettings,
    onSettingsChange: (VideoPlaybackSettings) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Enable Chapter Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Chapter Navigation")
            Switch(
                checked = settings.enableChapterNavigation,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(enableChapterNavigation = enabled))
                },
            )
        }

        // Auto Skip Intro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Auto-Skip Opening")
            Switch(
                checked = settings.enableAutoSkipIntro,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(enableAutoSkipIntro = enabled))
                },
            )
        }

        // Auto Skip Outro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Auto-Skip Ending")
            Switch(
                checked = settings.enableAutoSkipOutro,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(enableAutoSkipOutro = enabled))
                },
            )
        }

        // Enable Scene Markers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Scene Markers")
            Switch(
                checked = settings.enableSceneMarkers,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(enableSceneMarkers = enabled))
                },
            )
        }
    }
}

@Composable
private fun GeneralPlayerSettings(
    settings: VideoPlaybackSettings,
    onSettingsChange: (VideoPlaybackSettings) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Enable Fullscreen by Default
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Fullscreen by Default")
            Switch(
                checked = settings.enableFullscreenByDefault,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(enableFullscreenByDefault = enabled))
                },
            )
        }

        // Remember Volume
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Remember Volume")
            Switch(
                checked = settings.rememberVolume,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(rememberVolume = enabled))
                },
            )
        }

        // Remember Brightness
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Remember Brightness")
            Switch(
                checked = settings.rememberBrightness,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(rememberBrightness = enabled))
                },
            )
        }

        // Enable Gesture Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Gesture Controls")
            Switch(
                checked = settings.enableGestureControls,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(enableGestureControls = enabled))
                },
            )
        }
    }
}
