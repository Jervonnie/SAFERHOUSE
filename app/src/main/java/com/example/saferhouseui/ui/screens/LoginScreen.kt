package com.example.saferhouseui.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saferhouseui.viewmodel.AuthViewModel
import com.example.saferhouseui.ui.theme.*

import androidx.compose.ui.res.stringResource
import com.example.saferhouseui.R

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToDashboard: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val welcomeText = if (authViewModel.isReturningUser) 
                        stringResource(R.string.welcome_back_caps) 
                    else 
                        stringResource(R.string.welcome_caps)
                    
                    Text(
                        text = stringResource(R.string.app_name),
                        color = PrimaryTeal,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = welcomeText,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 4.sp
                    )
                }
            }

            // Login Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 80.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 35.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.sign_in),
                        color = Color.Black,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(35.dp))

                    SleekInputField(
                        value = email,
                        onValueChange = { 
                            email = it
                            loginError = false
                        },
                        placeholder = stringResource(R.string.email_address),
                        icon = Icons.Default.Email,
                        isError = loginError
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    SleekInputField(
                        value = password,
                        onValueChange = { 
                            password = it
                            loginError = false
                        },
                        placeholder = stringResource(R.string.password),
                        icon = Icons.Default.Lock,
                        isPassword = true,
                        isError = loginError
                    )

                    if (loginError) {
                        Text(
                            text = stringResource(R.string.invalid_account),
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp).align(Alignment.Start)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = PrimaryTeal,
                                    uncheckedColor = Color.LightGray,
                                    checkmarkColor = Color.White
                                )
                            )
                            Text(
                                text = stringResource(R.string.keep_me_logged_in),
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }

                        TextButton(onClick = { /* TODO */ }) {
                            Text(
                                text = stringResource(R.string.forgot),
                                color = PrimaryTeal,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = { 
                            if (authViewModel.login(email, password)) {
                                onNavigateToDashboard(email)
                            } else {
                                loginError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.login_btn),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.new_here) + " ",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        TextButton(onClick = onNavigateToRegister, contentPadding = PaddingValues(horizontal = 4.dp)) {
                            Text(
                                text = stringResource(R.string.create_account),
                                color = PrimaryTeal,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
