package com.example.saferhouseui.viewmodel

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.saferhouseui.ElderlyMember
import com.example.saferhouseui.UserProfile

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("saferhouse_prefs", Context.MODE_PRIVATE)

    // In-memory list of users (Mock database)
    val users = mutableStateListOf(
        UserProfile(
            "caretaker@demo.com",
            "password123",
            "caregiver",
            "Juan Dela Cruz",
            "Brgy. New Era, Quezon City",
            "09123456789",
            mutableListOf(ElderlyMember("1", "Lolo Mao", "QC Area", "0912-345-6789", 82, "Safe", "Just now"))
        )
    )

    var currentUserEmail by mutableStateOf("")
        private set

    val currentUser: UserProfile?
        get() = users.find { it.email == currentUserEmail }

    var isReturningUser by mutableStateOf(prefs.getBoolean("is_returning_user", false))
        private set

    fun login(email: String, password: String): Boolean {
        val user = users.find { it.email == email && it.password == password }
        return if (user != null) {
            currentUserEmail = email
            markAsReturning()
            true
        } else {
            false
        }
    }

    fun register(email: String, password: String) {
        if (users.none { it.email == email }) {
            users.add(UserProfile(email, password))
        }
        currentUserEmail = email
        markAsReturning()
    }

    private fun markAsReturning() {
        isReturningUser = true
        prefs.edit { putBoolean("is_returning_user", true) }
    }

    fun logout() {
        currentUserEmail = ""
    }

    // Helper to update user state from other ViewModels if needed
    // In a real app, this would be handled by a Repository
    fun updateUser(updatedUser: UserProfile) {
        val index = users.indexOfFirst { it.email == updatedUser.email }
        if (index != -1) {
            users[index] = updatedUser
        }
    }
}
