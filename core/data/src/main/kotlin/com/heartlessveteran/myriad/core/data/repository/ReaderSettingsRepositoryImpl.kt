package com.heartlessveteran.myriad.core.data.repository

import com.heartlessveteran.myriad.core.data.database.ReaderSettingsDao
import com.heartlessveteran.myriad.core.domain.entities.ReaderSettings
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.repository.ReaderSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ReaderSettingsRepository interface.
 * Handles data operations for reader settings using Room database.
 * Follows Clean Architecture by implementing domain repository interface.
 */
@Singleton
class ReaderSettingsRepositoryImpl @Inject constructor(
    private val readerSettingsDao: ReaderSettingsDao
) : ReaderSettingsRepository {

    override fun getReaderSettings(): Flow<ReaderSettings> {
        return readerSettingsDao.getReaderSettings().map { settings ->
            // Return default settings if none exist
            settings ?: ReaderSettings()
        }
    }

    override suspend fun getReaderSettingsSync(): Result<ReaderSettings> {
        return try {
            val settings = readerSettingsDao.getReaderSettingsSync() ?: ReaderSettings()
            Result.Success(settings)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get reader settings: ${e.message}")
        }
    }

    override suspend fun saveReaderSettings(settings: ReaderSettings): Result<Unit> {
        return try {
            readerSettingsDao.insertReaderSettings(settings)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to save reader settings: ${e.message}")
        }
    }

    override suspend fun updateReaderSettings(settings: ReaderSettings): Result<Unit> {
        return try {
            readerSettingsDao.updateReaderSettings(settings)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update reader settings: ${e.message}")
        }
    }

    override suspend fun resetReaderSettings(): Result<Unit> {
        return try {
            readerSettingsDao.resetReaderSettings()
            // Insert default settings
            readerSettingsDao.insertReaderSettings(ReaderSettings())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to reset reader settings: ${e.message}")
        }
    }
}