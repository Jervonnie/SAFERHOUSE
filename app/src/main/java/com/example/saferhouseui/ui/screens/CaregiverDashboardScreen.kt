package com.example.saferhouseui.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.saferhouseui.ElderlyMember
import com.example.saferhouseui.R
import com.example.saferhouseui.ui.theme.DarkBackground
import com.example.saferhouseui.ui.theme.PrimaryTeal

data class LogData(
    val color: Color,
    val title: String,
    val msg: String,
    val time: String,
    val elderName: String = "Unknown Member"
)

@Composable
fun CaregiverDashboardScreen(
    caregiverName: String,
    caregiverAddress: String,
    caregiverContact: String,
    managedElders: List<ElderlyMember>,
    currentFontSize: String,
    onFontSizeChange: (String) -> Unit,
    onUpdateProfile: (String, String, String) -> Unit,
    onAddElder: (String) -> Unit,
    onRemoveElder: (String) -> Unit,
    @Suppress("UNUSED_PARAMETER") onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf("dashboard") }
    var selectedElderForLogs by remember { mutableStateOf<ElderlyMember?>(null) }
    var selectedLog by remember { mutableStateOf<LogData?>(null) }
    var logBackDestination by remember { mutableStateOf("dashboard") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var pendingFontSize by remember { mutableStateOf(currentFontSize) }

    val fontScale = when (pendingFontSize) {
        "Small" -> 0.9f
        "Large" -> 1.15f
        else -> 1.0f
    }

    val context = LocalContext.current
    val triggerCall = { number: String ->
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = "tel:$number".toUri()
        }
        context.startActivity(intent)
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        when (currentScreen) {
            "dashboard" -> DashboardContent(
                name = caregiverName,
                address = caregiverAddress,
                managedElders = managedElders,
                fontScale = fontScale,
                selectedImageUri = selectedImageUri,
                onNavigateToLogs = { 
                    logBackDestination = "dashboard"
                    currentScreen = "logs" 
                },
                onNavigateToSettings = { currentScreen = "settings" },
                onNavigateToCallList = { triggerCall("911") },
                onNavigateToManagement = { currentScreen = "elder_management" },
                onNavigateToEditProfile = { currentScreen = "edit_profile" },
                onLogClick = { log ->
                    selectedLog = log
                    logBackDestination = "dashboard"
                    currentScreen = "log_detail"
                }
            )
            "edit_profile" -> EditCaregiverProfileContent(
                initialName = caregiverName,
                initialAddress = caregiverAddress,
                initialContact = caregiverContact,
                fontScale = fontScale,
                selectedImageUri = selectedImageUri,
                onImageSelected = { selectedImageUri = it },
                onBack = { currentScreen = "dashboard" },
                onSave = { name, address, contact ->
                    onUpdateProfile(name, address, contact)
                    currentScreen = "dashboard"
                }
            )
            "logs" -> ActivityLogsContent(
                title = stringResource(R.string.safety_alerts),
                managedElders = managedElders,
                fontScale = fontScale,
                onBack = { currentScreen = "dashboard" },
                onLogClick = { log ->
                    selectedLog = log
                    logBackDestination = "logs"
                    currentScreen = "log_detail"
                }
            )
            "call_list" -> CallListContent(
                managedElders = managedElders,
                fontScale = fontScale,
                onBack = { currentScreen = "dashboard" },
                onCallElder = { number -> triggerCall(number) }
            )
            "elder_management" -> ElderManagementContent(
                managedElders = managedElders,
                fontScale = fontScale,
                onBack = { currentScreen = "dashboard" },
                onAddElder = { currentScreen = "add_elder" },
                onRemoveElder = onRemoveElder,
                onSeeLogs = { elder -> 
                    selectedElderForLogs = elder
                    logBackDestination = "elder_management"
                    currentScreen = "specific_logs"
                },
                onSeeProfile = { elder ->
                    selectedElderForLogs = elder
                    currentScreen = "elder_profile"
                }
            )
            "add_elder" -> AddElderContent(
                fontScale = fontScale,
                onBack = { currentScreen = "elder_management" },
                onAdd = { code ->
                    onAddElder(code)
                    currentScreen = "elder_management"
                }
            )
            "elder_profile" -> ElderProfileContent(
                elder = selectedElderForLogs,
                fontScale = fontScale,
                onBack = { currentScreen = "elder_management" },
                onCallElder = { number -> triggerCall(number) }
            )
            "specific_logs" -> ActivityLogsContent(
                title = stringResource(R.string.elder_history, selectedElderForLogs?.name ?: ""),
                specificElderName = selectedElderForLogs?.name,
                fontScale = fontScale,
                onBack = { currentScreen = "elder_management" },
                onLogClick = { log ->
                    selectedLog = log
                    logBackDestination = "specific_logs"
                    currentScreen = "log_detail"
                }
            )
            "log_detail" -> LogDetailContent(
                log = selectedLog,
                fontScale = fontScale,
                onBack = { currentScreen = logBackDestination },
                onCallEmergency = { triggerCall("911") }
            )
            "settings" -> SettingsContent(
                name = caregiverName,
                managedElders = managedElders,
                fontScale = fontScale,
                onBack = { 
                    if (pendingFontSize != currentFontSize) onFontSizeChange(pendingFontSize)
                    currentScreen = "dashboard" 
                },
                onLogout = onLogout,
                onNavigateToFontSize = { currentScreen = "font_size_selection" }
            )
            "font_size_selection" -> FontSizeSelectionContent(
                currentFontSize = pendingFontSize,
                fontScale = fontScale,
                onFontSizeSelected = { pendingFontSize = it },
                onBack = { currentScreen = "settings" }
            )
        }
    }
}

