package com.example.saferhouseui.data.local

import androidx.room.*
import com.example.saferhouseui.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE userId = :userId")
    fun getSettingsByUserId(userId: String): Flow<UserSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettings)

    @Update
    suspend fun updateSettings(settings: UserSettings)
}
