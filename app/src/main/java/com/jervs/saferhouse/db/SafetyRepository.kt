package com.jervs.saferhouse.db

import com.jervs.saferhouse.model.EmergencyContact
import com.jervs.saferhouse.model.SafetyLog
import kotlinx.coroutines.flow.Flow

class SafetyRepository(private val contactDao: ContactDao, private val logDao: LogDao) {

    val allContacts: Flow<List<EmergencyContact>> = contactDao.getAllContacts()
    val allLogs: Flow<List<SafetyLog>> = logDao.getAllLogs()

    suspend fun insertContact(contact: EmergencyContact) {
        contactDao.insertContact(contact)
    }

    suspend fun insertLog(log: SafetyLog) {
        logDao.insertLog(log)
    }

    // Remote Sync Placeholder
    suspend fun syncWithRemote() {
        // TODO: Implement Supabase or Firebase sync logic here
    }
}
