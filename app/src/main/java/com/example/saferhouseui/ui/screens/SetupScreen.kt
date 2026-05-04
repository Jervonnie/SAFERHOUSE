package com.example.saferhouseui.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saferhouseui.ui.theme.*

import androidx.compose.ui.res.stringResource
import com.example.saferhouseui.R

@Composable
fun SetupScreen(
    role: String,
    onNavigateBack: () -> Unit,
    onComplete: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }

    val roleTitle = if (role == "elder") stringResource(R.string.elder_setup) else stringResource(R.string.caregiver_setup)

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
            // Header Section - Responsive weights
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
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = stringResource(R.string.profile_configuration),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Setup Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.75f),
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
                        text = roleTitle,
                        color = Color.Black,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(35.dp))

                    SleekInputField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = stringResource(R.string.full_name),
                        icon = Icons.Default.Badge
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    SleekInputField(
                        value = address,
                        onValueChange = { address = it },
                        placeholder = stringResource(R.string.home_address),
                        icon = Icons.Default.Home
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    SleekInputField(
                        value = contact,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() }) {
                                contact = it 
                            }
                        },
                        placeholder = stringResource(R.string.contact_number),
                        icon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { 
                            if (name.isNotBlank() && address.isNotBlank() && contact.isNotBlank()) {
                                onComplete(name, address, contact)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryTeal,
                            disabledContainerColor = PrimaryTeal.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        enabled = name.isNotBlank() && address.isNotBlank() && contact.isNotBlank()
                    ) {
                        Text(
                            text = stringResource(R.string.finish_setup),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(onClick = onNavigateBack) {
                        Text(
                            text = stringResource(R.string.go_back),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
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
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
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
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
