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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saferhouseui.ui.theme.*

import androidx.compose.ui.res.stringResource
import com.example.saferhouseui.R

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onUserCreated: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val invalidEmailMsg = stringResource(R.string.invalid_email)
    val passwordShortMsg = stringResource(R.string.password_too_short)
    val passwordMismatchMsg = stringResource(R.string.passwords_do_not_match)

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
            // Header Section - Sleek & Modern
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.25f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.app_name),
                        color = PrimaryTeal,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = stringResource(R.string.security_safety_peace),
                        color = PrimaryTeal.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Main Content Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.75f),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 80.dp) // Professional asymmetric curve
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 35.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title Text
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.join_the),
                            color = Color.Black.copy(alpha = 0.8f),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Light
                        )
                        Text(
                            text = stringResource(R.string.safehouse),
                            color = PrimaryTeal,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(35.dp))

                    // Input Fields
                    SleekInputField(
                        value = email,
                        onValueChange = { 
                            email = it 
                            emailError = false
                            errorMessage = ""
                        },
                        placeholder = stringResource(R.string.email_address),
                        icon = Icons.Default.Email,
                        isError = emailError
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    SleekInputField(
                        value = password,
                        onValueChange = { 
                            password = it 
                            passwordError = false 
                            errorMessage = ""
                        },
                        placeholder = stringResource(R.string.password),
                        icon = Icons.Default.Lock,
                        isPassword = true,
                        isError = passwordError
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    SleekInputField(
                        value = confirmPassword,
                        onValueChange = { 
                            confirmPassword = it 
                            passwordError = false 
                            errorMessage = ""
                        },
                        placeholder = stringResource(R.string.confirm_password),
                        icon = Icons.Default.Lock,
                        isPassword = true,
                        isError = passwordError
                    )

                    // Error Message
                    if (emailError || passwordError) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(top = 8.dp, start = 4.dp)
                                .align(Alignment.Start)
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Primary Action Button
                    Button(
                        onClick = {
                            val emailPattern = android.util.Patterns.EMAIL_ADDRESS
                            if (email.isEmpty() || !emailPattern.matcher(email).matches()) {
                                emailError = true
                                errorMessage = invalidEmailMsg
                            } else if (password.length < 8) {
                                passwordError = true
                                errorMessage = passwordShortMsg
                            } else if (password != confirmPassword) {
                                passwordError = true
                                errorMessage = passwordMismatchMsg
                            } else {
                                onUserCreated(email, password)
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
                            text = stringResource(R.string.create_account).uppercase(),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }


                    Spacer(modifier = Modifier.weight(1f))


                    Row(
                        modifier = Modifier.padding(bottom = 1.dp), // Pushes it up from the bottom edge
                        verticalAlignment = Alignment.CenterVertically
                    )
                     {
                        Text(
                            text = stringResource(R.string.already_a_member) + " ",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        TextButton(
                            onClick = onNavigateToLogin,
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.sign_in),
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

@Composable
fun SleekInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { 
            Text(
                text = placeholder, 
                color = Color.Gray.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal
            ) 
        },
        leadingIcon = { 
            Icon(
                imageVector = icon, 
                contentDescription = null,
                tint = if (isError) Color.Red else PrimaryTeal.copy(alpha = 0.7f)
            ) 
        },
        shape = RoundedCornerShape(16.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        isError = isError,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryTeal,
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            errorBorderColor = Color.Red,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            errorTextColor = Color.Black,
            cursorColor = PrimaryTeal,
            focusedContainerColor = Color(0xFFF8F9FA),
            unfocusedContainerColor = Color(0xFFF8F9FA)
        )
    )
}
