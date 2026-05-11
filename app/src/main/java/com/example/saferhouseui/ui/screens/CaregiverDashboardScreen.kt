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
import com.example.saferhouseui.R
import com.example.saferhouseui.ui.theme.DarkBackground
import com.example.saferhouseui.ui.theme.PrimaryTeal
import com.example.saferhouseui.data.model.ActivityLog
import com.example.saferhouseui.data.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogData(
    val color: Color,
    val title: String,
    val location: String,
    val time: String,
    val elderName: String = "Unknown Member",
    val age: String = ""
)

@Composable
fun CaregiverDashboardScreen(
    caregiverName: String,
    caregiverAddress: String,
    caregiverContact: String,
    managedElders: List<User> = emptyList(),
    currentFontSize: String,
    onFontSizeChange: (String) -> Unit,
    onUpdateProfile: (String, String, String) -> Unit,
    onAddElder: (String) -> Unit,
    onRemoveElder: (String) -> Unit,
    onUpdateCheckIn: (String, List<String>, String) -> Unit,
    onUpdateEmergencyContacts: (String, List<String>) -> Unit,
    activityLogs: List<ActivityLog> = emptyList(),
    @Suppress("UNUSED_PARAMETER") onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf("dashboard") }
    var selectedElderForLogs by remember { mutableStateOf<User?>(null) }
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
                activityLogs = activityLogs,
                fontScale = fontScale,
                selectedImageUri = selectedImageUri,
                onNavigateToLogs = { 
                    logBackDestination = "dashboard"
                    currentScreen = "logs" 
                },
                onNavigateToSettings = { currentScreen = "settings" },
                onCallElder = { number -> triggerCall(number) },
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
                activityLogs = activityLogs,
                fontScale = fontScale,
                onBack = { currentScreen = "dashboard" },
                onLogClick = { log ->
                    selectedLog = log
                    logBackDestination = "logs"
                    currentScreen = "log_detail"
                }
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
                onCallElder = { number -> triggerCall(number) },
                onUpdateCheckIn = { days, time ->
                    selectedElderForLogs?.let { 
                        onUpdateCheckIn(it.id, days, time)
                    }
                },
                onUpdateEmergencyContacts = { contacts ->
                    selectedElderForLogs?.let {
                        onUpdateEmergencyContacts(it.id, contacts)
                    }
                }
            )
            "specific_logs" -> ActivityLogsContent(
                title = stringResource(R.string.elder_history, selectedElderForLogs?.fullName ?: ""),
                specificElderName = selectedElderForLogs?.fullName,
                managedElders = managedElders,
                activityLogs = activityLogs,
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
                onCallEmergency = { triggerCall("") }
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
    managedElders: List<User>,
    activityLogs: List<ActivityLog>,
    fontScale: Float,
    selectedImageUri: Uri?,
    onNavigateToLogs: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onCallElder: (String) -> Unit,
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
                    onClick = { managedElders.firstOrNull()?.let { onCallElder(it.phoneNumber) } }
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
                val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
                val sdf = remember(locale) { SimpleDateFormat("HH:mm", locale) }
                
                val displayLogs = activityLogs.take(3).map { log ->
                    // For now mapping type to color
                    val color = when(log.type) {
                        "SOS", "FALL" -> Color.Red
                        "DAILY_CHECK_MISSED" -> Color(0xFFFFB800)
                        else -> Color(0xFF00C49A)
                    }
                    val elder = managedElders.find { it.id == log.userId }
                    LogData(
                        color = color,
                        title = log.type.replace("_", " "),
                        location = if (log.latitude != null) "${log.latitude}, ${log.longitude}" else "Last Known Loc",
                        time = sdf.format(Date(log.timestamp)),
                        elderName = elder?.fullName ?: "Unknown",
                        age = elder?.age?.toString() ?: ""
                    )
                }
                
                displayLogs.forEach { log ->
                    MiniActivityItem(log, fontScale, onClick = { onLogClick(log) })
                }
            }

            Spacer(modifier = Modifier.weight(1f))
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
    val context = LocalContext.current
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
                        title = stringResource(R.string.Age),
                        value = log?.age ?: "",
                        icon = Icons.Default.Cake,
                        color = PrimaryTeal,
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
                            text = "LOCATION COORDINATES",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.caregiverScaledSp(fontScale),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                log?.location?.let { coordinates ->
                                    try {
                                        val gmmIntentUri = "geo:0,0?q=$coordinates".toUri()
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        context.startActivity(mapIntent)
                                    } catch (_: Exception) {
                                        val browserIntent = Intent(Intent.ACTION_VIEW, "https://www.google.com/maps/search/?api=1&query=$coordinates".toUri())
                                        context.startActivity(browserIntent)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = log?.location ?: "No Coordinates Available",
                                color = PrimaryTeal,
                                fontSize = 16.caregiverScaledSp(fontScale),
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tap coordinates to view on Google Maps",
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 10.caregiverScaledSp(fontScale),
                            fontWeight = FontWeight.Medium
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
fun CallListContent(@Suppress("UNUSED_PARAMETER") managedElders: List<User>, fontScale: Float, onBack: () -> Unit, onCallElder: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 25.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = stringResource(R.string.call_members), color = Color.White, fontSize = 24.caregiverScaledSp(fontScale), fontWeight = FontWeight.Bold)
        }

        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp),
            color = Color(0xFFFF4B4B).copy(alpha = 0.1f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFFF4B4B).copy(alpha = 0.3f))
        ) {
            Row(modifier = Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(45.dp).clip(CircleShape).background(Color(0xFFFF4B4B).copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Emergency, contentDescription = null, tint = Color(0xFFFF4B4B))
                }
                Spacer(modifier = Modifier.width(15.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Emergency Services", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.caregiverScaledSp(fontScale))
                    Text(text = "Dial 911 for immediate help", color = Color(0xFFFF4B4B), fontSize = 12.caregiverScaledSp(fontScale))
                }
                IconButton(
                    onClick = { 
                        onCallElder("911") // Reusing the callback to trigger the dialer
                    },
                    modifier = Modifier.background(Color(0xFFFF4B4B), CircleShape)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call 911", tint = Color.White)
                }
            }
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
                            Text(text = elder.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.caregiverScaledSp(fontScale))
                            Text(text = if (elder.isEmergency) "Emergency" else "Safe", color = if(!elder.isEmergency) PrimaryTeal else Color.Red, fontSize = 12.caregiverScaledSp(fontScale))
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
    managedElders: List<User>,
    fontScale: Float,
    onBack: () -> Unit,
    onAddElder: () -> Unit,
    onRemoveElder: (String) -> Unit,
    onSeeLogs: (User) -> Unit,
    onSeeProfile: (User) -> Unit
) {
    var isEditMode by remember { mutableStateOf(false) }
    val elderToRemoveState = remember { mutableStateOf<User?>(null) }
    val elderToRemove = elderToRemoveState.value

    if (elderToRemove != null) {
        AlertDialog(
            onDismissRequest = { elderToRemoveState.value = null },
            title = { Text(stringResource(R.string.remove_member_title)) },
            text = { Text(stringResource(R.string.remove_member_msg, elderToRemove.fullName)) },
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
    elder: User,
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
                    Text(text = elder.fullName.take(1), color = PrimaryTeal, fontSize = 24.caregiverScaledSp(fontScale), fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(15.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = elder.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.caregiverScaledSp(fontScale))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if(!elder.isEmergency) PrimaryTeal else Color.Red))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if(!elder.isEmergency) "Safe" else "Emergency", color = Color.White.copy(alpha = 0.6f), fontSize = 12.caregiverScaledSp(fontScale))
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
fun ElderProfileContent(
    elder: User?,
    fontScale: Float,
    onBack: () -> Unit,
    onCallElder: (String) -> Unit,
    onUpdateCheckIn: (List<String>, String) -> Unit,
    onUpdateEmergencyContacts: (List<String>) -> Unit
) {
    if (elder == null) return
    val scrollState = rememberScrollState()
    
    var showCheckInSettings by remember { mutableStateOf(false) }
    var showEmergencySettings by remember { mutableStateOf(false) }

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
                Text(text = elder.fullName, color = Color.White, fontSize = 24.caregiverScaledSp(fontScale), fontWeight = FontWeight.Black)
                Text(text = "ID: ${elder.id}", color = PrimaryTeal, fontSize = 12.caregiverScaledSp(fontScale), fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(30.dp))
                
                // Action Buttons for Check-In and Contacts
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { showCheckInSettings = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.EventRepeat, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Check-In", fontSize = 12.caregiverScaledSp(fontScale), color = Color.White)
                    }
                    Button(
                        onClick = { showEmergencySettings = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Emergency, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Contacts", fontSize = 12.caregiverScaledSp(fontScale), color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
                
                CaregiverDetailSection(label = stringResource(R.string.Age), value = elder.age.toString(), icon = Icons.Default.Cake, fontScale = fontScale)
                Spacer(modifier = Modifier.height(20.dp))
                CaregiverDetailSection(label = stringResource(R.string.address), value = elder.address, icon = Icons.Default.LocationOn, fontScale = fontScale)
                Spacer(modifier = Modifier.height(20.dp))
                CaregiverDetailSection(label = stringResource(R.string.phone_number), value = elder.phoneNumber, icon = Icons.Default.Phone, fontScale = fontScale)
                
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

    if (showCheckInSettings) {
        CheckInSettingsDialog(
            currentDays = emptyList(), // Needs migration to UserSettings
            currentTime = "", // Needs migration to UserSettings
            fontScale = fontScale,
            onDismiss = { showCheckInSettings = false },
            onSave = { days, time ->
                onUpdateCheckIn(days, time)
                showCheckInSettings = false
            }
        )
    }

    if (showEmergencySettings) {
        EmergencyContactsDialog(
            currentContacts = emptyList(), // Needs migration to EmergencyContact
            fontScale = fontScale,
            onDismiss = { showEmergencySettings = false },
            onSave = { contacts ->
                onUpdateEmergencyContacts(contacts)
                showEmergencySettings = false
            }
        )
    }
}

@Composable
fun CheckInSettingsDialog(
    currentDays: List<String>,
    currentTime: String,
    fontScale: Float,
    onDismiss: () -> Unit,
    onSave: (List<String>, String) -> Unit
) {
    var selectedDays by remember { mutableStateOf(currentDays) }
    var selectedTime by remember { mutableStateOf(currentTime) }
    var customTime by remember { mutableStateOf("") }
    
    val daysList = listOf("Monday", "Wednesday", "Friday", "Sunday")
    val timePresets = listOf("Morning", "Afternoon", "Evening")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Safety Check-In", color = Color.Black, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Schedule Days", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = { selectedDays = if (selectedDays.size == 7) emptyList() else listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday") },
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedDays.size == 7) PrimaryTeal else Color.LightGray),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text("Everyday", fontSize = 10.sp)
                    }
                }
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    daysList.forEach { day ->
                        FilterChip(
                            selected = selectedDays.contains(day),
                            onClick = {
                                selectedDays = if (selectedDays.contains(day)) selectedDays - day else selectedDays + day
                            },
                            label = { Text(day, fontSize = 10.sp) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Time", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Column {
                    timePresets.forEach { time ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedTime == time, onClick = { selectedTime = time })
                            Text(time)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !timePresets.contains(selectedTime) && selectedTime.isNotEmpty(), onClick = { selectedTime = customTime })
                        OutlinedTextField(
                            value = customTime,
                            onValueChange = { 
                                customTime = it
                                selectedTime = it
                            },
                            placeholder = { Text("Custom Time (e.g. 10:00 AM)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(selectedDays, selectedTime) }) {
                Text("Save", color = PrimaryTeal, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun EmergencyContactsDialog(
    currentContacts: List<String>,
    fontScale: Float,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var contacts by remember { mutableStateOf(currentContacts.ifEmpty { listOf("") }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Emergency Contacts", color = Color.Black, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Add several contacts (Family, Barangay, etc.)", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                contacts.forEachIndexed { index, contact ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        OutlinedTextField(
                            value = contact,
                            onValueChange = { newValue ->
                                contacts = contacts.toMutableList().also { it[index] = newValue }
                            },
                            placeholder = { Text("Phone Number") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        IconButton(onClick = {
                            if (contacts.size > 1) contacts = contacts.toMutableList().also { it.removeAt(index) }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                        }
                    }
                }
                TextButton(onClick = { contacts = contacts + "" }) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryTeal)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Contact", color = PrimaryTeal)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(contacts.filter { it.isNotEmpty() }) }) {
                Text("Save", color = PrimaryTeal, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
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
                Text(text = log.location, color = Color.White.copy(alpha = 0.5f), fontSize = 11.caregiverScaledSp(fontScale))
            }
            Text(text = log.time, color = Color.White.copy(alpha = 0.3f), fontSize = 11.caregiverScaledSp(fontScale))
        }
    }
}

@Composable
fun ActivityLogsContent(
    title: String,
    specificElderName: String? = null,
    managedElders: List<User> = emptyList(),
    activityLogs: List<ActivityLog> = emptyList(),
    fontScale: Float,
    onBack: () -> Unit,
    onLogClick: (LogData) -> Unit
) {
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val sdf = remember(locale) { SimpleDateFormat("HH:mm", locale) }

    val logs = if (activityLogs.isNotEmpty()) {
        val filteredLogs = if (specificElderName != null) {
            val elderId = managedElders.find { it.fullName == specificElderName }?.id
            activityLogs.filter { it.userId == elderId }
        } else {
            activityLogs
        }

        filteredLogs.map { log ->
            val color = when(log.type) {
                "SOS", "FALL" -> Color.Red
                "DAILY_CHECK_MISSED" -> Color(0xFFFFB800)
                else -> Color(0xFF00C49A)
            }
            val elder = managedElders.find { it.id == log.userId }
            LogData(
                color = color,
                title = log.type.replace("_", " "),
                location = if (log.latitude != null) "${log.latitude}, ${log.longitude}" else "Last Known Loc",
                time = sdf.format(Date(log.timestamp)),
                elderName = elder?.fullName ?: "Unknown",
                age = elder?.age?.toString() ?: ""
            )
        }
    } else {
        emptyList()
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
                Text(text = log.location, color = Color.White.copy(alpha = 0.6f), fontSize = 13.caregiverScaledSp(fontScale))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PrimaryTeal.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun SettingsContent(
    name: String,
    @Suppress("UNUSED_PARAMETER") managedElders: List<User>,
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
fun SleekInputFieldWhite(
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
        placeholder = { Text(text = placeholder, color = Color.White.copy(alpha = 0.3f)) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = PrimaryTeal) },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
