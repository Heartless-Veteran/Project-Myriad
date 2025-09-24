package com.heartlessveteran.myriad.core.domain.repository

import com.heartlessveteran.myriad.core.domain.entities.ReaderSettings
import com.heartlessveteran.myriad.core.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for reader settings operations.
 * Handles persistence and retrieval of reader configuration.
 * Follows Clean Architecture principles.
 */
interface ReaderSettingsRepository {

    /**
     * Observes reader settings changes
     * @return Flow of reader settings
     */
    fun getReaderSettings(): Flow<ReaderSettings>

    /**
     * Gets reader settings synchronously
     * @return Result containing reader settings or error
     */
    suspend fun getReaderSettingsSync(): Result<ReaderSettings>

    /**
     * Saves reader settings
     * @param settings Reader settings to save
     * @return Result indicating success or error
     */
    suspend fun saveReaderSettings(settings: ReaderSettings): Result<Unit>

    /**
     * Updates reader settings
     * @param settings Reader settings to update
     * @return Result indicating success or error
     */
    suspend fun updateReaderSettings(settings: ReaderSettings): Result<Unit>

    /**
     * Resets reader settings to defaults
     * @return Result indicating success or error
     */
    suspend fun resetReaderSettings(): Result<Unit>
}