@Composable
fun Int.caregiverScaledSp(scale: Float): TextUnit = (this * scale).sp

@Composable
fun DashboardContent(
    name: String,
    address: String,
    @Suppress("UNUSED_PARAMETER") managedElders: List<ElderlyMember>,
    fontScale: Float,
    selectedImageUri: Uri?,
    onNavigateToLogs: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCallList: () -> Unit,
    onNavigateToManagement: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogClick: (LogData) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 20.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.dashboard_title),
                        color = PrimaryTeal,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = stringResource(R.string.system_online),
                        color = PrimaryTeal.copy(alpha = 0.5f),
                        fontSize = 10.caregiverScaledSp(fontScale),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 25.dp)
        ) {
            // Profile Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 40.dp, bottomEnd = 40.dp, topEnd = 10.dp, bottomStart = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color(0xFFF0F0F0))
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.fillMaxSize().padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name, 
                            color = Color.Black, 
                            fontWeight = FontWeight.Black, 
                            fontSize = 20.caregiverScaledSp(fontScale),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(text = stringResource(R.string.caregiver_label), color = PrimaryTeal, fontWeight = FontWeight.Bold, fontSize = 11.caregiverScaledSp(fontScale), letterSpacing = 1.sp)
                        Text(
                            text = address, 
                            color = Color.Gray, 
                            fontSize = 12.caregiverScaledSp(fontScale), 
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onNavigateToEditProfile,
                        modifier = Modifier
                            .size(36.dp)
                            .background(PrimaryTeal.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                DashboardActionButton(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.call_member),
                    subtitle = stringResource(R.string.emergency_hotline),
                    icon = Icons.Default.Call,
                    color = Color(0xFFFF4B4B),
                    fontScale = fontScale,
                    onClick = onNavigateToCallList
                )
                DashboardActionButton(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.manage_elders),
                    subtitle = stringResource(R.string.profiles_and_logs),
                    icon = Icons.Default.People,
                    color = Color(0xFFFFB800),
                    fontScale = fontScale,
                    onClick = onNavigateToManagement
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.safety_alerts), color = Color.White, fontSize = 18.caregiverScaledSp(fontScale), fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(R.string.see_all), 
                    color = PrimaryTeal, 
                    fontSize = 13.caregiverScaledSp(fontScale), 
                    modifier = Modifier.clickable { onNavigateToLogs() }
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val log1Name = managedElders.getOrNull(0)?.name ?: "Lolo Mao"
                val log2Name = managedElders.getOrNull(1)?.name ?: "Lola Maria"
                val demoLogs = listOf(
                    LogData(Color(0xFF00C49A), "Safe Check", "$log1Name confirmed check-in", "Now", log1Name),
                    LogData(Color(0xFFFFB800), "Fall Alert", "Possible fall detected for $log2Name", "12m ago", log2Name)
                )
                demoLogs.forEach { log ->
                    MiniActivityItem(log, fontScale, onClick = { onLogClick(log) })
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.system_secured),
                color = PrimaryTeal.copy(alpha = 0.3f),
                fontSize = 12.caregiverScaledSp(fontScale),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DashboardActionButton(
    modifier: Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    fontScale: Float,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, color = Color.White, fontSize = 15.caregiverScaledSp(fontScale), fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 11.caregiverScaledSp(fontScale))
        }
    }
}

