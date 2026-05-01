package com.example.saferhouseui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.saferhouseui.ElderlyMember
import com.example.saferhouseui.UserProfile
import java.util.UUID

class CaretakerViewModel(private val authViewModel: AuthViewModel) : ViewModel() {

    fun updateRole(role: String) {
        authViewModel.currentUser?.let { user ->
            authViewModel.updateUser(user.copy(role = role))
        }
    }

    fun updateProfile(name: String, address: String, contact: String) {
        authViewModel.currentUser?.let { user ->
            val updatedUser = user.copy(
                name = name,
                address = address,
                contact = contact
            )
            
            // Auto-populate first elderly member for demo purposes if caretaker
            if (updatedUser.role == "helper" && updatedUser.managedElders.isEmpty()) {
                updatedUser.managedElders.add(ElderlyMember("1", "Lolo Mao", "QC Area", "0912-345-6789", 82, "Safe", "Just now"))
            }
            
            authViewModel.updateUser(updatedUser)
        }
    }

    fun assignElderByCode(code: String) {
        // TODO: Implement real-time pairing with Supabase
        // For now, we'll just log this action as we move toward the new backend structure
        println("Assigning elder with code: $code")
    }
}
