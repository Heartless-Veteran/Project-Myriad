package com.heartlessveteran.myriad.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.di.LibraryDiContainer
import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.ui.components.ImportStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for handling file import operations.
 *
 * Manages:
 * - File selection and validation
 * - Import progress tracking
 * - Error handling and user feedback
 * - Integration with FileManagerService
 */
class FileImportViewModel(
    private val context: Context,
) : ViewModel() {
    private val mangaRepository = LibraryDiContainer.getMangaRepository(context)

    private val _importStatus = MutableStateFlow(ImportStatus())
    val importStatus: StateFlow<ImportStatus> = _importStatus.asStateFlow()

    private val _importedManga = MutableStateFlow<List<Manga>>(emptyList())
    val importedManga: StateFlow<List<Manga>> = _importedManga.asStateFlow()

    companion object {
        private const val TAG = "FileImportViewModel"
    }

    /**
     * Import a single manga file from URI.
     *
     * @param uri File URI from file picker
     */
    fun importFile(uri: Uri) {
        viewModelScope.launch {
            try {
                _importStatus.value =
                    ImportStatus(
                        isLoading = true,
                        message = "Processing file...",
                    )

                val filePath = getFilePathFromUri(uri)
                if (filePath == null) {
                    _importStatus.value =
                        ImportStatus(
                            isError = true,
                            message = "Unable to access the selected file. Please try selecting a different file.",
                        )
                    return@launch
                }

                Log.i(TAG, "Importing file: $filePath")

                val result = mangaRepository.importMangaFromFile(filePath)

                when (result) {
                    is Result.Success -> {
                        val manga = result.data
                        _importedManga.value = _importedManga.value + manga
                        _importStatus.value =
                            ImportStatus(
                                isSuccess = true,
                                message = "Successfully imported \"${manga.title}\"",
                            )
                        Log.i(TAG, "Successfully imported manga: ${manga.title}")
                    }
                    is Result.Error -> {
                        _importStatus.value =
                            ImportStatus(
                                isError = true,
                                message = result.message ?: "Failed to import manga",
                            )
                        Log.e(TAG, "Failed to import file", result.exception)
                    }
                    is Result.Loading -> {
                        _importStatus.value =
                            ImportStatus(
                                isLoading = true,
                                message = "Processing file...",
                            )
                    }
                }
            } catch (e: Exception) {
                _importStatus.value =
                    ImportStatus(
                        isError = true,
                        message = "An unexpected error occurred: ${e.message}",
                    )
                Log.e(TAG, "Error importing file", e)
            }
        }
    }

    /**
     * Import all manga files from a directory.
     *
     * @param uri Directory URI from directory picker
     */
    fun importDirectory(uri: Uri) {
        viewModelScope.launch {
            try {
                _importStatus.value =
                    ImportStatus(
                        isLoading = true,
                        message = "Scanning directory for manga files...",
                    )

                val directoryPath = getFilePathFromUri(uri)
                if (directoryPath == null) {
                    _importStatus.value =
                        ImportStatus(
                            isError = true,
                            message =
                                "Unable to access the selected directory. " +
                                    "Please try selecting a different directory.",
                        )
                    return@launch
                }

                Log.i(TAG, "Importing directory: $directoryPath")

                _importStatus.value =
                    ImportStatus(
                        isLoading = true,
                        message = "Importing manga files...",
                    )

                val result = mangaRepository.scanLocalMangaDirectory(directoryPath)

                when (result) {
                    is Result.Success -> {
                        val mangaList = result.data
                        _importedManga.value = _importedManga.value + mangaList

                        val message =
                            when (mangaList.size) {
                                0 -> "No manga files found in the selected directory"
                                1 -> "Successfully imported 1 manga"
                                else -> "Successfully imported ${mangaList.size} manga"
                            }

                        _importStatus.value =
                            ImportStatus(
                                isSuccess = true,
                                message = message,
                            )

                        Log.i(TAG, "Successfully imported ${mangaList.size} manga from directory")
                    }
                    is Result.Error -> {
                        _importStatus.value =
                            ImportStatus(
                                isError = true,
                                message = result.message ?: "Failed to import manga from directory",
                            )
                        Log.e(TAG, "Failed to import directory", result.exception)
                    }
                    is Result.Loading -> {
                        _importStatus.value =
                            ImportStatus(
                                isLoading = true,
                                message = "Scanning directory...",
                            )
                    }
                }
            } catch (e: Exception) {
                _importStatus.value =
                    ImportStatus(
                        isError = true,
                        message = "An unexpected error occurred: ${e.message}",
                    )
                Log.e(TAG, "Error importing directory", e)
            }
        }
    }

    /**
     * Clear the import status.
     */
    fun clearImportStatus() {
        _importStatus.value = ImportStatus()
    }

    /**
     * Clear imported manga list.
     */
    fun clearImportedManga() {
        _importedManga.value = emptyList()
    }

    /**
     * Get file path from URI using DocumentFile.
     * This handles both regular files and files from document providers.
     */
    private suspend fun getFilePathFromUri(uri: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                // For content URIs, we need to copy the file to cache first
                // or use DocumentFile for direct access
                val documentFile = DocumentFile.fromSingleUri(context, uri)

                if (documentFile == null || !documentFile.exists()) {
                    Log.w(TAG, "DocumentFile not found or doesn't exist: $uri")
                    return@withContext null
                }

                // For simplicity, we'll copy the file to cache and return that path
                // In a production app, you might want to work directly with the URI
                val cacheFile = copyUriToCache(uri, documentFile.name ?: "imported_manga")
                cacheFile?.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "Error getting file path from URI: $uri", e)
                null
            }
        }

    /**
     * Copy file from URI to app cache directory.
     */
    private suspend fun copyUriToCache(
        uri: Uri,
        fileName: String,
    ): java.io.File? =
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Log.w(TAG, "Could not open input stream for URI: $uri")
                    return@withContext null
                }

                val cacheDir = java.io.File(context.cacheDir, "import_temp")
                cacheDir.mkdirs()

                val cacheFile = java.io.File(cacheDir, fileName)

                inputStream.use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                Log.d(TAG, "Copied file to cache: ${cacheFile.absolutePath}")
                cacheFile
            } catch (e: Exception) {
                Log.e(TAG, "Error copying URI to cache: $uri", e)
                null
            }
        }
}