@Composable
fun LogDetailContent(
    log: LogData?,
    fontScale: Float,
    onBack: () -> Unit,
    onCallEmergency: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 25.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.log_analysis),
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 14.caregiverScaledSp(fontScale),
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = Color.White.copy(alpha = 0.02f),
            shape = RoundedCornerShape(topStart = 80.dp, bottomEnd = 80.dp, topEnd = 20.dp, bottomStart = 20.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(35.dp))
                        .background(log?.color?.copy(alpha = 0.1f) ?: PrimaryTeal.copy(alpha = 0.1f))
                        .border(1.dp, log?.color?.copy(alpha = 0.2f) ?: PrimaryTeal.copy(alpha = 0.2f), RoundedCornerShape(35.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            log?.title?.contains("Fall", ignoreCase = true) == true -> Icons.Default.Warning
                            log?.title?.contains("Safe", ignoreCase = true) == true -> Icons.Default.HealthAndSafety
                            log?.title?.contains("Medicine", ignoreCase = true) == true -> Icons.Default.MedicalServices
                            else -> Icons.Default.Notifications
                        },
                        contentDescription = null,
                        tint = log?.color ?: PrimaryTeal,
                        modifier = Modifier.size(45.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = log?.title?.uppercase() ?: "TITLE OF THE ALERT",
                    color = Color.White,
                    fontSize = 28.caregiverScaledSp(fontScale),
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(30.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IndustrialLogTile(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.timestamp),
                        value = log?.time ?: "00:00",
                        icon = Icons.Default.AccessTime,
                        fontScale = fontScale,
                        compact = true
                    )
                    IndustrialLogTile(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.status),
                        value = if (log?.color == Color.Red) stringResource(R.string.urgent) else stringResource(R.string.resolved),
                        icon = Icons.Default.Info,
                        color = log?.color ?: PrimaryTeal,
                        fontScale = fontScale,
                        compact = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                IndustrialLogTile(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.name).uppercase(),
                    value = log?.elderName ?: "Unknown Member",
                    icon = Icons.Default.Person,
                    fontScale = fontScale,
                    compact = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    color = Color.White.copy(alpha = 0.03f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = stringResource(R.string.system_description),
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.caregiverScaledSp(fontScale),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = log?.msg ?: "System detected activity at recorded timestamp.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.caregiverScaledSp(fontScale),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))

                if (log?.color == Color.Red) {
                    Button(
                        onClick = onCallEmergency,
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4B4B)),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(stringResource(R.string.call_emergency_btn), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.caregiverScaledSp(fontScale))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, PrimaryTeal)
                ) {
                    Text(stringResource(R.string.done), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.caregiverScaledSp(fontScale))
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun IndustrialLogTile(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color = PrimaryTeal,
    fontScale: Float,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    compact: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.03f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 12.dp else 20.dp),
            horizontalAlignment = alignment
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color.copy(alpha = 0.5f), modifier = Modifier.size(if (compact) 10.dp else 12.dp))
                Spacer(modifier = Modifier.width(if (compact) 6.dp else 8.dp))
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = if (compact) 9.caregiverScaledSp(fontScale) else 10.caregiverScaledSp(fontScale),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(if (compact) 6.dp else 12.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = if (compact) 16.caregiverScaledSp(fontScale) else 18.caregiverScaledSp(fontScale),
                fontWeight = FontWeight.Bold,
                textAlign = if (alignment == Alignment.Start) TextAlign.Start else TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CallListContent(@Suppress("UNUSED_PARAMETER") managedElders: List<ElderlyMember>, fontScale: Float, onBack: () -> Unit, onCallElder: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 25.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = stringResource(R.string.call_members), color = Color.White, fontSize = 24.caregiverScaledSp(fontScale), fontWeight = FontWeight.Bold)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(15.dp)) {
            items(managedElders) { elder ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(45.dp).clip(CircleShape).background(PrimaryTeal.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryTeal)
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = elder.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.caregiverScaledSp(fontScale))
                            Text(text = elder.status, color = if(elder.status == "Safe") PrimaryTeal else Color.Red, fontSize = 12.caregiverScaledSp(fontScale))
                        }
                        IconButton(
                            onClick = { onCallElder(elder.phoneNumber) },
                            modifier = Modifier.background(PrimaryTeal, CircleShape)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ElderManagementContent(
    managedElders: List<ElderlyMember>,
    fontScale: Float,
    onBack: () -> Unit,
    onAddElder: () -> Unit,
    onRemoveElder: (String) -> Unit,
    onSeeLogs: (ElderlyMember) -> Unit,
    onSeeProfile: (ElderlyMember) -> Unit
) {
    var isEditMode by remember { mutableStateOf(false) }
    val elderToRemoveState = remember { mutableStateOf<ElderlyMember?>(null) }
    val elderToRemove = elderToRemoveState.value

    if (elderToRemove != null) {
        AlertDialog(
            onDismissRequest = { elderToRemoveState.value = null },
            title = { Text(stringResource(R.string.remove_member_title)) },
            text = { Text(stringResource(R.string.remove_member_msg, elderToRemove.name)) },
            confirmButton = {
                TextButton(onClick = {
                    onRemoveElder(elderToRemove.id)
                    elderToRemoveState.value = null
                }) {
                    Text(stringResource(R.string.remove), color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { elderToRemoveState.value = null }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddElder,
                containerColor = PrimaryTeal,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Elder")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(padding).padding(horizontal = 25.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal)
                }
                Spacer(modifier = Modifier.width(15.dp))
                Text(text = stringResource(R.string.managed_members), color = Color.White, fontSize = 24.caregiverScaledSp(fontScale), fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = { isEditMode = !isEditMode },
                    modifier = Modifier.background(if (isEditMode) Color.Red.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isEditMode) Icons.Default.Close else Icons.Default.Delete,
                        contentDescription = "Toggle Delete",
                        tint = if (isEditMode) Color.Red else Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(managedElders) { elder ->
                    ArtisticElderCard(
                        elder = elder,
                        fontScale = fontScale,
                        isEditMode = isEditMode,
                        onRemove = { elderToRemoveState.value = elder },
                        onSeeLogs = { onSeeLogs(elder) },
                        onSeeProfile = { onSeeProfile(elder) }
                    )
                }
            }
        }
    }
}

@Composable
fun AddElderContent(
    fontScale: Float,
    onBack: () -> Unit,
    onAdd: (String) -> Unit
) {
    var elderCode by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 25.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = stringResource(R.string.elder_setup), color = Color.White, fontSize = 24.caregiverScaledSp(fontScale), fontWeight = FontWeight.Bold)
        }
        
        Text(
            text = stringResource(R.string.linking_account),
            color = PrimaryTeal,
            fontSize = 11.caregiverScaledSp(fontScale),
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(30.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(25.dp)) {
                Text(
                    text = stringResource(R.string.assign_elder_title),
                    color = Color.Black,
                    fontSize = 22.caregiverScaledSp(fontScale),
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.assign_elder_desc),
                    color = Color.Gray,
                    fontSize = 13.caregiverScaledSp(fontScale),
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(25.dp))
                
                SleekInputField(
                    value = elderCode,
                    onValueChange = { 
                        if (it.length <= 6) elderCode = it.uppercase() 
                    },
                    placeholder = stringResource(R.string.enter_elder_code),
                    icon = Icons.Default.VpnKey
                )
                
                Spacer(modifier = Modifier.height(30.dp))
                
                Button(
                    onClick = { 
                        if (elderCode.length == 6) {
                            onAdd(elderCode)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryTeal,
                        disabledContainerColor = Color.LightGray.copy(alpha = 0.4f),
                        disabledContentColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(14.dp),
                    enabled = elderCode.length == 6
                ) {
                    Text(stringResource(R.string.assign_member), fontWeight = FontWeight.Bold, fontSize = 16.caregiverScaledSp(fontScale))
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
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = placeholder, color = Color.Gray.copy(alpha = 0.5f)) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = PrimaryTeal) },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryTeal,
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            cursorColor = PrimaryTeal,
            focusedContainerColor = Color(0xFFF8F9FA),
            unfocusedContainerColor = Color(0xFFF8F9FA)
        )
    )
}

@Composable
fun ArtisticElderCard(
    elder: ElderlyMember,
    fontScale: Float,
    isEditMode: Boolean = false,
    onRemove: () -> Unit = {},
    onSeeLogs: () -> Unit,
    onSeeProfile: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(topStart = 40.dp, bottomEnd = 40.dp, topEnd = 10.dp, bottomStart = 10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(15.dp)).background(PrimaryTeal.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = elder.name.take(1), color = PrimaryTeal, fontSize = 24.caregiverScaledSp(fontScale), fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(15.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = elder.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.caregiverScaledSp(fontScale))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if(elder.status == "Safe") PrimaryTeal else Color.Red))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = elder.status, color = Color.White.copy(alpha = 0.6f), fontSize = 12.caregiverScaledSp(fontScale))
                    }
                }
                
                if (isEditMode) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.background(Color.Red.copy(alpha = 0.2f), CircleShape).size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onSeeProfile,
                    modifier = Modifier.weight(1f).height(45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Text(stringResource(R.string.profile).uppercase(), fontSize = 12.caregiverScaledSp(fontScale), fontWeight = FontWeight.Bold, color = Color.White)
                }
                Button(
                    onClick = onSeeLogs,
                    modifier = Modifier.weight(1f).height(45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.activity).uppercase(), fontSize = 12.caregiverScaledSp(fontScale), fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ElderProfileContent(elder: ElderlyMember?, fontScale: Float, onBack: () -> Unit, onCallElder: (String) -> Unit) {
    if (elder == null) return
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 25.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = stringResource(R.string.profile), color = Color.White, fontSize = 24.caregiverScaledSp(fontScale), fontWeight = FontWeight.Bold)
        }

        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(topStart = 80.dp, bottomEnd = 80.dp, topEnd = 20.dp, bottomStart = 20.dp),
            border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(30.dp)).background(PrimaryTeal.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(50.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = elder.name, color = Color.White, fontSize = 24.caregiverScaledSp(fontScale), fontWeight = FontWeight.Black)
                Text(text = "ID: ${elder.id}", color = PrimaryTeal, fontSize = 12.caregiverScaledSp(fontScale), fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(40.dp))
                
                CaregiverDetailSection(label = stringResource(R.string.phone_number), value = elder.phoneNumber, icon = Icons.Default.Phone, fontScale = fontScale)
                Spacer(modifier = Modifier.height(20.dp))
                CaregiverDetailSection(label = stringResource(R.string.address), value = elder.address, icon = Icons.Default.LocationOn, fontScale = fontScale)
                Spacer(modifier = Modifier.height(20.dp))
                CaregiverDetailSection(label = stringResource(R.string.status).uppercase(), value = elder.status, icon = Icons.Default.Info, fontScale = fontScale)
                
                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { onCallElder(elder.phoneNumber) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = stringResource(R.string.call_member_caps), fontWeight = FontWeight.ExtraBold, fontSize = 16.caregiverScaledSp(fontScale))
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun CaregiverDetailSection(label: String, value: String, icon: ImageVector, fontScale: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = PrimaryTeal.copy(alpha = 0.6f), fontSize = 10.caregiverScaledSp(fontScale), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.03f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(modifier = Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(15.dp))
                Text(text = value, color = Color.White, fontSize = 15.caregiverScaledSp(fontScale), fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun MiniActivityItem(log: LogData, fontScale: Float, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(log.color))
            Spacer(modifier = Modifier.width(15.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = log.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.caregiverScaledSp(fontScale))
                Text(text = log.msg, color = Color.White.copy(alpha = 0.5f), fontSize = 11.caregiverScaledSp(fontScale))
            }
            Text(text = log.time, color = Color.White.copy(alpha = 0.3f), fontSize = 11.caregiverScaledSp(fontScale))
        }
    }
}

