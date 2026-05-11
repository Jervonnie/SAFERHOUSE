package com.example.saferhouseui.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "pairing_codes")
@Serializable
data class PairingCode(
    @PrimaryKey
    val code: String, // The 6-digit random code
    val elderId: String, // References users.id
    val expiresAt: Long // Timestamp (now + 15 mins)
)
