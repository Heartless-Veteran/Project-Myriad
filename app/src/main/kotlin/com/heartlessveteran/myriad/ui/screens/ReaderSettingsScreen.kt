package com.heartlessveteran.myriad.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heartlessveteran.myriad.core.domain.entities.*
import com.heartlessveteran.myriad.ui.viewmodel.ReaderSettingsEvent
import com.heartlessveteran.myriad.ui.viewmodel.ReaderSettingsUiEvent
import com.heartlessveteran.myriad.ui.viewmodel.TempReaderSettingsViewModel

/**
 * Reader Settings Screen following Material 3 design guidelines.
 * Provides comprehensive customization options for the manga reader.
 * Uses MVVM architecture with Hilt dependency injection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: TempReaderSettingsViewModel = TempReaderSettingsViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.readerSettings.collectAsStateWithLifecycle()

    // Handle UI events
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is ReaderSettingsUiEvent.ShowError -> {
                    // In a real app, you'd show a SnackBar or Toast
                }
                is ReaderSettingsUiEvent.SettingsSaved -> {
                    // Optional: Show success message
                }
                is ReaderSettingsUiEvent.SettingsReset -> {
                    // Optional: Show reset confirmation
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Reader Settings",
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(ReaderSettingsEvent.ResetToDefaults) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Reset to defaults"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Reading Direction Section
            item {
                SettingsSection(title = "Reading Direction") {
                    ReadingDirectionSettings(
                        currentDirection = settings.readingDirection,
                        onDirectionChanged = { 
                            viewModel.onEvent(ReaderSettingsEvent.UpdateReadingDirection(it)) 
                        }
                    )
                }
            }

            // Page Layout Section
            item {
                SettingsSection(title = "Page Layout") {
                    PageLayoutSettings(
                        currentLayout = settings.pageLayout,
                        onLayoutChanged = { 
                            viewModel.onEvent(ReaderSettingsEvent.UpdatePageLayout(it)) 
                        }
                    )
                }
            }

            // Display Settings Section
            item {
                SettingsSection(title = "Display") {
                    DisplaySettings(
                        settings = settings,
                        onBackgroundColorChanged = { 
                            viewModel.onEvent(ReaderSettingsEvent.UpdateBackgroundColor(it)) 
                        },
                        onZoomTypeChanged = { 
                            viewModel.onEvent(ReaderSettingsEvent.UpdateZoomType(it)) 
                        },
                        onCustomZoomChanged = { 
                            viewModel.onEvent(ReaderSettingsEvent.UpdateCustomZoom(it)) 
                        },
                        onFullscreenChanged = { 
                            viewModel.onEvent(ReaderSettingsEvent.UpdateFullscreenMode(it)) 
                        }
                    )
                }
            }

            // Navigation Settings Section
            item {
                SettingsSection(title = "Navigation") {
                    NavigationSettings(
                        settings = settings,
                        onVolumeKeyNavigationChanged = { 
                            viewModel.onEvent(ReaderSettingsEvent.UpdateVolumeKeyNavigation(it)) 
                        },
                        onTapNavigationChanged = { 
                            viewModel.onEvent(ReaderSettingsEvent.UpdateTapNavigation(it)) 
                        }
                    )
                }
            }

            // Advanced Settings Section
            item {
                SettingsSection(title = "Advanced") {
                    AdvancedSettings(
                        settings = settings,
                        onKeepScreenOnChanged = { 
                            viewModel.onEvent(ReaderSettingsEvent.UpdateKeepScreenOn(it)) 
                        },
                        onShowPageIndicatorChanged = { 
                            viewModel.onEvent(ReaderSettingsEvent.UpdateShowPageIndicator(it)) 
                        },
                        onDoubleTapZoomChanged = { 
                            viewModel.onEvent(ReaderSettingsEvent.UpdateDoubleTapZoom(it)) 
                        },
                        onCropBordersChanged = { 
                            viewModel.onEvent(ReaderSettingsEvent.UpdateCropBorders(it)) 
                        },
                        onAnimationDurationChanged = { 
                            viewModel.onEvent(ReaderSettingsEvent.UpdateAnimationDuration(it)) 
                        },
                        onPageSpacingChanged = { 
                            viewModel.onEvent(ReaderSettingsEvent.UpdatePageSpacing(it)) 
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun ReadingDirectionSettings(
    currentDirection: ReadingDirection,
    onDirectionChanged: (ReadingDirection) -> Unit
) {
    val directions = listOf(
        ReadingDirection.LEFT_TO_RIGHT to "Left to Right",
        ReadingDirection.RIGHT_TO_LEFT to "Right to Left", 
        ReadingDirection.VERTICAL to "Vertical",
        ReadingDirection.WEBTOON to "Webtoon"
    )

    Column(modifier = Modifier.selectableGroup()) {
        directions.forEach { (direction, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = currentDirection == direction,
                        onClick = { onDirectionChanged(direction) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentDirection == direction,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PageLayoutSettings(
    currentLayout: PageLayout,
    onLayoutChanged: (PageLayout) -> Unit
) {
    val layouts = listOf(
        PageLayout.SINGLE_PAGE to "Single Page",
        PageLayout.DOUBLE_PAGE to "Double Page",
        PageLayout.AUTOMATIC to "Automatic"
    )

    Column(modifier = Modifier.selectableGroup()) {
        layouts.forEach { (layout, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = currentLayout == layout,
                        onClick = { onLayoutChanged(layout) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentLayout == layout,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun DisplaySettings(
    settings: ReaderSettings,
    onBackgroundColorChanged: (BackgroundColor) -> Unit,
    onZoomTypeChanged: (ZoomType) -> Unit,
    onCustomZoomChanged: (Float) -> Unit,
    onFullscreenChanged: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Background Color
        Text(
            text = "Background Color",
            style = MaterialTheme.typography.titleSmall
        )
        
        val backgroundColors = listOf(
            BackgroundColor.BLACK to "Black",
            BackgroundColor.WHITE to "White",
            BackgroundColor.GRAY to "Gray",
            BackgroundColor.AUTOMATIC to "Automatic"
        )

        Column(modifier = Modifier.selectableGroup()) {
            backgroundColors.forEach { (color, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = settings.backgroundColor == color,
                            onClick = { onBackgroundColorChanged(color) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = settings.backgroundColor == color,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Divider()

        // Zoom Settings
        Text(
            text = "Zoom Type",
            style = MaterialTheme.typography.titleSmall
        )

        val zoomTypes = listOf(
            ZoomType.FIT_WIDTH to "Fit Width",
            ZoomType.FIT_HEIGHT to "Fit Height",
            ZoomType.FIT_SCREEN to "Fit Screen",
            ZoomType.ORIGINAL_SIZE to "Original Size",
            ZoomType.CUSTOM to "Custom"
        )

        Column(modifier = Modifier.selectableGroup()) {
            zoomTypes.forEach { (zoomType, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = settings.zoomType == zoomType,
                            onClick = { onZoomTypeChanged(zoomType) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = settings.zoomType == zoomType,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Custom Zoom Slider
        if (settings.zoomType == ZoomType.CUSTOM) {
            Column {
                Text(
                    text = "Custom Zoom: ${String.format("%.1fx", settings.customZoomLevel)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Slider(
                    value = settings.customZoomLevel,
                    onValueChange = onCustomZoomChanged,
                    valueRange = 0.5f..3.0f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Divider()

        // Fullscreen Mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fullscreen Mode",
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = settings.fullscreenMode,
                onCheckedChange = onFullscreenChanged
            )
        }
    }
}

@Composable
private fun NavigationSettings(
    settings: ReaderSettings,
    onVolumeKeyNavigationChanged: (Boolean) -> Unit,
    onTapNavigationChanged: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Volume Key Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Volume Key Navigation",
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = settings.enableVolumeKeyNavigation,
                onCheckedChange = onVolumeKeyNavigationChanged
            )
        }

        // Tap Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tap Navigation",
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = settings.enableTapNavigation,
                onCheckedChange = onTapNavigationChanged
            )
        }
    }
}

@Composable
private fun AdvancedSettings(
    settings: ReaderSettings,
    onKeepScreenOnChanged: (Boolean) -> Unit,
    onShowPageIndicatorChanged: (Boolean) -> Unit,
    onDoubleTapZoomChanged: (Boolean) -> Unit,
    onCropBordersChanged: (Boolean) -> Unit,
    onAnimationDurationChanged: (Int) -> Unit,
    onPageSpacingChanged: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Keep Screen On
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Keep Screen On",
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = settings.keepScreenOn,
                onCheckedChange = onKeepScreenOnChanged
            )
        }

        // Show Page Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Show Page Indicator",
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = settings.showPageIndicator,
                onCheckedChange = onShowPageIndicatorChanged
            )
        }

        // Double Tap Zoom
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Double Tap to Zoom",
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = settings.enableDoubleTapZoom,
                onCheckedChange = onDoubleTapZoomChanged
            )
        }

        // Crop Borders
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Crop Page Borders",
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = settings.cropBorders,
                onCheckedChange = onCropBordersChanged
            )
        }

        Divider()

        // Page Spacing
        Column {
            Text(
                text = "Page Spacing: ${settings.pageSpacing}dp",
                style = MaterialTheme.typography.bodySmall
            )
            Slider(
                value = settings.pageSpacing.toFloat(),
                onValueChange = { onPageSpacingChanged(it.toInt()) },
                valueRange = 0f..32f,
                steps = 7,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Animation Duration
        Column {
            Text(
                text = "Animation Duration: ${settings.animationDuration}ms",
                style = MaterialTheme.typography.bodySmall
            )
            Slider(
                value = settings.animationDuration.toFloat(),
                onValueChange = { onAnimationDurationChanged(it.toInt()) },
                valueRange = 0f..1000f,
                steps = 19,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}