@Composable
fun ActivityLogsContent(
    title: String,
    specificElderName: String? = null,
    @Suppress("UNUSED_PARAMETER") managedElders: List<ElderlyMember> = emptyList(),
    fontScale: Float,
    onBack: () -> Unit,
    onLogClick: (LogData) -> Unit
) {
    val logs = if (specificElderName != null) {
        listOf(
            LogData(PrimaryTeal, "Fall Detected", "$specificElderName - Living Room Alert", "14:02", specificElderName),
            LogData(Color(0xFFFFB800), "Activity Detected", "$specificElderName is moving", "12:45", specificElderName)
        )
    } else {
        val name1 = managedElders.getOrNull(0)?.name ?: "Lolo Mao"
        val name2 = managedElders.getOrNull(1)?.name ?: "Lola Maria"
        listOf(
            LogData(PrimaryTeal, "Fall Detected", "$name1 - Living Room Alert", "14:02", name1),
            LogData(Color(0xFFFFB800), "Activity Detected", "$name2 - Bedroom Activity", "12:45", name2)
        )
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 25.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = title, color = Color.White, fontSize = 22.caregiverScaledSp(fontScale), fontWeight = FontWeight.Bold)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(15.dp)) {
            items(logs) { log ->
                ModernLogTile(log, fontScale, onClick = { onLogClick(log) })
            }
        }
    }
}

