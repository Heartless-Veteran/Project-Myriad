package com.heartlessveteran.myriad.core.data.manager

import com.heartlessveteran.myriad.core.data.source.LocalSource
import com.heartlessveteran.myriad.core.data.source.SampleOnlineSource
import com.heartlessveteran.myriad.core.domain.entities.Plugin
import com.heartlessveteran.myriad.core.domain.entities.PluginType
import com.heartlessveteran.myriad.core.domain.manager.PluginManager
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.repository.PluginRepository
import com.heartlessveteran.myriad.core.domain.repository.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

/**
 * Implementation of PluginManager for managing source plugins
 */
class PluginManagerImpl(
    private val pluginRepository: PluginRepository
) : PluginManager {

    // Registry of available source implementations
    private val sourceRegistry = mapOf(
        "local" to { LocalSource() },
        "sample_online" to { SampleOnlineSource() }
    )

    override fun getAllPlugins(): Flow<List<Plugin>> {
        return pluginRepository.getAllPlugins()
    }

    override fun getEnabledPlugins(): Flow<List<Plugin>> {
        return pluginRepository.getEnabledPlugins()
    }

    override fun getEnabledSources(): Flow<List<Source>> {
        return getEnabledPlugins().map { plugins ->
            plugins.mapNotNull { plugin ->
                sourceRegistry[plugin.id]?.invoke()
            }
        }
    }

    override suspend fun setPluginEnabled(pluginId: String, enabled: Boolean): Result<Unit> {
        return pluginRepository.updatePluginEnabled(pluginId, enabled)
    }

    override suspend fun installPlugin(plugin: Plugin): Result<Unit> {
        return pluginRepository.savePlugin(plugin)
    }

    override suspend fun uninstallPlugin(pluginId: String): Result<Unit> {
        return pluginRepository.deletePlugin(pluginId)
    }

    override suspend fun getSourceByPluginId(pluginId: String): Result<Source> {
        return try {
            val sourceFactory = sourceRegistry[pluginId]
            if (sourceFactory != null) {
                Result.Success(sourceFactory())
            } else {
                Result.Error(
                    NoSuchElementException("Source not found"),
                    "No source implementation found for plugin: $pluginId"
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get source for plugin $pluginId: ${e.message}")
        }
    }

    override suspend fun initializeDefaultPlugins(): Result<Unit> {
        return try {
            // Create default local plugin
            val localPlugin = Plugin(
                id = "local",
                name = "Local Storage",
                author = "Project Myriad",
                version = "1.0.0",
                description = "Access manga from local storage (.cbz, .cbr files)",
                language = "en",
                baseUrl = "",
                isEnabled = true,
                isInstalled = true,
                lastUpdated = Date(),
                dateInstalled = Date(),
                type = PluginType.MANGA
            )

            // Create sample online plugin
            val onlinePlugin = Plugin(
                id = "sample_online",
                name = "Sample Online",
                author = "Project Myriad",
                version = "1.0.0",
                description = "Sample online manga source for demonstration",
                language = "en",
                baseUrl = "https://api.sample-manga.com",
                isEnabled = true,
                isInstalled = true,
                lastUpdated = Date(),
                dateInstalled = Date(),
                type = PluginType.MANGA
            )

            // Install default plugins
            pluginRepository.savePlugin(localPlugin)
            pluginRepository.savePlugin(onlinePlugin)

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to initialize default plugins: ${e.message}")
        }
    }
}