package com.jervs.saferhouse.db

import androidx.room.*
import com.jervs.saferhouse.model.EmergencyContact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY priority ASC")
    fun getAllContacts(): Flow<List<EmergencyContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContact)

    @Delete
    suspend fun deleteContact(contact: EmergencyContact)

    @Update
    suspend fun updateContact(contact: EmergencyContact)
}
