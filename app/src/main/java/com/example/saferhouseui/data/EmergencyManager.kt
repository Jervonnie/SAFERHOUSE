package com.example.saferhouseui.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.saferhouseui.data.model.EmergencyContact
import com.example.saferhouseui.data.model.User
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

class EmergencyManager(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun executeEscalation(user: User?, contacts: List<EmergencyContact>) {
        val elderName = user?.fullName ?: "User"
        val elderContact = user?.phoneNumber ?: "Unknown"
        
        // 1. Get Coordinates
        val locationLink = try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                if (location != null) {
                    "https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
                } else "Location unavailable"
            } else "Permission denied"
        } catch (e: Exception) {
            Log.e("EmergencyManager", "Error getting location: ${e.message}")
            "Error retrieving location"
        }

        val message = "EMERGENCY ALERT: $elderName (Ph: $elderContact) needs help!\nMaps: $locationLink"

        // 2. Local Alarm (Siren)
        triggerLocalAlarm()

        // 3. SMS Escalation
        val sortedContacts = contacts.sortedBy { it.priority }
        val smsManager = context.getSystemService(SmsManager::class.java)

        // First pass: Family/Caregivers
        val primaryContacts = sortedContacts.filter { !it.isBarangay }
        primaryContacts.forEach { contact ->
            sendSms(smsManager, contact.phoneNumber, message)
        }

        // 4. Voice Call to primary
        if (primaryContacts.isNotEmpty()) {
            initiateCall(primaryContacts[0].phoneNumber)
        }

        // 5. Barangay Escalation
        val barangayContacts = sortedContacts.filter { it.isBarangay }
        barangayContacts.forEach { contact ->
            sendSms(smsManager, contact.phoneNumber, "[ESCALATED] $message")
        }
    }

    private fun sendSms(smsManager: SmsManager, phoneNumber: String, message: String) {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Log.d("EmergencyManager", "SMS sent to $phoneNumber")
            }
        } catch (e: Exception) {
            Log.e("EmergencyManager", "Failed to send SMS to $phoneNumber: ${e.message}")
        }
    }

    private fun initiateCall(phoneNumber: String) {
        try {
            val intent = if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent(Intent.ACTION_CALL).apply {
                    data = "tel:$phoneNumber".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(Intent.ACTION_DIAL).apply {
                    data = "tel:$phoneNumber".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("EmergencyManager", "Call failed: ${e.message}")
        }
    }

    fun triggerLocalAlarm() {
        try {
            val alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone = RingtoneManager.getRingtone(context, alert)
            ringtone.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone.play()
        } catch (e: Exception) {
            Log.e("EmergencyManager", "Alarm failed: ${e.message}")
        }
    }
}
