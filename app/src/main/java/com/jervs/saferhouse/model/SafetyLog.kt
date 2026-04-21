package com.jervs.saferhouse.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "safety_logs")
data class SafetyLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String,
    val description: String? = null
)
