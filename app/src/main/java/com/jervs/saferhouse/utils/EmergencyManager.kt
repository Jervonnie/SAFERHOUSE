package com.jervs.saferhouse.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import com.jervs.saferhouse.db.AppDatabase
import com.jervs.saferhouse.model.SafetyLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EmergencyManager(private val context: Context) {

    private val repository by lazy {
        val db = AppDatabase.getDatabase(context)
        com.jervs.saferhouse.db.SafetyRepository(db.contactDao(), db.logDao())
    }

    private val locationHelper = LocationHelper(context)

    fun triggerEmergency() {
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Get Location
            locationHelper.getLastKnownLocation { location ->
                val locationMsg = if (location != null) {
                    "My location: https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
                } else {
                    "Location unavailable."
                }

                val fullMessage = "EMERGENCY! I need help. $locationMsg"

                // 2. Fetch Contacts from Room
                launch {
                    val contacts = repository.allContacts.first()
                    if (contacts.isEmpty()) {
                        Log.e("EmergencyManager", "No emergency contacts found!")
                        return@launch
                    }

                    // 3. Send SMS to all contacts
                    val smsManager = context.getSystemService(SmsManager::class.java)
                    contacts.forEach { contact ->
                        try {
                            smsManager.sendTextMessage(contact.phoneNumber, null, fullMessage, null, null)
                            Log.d("EmergencyManager", "SMS sent to ${contact.name}")
                        } catch (e: Exception) {
                            Log.e("EmergencyManager", "Failed to send SMS to ${contact.name}: ${e.message}")
                        }
                    }

                    // 4. Call the primary contact (priority 1)
                    val primaryContact = contacts.find { it.priority == 1 } ?: contacts[0]
                    makeCall(primaryContact.phoneNumber)

                    // 5. Save to Logs
                    repository.insertLog(SafetyLog(
                        eventType = "EMERGENCY_TRIGGERED",
                        status = "COMPLETED",
                        description = "Alert sent to ${contacts.size} contacts. Call initiated to ${primaryContact.name}."
                    ))
                }
            }
        }
    }

    private fun makeCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (context.checkSelfPermission(android.Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            context.startActivity(intent)
        }
    }
}
