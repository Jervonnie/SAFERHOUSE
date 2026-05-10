package com.example.saferhouseui.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "user_settings")
@Serializable
data class UserSettings(
    @PrimaryKey
    val userId: String, // References users.id
    val language: String = "English",
    val fontSize: String = "Normal",
    val fallSensitivity: Float = 0.5f,
    val isAutoSmsEnabled: Boolean = true
)
