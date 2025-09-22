package com.heartlessveteran.myriad.core.data.repository

import com.heartlessveteran.myriad.core.data.database.PluginDao
import com.heartlessveteran.myriad.core.domain.entities.Plugin
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.repository.PluginRepository
import kotlinx.coroutines.flow.Flow
/**
 * Implementation of PluginRepository using Room database
 */
class PluginRepositoryImpl(
    private val pluginDao: PluginDao
) : PluginRepository {

    override fun getAllPlugins(): Flow<List<Plugin>> {
        return pluginDao.getAllPlugins()
    }

    override fun getEnabledPlugins(): Flow<List<Plugin>> {
        return pluginDao.getEnabledPlugins()
    }

    override suspend fun getPlugin(id: String): Result<Plugin> {
        return try {
            val plugin = pluginDao.getPlugin(id)
            if (plugin != null) {
                Result.Success(plugin)
            } else {
                Result.Error(
                    NoSuchElementException("Plugin not found"),
                    "No plugin found with ID: $id"
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get plugin: ${e.message}")
        }
    }

    override suspend fun savePlugin(plugin: Plugin): Result<Unit> {
        return try {
            pluginDao.insertPlugin(plugin)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to save plugin: ${e.message}")
        }
    }

    override suspend fun updatePluginEnabled(id: String, enabled: Boolean): Result<Unit> {
        return try {
            pluginDao.updatePluginEnabled(id, enabled)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update plugin enabled state: ${e.message}")
        }
    }

    override suspend fun deletePlugin(id: String): Result<Unit> {
        return try {
            pluginDao.deletePlugin(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to delete plugin: ${e.message}")
        }
    }
}