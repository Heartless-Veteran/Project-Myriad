package com.heartlessveteran.myriad.data.file

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

/**
 * File Import/Export System - Phase 1 Implementation
 * Handles .cbz/.cbr manga files and .mp4/.mkv/.avi anime files
 */
interface FileManager {
    suspend fun importMangaFile(inputStream: InputStream, fileName: String): Result<String>
    suspend fun importAnimeFile(inputStream: InputStream, fileName: String): Result<String>
    suspend fun exportFile(localPath: String, outputPath: String): Result<Unit>
    fun getSupportedMangaExtensions(): List<String>
    fun getSupportedAnimeExtensions(): List<String>
    fun isFileSupported(fileName: String): Boolean
}

class FileManagerImpl(
    private val context: Context
) : FileManager {
    
    companion object {
        private val MANGA_EXTENSIONS = listOf("cbz", "cbr", "zip", "rar")
        private val ANIME_EXTENSIONS = listOf("mp4", "mkv", "avi", "webm")
    }
    
    private val mangaDirectory = File(context.filesDir, "manga")
    private val animeDirectory = File(context.filesDir, "anime")
    
    init {
        // Create directories if they don't exist
        mangaDirectory.mkdirs()
        animeDirectory.mkdirs()
    }
    
    override suspend fun importMangaFile(
        inputStream: InputStream, 
        fileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val extension = fileName.substringAfterLast('.', "").lowercase()
            if (extension !in MANGA_EXTENSIONS) {
                return@withContext Result.failure(
                    IllegalArgumentException("Unsupported manga file format: $extension")
                )
            }
            
            val targetFile = File(mangaDirectory, fileName)
            targetFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            
            Result.success(targetFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun importAnimeFile(
        inputStream: InputStream, 
        fileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val extension = fileName.substringAfterLast('.', "").lowercase()
            if (extension !in ANIME_EXTENSIONS) {
                return@withContext Result.failure(
                    IllegalArgumentException("Unsupported anime file format: $extension")
                )
            }
            
            val targetFile = File(animeDirectory, fileName)
            targetFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            
            Result.success(targetFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun exportFile(
        localPath: String, 
        outputPath: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(localPath)
            val targetFile = File(outputPath)
            
            if (!sourceFile.exists()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Source file does not exist: $localPath")
                )
            }
            
            targetFile.parentFile?.mkdirs()
            sourceFile.copyTo(targetFile, overwrite = true)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getSupportedMangaExtensions(): List<String> = MANGA_EXTENSIONS
    
    override fun getSupportedAnimeExtensions(): List<String> = ANIME_EXTENSIONS
    
    override fun isFileSupported(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in MANGA_EXTENSIONS || extension in ANIME_EXTENSIONS
    }
    
    fun getMangaFiles(): List<File> {
        return mangaDirectory.listFiles()?.toList() ?: emptyList()
    }
    
    fun getAnimeFiles(): List<File> {
        return animeDirectory.listFiles()?.toList() ?: emptyList()
    }
}