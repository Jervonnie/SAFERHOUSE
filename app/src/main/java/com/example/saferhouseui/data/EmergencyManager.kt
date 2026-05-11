package com.example.saferhouseui.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.saferhouseui.data.model.EmergencyContact
import com.example.saferhouseui.data.model.User
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

class EmergencyManager(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun executeEscalation(user: User?, contacts: List<EmergencyContact>) {
        val elderName = user?.fullName ?: "User"
        
        // 1. Get Coordinates
        val locationLink = try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                
                val cts = CancellationTokenSource()
                val location = try {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
                        ?: fusedLocationClient.lastLocation.await()
                } catch (e: Exception) {
                    Log.e("EmergencyManager", "Location task failed: ${e.message}")
                    null
                }

                if (location != null) {
                    "Coords: ${location.latitude}, ${location.longitude}"
                } else {
                    ""
                }
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e("EmergencyManager", "Error getting location: ${e.message}")
            ""
        }

        // Extremely simple message without links to bypass strict carrier/spam filters
        val message = "SaferHouse: $elderName needs help! $locationLink".trim()

        // 2. Local Alarm (Siren)
        triggerLocalAlarm()

        // 3. SMS Escalation
        val sortedContacts = contacts.sortedBy { it.priority }
        val primaryContacts = sortedContacts.filter { !it.isBarangay }
        
        val smsManager: SmsManager? = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
        } catch (e: Exception) {
            Log.e("EmergencyManager", "Failed to get SmsManager: ${e.message}")
            null
        }

        if (smsManager != null) {
            // First pass: Family/Caregivers
            primaryContacts.forEach { contact ->
                sendSms(smsManager, contact.phoneNumber, message)
            }
            
            // Also send to the direct caregiver number if it's set in user profile
            user?.caregiverPhoneNumber?.let { cgPhone ->
                if (cgPhone.isNotBlank() && primaryContacts.none { it.phoneNumber == cgPhone }) {
                    sendSms(smsManager, cgPhone, message)
                }
            }

            // 5. Barangay Escalation
            val barangayContacts = sortedContacts.filter { it.isBarangay }
            barangayContacts.forEach { contact ->
                sendSms(smsManager, contact.phoneNumber, "ESCALATED: $message")
            }
        } else {
            Log.e("EmergencyManager", "SmsManager is null, cannot send SMS")
        }

        // 4. Voice Call to primary
        if (primaryContacts.isNotEmpty()) {
            initiateCall(primaryContacts[0].phoneNumber)
        } else if (!user?.caregiverPhoneNumber.isNullOrEmpty()) {
            initiateCall(user?.caregiverPhoneNumber!!)
        }
    }

    private fun sendSms(smsManager: SmsManager, phoneNumber: String, message: String) {
        if (phoneNumber.isBlank()) return
        
        // Clean phone number: remove spaces, dashes, parentheses
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                val parts = smsManager.divideMessage(message)
                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(cleanNumber, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(cleanNumber, null, message, null, null)
                }
                Log.d("EmergencyManager", "SMS triggered to $cleanNumber: $message")
            } else {
                Log.e("EmergencyManager", "SEND_SMS permission missing")
            }
        } catch (e: Exception) {
            Log.e("EmergencyManager", "Failed to send SMS to $cleanNumber: ${e.message}")
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
