package com.jervs.saferhouse.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "safety_logs")
data class SafetyLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventType: String, // e.g., "FALL", "EMERGENCY_BUTTON", "CHECK_IN"
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // e.g., "RESOLVED", "ALERT_SENT"
    val description: String? = null
)
