package com.example.saferhouseui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.saferhouseui.ui.screens.*
import com.example.saferhouseui.ui.theme.PrimaryTeal
import com.example.saferhouseui.ui.theme.SaferHouseUITheme

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saferhouseui.viewmodel.AuthViewModel
import com.example.saferhouseui.viewmodel.CaregiverViewModel
import com.example.saferhouseui.viewmodel.ElderlyViewModel
import com.example.saferhouseui.viewmodel.UserPreferenceViewModel

class MainActivity : AppCompatActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // Permissions granted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkAndRequestPermissions()

        setContent {
            val prefViewModel: UserPreferenceViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()
            val elderlyViewModel: ElderlyViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return ElderlyViewModel(application, authViewModel) as T
                    }
                }
            )
            val caregiverViewModel: CaregiverViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return CaregiverViewModel(authViewModel) as T
                    }
                }
            )

            SaferHouseUITheme {
                Box {
                    AppNavigation(
                        prefViewModel = prefViewModel,
                        authViewModel = authViewModel,
                        caregiverViewModel = caregiverViewModel,
                        elderlyViewModel = elderlyViewModel
                    )
                    
                    if (prefViewModel.isLoading) {
                        LoadingOverlay()
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
}

@Composable
fun LoadingOverlay() {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.Black.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryTeal)
        }
    }
}

// Simple Data Class for our Demo User
data class UserProfile(
    val email: String,
    val password: String,
    var role: String? = null,
    var name: String = "",
    var address: String = "",
    var contact: String = "",
    val managedElders: MutableList<ElderlyMember> = mutableListOf()
)

data class ElderlyMember(
    val id: String,
    val name: String,
    val age: String = "",
    val address: String = "",
    val phoneNumber: String = "09XXXXXXXXX",
    val batteryLevel: Int = 100,
    val status: String = "Safe",
    val lastSeen: String = "Just now"
)

@Composable
fun AppNavigation(
    prefViewModel: UserPreferenceViewModel,
    authViewModel: AuthViewModel,
    caregiverViewModel: CaregiverViewModel,
    elderlyViewModel: ElderlyViewModel
) {
    val navController = rememberNavController()
    val currentUser = authViewModel.currentUser

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                onNavigateToDashboard = { email ->
                    val user = authViewModel.users.find { it.email == email }
                    if (user != null) {
                        val route = when (user.role) {
                            "caregiver" -> "caregiver_dashboard"
                            "elder" -> "elderly_dashboard"
                            else -> "role"
                        }
                        navController.navigate(route) {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onUserCreated = { email: String, password: String ->
                    authViewModel.register(email, password)
                    navController.navigate("role")
                }
            )
        }
        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("role") {
            RoleScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNext = { role ->
                    caregiverViewModel.updateRole(role)
                    navController.navigate("setup/$role") {
                        popUpTo("role") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "setup/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "elder"
            SetupScreen(
                role = role,
                onNavigateBack = { navController.popBackStack() },
                onComplete = { name, age, address, contact ->
                    if (role == "caregiver") {
                        caregiverViewModel.updateProfile(name, address, contact)
                    } else {
                        elderlyViewModel.updateProfile(name, address, contact)
                    }
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("caregiver_dashboard") {
            currentUser?.let { user ->
                CaregiverDashboardScreen(
                    caregiverName = user.name,
                    caregiverAddress = user.address,
                    caregiverContact = user.contact,
                    managedElders = user.managedElders,
                    currentFontSize = prefViewModel.fontSize,
                    onFontSizeChange = { prefViewModel.setAppFontSize(it) },
                    onUpdateProfile = { name, address, contact ->
                        caregiverViewModel.updateProfile(name, address, contact)
                    },
                    onAddElder = { code -> 
                        caregiverViewModel.assignElderByCode(code)
                    },
                    onRemoveElder = { elderId ->
                        caregiverViewModel.removeElderlyMember(elderId)
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("caregiver_dashboard") { inclusive = true }
                        }
                    }
                )
            }
        }
        composable("elderly_dashboard") {
            currentUser?.let { user ->
                val caregiver = authViewModel.users.find { it.role == "caregiver" }
                ElderlyDashboardScreen(
                    elderName = user.name,
                    elderAddress = user.address,
                    elderContact = user.contact,
                    caregiverName = caregiver?.name ?: "Juan Dela Cruz",
                    caregiverAddress = caregiver?.address ?: "Brgy. New Era, Quezon City",
                    caregiverContact = caregiver?.contact ?: "09123456789",
                    currentLanguage = prefViewModel.language,
                    currentFontSize = prefViewModel.fontSize,
                    isEmergencyActive = elderlyViewModel.isEmergencyActive,
                    isConfirmationDialogOpen = elderlyViewModel.isConfirmationDialogOpen,
                    countdownValue = elderlyViewModel.countdownValue,
                    onEmergencyToggle = { elderlyViewModel.toggleEmergency() },
                    onConfirmEmergency = { elderlyViewModel.confirmEmergency() },
                    onCancelEmergency = { elderlyViewModel.cancelEmergency() },
                    onLanguageChange = { prefViewModel.setAppLanguage(it) },
                    onFontSizeChange = { prefViewModel.setAppFontSize(it) },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("elderly_dashboard") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
