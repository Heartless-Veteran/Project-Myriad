package com.heartlessveteran.myriad.core.domain.manager

import com.heartlessveteran.myriad.core.domain.entities.Plugin
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.repository.Source
import kotlinx.coroutines.flow.Flow

/**
 * Manager interface for plugin operations and source loading
 */
interface PluginManager {
    /**
     * Gets all available plugins
     * @return Flow of list of plugins
     */
    fun getAllPlugins(): Flow<List<Plugin>>

    /**
     * Gets enabled plugins
     * @return Flow of list of enabled plugins
     */
    fun getEnabledPlugins(): Flow<List<Plugin>>

    /**
     * Gets enabled source implementations
     * @return Flow of list of active sources
     */
    fun getEnabledSources(): Flow<List<Source>>

    /**
     * Enables or disables a plugin
     * @param pluginId Plugin identifier
     * @param enabled Whether to enable the plugin
     * @return Result indicating success or error
     */
    suspend fun setPluginEnabled(pluginId: String, enabled: Boolean): Result<Unit>

    /**
     * Installs a new plugin
     * @param plugin Plugin to install
     * @return Result indicating success or error
     */
    suspend fun installPlugin(plugin: Plugin): Result<Unit>

    /**
     * Uninstalls a plugin
     * @param pluginId Plugin identifier
     * @return Result indicating success or error
     */
    suspend fun uninstallPlugin(pluginId: String): Result<Unit>

    /**
     * Gets a source implementation by plugin ID
     * @param pluginId Plugin identifier
     * @return Result containing source or error
     */
    suspend fun getSourceByPluginId(pluginId: String): Result<Source>

    /**
     * Initializes default plugins
     * @return Result indicating success or error
     */
    suspend fun initializeDefaultPlugins(): Result<Unit>
}