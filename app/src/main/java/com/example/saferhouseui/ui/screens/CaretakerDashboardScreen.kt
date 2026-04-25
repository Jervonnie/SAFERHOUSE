package com.example.saferhouseui.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.saferhouseui.ElderlyMember
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
fun CaretakerDashboardScreen(
    caretakerName: String,
    caretakerAddress: String,
    caretakerContact: String,
    managedElders: List<ElderlyMember>,
    onUpdateProfile: (String, String, String) -> Unit,
    onAddElder: (String) -> Unit,
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf("dashboard") }
    var selectedElderForLogs by remember { mutableStateOf<ElderlyMember?>(null) }
    var selectedLog by remember { mutableStateOf<LogData?>(null) }
    var logBackDestination by remember { mutableStateOf("dashboard") }
    var selectedLanguage by remember { mutableStateOf("English") }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        when (currentScreen) {
            "dashboard" -> DashboardContent(
                name = caretakerName,
                address = caretakerAddress,
                managedElders = managedElders,
                onNavigateToLogs = { 
                    logBackDestination = "dashboard"
                    currentScreen = "logs" 
                },
                onNavigateToSettings = { currentScreen = "settings" },
                onNavigateToCallList = { currentScreen = "call_list" },
                onNavigateToManagement = { currentScreen = "elder_management" },
                onNavigateToEditProfile = { currentScreen = "edit_profile" },
                onLogClick = { log ->
                    selectedLog = log
                    logBackDestination = "dashboard"
                    currentScreen = "log_detail"
                }
            )
            "edit_profile" -> EditCaretakerProfileContent(
                initialName = caretakerName,
                initialAddress = caretakerAddress,
                initialContact = caretakerContact,
                onBack = { currentScreen = "dashboard" },
                onSave = { name, address, contact ->
                    onUpdateProfile(name, address, contact)
                    currentScreen = "dashboard"
                }
            )
            "logs" -> ActivityLogsContent(
                title = "Global Activity",
                managedElders = managedElders,
                onBack = { currentScreen = "dashboard" },
                onLogClick = { log ->
                    selectedLog = log
                    logBackDestination = "logs"
                    currentScreen = "log_detail"
                }
            )
            "call_list" -> CallListContent(
                managedElders = managedElders,
                onBack = { currentScreen = "dashboard" }
            )
            "elder_management" -> ElderManagementContent(
                managedElders = managedElders,
                onBack = { currentScreen = "dashboard" },
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
            "elder_profile" -> ElderProfileContent(
                elder = selectedElderForLogs,
                onBack = { currentScreen = "elder_management" }
            )
            "specific_logs" -> ActivityLogsContent(
                title = "${selectedElderForLogs?.name}'s History",
                specificElderName = selectedElderForLogs?.name,
                onBack = { currentScreen = "elder_management" },
                onLogClick = { log ->
                    selectedLog = log
                    logBackDestination = "specific_logs"
                    currentScreen = "log_detail"
                }
            )
            "log_detail" -> LogDetailContent(
                log = selectedLog,
                onBack = { currentScreen = logBackDestination }
            )
            "settings" -> SettingsContent(
                name = caretakerName,
                managedElders = managedElders,
                onAddElder = onAddElder,
                onBack = { currentScreen = "dashboard" },
                onLogout = onLogout,
                selectedLanguage = selectedLanguage,
                onNavigateToLanguage = { currentScreen = "language_selection" }
            )
            "language_selection" -> LanguageSelectionContent(
                currentLanguage = selectedLanguage,
                onLanguageSelected = { 
                    selectedLanguage = it
                    currentScreen = "settings"
                },
                onBack = { currentScreen = "settings" }
            )
        }
    }
}

