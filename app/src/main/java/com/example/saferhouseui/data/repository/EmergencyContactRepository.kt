package com.example.saferhouseui.data.repository

import com.example.saferhouseui.data.local.EmergencyContactDao
import com.example.saferhouseui.data.model.EmergencyContact
import kotlinx.coroutines.flow.Flow

class EmergencyContactRepository(private val emergencyContactDao: EmergencyContactDao) {
    fun getAllContacts(): Flow<List<EmergencyContact>> = emergencyContactDao.getAllContacts()

    suspend fun addContact(contact: EmergencyContact) {
        emergencyContactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: EmergencyContact) {
        emergencyContactDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: EmergencyContact) {
        emergencyContactDao.deleteContact(contact)
    }
}
