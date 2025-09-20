package com.heartlessveteran.myriad.di

import android.content.Context
import com.heartlessveteran.myriad.data.database.MyriadDatabase
import com.heartlessveteran.myriad.data.repository.MangaRepositoryImpl
import com.heartlessveteran.myriad.data.services.FileManagerServiceImpl
import com.heartlessveteran.myriad.domain.repository.MangaRepository
import com.heartlessveteran.myriad.domain.services.FileManagerService

/**
 * Manual dependency injection container for Library/Core features.
 * Temporary solution until Hilt/KSP is fully enabled.
 *
 * This container provides:
 * - FileManagerService for manga import/export
 * - MangaRepository with integrated file management
 * - Database access layer
 */
object LibraryDiContainer {
    @Volatile
    private var database: MyriadDatabase? = null

    @Volatile
    private var fileManagerService: FileManagerService? = null

    @Volatile
    private var mangaRepository: MangaRepository? = null

    /**
     * Get the Room database instance.
     */
    fun getDatabase(context: Context): MyriadDatabase =
        database ?: synchronized(this) {
            database ?: DatabaseModule.provideDatabase(context).also { database = it }
        }

    /**
     * Get FileManagerService instance.
     */
    fun getFileManagerService(context: Context): FileManagerService =
        fileManagerService ?: synchronized(this) {
            fileManagerService ?: FileManagerServiceImpl(context).also { fileManagerService = it }
        }

    /**
     * Get MangaRepository instance with FileManagerService integration.
     */
    fun getMangaRepository(context: Context): MangaRepository =
        mangaRepository ?: synchronized(this) {
            mangaRepository ?: MangaRepositoryImpl(
                mangaDao = getDatabase(context).mangaDao(),
                // TODO: Add FileManagerService when available via Hilt
            ).also { mangaRepository = it }
        }

    /**
     * Clear all cached instances (useful for testing).
     */
    fun clearInstances() {
        synchronized(this) {
            database?.close()
            database = null
            fileManagerService = null
            mangaRepository = null
        }
    }
}
