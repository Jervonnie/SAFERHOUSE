package com.example.saferhouseui.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saferhouseui.R
import com.example.saferhouseui.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SetupScreen(
    role: String,
    onNavigateBack: () -> Unit,
    onComplete: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var caregiverName by remember { mutableStateOf("") }
    var caregiverPhone by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

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
            // Header Section - Fixed height instead of weight to avoid overlapping
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
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
                    .weight(1f),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 80.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 35.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))
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

                    if (role == "elder") {
                        Spacer(modifier = Modifier.height(18.dp))

                        SleekInputField(
                            value = age,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) {
                                    age = it
                                }
                            },
                            placeholder = stringResource(R.string.Age),
                            icon = Icons.Default.Cake,
                            keyboardType = KeyboardType.Number
                        )
                    }

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

                    if (role == "elder") {
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "PRIMARY CAREGIVER",
                            color = PrimaryTeal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        SleekInputField(
                            value = caregiverName,
                            onValueChange = { caregiverName = it },
                            placeholder = "Caregiver Full Name",
                            icon = Icons.Default.Badge
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        SleekInputField(
                            value = caregiverPhone,
                            onValueChange = { 
                                if (it.all { char -> char.isDigit() }) {
                                    caregiverPhone = it 
                                }
                            },
                            placeholder = "Caregiver Contact Number",
                            icon = Icons.Default.Phone,
                            keyboardType = KeyboardType.Number
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = { 
                            val isAgeValid = if (role == "elder") age.isNotBlank() else true
                            val isCaregiverValid = if (role == "elder") caregiverName.isNotBlank() && caregiverPhone.isNotBlank() else true
                            
                            if (name.isNotBlank() && isAgeValid && address.isNotBlank() && contact.isNotBlank() && isCaregiverValid) {
                                scope.launch {
                                    onComplete(name, caregiverName, address, contact, caregiverPhone)
                                }
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
                        enabled = name.isNotBlank() && 
                                 (if (role == "elder") age.isNotBlank() && caregiverName.isNotBlank() && caregiverPhone.isNotBlank() else true) && 
                                 address.isNotBlank() && contact.isNotBlank()
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
                    
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
@Suppress("unused")
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
