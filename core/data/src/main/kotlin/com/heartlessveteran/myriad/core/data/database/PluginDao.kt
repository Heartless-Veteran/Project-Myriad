package com.heartlessveteran.myriad.core.data.database

import androidx.room.*
import com.heartlessveteran.myriad.core.domain.entities.Plugin
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for plugin operations
 */
@Dao
interface PluginDao {
    /**
     * Gets all plugins
     * @return Flow of list of plugins
     */
    @Query("SELECT * FROM plugins ORDER BY name ASC")
    fun getAllPlugins(): Flow<List<Plugin>>

    /**
     * Gets enabled plugins
     * @return Flow of list of enabled plugins
     */
    @Query("SELECT * FROM plugins WHERE isEnabled = 1 ORDER BY name ASC")
    fun getEnabledPlugins(): Flow<List<Plugin>>

    /**
     * Gets a specific plugin by ID
     * @param id Plugin identifier
     * @return Plugin or null
     */
    @Query("SELECT * FROM plugins WHERE id = :id")
    suspend fun getPlugin(id: String): Plugin?

    /**
     * Inserts a plugin
     * @param plugin Plugin to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlugin(plugin: Plugin)

    /**
     * Updates a plugin
     * @param plugin Plugin to update
     */
    @Update
    suspend fun updatePlugin(plugin: Plugin)

    /**
     * Updates plugin enabled state
     * @param id Plugin identifier
     * @param enabled Whether plugin should be enabled
     */
    @Query("UPDATE plugins SET isEnabled = :enabled WHERE id = :id")
    suspend fun updatePluginEnabled(id: String, enabled: Boolean)

    /**
     * Deletes a plugin
     * @param id Plugin identifier
     */
    @Query("DELETE FROM plugins WHERE id = :id")
    suspend fun deletePlugin(id: String)

    /**
     * Checks if a plugin exists
     * @param id Plugin identifier
     * @return True if plugin exists
     */
    @Query("SELECT COUNT(*) > 0 FROM plugins WHERE id = :id")
    suspend fun pluginExists(id: String): Boolean
}