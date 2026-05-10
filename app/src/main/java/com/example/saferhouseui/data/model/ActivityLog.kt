package com.example.saferhouseui.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(tableName = "activity_logs")
@Serializable
data class ActivityLog(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String, // References users.id
    val type: String, // "FALL", "SOS", "NORMAL", "BATTERY_LOW"
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val batteryLevel: Int? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false // [SYNC VARIABLE] for Room -> Supabase
)