@Composable
fun ModernLogTile(log: LogData, fontScale: Float, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = log.time, color = Color.White.copy(alpha = 0.4f), fontSize = 11.caregiverScaledSp(fontScale))
                Text(text = log.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.caregiverScaledSp(fontScale))
                Text(text = log.msg, color = Color.White.copy(alpha = 0.6f), fontSize = 13.caregiverScaledSp(fontScale))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PrimaryTeal.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun SettingsContent(
    name: String,
    @Suppress("UNUSED_PARAMETER") managedElders: List<ElderlyMember>,
    fontScale: Float,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToFontSize: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 25.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = stringResource(R.string.settings), color = Color.White, fontSize = 24.caregiverScaledSp(fontScale), fontWeight = FontWeight.Bold)
        }

        Text(text = stringResource(R.string.system_preferences), color = PrimaryTeal, fontSize = 11.caregiverScaledSp(fontScale), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(15.dp))
        
        SettingsTile(Icons.Default.TextFormat, stringResource(R.string.font_size), stringResource(R.string.change_font_size), fontScale, onClick = onNavigateToFontSize)
        SettingsTile(Icons.Default.Person, stringResource(R.string.account_identity), name, fontScale)
        
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(bottom = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(stringResource(R.string.done), fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.caregiverScaledSp(fontScale))
        }

        TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
            Text(stringResource(R.string.logout), color = Color(0xFFFF4B4B), fontWeight = FontWeight.Bold, fontSize = 16.caregiverScaledSp(fontScale))
        }
    }
}

