package com.jervs.saferhouse.db

import com.jervs.saferhouse.model.EmergencyContact
import com.jervs.saferhouse.model.SafetyLog
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SafetyRepository(private val contactDao: ContactDao, private val logDao: LogDao) {

    val allContacts: Flow<List<EmergencyContact>> = contactDao.getAllContacts()
    val allLogs: Flow<List<SafetyLog>> = logDao.getAllLogs()

    suspend fun insertContact(contact: EmergencyContact) {
        contactDao.insertContact(contact)
        syncContactWithRemote(contact)
    }

    suspend fun insertLog(log: SafetyLog) {
        logDao.insertLog(log)
        syncLogWithRemote(log)
    }

    suspend fun fetchContactsFromRemote() {
        try {
            val remoteContacts = SupabaseManager.client.postgrest["emergency_contacts"]
                .select().decodeList<EmergencyContact>()
            
            remoteContacts.forEach { contact ->
                contactDao.insertContact(contact)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun syncContactWithRemote(contact: EmergencyContact) {
        try {
            SupabaseManager.client.postgrest["emergency_contacts"].upsert(contact)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun syncLogWithRemote(log: SafetyLog) {
        try {
            SupabaseManager.client.postgrest["safety_logs"].upsert(log)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
