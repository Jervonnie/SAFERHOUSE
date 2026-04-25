package com.example.saferhouseui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.saferhouseui.ui.screens.*
import com.example.saferhouseui.ui.theme.SaferHouseUITheme

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saferhouseui.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SaferHouseUITheme {
                val userViewModel: UserViewModel = viewModel()
                AppNavigation(userViewModel)
            }
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
    val phoneNumber: String = "09XXXXXXXXX",
    val batteryLevel: Int = 100,
    val status: String = "Safe",
    val lastSeen: String = "Just now"
)

@Composable
fun AppNavigation(userViewModel: UserViewModel) {
    val navController = rememberNavController()
    val currentUser = userViewModel.currentUser

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                userViewModel = userViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToDashboard = { email ->
                    val user = userViewModel.users.find { it.email == email }
                    if (user != null) {
                        val route = when (user.role) {
                            "helper" -> "caretaker_dashboard"
                            "elder" -> "elderly_dashboard"
                            else -> "role" // Redirect to role selection if profile is incomplete
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
                    userViewModel.register(email, password)
                    navController.navigate("role")
                }
            )
        }
        composable("role") {
            RoleScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNext = { role ->
                    userViewModel.updateRole(role)
                    navController.navigate("setup/$role")
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
                onComplete = { name, address, contact ->
                    userViewModel.updateProfile(name, address, contact)
                    userViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("caretaker_dashboard") {
            currentUser?.let { user ->
                CaretakerDashboardScreen(
                    caretakerName = user.name,
                    caretakerAddress = user.address,
                    caretakerContact = user.contact,
                    managedElders = user.managedElders,
                    onUpdateProfile = { name, address, contact ->
                        userViewModel.updateProfile(name, address, contact)
                    },
                    onAddElder = { name -> userViewModel.addElderlyMember(name) },
                    onLogout = {
                        userViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("caretaker_dashboard") { inclusive = true }
                        }
                    }
                )
            }
        }
        composable("elderly_dashboard") {
            currentUser?.let { user ->
                ElderlyDashboardScreen(
                    elderName = user.name,
                    caretakerName = "Juan Dela Cruz", // Mock for now
                    onLogout = {
                        userViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("elderly_dashboard") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
