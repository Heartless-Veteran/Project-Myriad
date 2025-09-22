package com.heartlessveteran.myriad.core.domain.repository

import com.heartlessveteran.myriad.core.domain.entities.Plugin
import com.heartlessveteran.myriad.core.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for plugin management operations
 */
interface PluginRepository {
    /**
     * Gets all plugins from the database
     * @return Flow of list of plugins
     */
    fun getAllPlugins(): Flow<List<Plugin>>

    /**
     * Gets enabled plugins from the database
     * @return Flow of list of enabled plugins
     */
    fun getEnabledPlugins(): Flow<List<Plugin>>

    /**
     * Gets a specific plugin by ID
     * @param id Plugin identifier
     * @return Result containing plugin or error
     */
    suspend fun getPlugin(id: String): Result<Plugin>

    /**
     * Inserts or updates a plugin
     * @param plugin Plugin to save
     * @return Result indicating success or error
     */
    suspend fun savePlugin(plugin: Plugin): Result<Unit>

    /**
     * Updates plugin enabled state
     * @param id Plugin identifier
     * @param enabled Whether plugin should be enabled
     * @return Result indicating success or error
     */
    suspend fun updatePluginEnabled(id: String, enabled: Boolean): Result<Unit>

    /**
     * Deletes a plugin
     * @param id Plugin identifier
     * @return Result indicating success or error
     */
    suspend fun deletePlugin(id: String): Result<Unit>
}