package com.heartlessveteran.myriad.feature.ai.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.heartlessveteran.myriad.core.domain.ai.AIProvider
import com.heartlessveteran.myriad.core.data.ai.GeminiProvider
import com.heartlessveteran.myriad.core.data.ai.OpenAIProvider

/**
 * Composable component for selecting AI providers.
 * Follows Material Design 3 guidelines and project UI patterns.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIProviderSelector(
    providers: List<AIProvider>,
    selectedProvider: AIProvider,
    onProviderSelected: (AIProvider) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            value = selectedProvider.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("AI Provider") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            providers.forEach { provider ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = provider.name,
                            color = if (provider.isAvailable) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }
                        )
                    },
                    onClick = {
                        if (provider.isAvailable) {
                            onProviderSelected(provider)
                            expanded = false
                        }
                    },
                    enabled = provider.isAvailable
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AIProviderSelectorPreview() {
    val providers = listOf(
        GeminiProvider(),
        OpenAIProvider()
    )
    
    AIProviderSelector(
        providers = providers,
        selectedProvider = providers.first(),
        onProviderSelected = {}
    )
}