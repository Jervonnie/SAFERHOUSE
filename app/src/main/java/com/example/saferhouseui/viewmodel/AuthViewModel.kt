package com.example.saferhouseui.viewmodel

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saferhouseui.data.model.User
import com.example.saferhouseui.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AuthViewModel(
    application: Application,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("saferhouse_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    var isReturningUser by mutableStateOf(prefs.getBoolean("is_returning_user", false))
        private set

    var loginInProgress by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            // Check if there's a "remembered" user session in the DB
            _currentUser.value = userRepository.getCurrentUser()
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        loginInProgress = true
        return try {
            val user = userRepository.getUserByEmail(email)
            if (user != null) {
                // In a real app, we'd verify the password here
                _currentUser.value = user
                markAsReturning()
                true
            } else {
                false
            }
        } finally {
            loginInProgress = false
        }
    }

    suspend fun register(email: String, fullName: String, role: String) {
        val newUser = User(
            id = UUID.randomUUID().toString(),
            email = email,
            fullName = fullName,
            age = 0,
            phoneNumber = "",
            address = "",
            role = role
        )
        userRepository.insertUser(newUser)
        _currentUser.value = newUser
        markAsReturning()
    }

    private fun markAsReturning() {
        isReturningUser = true
        prefs.edit { putBoolean("is_returning_user", true) }
    }

    fun logout() {
        _currentUser.value = null
    }

    suspend fun updateUser(updatedUser: User) {
        userRepository.updateUser(updatedUser)
        _currentUser.value = updatedUser
    }
}