@Composable
fun SettingsTile(icon: ImageVector, title: String, value: String, fontScale: Float, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(RoundedCornerShape(15.dp)).background(Color.White.copy(alpha = 0.05f)).clickable { onClick() }.padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(15.dp))
        Column {
            Text(text = title, color = Color.White.copy(alpha = 0.5f), fontSize = 11.caregiverScaledSp(fontScale))
            Text(text = value, color = Color.White, fontSize = 15.caregiverScaledSp(fontScale), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun FontSizeSelectionContent(currentFontSize: String, fontScale: Float, onFontSizeSelected: (String) -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 25.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = stringResource(R.string.font_size), color = Color.White, fontSize = 24.caregiverScaledSp(fontScale), fontWeight = FontWeight.Bold)
        }
        LanguageRadioTile(stringResource(R.string.small), currentFontSize == "Small", fontScale, onClick = { onFontSizeSelected("Small") })
        Spacer(modifier = Modifier.height(15.dp))
        LanguageRadioTile(stringResource(R.string.medium), currentFontSize == "Medium", fontScale, onClick = { onFontSizeSelected("Medium") })
        Spacer(modifier = Modifier.height(15.dp))
        LanguageRadioTile(stringResource(R.string.large), currentFontSize == "Large", fontScale, onClick = { onFontSizeSelected("Large") })
    }
}

