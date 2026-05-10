package com.example.saferhouseui.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "safety_checks")
data class SafetyCheck(
    @PrimaryKey val userId: String,
    val lastCheckTime: Long,
    val isPending: Boolean = false,
    val checkIntervalMinutes: Int = 1440 // 24 hours
)

@Dao
interface SafetyCheckDao {
    @Query("SELECT * FROM safety_checks WHERE userId = :userId")
    fun getSafetyCheck(userId: String): Flow<SafetyCheck?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSafetyCheck(check: SafetyCheck)
}
