package com.example.saferhouseui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.saferhouseui.ElderlyMember
import com.example.saferhouseui.UserProfile

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("saferhouse_prefs", Context.MODE_PRIVATE)

    // In-memory list of users
    val users = mutableStateListOf<UserProfile>(
        UserProfile(
            "caretaker@demo.com",
            "password123",
            "helper",
            "Juan Dela Cruz",
            "Brgy. New Era, Quezon City",
            "09123456789",
            mutableListOf(ElderlyMember("1", "Lolo Mao", "0912-345-6789", 82, "Safe", "Just now"))
        )
    )

    var isReturningUser by mutableStateOf(prefs.getBoolean("is_returning_user", false))
        private set

    // Current session state
    var currentUserEmail by mutableStateOf("")
        private set

    val currentUser: UserProfile?
        get() = users.find { it.email == currentUserEmail }

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
        users.add(UserProfile(email, password))
        currentUserEmail = email
        markAsReturning()
    }

    private fun markAsReturning() {
        isReturningUser = true
        prefs.edit().putBoolean("is_returning_user", true).apply()
    }

    fun updateRole(role: String) {
        val index = users.indexOfFirst { it.email == currentUserEmail }
        if (index != -1) {
            users[index] = users[index].copy(role = role)
        }
    }

    fun updateProfile(name: String, address: String, contact: String) {
        val index = users.indexOfFirst { it.email == currentUserEmail }
        if (index != -1) {
            val user = users[index]
            val updatedUser = user.copy(
                name = name,
                address = address,
                contact = contact
            )
            
            if (updatedUser.role == "helper" && updatedUser.managedElders.isEmpty()) {
                updatedUser.managedElders.add(ElderlyMember("1", "Lolo Mao", "0912-345-6789", 82, "Safe", "Just now"))
            }
            
            users[index] = updatedUser
        }
    }

    fun addElderlyMember(name: String) {
        val index = users.indexOfFirst { it.email == currentUserEmail }
        if (index != -1) {
            val user = users[index]
            val newList = user.managedElders.toMutableList()
            newList.add(ElderlyMember(id = java.util.UUID.randomUUID().toString(), name = name))
            users[index] = user.copy(managedElders = newList)
        }
    }

    fun logout() {
        currentUserEmail = ""
    }
}
