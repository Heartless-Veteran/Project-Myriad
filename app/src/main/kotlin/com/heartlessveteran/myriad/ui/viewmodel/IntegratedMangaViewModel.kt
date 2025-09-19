package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.domain.services.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Integration ViewModel demonstrating how all the major services work together.
 *
 * This ViewModel provides a complete workflow example:
 * - File import from local archives
 * - Online content discovery and search
 * - Download management for offline reading
 * - Integration with the enhanced reader
 */
class IntegratedMangaViewModel(
    private val fileManagerService: FileManagerService,
    private val sourceService: SourceService,
    private val downloadService: DownloadService,
) : ViewModel() {
    // UI State management
    private val _uiState = MutableStateFlow(IntegratedMangaUiState())
    val uiState: StateFlow<IntegratedMangaUiState> = _uiState.asStateFlow()

    // Download queue observation
    val downloadQueue: Flow<List<DownloadTask>> = downloadService.getDownloadQueue()
    val overallProgress: Flow<Float> = downloadService.getOverallProgress()
    val areDownloadsActive: Flow<Boolean> = downloadService.areDownloadsActive()

    init {
        loadAvailableSources()
    }

    /**
     * Import manga from a local file (.cbz/.cbr).
     */
    fun importMangaFromFile(filePath: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = fileManagerService.importMangaFromFile(filePath)) {
                is Result.Success -> {
                    val manga = result.data
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            recentlyImported = _uiState.value.recentlyImported + manga,
                            successMessage = "Successfully imported: ${manga.title}",
                        )
                }
                is Result.Error -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Import failed: ${result.message}",
                        )
                }
                is Result.Loading -> {
                    // Already showing loading
                }
            }
        }
    }

    /**
     * Scan a directory for manga files and import them.
     */
    fun scanDirectoryForManga(directoryPath: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = fileManagerService.scanDirectoryForManga(directoryPath)) {
                is Result.Success -> {
                    val mangaList = result.data
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            recentlyImported = _uiState.value.recentlyImported + mangaList,
                            successMessage = "Successfully imported ${mangaList.size} manga",
                        )
                }
                is Result.Error -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Directory scan failed: ${result.message}",
                        )
                }
                is Result.Loading -> {
                    // Already showing loading
                }
            }
        }
    }

    /**
     * Search for manga across all enabled sources.
     */
    fun searchMangaOnline(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, searchResults = emptyList())

            sourceService
                .searchMangaAcrossSources(query)
                .collect { searchResult ->
                    val currentResults = _uiState.value.searchResults.toMutableList()
                    val existingIndex = currentResults.indexOfFirst { it.sourceId == searchResult.sourceId }

                    if (existingIndex >= 0) {
                        currentResults[existingIndex] = searchResult
                    } else {
                        currentResults.add(searchResult)
                    }

                    _uiState.value =
                        _uiState.value.copy(
                            searchResults = currentResults,
                            isSearching = currentResults.any { it.results.isEmpty() && it.error == null },
                        )
                }
        }
    }

    /**
     * Get latest manga from a specific source.
     */
    fun getLatestManga(sourceId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = sourceService.getLatestManga(sourceId)) {
                is Result.Success -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            latestManga = result.data,
                        )
                }
                is Result.Error -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to load latest manga: ${result.message}",
                        )
                }
                is Result.Loading -> {
                    // Already showing loading
                }
            }
        }
    }

    /**
     * Download manga for offline reading.
     */
    fun downloadManga(
        manga: Manga,
        chapterIds: List<String>? = null,
    ) {
        viewModelScope.launch {
            when (val result = downloadService.enqueueMangaDownload(manga, chapterIds)) {
                is Result.Success -> {
                    _uiState.value =
                        _uiState.value.copy(
                            successMessage = "Download queued: ${manga.title}",
                        )
                }
                is Result.Error -> {
                    _uiState.value =
                        _uiState.value.copy(
                            errorMessage = "Failed to queue download: ${result.message}",
                        )
                }
                is Result.Loading -> {
                    // Downloads are queued asynchronously
                }
            }
        }
    }

    /**
     * Pause a download.
     */
    fun pauseDownload(taskId: String) {
        viewModelScope.launch {
            downloadService.pauseDownload(taskId)
        }
    }

    /**
     * Resume a download.
     */
    fun resumeDownload(taskId: String) {
        viewModelScope.launch {
            downloadService.resumeDownload(taskId)
        }
    }

    /**
     * Cancel a download.
     */
    fun cancelDownload(taskId: String) {
        viewModelScope.launch {
            downloadService.cancelDownload(taskId)
        }
    }

    /**
     * Retry a failed download.
     */
    fun retryDownload(taskId: String) {
        viewModelScope.launch {
            downloadService.retryDownload(taskId)
        }
    }

    /**
     * Clear completed downloads.
     */
    fun clearCompletedDownloads() {
        viewModelScope.launch {
            downloadService.clearCompletedDownloads()
        }
    }

    /**
     * Enable or disable a content source.
     */
    fun setSourceEnabled(
        sourceId: String,
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            sourceService.setSourceEnabled(sourceId, enabled)
            loadAvailableSources() // Refresh sources
        }
    }

    /**
     * Clear messages (success/error).
     */
    fun clearMessages() {
        _uiState.value =
            _uiState.value.copy(
                successMessage = null,
                errorMessage = null,
            )
    }

    private fun loadAvailableSources() {
        _uiState.value =
            _uiState.value.copy(
                availableSources = sourceService.getAvailableSources(),
                enabledSources = sourceService.getEnabledSources(),
            )
    }
}

