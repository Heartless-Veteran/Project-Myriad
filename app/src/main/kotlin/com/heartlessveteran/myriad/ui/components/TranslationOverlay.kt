package com.heartlessveteran.myriad.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heartlessveteran.myriad.services.TranslatedTextBound

/**
 * Translation overlay component that displays translated text over manga pages
 */
@Composable
fun TranslationOverlay(
    translatedTexts: List<TranslatedTextBound>,
    isVisible: Boolean,
    pageWidth: Dp,
    pageHeight: Dp,
    onToggleTranslation: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    
    Box(modifier = modifier.size(pageWidth, pageHeight)) {
        // Draw translation text boxes
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawTranslationBoxes(translatedTexts, pageWidth, pageHeight)
        }
        
        // Individual text overlays
        translatedTexts.forEach { textBound ->
            TranslationTextBox(
                textBound = textBound,
                pageWidth = pageWidth,
                pageHeight = pageHeight
            )
        }
        
        // Translation control toggle
        FloatingActionButton(
            onClick = onToggleTranslation,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Hide Translation"
            )
        }
    }
}

/**
 * Individual text box showing translation
 */
@Composable
private fun TranslationTextBox(
    textBound: TranslatedTextBound,
    pageWidth: Dp,
    pageHeight: Dp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    // Convert pixel coordinates to Dp
    val xDp = with(density) { textBound.x.toDp() }
    val yDp = with(density) { textBound.y.toDp() }
    val widthDp = with(density) { textBound.width.toDp() }
    val heightDp = with(density) { textBound.height.toDp() }
    
    var showOriginal by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .offset(x = xDp, y = yDp)
            .size(width = widthDp.coerceAtLeast(80.dp), height = heightDp.coerceAtLeast(30.dp))
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable { showOriginal = !showOriginal },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showOriginal) textBound.originalText else textBound.translatedText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (showOriginal) FontWeight.Normal else FontWeight.Medium
                    ),
                    color = if (showOriginal) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) 
                    else 
                        MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 3
                )
            }
        }
        
        // Confidence indicator
        if (textBound.confidence < 0.7f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp)
                    .background(
                        color = Color(0xFFFFA500).copy(alpha = 0.8f), // Orange color
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

/**
 * Draw background boxes for translation areas
 */
private fun DrawScope.drawTranslationBoxes(
    translatedTexts: List<TranslatedTextBound>,
    pageWidth: Dp,
    pageHeight: Dp
) {
    translatedTexts.forEach { textBound ->
        val rect = androidx.compose.ui.geometry.Rect(
            offset = Offset(textBound.x, textBound.y),
            size = Size(textBound.width, textBound.height)
        )
        
        // Draw semi-transparent background
        drawRect(
            color = Color.Black.copy(alpha = 0.1f),
            topLeft = rect.topLeft,
            size = rect.size
        )
        
        // Draw border
        drawRect(
            color = Color.Blue.copy(alpha = 0.3f),
            topLeft = rect.topLeft,
            size = rect.size,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
        )
    }
}

/**
 * Translation control panel
 */
@Composable
fun TranslationControlPanel(
    isTranslationVisible: Boolean,
    isTranslating: Boolean,
    onToggleTranslation: () -> Unit,
    onStartTranslation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Translation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isTranslating) "Processing..." else "Tap to translate text",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isTranslationVisible) {
                    TextButton(onClick = onToggleTranslation) {
                        Text("Hide")
                    }
                }
                
                if (isTranslating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    FilledTonalButton(onClick = onStartTranslation) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Translate",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Translate")
                    }
                }
            }
        }
    }
}

/**
 * Translation settings panel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationSettingsPanel(
    targetLanguage: String,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val languages = listOf(
        "English" to "en",
        "Spanish" to "es", 
        "French" to "fr",
        "German" to "de",
        "Korean" to "ko",
        "Chinese" to "zh"
    )
    
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Translation Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = languages.find { it.second == targetLanguage }?.first ?: "English",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Target Language") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    languages.forEach { (name, code) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                onLanguageChange(code)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}