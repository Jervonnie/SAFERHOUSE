package com.jervs.saferhouse.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val relation: String, // e.g., Family, Barangay, Doctor
    val priority: Int // 1 for primary, 2 for secondary, etc.
)
