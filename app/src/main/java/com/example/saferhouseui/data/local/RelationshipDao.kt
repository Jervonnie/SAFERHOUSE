package com.example.saferhouseui.data.local

import androidx.room.*
import com.example.saferhouseui.data.model.Relationship
import kotlinx.coroutines.flow.Flow

@Dao
interface RelationshipDao {
    @Query("SELECT * FROM relationships")
    fun getAllRelationships(): Flow<List<Relationship>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(relationship: Relationship)

    @Delete
    suspend fun deleteRelationship(relationship: Relationship)
}
