package com.example.saferhouseui.viewmodel

import android.app.Application
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class ElderlyViewModel(
    application: Application,
    private val authViewModel: AuthViewModel
) : AndroidViewModel(application) {
    
    // The final emergency state (Alert sent)
    var isEmergencyActive by mutableStateOf(false)
        private set

    // The state for the 10-second confirmation dialog
    var isConfirmationDialogOpen by mutableStateOf(false)
        private set

    var countdownValue by mutableIntStateOf(10)
        private set

    // Indicates if the audio distress feature is running in background
    var isAudioDetectionEnabled by mutableStateOf(true)
        private set

    private var countdownJob: Job? = null
    private var audioDetectionJob: Job? = null

    init {
        // startAudioDistressDetection() // DISABLED: Prevent automated SMS/Calls during dev
    }

    /**
     * Simulates the constant background listening for distress keywords.
     * Both manual button and this detection will lead to the same confirmation process.
     */
    fun startAudioDistressDetection() {
        isAudioDetectionEnabled = true
        audioDetectionJob?.cancel()
        audioDetectionJob = viewModelScope.launch {
            Log.d("ElderlyViewModel", "Audio Distress Detection started. Listening for keywords...")
            // Simulated: If a keyword is detected (e.g., after some time), it triggers the same confirmation dialog
            // We don't trigger it immediately to avoid "already ON" state on dashboard entry
            delay(60000) // Simulate detection after 1 minute for demo purposes
            Log.d("ElderlyViewModel", "Keyword Detected via Audio!")
            triggerEmergency()
        }
    }

    /**
     * Entry point for both Manual SOS and Audio Detection.
     * Triggers the 10-second confirmation dialog.
     */
    fun triggerEmergency() {
        if (!isEmergencyActive && !isConfirmationDialogOpen) {
            isConfirmationDialogOpen = true
            startCountdown()
        }
    }

    private fun startCountdown() {
        countdownValue = 10
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (countdownValue > 0) {
                delay(1000)
                countdownValue--
            }
            confirmEmergency()
        }
    }

    fun cancelEmergency() {
        isConfirmationDialogOpen = false
        countdownJob?.cancel()
        Log.d("ElderlyViewModel", "Emergency cancelled by user.")
    }

    fun confirmEmergency() {
        isConfirmationDialogOpen = false
        countdownJob?.cancel()
        isEmergencyActive = true
        sendEmergencyEscalation()
    }

    private fun sendEmergencyEscalation() {
        // In a mock setup where the Elder isn't linked yet, we find the mock caregiver
        val caregiver = authViewModel.users.find { it.role == "caregiver" }
        val destinationContact = caregiver?.contact ?: "09123456789" // Default mock caregiver contact

        val elderName = authViewModel.currentUser?.name ?: "User"
        val elderContact = authViewModel.currentUser?.contact ?: "Unknown"
        val location = authViewModel.currentUser?.address ?: "Unknown Location"

        Log.d("ElderlyViewModel", "Escalation Response: Sending Automated SMS and Phone Call to $destinationContact (Caregiver)")

        // Automated SMS with Location
        try {
            val smsManager = getApplication<Application>().getSystemService(SmsManager::class.java)
            // Use the phone number registered in the setup for identifying the elder in the message body,
            // as the SIM number used for sending might be different from the one used during registration.
            val message = "EMERGENCY ALERT: $elderName (Reg. No: $elderContact) needs help!\nLocation: $location\nView on Maps: https://www.google.com/maps/search/?api=1&query=${location.replace(" ", "+")}"
            
            // The destination for the SMS is the Caregiver's contact number, not the Elder's own number.
            smsManager.sendTextMessage(destinationContact, null, message, null, null)
        } catch (e: Exception) {
            Log.e("ElderlyViewModel", "Failed to send SMS: ${e.message}")
        }

        // Redirect to Dialer (ACTION_DIAL) for user control - Calling the Caregiver
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = "tel:$destinationContact".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            Log.e("ElderlyViewModel", "Failed to open Dialer: ${e.message}")
        }
    }

    fun toggleEmergency() {
        if (isEmergencyActive) {
            // Option to reset state if already active
            isEmergencyActive = false
        } else {
            // Manual SOS button - zero trust, immediate escalation
            confirmEmergency()
        }
    }

    fun updateProfile(name: String, address: String, contact: String) {
        authViewModel.currentUser?.let { user ->
            authViewModel.updateUser(
                user.copy(
                    name = name,
                    address = address,
                    contact = contact
                )
            )
        }
    }
}
