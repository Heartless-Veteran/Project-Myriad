package com.heartlessveteran.myriad.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.heartlessveteran.myriad.core.domain.entities.ReaderSettings
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ReaderSettings entities.
 * Provides database operations for reader configuration data.
 */
@Dao
interface ReaderSettingsDao {

    /**
     * Gets reader settings as a Flow for reactive updates
     * @return Flow of reader settings
     */
    @Query("SELECT * FROM reader_settings WHERE id = 'default' LIMIT 1")
    fun getReaderSettings(): Flow<ReaderSettings?>

    /**
     * Gets reader settings as a suspend function
     * @return Reader settings or null
     */
    @Query("SELECT * FROM reader_settings WHERE id = 'default' LIMIT 1")
    suspend fun getReaderSettingsSync(): ReaderSettings?

    /**
     * Inserts or updates reader settings
     * @param settings Reader settings to save
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReaderSettings(settings: ReaderSettings)

    /**
     * Updates reader settings
     * @param settings Reader settings to update
     */
    @Update
    suspend fun updateReaderSettings(settings: ReaderSettings)

    /**
     * Resets reader settings to defaults
     */
    @Query("DELETE FROM reader_settings WHERE id = 'default'")
    suspend fun resetReaderSettings()
}