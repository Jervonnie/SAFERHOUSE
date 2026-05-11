package com.example.saferhouseui

import android.Manifest
import android.os.Build
import android.content.Intent
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
import com.example.saferhouseui.data.model.*
import com.example.saferhouseui.data.repository.ActivityRepository
import com.example.saferhouseui.data.repository.EmergencyContactRepository
import com.example.saferhouseui.data.repository.UserRepository
import com.example.saferhouseui.ui.screens.*
import com.example.saferhouseui.ui.theme.PrimaryTeal
import com.example.saferhouseui.ui.theme.SaferHouseUITheme

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saferhouseui.service.SafetyMonitoringService
import com.example.saferhouseui.viewmodel.AuthViewModel
import com.example.saferhouseui.viewmodel.CaregiverViewModel
import com.example.saferhouseui.viewmodel.ElderlyViewModel
import com.example.saferhouseui.viewmodel.UserPreferenceViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            android.util.Log.d("MainActivity", "All permissions granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkAndRequestPermissions()

        setContent {
            val app = application as SaferHouseApplication
            val activityRepository = ActivityRepository(app.database.activityLogDao())
            val userRepository = UserRepository(app.database.userDao())
            val emergencyContactRepository = EmergencyContactRepository(app.database.emergencyContactDao())
            
            val prefViewModel: UserPreferenceViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return AuthViewModel(application, userRepository) as T
                    }
                }
            )
            val elderlyViewModel: ElderlyViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return ElderlyViewModel(application, authViewModel, activityRepository, emergencyContactRepository) as T
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
                val currentUser by authViewModel.currentUser.collectAsState()
                
                LaunchedEffect(currentUser) {
                    val user = currentUser
                    val hasMicPermission = ContextCompat.checkSelfPermission(
                        this@MainActivity, 
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    
                    if (user != null && hasMicPermission && 
                        (user.role.equals("ELDERLY", ignoreCase = true) || user.role.equals("ELDER", ignoreCase = true))) {
                        val intent = Intent(this@MainActivity, SafetyMonitoringService::class.java)
                        ContextCompat.startForegroundService(this@MainActivity, intent)
                    } else {
                        val intent = Intent(this@MainActivity, SafetyMonitoringService::class.java)
                        stopService(intent)
                    }
                }

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
        val permissions = mutableListOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
        
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

@Composable
fun AppNavigation(
    prefViewModel: UserPreferenceViewModel,
    authViewModel: AuthViewModel,
    caregiverViewModel: CaregiverViewModel,
    elderlyViewModel: ElderlyViewModel
) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                onNavigateToDashboard = { selectedEmail ->
                    val user = currentUser
                    if (user != null && user.email.equals(selectedEmail, ignoreCase = true)) {
                        val route = when (user.role.lowercase()) {
                            "caregiver" -> "caregiver_dashboard"
                            "elderly", "elder" -> "elderly_dashboard"
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
                onUserCreated = { email, password ->
                    scope.launch {
                        authViewModel.register(email, "New User", "ELDERLY")
                        navController.navigate("role")
                    }
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
                onComplete = { name, caregiverName, address, contact, caregiverPhone ->
                    scope.launch {
                        if (role == "caregiver") {
                            caregiverViewModel.updateProfile(name, address, contact)
                        } else {
                            elderlyViewModel.updateProfile(name, caregiverName, address, contact, caregiverPhone)
                        }
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("caregiver_dashboard") {
            currentUser?.let { user ->
                CaregiverDashboardScreen(
                    caregiverName = user.fullName,
                    caregiverAddress = user.address,
                    caregiverContact = user.phoneNumber,
                    managedElders = emptyList(), // Should be fetched from CaregiverViewModel
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
                    onUpdateCheckIn = { elderId, days, time ->
                        caregiverViewModel.updateCheckInSchedule(elderId, days, time)
                    },
                    onUpdateEmergencyContacts = { elderId, contacts ->
                        caregiverViewModel.updateEmergencyContacts(elderId, contacts)
                    },
                    activityLogs = emptyList(), // Should be fetched from CaregiverViewModel
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
                ElderlyDashboardScreen(
                    elderName = user.fullName,
                    elderAddress = user.address,
                    elderContact = user.phoneNumber,
                    caregiverName = user.caregiverName ?: "Not Assigned",
                    caregiverAddress = "Location pending",
                    caregiverContact = user.caregiverPhoneNumber ?: "N/A",
                    currentLanguage = prefViewModel.language,
                    currentFontSize = prefViewModel.fontSize,
                    isEmergencyActive = elderlyViewModel.isEmergencyActive,
                    isConfirmationDialogOpen = elderlyViewModel.isConfirmationDialogOpen,
                    isCheckInPending = elderlyViewModel.isCheckInPending,
                    isLocalAlarmActive = elderlyViewModel.isLocalAlarmActive,
                    countdownValue = elderlyViewModel.countdownValue,
                    onEmergencyToggle = { elderlyViewModel.toggleEmergency() },
                    onConfirmEmergency = { elderlyViewModel.confirmEmergency() },
                    onCancelEmergency = { elderlyViewModel.cancelEmergency() },
                    onCheckInResponse = { elderlyViewModel.respondToCheckIn() },
                    onStopAlarm = { elderlyViewModel.stopLocalAlarm() },
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
