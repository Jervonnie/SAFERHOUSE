package com.example.saferhouseui.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(tableName = "relationships")
@Serializable
data class Relationship(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val elderlyId: String, // References users.id
    val caregiverId: String, // References users.id
    val createdAt: Long = System.currentTimeMillis()
)
