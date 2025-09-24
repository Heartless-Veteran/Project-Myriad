package com.heartlessveteran.myriad.core.domain.usecase

import com.heartlessveteran.myriad.core.domain.entities.ReaderSettings
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.repository.ReaderSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for getting reader settings.
 * Handles business logic for retrieving reader configuration.
 */
@Singleton
class GetReaderSettingsUseCase @Inject constructor(
    private val repository: ReaderSettingsRepository
) {
    /**
     * Gets reader settings as a reactive stream
     * @return Flow of reader settings
     */
    operator fun invoke(): Flow<ReaderSettings> {
        return repository.getReaderSettings()
    }
}

/**
 * Use case for saving reader settings.
 * Handles business logic for persisting reader configuration.
 */
@Singleton
class SaveReaderSettingsUseCase @Inject constructor(
    private val repository: ReaderSettingsRepository
) {
    /**
     * Saves reader settings
     * @param settings Reader settings to save
     * @return Result indicating success or error
     */
    suspend operator fun invoke(settings: ReaderSettings): Result<Unit> {
        return try {
            repository.saveReaderSettings(settings)
        } catch (e: Exception) {
            Result.Error(e, "Failed to save reader settings: ${e.message}")
        }
    }
}

/**
 * Use case for updating reader settings.
 * Handles business logic for modifying reader configuration.
 */
@Singleton
class UpdateReaderSettingsUseCase @Inject constructor(
    private val repository: ReaderSettingsRepository
) {
    /**
     * Updates reader settings
     * @param settings Reader settings to update
     * @return Result indicating success or error
     */
    suspend operator fun invoke(settings: ReaderSettings): Result<Unit> {
        return try {
            repository.updateReaderSettings(settings)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update reader settings: ${e.message}")
        }
    }
}

/**
 * Use case for resetting reader settings to defaults.
 * Handles business logic for restoring default reader configuration.
 */
@Singleton
class ResetReaderSettingsUseCase @Inject constructor(
    private val repository: ReaderSettingsRepository
) {
    /**
     * Resets reader settings to defaults
     * @return Result indicating success or error
     */
    suspend operator fun invoke(): Result<Unit> {
        return try {
            repository.resetReaderSettings()
        } catch (e: Exception) {
            Result.Error(e, "Failed to reset reader settings: ${e.message}")
        }
    }
}