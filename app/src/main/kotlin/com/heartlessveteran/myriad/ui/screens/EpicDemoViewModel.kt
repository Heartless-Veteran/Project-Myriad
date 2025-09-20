package com.heartlessveteran.myriad.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.di.AppDiContainer
import com.heartlessveteran.myriad.domain.ai.AIFeature
import com.heartlessveteran.myriad.domain.vault.VaultStatistics
import com.heartlessveteran.myriad.app.extensions.ExtensionConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Epic Demo Screen
 */
class EpicDemoViewModel(
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(EpicDemoUiState())
    val uiState: StateFlow<EpicDemoUiState> = _uiState.asStateFlow()

    fun initializeExtensions() {
        viewModelScope.launch {
            try {
                val extensionManager = AppDiContainer.getExtensionManager(context)
                val result = extensionManager.initialize()
                
                if (result.isSuccess) {
                    val configuration = extensionManager.getExtensionConfiguration()
                    _uiState.value = _uiState.value.copy(
                        extensionConfiguration = configuration,
                        isLoading = false
                    )
                } else {
                    // Handle error - for demo, just show empty configuration
                    _uiState.value = _uiState.value.copy(
                        extensionConfiguration = ExtensionConfiguration(
                            totalSources = 0,
                            enabledSources = 0,
                            builtInSources = 0,
                            externalSources = 0,
                            sdkVersion = "1.0.0"
                        ),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun initializeVault() {
        viewModelScope.launch {
            try {
                val vaultService = AppDiContainer.getVaultService(context)
                val result = vaultService.getVaultStatistics()
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        vaultStatistics = (result as com.heartlessveteran.myriad.domain.models.Result.Success).data,
                        isLoading = false
                    )
                } else {
                    // Handle error - for demo, show empty statistics
                    _uiState.value = _uiState.value.copy(
                        vaultStatistics = com.heartlessveteran.myriad.domain.vault.VaultStatistics(
                            totalItems = 0,
                            totalSize = 0L,
                            mangaCount = 0,
                            animeCount = 0,
                            novelCount = 0,
                            audioCount = 0,
                            totalCollections = 0,
                            totalTags = 0,
                            diskSpaceUsed = 0L,
                            averageFileSize = 0L
                        ),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun initializeAICore() {
        viewModelScope.launch {
            try {
                val aiCore = AppDiContainer.getAICore()
                val initResult = aiCore.initialize()
                
                if (initResult.isSuccess) {
                    val featuresResult = aiCore.getAvailableFeatures()
                    if (featuresResult.isSuccess) {
                        _uiState.value = _uiState.value.copy(
                            aiFeatures = (featuresResult as com.heartlessveteran.myriad.domain.models.Result.Success).data,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        aiFeatures = emptyList(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}

/**
 * UI State for the Epic Demo Screen
 */
data class EpicDemoUiState(
    val isLoading: Boolean = false,
    val extensionConfiguration: ExtensionConfiguration? = null,
    val vaultStatistics: VaultStatistics? = null,
    val aiFeatures: List<AIFeature> = emptyList(),
    val error: String? = null
)