package com.example.saferhouseui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saferhouseui.data.model.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class CaregiverViewModel(private val authViewModel: AuthViewModel) : ViewModel() {

    private var currentUser: User? = null

    init {
        viewModelScope.launch {
            authViewModel.currentUser.collectLatest {
                currentUser = it
            }
        }
    }

    fun updateRole(role: String) {
        currentUser?.let { user ->
            viewModelScope.launch {
                authViewModel.updateUser(user.copy(role = role))
            }
        }
    }

    fun updateProfile(name: String, address: String, contact: String) {
        currentUser?.let { user ->
            viewModelScope.launch {
                val updatedUser = user.copy(
                    fullName = name,
                    address = address,
                    phoneNumber = contact
                )
                
                authViewModel.updateUser(updatedUser)
            }
        }
    }

    fun assignElderByCode(code: String) {
        // TODO: Implement real-time pairing with Supabase
        // For now, we'll just log this action as we move toward the new backend structure
        println("Assigning elder with code: $code")
    }

    fun removeElderlyMember(elderId: String) {
        // Logic to be moved to Repository/Database
        println("Removing elder: $elderId")
    }

    fun updateCheckInSchedule(elderId: String, days: List<String>, time: String) {
        // Logic to be moved to Repository/Database
        println("Updating schedule for $elderId: $days at $time")
    }

    fun updateEmergencyContacts(elderId: String, contacts: List<String>) {
        // Logic to be moved to Repository/Database
        println("Updating contacts for $elderId: $contacts")
    }
}
