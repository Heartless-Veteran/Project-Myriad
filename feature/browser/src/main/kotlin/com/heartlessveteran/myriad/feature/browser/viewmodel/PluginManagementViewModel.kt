package com.heartlessveteran.myriad.feature.browser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.core.domain.entities.Plugin
import com.heartlessveteran.myriad.core.domain.manager.PluginManager
import com.heartlessveteran.myriad.core.domain.model.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI state for plugin management screen
 */
data class PluginManagementUiState(
    val plugins: List<Plugin> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isInitializing: Boolean = false
)

/**
 * ViewModel for plugin management functionality
 */
class PluginManagementViewModel(
    private val pluginManager: PluginManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PluginManagementUiState())
    val uiState: StateFlow<PluginManagementUiState> = _uiState.asStateFlow()

    init {
        initializePlugins()
        loadPlugins()
    }

    /**
     * Initializes default plugins on first run
     */
    private fun initializePlugins() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isInitializing = true)
            
            when (val result = pluginManager.initializeDefaultPlugins()) {
                is Result.Success -> {
                    // Default plugins initialized successfully
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to initialize plugins: ${result.message}",
                        isInitializing = false
                    )
                }
                is Result.Loading -> {
                    // Should not happen in this context
                }
            }
            
            _uiState.value = _uiState.value.copy(isInitializing = false)
        }
    }

    /**
     * Loads all plugins from the repository
     */
    private fun loadPlugins() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            pluginManager.getAllPlugins()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load plugins: ${e.message}"
                    )
                }
                .collect { plugins ->
                    _uiState.value = _uiState.value.copy(
                        plugins = plugins,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    /**
     * Toggles plugin enabled state
     */
    fun togglePlugin(pluginId: String, enabled: Boolean) {
        viewModelScope.launch {
            when (val result = pluginManager.setPluginEnabled(pluginId, enabled)) {
                is Result.Success -> {
                    // Plugin state updated successfully
                    // The UI will automatically update via the Flow
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update plugin: ${result.message}"
                    )
                }
                is Result.Loading -> {
                    // Should not happen in this context
                }
            }
        }
    }

    /**
     * Uninstalls a plugin
     */
    fun uninstallPlugin(pluginId: String) {
        viewModelScope.launch {
            when (val result = pluginManager.uninstallPlugin(pluginId)) {
                is Result.Success -> {
                    // Plugin uninstalled successfully
                    // The UI will automatically update via the Flow
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to uninstall plugin: ${result.message}"
                    )
                }
                is Result.Loading -> {
                    // Should not happen in this context
                }
            }
        }
    }

    /**
     * Clears any error messages
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Refreshes the plugin list
     */
    fun refresh() {
        loadPlugins()
    }
}