@Composable
fun DashboardContent(
    name: String,
    address: String,
    managedElders: List<ElderlyMember>,
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
                        text = "SaferHouse",
                        color = PrimaryTeal,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "SYSTEM ONLINE",
                        color = PrimaryTeal.copy(alpha = 0.5f),
                        fontSize = 10.sp,
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
                        Icon(
                            Icons.Default.Person, 
                            contentDescription = null, 
                            tint = Color.Gray, 
                            modifier = Modifier.fillMaxSize().padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name, 
                            color = Color.Black, 
                            fontWeight = FontWeight.Black, 
                            fontSize = 20.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(text = "CARETAKER", color = PrimaryTeal, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                        Text(
                            text = address, 
                            color = Color.Gray, 
                            fontSize = 12.sp, 
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
                    title = "Call Member",
                    subtitle = "${managedElders.size} Connected",
                    icon = Icons.Default.Call,
                    color = PrimaryTeal,
                    onClick = onNavigateToCallList
                )
                DashboardActionButton(
                    modifier = Modifier.weight(1f),
                    title = "Manage Elders",
                    subtitle = "Profiles & Logs",
                    icon = Icons.Default.People,
                    color = Color(0xFFFFB800),
                    onClick = onNavigateToManagement
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Global Activity", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "See All", 
                    color = PrimaryTeal, 
                    fontSize = 13.sp, 
                    modifier = Modifier.clickable { onNavigateToLogs() }
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val log1Name = managedElders.getOrNull(0)?.name ?: "Lolo Mao"
                val log2Name = managedElders.getOrNull(1)?.name ?: "Lola Maria"
                val demoLogs = listOf(
                    LogData(Color(0xFF00C49A), "Safe Check", "$log1Name confirmed check-in", "Now", log1Name),
                    LogData(Color(0xFFFFB800), "Geo-Fence", "$log2Name left Safe Zone", "12m ago", log2Name)
                )
                demoLogs.forEach { log ->
                    MiniActivityItem(log, onClick = { onLogClick(log) })
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "SYSTEM STATUS: SECURED",
                color = PrimaryTeal.copy(alpha = 0.3f),
                fontSize = 12.sp,
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
            Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        }
    }
}

@Composable
fun LogDetailContent(
    log: LogData?,
    onBack: () -> Unit
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
                text = "LOG ANALYSIS",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 14.sp,
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
                // Header Icon
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
                            log?.title?.contains("Safe", ignoreCase = true) == true -> Icons.Default.GpsFixed
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
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )
                Text(
                    text = "EVENT ID: #SH-${(1000..9999).random()}",
                    color = PrimaryTeal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Detailed Info Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IndustrialLogTile(
                        modifier = Modifier.weight(1f),
                        title = "TIMESTAMP",
                        value = log?.time ?: "00:00",
                        icon = Icons.Default.AccessTime,
                        compact = true
                    )
                    IndustrialLogTile(
                        modifier = Modifier.weight(1f),
                        title = "STATUS",
                        value = if (log?.color == Color.Red) "URGENT" else "RESOLVED",
                        icon = Icons.Default.Info,
                        color = log?.color ?: PrimaryTeal,
                        compact = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                IndustrialLogTile(
                    modifier = Modifier.fillMaxWidth(),
                    title = "AFFECTED MEMBER",
                    value = log?.elderName ?: "Unknown Member",
                    icon = Icons.Default.Person,
                    compact = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // System Description Area (The "Rounded Bar" style)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = PrimaryTeal.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYSTEM DESCRIPTION",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = log?.msg ?: "System detected activity at recorded timestamp.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))

                // Actions
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, PrimaryTeal)
                ) {
                    Text(
                        text = "ACKNOWLEDGE & DISMISS",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
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
                    fontSize = if (compact) 9.sp else 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(if (compact) 6.dp else 12.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = if (alignment == Alignment.Start) TextAlign.Start else TextAlign.Center,
                lineHeight = if (compact) 18.sp else 20.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CallListContent(managedElders: List<ElderlyMember>, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 25.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = "Call Members", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
                            Text(text = elder.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = elder.status, color = if(elder.status == "Safe") PrimaryTeal else Color.Red, fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = { /* Trigger Call */ },
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
    onBack: () -> Unit,
    onSeeLogs: (ElderlyMember) -> Unit,
    onSeeProfile: (ElderlyMember) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 25.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = "Managed Members", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            items(managedElders) { elder ->
                ArtisticElderCard(
                    elder = elder,
                    onSeeLogs = { onSeeLogs(elder) },
                    onSeeProfile = { onSeeProfile(elder) }
                )
            }
        }
    }
}

@Composable
fun ArtisticElderCard(
    elder: ElderlyMember,
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
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(PrimaryTeal.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = elder.name.take(1),
                        color = PrimaryTeal,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(15.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = elder.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if(elder.status == "Safe") PrimaryTeal else Color.Red))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = elder.status, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "${elder.batteryLevel}%", color = if(elder.batteryLevel < 20) Color.Red else PrimaryTeal, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "BATTERY", color = Color.White.copy(alpha = 0.3f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
                    Text("PROFILE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Button(
                    onClick = onSeeLogs,
                    modifier = Modifier.weight(1f).height(45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ACTIVITY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ElderProfileContent(elder: ElderlyMember?, onBack: () -> Unit) {
    if (elder == null) return
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 25.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = "Member Profile", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(topStart = 80.dp, bottomEnd = 80.dp, topEnd = 20.dp, bottomStart = 20.dp),
            border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(PrimaryTeal.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(50.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = elder.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(text = "ID: ${elder.id}", color = PrimaryTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(40.dp))
                
                DetailSection(label = "PHONE NUMBER", value = elder.phoneNumber, icon = Icons.Default.Phone)
                Spacer(modifier = Modifier.height(20.dp))
                DetailSection(label = "CURRENT STATUS", value = elder.status, icon = Icons.Default.Info)
            }
        }
    }
}

@Composable
fun DetailSection(label: String, value: String, icon: ImageVector) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = PrimaryTeal.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
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
                Text(text = value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun MiniActivityItem(log: LogData, onClick: () -> Unit) {
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
                Text(text = log.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = log.msg, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            }
            Text(text = log.time, color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
        }
    }
}

@Composable
fun ActivityLogsContent(
    title: String,
    specificElderName: String? = null,
    managedElders: List<ElderlyMember> = emptyList(),
    onBack: () -> Unit,
    onLogClick: (LogData) -> Unit
) {
    val logs = if (specificElderName != null) {
        // Generate logs specifically for this elder
        listOf(
            LogData(PrimaryTeal, "Fall Detected", "$specificElderName - Living Room Alert", "14:02", specificElderName),
            LogData(Color(0xFFFFB800), "Safe Zone Exit", "$specificElderName - Front Door", "12:45", specificElderName),
            LogData(PrimaryTeal, "Check-in", "$specificElderName confirmed routine", "08:30", specificElderName),
            LogData(PrimaryTeal, "Medicine taken", "$specificElderName - Medication Log", "07:00", specificElderName)
        )
    } else {
        // Global logs for the dashboard/general logs view
        val name1 = managedElders.getOrNull(0)?.name ?: "Lolo Mao"
        val name2 = managedElders.getOrNull(1)?.name ?: "Lola Maria"
        listOf(
            LogData(PrimaryTeal, "Fall Detected", "$name1 - Living Room Alert", "14:02", name1),
            LogData(Color(0xFFFFB800), "Safe Zone Exit", "$name2 - Front Door", "12:45", name2),
            LogData(PrimaryTeal, "Check-in", "$name1 confirmed routine", "08:30", name1),
            LogData(PrimaryTeal, "Medicine taken", "$name2 - Vitamins", "07:00", name2)
        )
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 25.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            item { Text("RECENT HISTORY", color = PrimaryTeal, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp) }
            items(logs) { log ->
                ModernLogTile(log, onClick = { onLogClick(log) })
            }
        }
    }
}

@Composable
fun ModernLogTile(log: LogData, onClick: () -> Unit) {
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
                Text(text = log.time, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                Text(text = log.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = log.msg, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PrimaryTeal.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun SettingsContent(
    name: String,
    managedElders: List<ElderlyMember>,
    onAddElder: (String) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    selectedLanguage: String,
    onNavigateToLanguage: () -> Unit
) {
    var showAddElderDialog by remember { mutableStateOf(false) }
    var newElderName by remember { mutableStateOf("") }
    var newElderPhone by remember { mutableStateOf("") }

    if (showAddElderDialog) {
        Dialog(
            onDismissRequest = { showAddElderDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .padding(horizontal = 35.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SaferHouse",
                        color = PrimaryTeal,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "Elder Registration",
                        color = Color.Black.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(50.dp))

                    Text(
                        text = "New Member",
                        color = Color.Black,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(35.dp))

                    SleekInputField(
                        value = newElderName,
                        onValueChange = { newElderName = it },
                        placeholder = "Full Name",
                        icon = Icons.Default.Badge
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    SleekInputField(
                        value = newElderPhone,
                        onValueChange = { newElderPhone = it },
                        placeholder = "Contact Number",
                        icon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (newElderName.isNotBlank()) {
                                onAddElder(newElderName)
                                showAddElderDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("FINISH SYNC", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(onClick = { showAddElderDialog = false }) {
                        Text("Cancel Registration", color = Color.Gray)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 25.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = "Settings", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Text(text = "SYSTEM PREFERENCES", color = PrimaryTeal, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(15.dp))
        
        SettingsTile(Icons.Default.Language, "Interface Language", selectedLanguage, onClick = onNavigateToLanguage)
        SettingsTile(Icons.Default.Person, "Account Identity", name)
        
        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "MANAGED ASSETS", color = PrimaryTeal, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            IconButton(
                onClick = { showAddElderDialog = true },
                modifier = Modifier.size(24.dp).background(PrimaryTeal.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = PrimaryTeal, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(15.dp))

        managedElders.forEach { elder ->
            MiniElderAsset(elder)
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Text("Logout", color = Color(0xFFFF4B4B), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MiniElderAsset(elder: ElderlyMember) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(35.dp).clip(RoundedCornerShape(10.dp)).background(PrimaryTeal.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Text(elder.name.take(1), color = PrimaryTeal, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Column {
                Text(text = elder.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "CONNECTED", color = PrimaryTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
    }
}

@Composable
fun LanguageSelectionContent(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 25.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = "Language", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        LanguageRadioTile("English", currentLanguage == "English", onClick = { onLanguageSelected("English") })
        Spacer(modifier = Modifier.height(15.dp))
        LanguageRadioTile("Tagalog", currentLanguage == "Tagalog", onClick = { onLanguageSelected("Tagalog") })
    }
}

@Composable
fun LanguageRadioTile(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = if (isSelected) PrimaryTeal.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, if (isSelected) PrimaryTeal else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryTeal, unselectedColor = Color.Gray)
            )
        }
    }
}

@Composable
fun SettingsTile(icon: ImageVector, title: String, value: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(15.dp))
        Column {
            Text(text = title, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            Text(text = value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun EditCaretakerProfileContent(
    initialName: String,
    initialAddress: String,
    initialContact: String,
    onBack: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var address by remember { mutableStateOf(initialAddress) }
    var contact by remember { mutableStateOf(initialContact) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 25.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal)
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = "Edit Profile", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(topStart = 80.dp, bottomEnd = 80.dp, topEnd = 20.dp, bottomStart = 20.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(PrimaryTeal.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(50.dp))
                }

                Spacer(modifier = Modifier.height(40.dp))

                SleekInputFieldWhite(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Full Name",
                    icon = Icons.Default.Badge
                )

                Spacer(modifier = Modifier.height(20.dp))

                SleekInputFieldWhite(
                    value = address,
                    onValueChange = { address = it },
                    placeholder = "Home Address",
                    icon = Icons.Default.Home
                )

                Spacer(modifier = Modifier.height(20.dp))

                SleekInputFieldWhite(
                    value = contact,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() }) {
                            contact = it
                        }
                    },
                    placeholder = "Contact Number",
                    icon = Icons.Default.Phone
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { onSave(name, address, contact) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("UPDATE IDENTITY", fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun SleekInputFieldWhite(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector
) {
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
