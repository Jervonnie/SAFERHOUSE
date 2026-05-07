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
        startAudioDistressDetection()
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
        val contact = authViewModel.currentUser?.contact ?: "09XXXXXXXXX"
        val name = authViewModel.currentUser?.name ?: "User"

        Log.d("ElderlyViewModel", "Escalation Response: Sending Automated SMS and Phone Call to $contact")

        // Automated SMS
        try {
            val smsManager = getApplication<Application>().getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(contact, null, "EMERGENCY ALERT: $name needs help!", null, null)
        } catch (e: Exception) {
            Log.e("ElderlyViewModel", "Failed to send SMS: ${e.message}")
        }

        // Automated Call
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = "tel:$contact".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            Log.e("ElderlyViewModel", "Failed to start Call: ${e.message}")
        }
    }

    fun toggleEmergency() {
        if (isEmergencyActive) {
            // Option to reset state if already active
            isEmergencyActive = false
        } else {
            // Manual trigger starts the same process as audio detection
            triggerEmergency()
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
