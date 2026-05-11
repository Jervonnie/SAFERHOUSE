package com.example.saferhouseui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saferhouseui.data.EmergencyManager
import com.example.saferhouseui.data.model.ActivityLog
import com.example.saferhouseui.data.model.EmergencyContact
import com.example.saferhouseui.data.model.User
import com.example.saferhouseui.data.repository.ActivityRepository
import com.example.saferhouseui.data.repository.EmergencyContactRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ElderlyViewModel(
    application: Application,
    private val authViewModel: AuthViewModel,
    private val activityRepository: ActivityRepository,
    private val emergencyContactRepository: EmergencyContactRepository
) : AndroidViewModel(application) {
    
    private val emergencyManager = EmergencyManager(application)
    private var currentUser: User? = null

    private val _emergencyContacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val emergencyContacts: StateFlow<List<EmergencyContact>> = _emergencyContacts.asStateFlow()

    var isEmergencyActive by mutableStateOf(false)
        private set

    var isConfirmationDialogOpen by mutableStateOf(false)
        private set

    var countdownValue by mutableIntStateOf(10)
        private set

    var isAudioDetectionEnabled by mutableStateOf(true)
        private set

    var isCheckInPending by mutableStateOf(false)
        private set
    
    private var checkInTimeoutJob: Job? = null
    
    var isLocalAlarmActive by mutableStateOf(false)
        private set
    
    private var mediaPlayer: MediaPlayer? = null
    private var countdownJob: Job? = null
    private var audioDetectionJob: Job? = null

    var generatedPairingCode by mutableStateOf<String?>(null)
        private set

    private val emergencyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.saferhouseui.EMERGENCY_TRIGGER") {
                val type = intent.getStringExtra("type") ?: "UNKNOWN"
                Log.d("ElderlyViewModel", "Received emergency broadcast: $type")
                triggerEmergency()
            }
        }
    }

    init {
        val filter = IntentFilter("com.example.saferhouseui.EMERGENCY_TRIGGER")
        ContextCompat.registerReceiver(
            application,
            emergencyReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )

        viewModelScope.launch {
            authViewModel.currentUser.collectLatest { user ->
                currentUser = user
                if (user != null) {
                    launch {
                        emergencyContactRepository.getAllContacts().collect { contacts ->
                            _emergencyContacts.value = contacts.filter { it.elderlyId == user.id }
                        }
                    }
                }
            }
        }
    }

    fun generatePairingCode() {
        val code = (100000..999999).random().toString()
        generatedPairingCode = code
        addLog("PAIRING", "Generated new pairing code: $code")
    }

    fun triggerDailyCheckIn() {
        isCheckInPending = true
        updateStatus("Check-In Pending")
        checkInTimeoutJob?.cancel()
        checkInTimeoutJob = viewModelScope.launch {
            delay(300000) 
            if (isCheckInPending) {
                notifyCheckInMissed()
            }
        }
    }

    fun respondToCheckIn() {
        isCheckInPending = false
        checkInTimeoutJob?.cancel()
        updateStatus("Safe")
        addLog("DAILY_CHECK", "Elder responded to daily check-in and is safe.")
    }

    private fun notifyCheckInMissed() {
        isCheckInPending = false
        updateStatus("Missed Check-In")
        addLog("DAILY_CHECK_MISSED", "Elder missed the scheduled daily check-in.")
    }

    fun triggerEmergency() {
        // Restore confirmation dialog to prevent false alarms from casual talk/stories
        isConfirmationDialogOpen = true
        startCountdown()
    }

    private fun startCountdown() {
        isConfirmationDialogOpen = true
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
        updateStatus("Emergency")
        addLog("SOS", "Emergency alert triggered.")
        sendEmergencyEscalation()
    }

    private fun sendEmergencyEscalation() {
        viewModelScope.launch {
            emergencyManager.executeEscalation(currentUser, _emergencyContacts.value)
        }
        if (_emergencyContacts.value.isEmpty()) {
            startLocalAlarm()
        }
    }

    fun startLocalAlarm() {
        if (isLocalAlarmActive) return
        isLocalAlarmActive = true
        try {
            val alert: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer.create(getApplication(), alert)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e("ElderlyViewModel", "Error playing local alarm: ${e.message}")
        }
    }

    fun stopLocalAlarm() {
        isLocalAlarmActive = false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun toggleEmergency() {
        if (isEmergencyActive) {
            isEmergencyActive = false
            updateStatus("Safe")
        } else {
            confirmEmergency()
        }
    }

    private fun updateStatus(status: String) {
        // Handled by room/supabase later
    }

    private fun addLog(type: String, description: String) {
        val currentElderUser = currentUser ?: return
        viewModelScope.launch {
            activityRepository.addLog(
                ActivityLog(
                    userId = currentElderUser.id,
                    type = type,
                    description = description
                )
            )
        }
    }

    fun updateProfile(name: String, caregiverName: String, address: String, contact: String, caregiverPhone: String) {
        viewModelScope.launch {
            currentUser?.let { user ->
                authViewModel.updateUser(
                    user.copy(
                        fullName = name,
                        address = address,
                        phoneNumber = contact,
                        caregiverName = caregiverName,
                        caregiverPhoneNumber = caregiverPhone
                    )
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(emergencyReceiver)
        } catch (e: Exception) {
            Log.e("ElderlyViewModel", "Error unregistering receiver: ${e.message}")
        }
    }
}
