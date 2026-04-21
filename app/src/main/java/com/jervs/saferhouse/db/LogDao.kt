package com.jervs.saferhouse.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jervs.saferhouse.model.SafetyLog
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM safety_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SafetyLog>>

    @Insert
    suspend fun insertLog(log: SafetyLog)

    @Query("DELETE FROM safety_logs")
    suspend fun deleteAllLogs()
}