@Composable
fun LanguageRadioTile(label: String, isSelected: Boolean, fontScale: Float, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = if (isSelected) PrimaryTeal.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, if (isSelected) PrimaryTeal else Color.Transparent)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, color = Color.White, fontSize = 18.caregiverScaledSp(fontScale), fontWeight = FontWeight.Bold)
            RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = PrimaryTeal))
        }
    }
}

@Composable
fun EditCaregiverProfileContent(
    initialName: String,
    initialAddress: String,
    initialContact: String,
    fontScale: Float,
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    onBack: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var address by remember { mutableStateOf(initialAddress) }
    var contact by remember { mutableStateOf(initialContact) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImageSelected(uri)
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 25.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = stringResource(R.string.profile), color = Color.White, fontSize = 24.caregiverScaledSp(fontScale), fontWeight = FontWeight.Bold)
        }
        
        SleekInputFieldWhite(value = name, onValueChange = { name = it }, placeholder = stringResource(R.string.full_name), icon = Icons.Default.Badge)
        Spacer(modifier = Modifier.height(20.dp))
        SleekInputFieldWhite(value = address, onValueChange = { address = it }, placeholder = stringResource(R.string.address), icon = Icons.Default.Home)
        Spacer(modifier = Modifier.height(20.dp))
        SleekInputFieldWhite(value = contact, onValueChange = { contact = it }, placeholder = stringResource(R.string.contact_number), icon = Icons.Default.Phone)
        
        Spacer(modifier = Modifier.height(30.dp))

        // Profile Picture Section - Single Button implementation
        Text(
            text = stringResource(R.string.profile_picture_caps),
            color = PrimaryTeal,
            fontSize = 11.caregiverScaledSp(fontScale),
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(15.dp))
        
        Surface(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.fillMaxSize().padding(15.dp))
                    }
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column {
                    Text(
                        text = if (selectedImageUri == null) stringResource(R.string.select_profile_photo) else stringResource(R.string.change_photo),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.caregiverScaledSp(fontScale)
                    )
                    Text(
                        text = stringResource(R.string.tap_to_browse),
                        color = PrimaryTeal,
                        fontSize = 12.caregiverScaledSp(fontScale)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = PrimaryTeal,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { onSave(name, address, contact) },
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(bottom = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(stringResource(R.string.update), fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.caregiverScaledSp(fontScale))
        }
    }
}

@Composable
fun SleekInputFieldWhite(value: String, onValueChange: (String) -> Unit, placeholder: String, icon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = placeholder, color = Color.White.copy(alpha = 0.3f)) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = PrimaryTeal) },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryTeal,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = PrimaryTeal,
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
        )
    )
}