/**
 * UI State for the integrated manga management screen.
 */
data class IntegratedMangaUiState(
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val recentlyImported: List<Manga> = emptyList(),
    val latestManga: List<Manga> = emptyList(),
    val searchResults: List<SourceSearchResult> = emptyList(),
    val availableSources: List<ContentSource> = emptyList(),
    val enabledSources: List<ContentSource> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null,
)

/**
 * Example usage and integration patterns for the services.
 */
object IntegrationExamples {
    /**
     * Complete workflow: Import local files, search online, and manage downloads.
     */
    suspend fun completeWorkflowExample(
        fileManagerService: FileManagerService,
        sourceService: SourceService,
        downloadService: DownloadService,
    ) {
        // Step 1: Import local manga files
        val localImportResult = fileManagerService.importMangaFromFile("/path/to/manga.cbz")

        if (localImportResult is Result.Success) {
            println("Imported local manga: ${localImportResult.data.title}")
        }

        // Step 2: Search for manga online
        sourceService.searchMangaAcrossSources("One Piece").collect { searchResult ->
            println("Search results from ${searchResult.sourceName}: ${searchResult.results.size} found")

            // Step 3: Download interesting manga for offline reading
            searchResult.results.take(1).forEach { manga ->
                downloadService.enqueueMangaDownload(manga)
                println("Queued download: ${manga.title}")
            }
        }

        // Step 4: Monitor download progress
        downloadService.getDownloadQueue().collect { downloadTasks ->
            val activeTasks = downloadTasks.filter { it.status == DownloadStatus.IN_PROGRESS }
            println("Active downloads: ${activeTasks.size}")

            activeTasks.forEach { task ->
                println("${task.mangaTitle}: ${(task.progress * 100).toInt()}%")
            }
        }
    }

    /**
     * Source management example.
     */
    suspend fun sourceManagementExample(sourceService: SourceService) {
        // Get all available sources
        val availableSources = sourceService.getAvailableSources()
        println("Available sources: ${availableSources.map { it.name }}")

        // Enable/disable sources
        sourceService.setSourceEnabled("mangadx", true)
        sourceService.setSourceEnabled("komikku", false)

        // Get enabled sources only
        val enabledSources = sourceService.getEnabledSources()
        println("Enabled sources: ${enabledSources.map { it.name }}")

        // Search across enabled sources
        sourceService.searchMangaAcrossSources("Attack on Titan").collect { result ->
            if (result.error == null) {
                println("Found ${result.results.size} results from ${result.sourceName}")
            } else {
                println("Search failed on ${result.sourceName}: ${result.error}")
            }
        }
    }

    /**
     * File management example with error handling.
     */
    suspend fun fileManagementExample(fileManagerService: FileManagerService) {
        // Import single file
        when (val result = fileManagerService.importMangaFromFile("/path/to/manga.cbz")) {
            is Result.Success -> {
                val manga = result.data
                println("Imported: ${manga.title} (${manga.totalChapters} chapters)")
            }
            is Result.Error -> {
                println("Import failed: ${result.message}")
            }
            is Result.Loading -> {
                println("Import in progress...")
            }
        }

        // Scan directory for multiple files
        when (val result = fileManagerService.scanDirectoryForManga("/path/to/manga/directory")) {
            is Result.Success -> {
                println("Imported ${result.data.size} manga from directory")
                result.data.forEach { manga ->
                    println("- ${manga.title}")
                }
            }
            is Result.Error -> {
                println("Directory scan failed: ${result.message}")
            }
            is Result.Loading -> {
                println("Scanning directory...")
            }
        }

        // Validate files before processing
        val isSupported = fileManagerService.isSupportedMangaFile("/path/to/file.cbz")
        if (isSupported) {
            println("File format is supported")
        }

        // Clean up temporary files
        fileManagerService.cleanupTemporaryFiles()
        println("Cleaned up temporary files")
    }
}
