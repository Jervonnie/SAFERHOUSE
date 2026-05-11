package com.example.saferhouseui.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(tableName = "emergency_contacts")
@Serializable
data class EmergencyContact(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val elderlyId: String, // References users.id
    val name: String,
    val phoneNumber: String,
    val priority: Int, // 1, 2, 3...
    val isBarangay: Boolean = false
)
