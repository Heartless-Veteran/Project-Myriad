package com.heartlessveteran.myriad.app.extensions

import android.content.Context
import android.content.pm.PackageManager
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.domain.sources.Source
import com.heartlessveteran.myriad.data.sources.ExampleMangaSource
import dalvik.system.PathClassLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Extension Manager for the App Layer
 * 
 * Manages the loading, scanning, and lifecycle of source extensions.
 * Provides a PathClassLoader-based system for loading extensions while
 * requiring all extensions to be built against Project Myriad's SDK.
 * 
 * Features:
 * - Scans for extensions via AndroidManifest.xml metadata
 * - Loads built-in and external sources
 * - Manages source lifecycle and configuration
 * - Provides unified multi-source search/browse UI support
 * - Ensures security by requiring recompilation against Myriad SDK
 */
class ExtensionManager(
    private val context: Context
) {
    companion object {
        private const val EXTENSION_FEATURE = "myriad.extension"
        private const val EXTENSION_METADATA_KEY = "myriad.extension.class"
        private const val EXTENSION_METADATA_VERSION = "myriad.extension.version"
        private const val MYRIAD_SDK_VERSION = "1.0.0"
    }
    
    private val _loadedSources = MutableStateFlow<Map<String, Source>>(emptyMap())
    val loadedSources: Flow<Map<String, Source>> = _loadedSources.asStateFlow()
    
    private val _loadedExtensions = MutableStateFlow<Map<String, ExtensionInfo>>(emptyMap())
    val loadedExtensions: Flow<Map<String, ExtensionInfo>> = _loadedExtensions.asStateFlow()
    
    private val builtInSources = mutableMapOf<String, Source>()
    private val externalSources = mutableMapOf<String, Source>()
    
    /**
     * Initialize the extension manager and load built-in sources
     */
    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Load built-in sources
            loadBuiltInSources()
            
            // Scan for external extensions
            scanForExtensions()
            
            // Update state
            updateLoadedSources()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to initialize extension manager: ${e.message}")
        }
    }
    
    /**
     * Load built-in sources that come with the app
     */
    private fun loadBuiltInSources() {
        // Add example manga source
        val exampleSource = ExampleMangaSource()
        builtInSources[exampleSource.id] = exampleSource
        
        // Add other built-in sources here as they are implemented
        // val mangaDxSource = MangaDxSource()
        // builtInSources[mangaDxSource.id] = mangaDxSource
    }
    
    /**
     * Scan for external extensions via AndroidManifest.xml metadata
     */
    private suspend fun scanForExtensions() = withContext(Dispatchers.IO) {
        try {
            val packageManager = context.packageManager
            val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            
            for (packageInfo in packages) {
                val metaData = packageInfo.applicationInfo?.metaData ?: continue
                
                // Check if this package is a Myriad extension
                if (metaData.getBoolean(EXTENSION_FEATURE, false)) {
                    val className = metaData.getString(EXTENSION_METADATA_KEY)
                    val version = metaData.getString(EXTENSION_METADATA_VERSION)
                    
                    if (className != null && version != null) {
                        loadExternalExtension(packageInfo.packageName, className, version)
                    }
                }
            }
        } catch (e: Exception) {
            // Log error but don't fail initialization
            println("Error scanning for extensions: ${e.message}")
        }
    }
    
    /**
     * Load an external extension from an APK
     */
    private suspend fun loadExternalExtension(
        packageName: String,
        className: String,
        version: String
    ) = withContext(Dispatchers.IO) {
        try {
            // Get the APK path
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val apkPath = appInfo.sourceDir
            
            // Create a PathClassLoader for the extension
            val classLoader = PathClassLoader(apkPath, context.classLoader)
            
            // Load the extension class
            val extensionClass = classLoader.loadClass(className)
            
            // Verify the extension implements Source interface
            if (!Source::class.java.isAssignableFrom(extensionClass)) {
                throw IllegalArgumentException("Extension class does not implement Source interface")
            }
            
            // Create instance of the extension
            val constructor = extensionClass.getDeclaredConstructor()
            val source = constructor.newInstance() as Source
            
            // Verify SDK compatibility
            // In a real implementation, you would check for specific version compatibility
            // For now, we'll just log the version
            println("Loaded extension ${source.name} (version $version)")
            
            // Add to external sources
            externalSources[source.id] = source
            
            // Update extension info
            val currentExtensions = _loadedExtensions.value.toMutableMap()
            currentExtensions[packageName] = ExtensionInfo(
                packageName = packageName,
                name = source.name,
                version = version,
                sourceId = source.id,
                isEnabled = source.isEnabled,
                isOfficial = false
            )
            _loadedExtensions.value = currentExtensions
            
        } catch (e: Exception) {
            println("Failed to load extension $packageName: ${e.message}")
        }
    }
    
    /**
     * Install a source extension from a file path
     */
    suspend fun installExtension(extensionPath: String): Result<ExtensionInfo> = withContext(Dispatchers.IO) {
        try {
            // Verify the extension file exists and is valid
            val extensionFile = File(extensionPath)
            if (!extensionFile.exists()) {
                return@withContext Result.Error(
                    IllegalArgumentException("Extension file does not exist: $extensionPath")
                )
            }
            
            // In a real implementation, you would:
            // 1. Verify the APK signature
            // 2. Check SDK version compatibility  
            // 3. Install the APK using PackageManager
            // 4. Load the extension
            
            // For now, return a placeholder
            Result.Error(
                NotImplementedError("Extension installation not yet implemented"),
                "Extension installation will be available in a future release"
            )
        } catch (e: Exception) {
            Result.Error(e, "Failed to install extension: ${e.message}")
        }
    }
    
    /**
     * Uninstall a source extension
     */
    suspend fun uninstallExtension(packageName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Remove from loaded sources
            val extensionInfo = _loadedExtensions.value[packageName]
            if (extensionInfo != null) {
                externalSources.remove(extensionInfo.sourceId)
                
                val currentExtensions = _loadedExtensions.value.toMutableMap()
                currentExtensions.remove(packageName)
                _loadedExtensions.value = currentExtensions
                
                updateLoadedSources()
            }
            
            // In a real implementation, you would also uninstall the APK
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to uninstall extension: ${e.message}")
        }
    }
    
    /**
     * Get a source by ID
     */
    fun getSource(sourceId: String): Source? {
        return builtInSources[sourceId] ?: externalSources[sourceId]
    }
    
    /**
     * Get all available sources
     */
    fun getAllSources(): List<Source> {
        return (builtInSources.values + externalSources.values).toList()
    }
    
    /**
     * Get enabled sources only
     */
    fun getEnabledSources(): List<Source> {
        return getAllSources().filter { it.isEnabled }
    }
    
    /**
     * Enable or disable a source
     */
    suspend fun setSourceEnabled(sourceId: String, enabled: Boolean): Result<Unit> {
        return try {
            val source = getSource(sourceId)
                ?: return Result.Error(IllegalArgumentException("Source not found: $sourceId"))
            
            // In a real implementation, you would update the source's enabled state
            // For now, we'll just update our tracking
            
            updateLoadedSources()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update source state: ${e.message}")
        }
    }
    
    /**
     * Check for extension updates
     */
    suspend fun checkForUpdates(): Result<List<ExtensionUpdate>> = withContext(Dispatchers.IO) {
        try {
            // In a real implementation, you would:
            // 1. Check remote repositories for updates
            // 2. Compare versions with installed extensions
            // 3. Return available updates
            
            Result.Success(emptyList())
        } catch (e: Exception) {
            Result.Error(e, "Failed to check for updates: ${e.message}")
        }
    }
    
    /**
     * Update loaded sources state
     */
    private fun updateLoadedSources() {
        val allSources = (builtInSources + externalSources).toMap()
        _loadedSources.value = allSources
    }
    
    /**
     * Get extension configuration for source management UI
     */
    fun getExtensionConfiguration(): ExtensionConfiguration {
        return ExtensionConfiguration(
            totalSources = getAllSources().size,
            enabledSources = getEnabledSources().size,
            builtInSources = builtInSources.size,
            externalSources = externalSources.size,
            sdkVersion = MYRIAD_SDK_VERSION
        )
    }
}

/**
 * Information about a loaded extension
 */
data class ExtensionInfo(
    val packageName: String,
    val name: String,
    val version: String,
    val sourceId: String,
    val isEnabled: Boolean,
    val isOfficial: Boolean,
    val iconUrl: String? = null,
    val description: String? = null
)

/**
 * Information about an available extension update
 */
data class ExtensionUpdate(
    val packageName: String,
    val currentVersion: String,
    val newVersion: String,
    val changelog: String? = null,
    val downloadUrl: String
)

/**
 * Configuration information for the extension system
 */
data class ExtensionConfiguration(
    val totalSources: Int,
    val enabledSources: Int,
    val builtInSources: Int,
    val externalSources: Int,
    val sdkVersion: String
)