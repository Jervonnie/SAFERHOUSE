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

    init {
        viewModelScope.launch {
            _currentUser.value = userRepository.getCurrentUser()
        }
    }

    fun login(email: String, password: String): Boolean {
        // Mock login - in reality, this would call Supabase
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() // Mock finding user
            if (user != null && user.email == email) {
                _currentUser.value = user
                markAsReturning()
            }
        }
        return true // Always return true for mock
    }

    fun register(email: String, fullName: String, role: String) {
        val newUser = User(
            id = UUID.randomUUID().toString(),
            email = email,
            fullName = fullName,
            age = 0,
            phoneNumber = "",
            address = "",
            role = role
        )
        viewModelScope.launch {
            userRepository.insertUser(newUser)
            _currentUser.value = newUser
            markAsReturning()
        }
    }

    private fun markAsReturning() {
        isReturningUser = true
        prefs.edit { putBoolean("is_returning_user", true) }
    }

    fun logout() {
        _currentUser.value = null
        // Potentially clear DB or just local state
    }

    fun updateUser(updatedUser: User) {
        viewModelScope.launch {
            userRepository.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }
}
