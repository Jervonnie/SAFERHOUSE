package com.example.saferhouseui.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "activity_logs")
@Suppress("unused")
data class ActivityLog(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val type: String, // "FALL", "SOS", "NORMAL"
    val timestamp: Long = System.currentTimeMillis(),
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isSynced: Boolean = false
)
