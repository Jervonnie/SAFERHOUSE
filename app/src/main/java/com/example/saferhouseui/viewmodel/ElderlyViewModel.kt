package com.example.saferhouseui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class ElderlyViewModel(application: Application) : AndroidViewModel(application) {
    var isEmergencyActive by mutableStateOf(false)
        private set

    fun toggleEmergency() {
        isEmergencyActive = !isEmergencyActive
    }
}
