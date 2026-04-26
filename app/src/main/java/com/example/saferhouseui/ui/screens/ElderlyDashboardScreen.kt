package com.example.saferhouseui.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.saferhouseui.R
import com.example.saferhouseui.ui.theme.DarkBackground
import com.example.saferhouseui.ui.theme.PrimaryTeal
import kotlinx.coroutines.launch

@Composable
fun ElderlyDashboardScreen(
    elderName: String,
    elderAddress: String,
    elderContact: String,
    caretakerName: String,
    currentLanguage: String,
    currentFontSize: String,
    onLanguageChange: (String) -> Unit,
    onFontSizeChange: (String) -> Unit,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("dashboard") }

    // Font Scaling Logic
    val fontScale = when (currentFontSize) {
        "Small" -> 0.85f
        "Large" -> 1.25f
        else -> 1.0f // Medium
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ElderlyDrawerContent(
                        currentScreen = currentScreen,
                        fontScale = fontScale,
                        onNavigate = { screen ->
                            currentScreen = screen
                            scope.launch { drawerState.close() }
                        },
                        onLogout = onLogout,
                        onClose = { scope.launch { drawerState.close() } }
                    )
                }
            },
            gesturesEnabled = true
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    containerColor = DarkBackground,
                    topBar = {
                        ElderlyTopBar(
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                    }
                ) { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        when (currentScreen) {
                            "dashboard" -> ElderlyDashboardContent(elderName, fontScale)
                            "profile" -> ElderlyProfileContent(
                                name = elderName,
                                address = elderAddress,
                                contact = elderContact,
                                caretaker = caretakerName,
                                fontScale = fontScale,
                                onBack = { currentScreen = "dashboard" }
                            )
                            "settings" -> ElderlySettingsContent(
                                currentLanguage = currentLanguage,
                                onLanguageChange = onLanguageChange,
                                currentFontSize = currentFontSize,
                                onFontSizeChange = onFontSizeChange,
                                fontScale = fontScale,
                                onBack = { currentScreen = "dashboard" }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper to scale SP
@Composable
fun Int.scaledSp(scale: Float): TextUnit = (this * scale).sp

@Composable
fun ElderlyDashboardContent(elderName: String, fontScale: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.fall_detection).uppercase(),
                color = PrimaryTeal.copy(alpha = 0.5f),
                fontSize = 12.scaledSp(fontScale),
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Text(
                text = stringResource(R.string.welcome, elderName),
                color = Color.White,
                fontSize = 28.scaledSp(fontScale),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        // Status Pill
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(50.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(PrimaryTeal)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.status_active),
                    color = Color(0xFF1A1A1A),
                    fontSize = 18.scaledSp(fontScale),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Emergency Button
        var isEmergencyActive by remember { mutableStateOf(false) }
        EmergencyButtonRefined(
            isActive = isEmergencyActive,
            fontScale = fontScale,
            onClick = { isEmergencyActive = !isEmergencyActive }
        )

        // Helper Text
        Text(
            text = if (isEmergencyActive) stringResource(R.string.emergency_alert_sent) else stringResource(R.string.sos_button),
            color = if (isEmergencyActive) Color(0xFFFF1744) else Color.White.copy(alpha = 0.7f),
            fontSize = 20.scaledSp(fontScale),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
        
        // System Status Indicator
        Row(verticalAlignment = Alignment.CenterVertically) {
            PulseIndicator(color = PrimaryTeal)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.monitoring_active),
                color = PrimaryTeal.copy(alpha = 0.4f),
                fontSize = 14.scaledSp(fontScale),
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun ElderlyProfileContent(
    name: String,
    address: String,
    contact: String,
    caretaker: String,
    fontScale: Float,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = stringResource(R.string.profile), color = Color.White, fontSize = 24.scaledSp(fontScale), fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            item {
                ProfileCard(
                    title = stringResource(R.string.elder_info).uppercase(),
                    name = name,
                    address = address,
                    contact = contact,
                    idLabel = stringResource(R.string.elder_id),
                    idValue = "E-000123",
                    fontScale = fontScale
                )
            }
            
            item {
                ProfileCard(
                    title = stringResource(R.string.caretaker_info).uppercase(),
                    name = caretaker,
                    address = "123 Caretaker Lane, QC",
                    contact = "0987-654-3210",
                    idLabel = stringResource(R.string.caretaker_id),
                    idValue = "C-000456",
                    fontScale = fontScale
                )
            }

            item {
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.5f))
                ) {
                    Text(stringResource(R.string.done), color = PrimaryTeal, fontWeight = FontWeight.Bold, fontSize = 16.scaledSp(fontScale))
                }
            }
        }
    }
}

@Composable
fun ProfileCard(
    title: String,
    name: String,
    address: String,
    contact: String,
    idLabel: String,
    idValue: String,
    fontScale: Float
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(PrimaryTeal.copy(alpha = 0.1f))
                    .border(2.dp, PrimaryTeal.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(45.dp))
            }
            
            Spacer(modifier = Modifier.height(15.dp))
            
            Text(text = name, color = Color.White, fontSize = 24.scaledSp(fontScale), fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(text = title, color = PrimaryTeal, fontSize = 11.scaledSp(fontScale), fontWeight = FontWeight.Black, letterSpacing = 2.sp)

            Spacer(modifier = Modifier.height(25.dp))

            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                ProfileDetailItem(stringResource(R.string.address).uppercase(), address, Icons.Default.Home, fontScale)
                ProfileDetailItem(stringResource(R.string.contact).uppercase(), contact, Icons.Default.Phone, fontScale)
                ProfileDetailItem(idLabel, idValue, Icons.Default.Badge, fontScale)
            }
        }
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String, icon: ImageVector, fontScale: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = Color.White.copy(alpha = 0.4f), fontSize = 12.scaledSp(fontScale), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = value, color = Color.White, fontSize = 20.scaledSp(fontScale), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmergencyButtonRefined(
    isActive: Boolean = false,
    fontScale: Float,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActive) 600 else 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(if (isActive) Color(0xFFFF1744).copy(alpha = 0.3f) else Color(0xFFFF1744).copy(alpha = 0.1f))
        )
        
        Surface(
            modifier = Modifier
                .size(190.dp)
                .clickable { onClick() },
            color = if (isActive) Color.White else Color(0xFFFF1744),
            shape = CircleShape,
            shadowElevation = 15.dp,
            border = BorderStroke(4.dp, if (isActive) Color(0xFFFF1744) else Color.White.copy(alpha = 0.2f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (isActive) stringResource(R.string.sos) else stringResource(R.string.help),
                    color = if (isActive) Color(0xFFFF1744) else Color.White,
                    fontSize = 36.scaledSp(fontScale),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
fun ElderlyTopBar(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 25.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.dashboard_title),
            color = PrimaryTeal,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-1).sp
        )
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(50.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun ElderlyDrawerContent(
    currentScreen: String,
    fontScale: Float,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color(0xFFF8F9FA),
        drawerShape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp),
        modifier = Modifier.fillMaxHeight().width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp, bottom = 30.dp, start = 25.dp, end = 25.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.menu),
                    color = PrimaryTeal,
                    fontSize = 24.scaledSp(fontScale),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DrawerMenuItem(stringResource(R.string.elder_home), Icons.Default.Dashboard, currentScreen == "dashboard", fontScale) { onNavigate("dashboard") }
                DrawerMenuItem(stringResource(R.string.profile), Icons.Default.Person, currentScreen == "profile", fontScale) { onNavigate("profile") }
                DrawerMenuItem(stringResource(R.string.settings), Icons.Default.Settings, currentScreen == "settings", fontScale) { onNavigate("settings") }
            }
            
            Spacer(modifier = Modifier.weight(1f))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(15.dp))
                    .clickable { onLogout() }
                    .background(Color(0xFFFF1744).copy(alpha = 0.05f))
                    .border(1.dp, Color(0xFFFF1744).copy(alpha = 0.1f), RoundedCornerShape(15.dp))
                    .padding(15.dp),
                color = Color.Transparent
            ) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.logout).uppercase(),
                            color = Color(0xFFFF1744),
                            fontSize = 18.scaledSp(fontScale),
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(15.dp))
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFFF1744), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    fontScale: Float,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) PrimaryTeal.copy(alpha = 0.15f) else Color.Transparent,
        label = "bg"
    )
    val contentColor by animateColorAsState(
        if (isSelected) PrimaryTeal else Color.Black.copy(alpha = 0.7f),
        label = "content"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = contentColor,
                fontSize = 20.scaledSp(fontScale),
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(18.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun PulseIndicator(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
            .border(2.dp, color, CircleShape)
    )
}

@Composable
fun ElderlySettingsContent(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    currentFontSize: String,
    onFontSizeChange: (String) -> Unit,
    fontScale: Float,
    onBack: () -> Unit
) {
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }
    var selectedFontSize by remember { mutableStateOf(currentFontSize) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 25.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryTeal, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = stringResource(R.string.settings), color = Color.White, fontSize = 24.scaledSp(fontScale), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Language Section
        Text(text = stringResource(R.string.language).uppercase(), color = PrimaryTeal, fontSize = 12.scaledSp(fontScale), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(15.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SelectionButton(
                label = "English",
                isSelected = selectedLanguage == "en",
                fontScale = fontScale,
                modifier = Modifier.weight(1f),
                onClick = { selectedLanguage = "en" }
            )
            SelectionButton(
                label = "Tagalog",
                isSelected = selectedLanguage == "tl",
                fontScale = fontScale,
                modifier = Modifier.weight(1f),
                onClick = { selectedLanguage = "tl" }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Font Size Section
        Text(text = stringResource(R.string.font_size).uppercase(), color = PrimaryTeal, fontSize = 12.scaledSp(fontScale), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(15.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SelectionButton(
                label = "Small",
                isSelected = selectedFontSize == "Small",
                fontScale = fontScale,
                modifier = Modifier.fillMaxWidth(),
                onClick = { selectedFontSize = "Small" }
            )
            SelectionButton(
                label = "Medium",
                isSelected = selectedFontSize == "Medium",
                fontScale = fontScale,
                modifier = Modifier.fillMaxWidth(),
                onClick = { selectedFontSize = "Medium" }
            )
            SelectionButton(
                label = "Large",
                isSelected = selectedFontSize == "Large",
                fontScale = fontScale,
                modifier = Modifier.fillMaxWidth(),
                onClick = { selectedFontSize = "Large" }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = {
                if (selectedLanguage != currentLanguage) {
                    onLanguageChange(selectedLanguage)
                }
                if (selectedFontSize != currentFontSize) {
                    onFontSizeChange(selectedFontSize)
                }
                onBack()
            },
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(bottom = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
            shape = RoundedCornerShape(15.dp)
        ) {
            Text(stringResource(R.string.done), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.scaledSp(fontScale))
        }
    }
}

@Composable
fun SelectionButton(
    label: String,
    isSelected: Boolean,
    fontScale: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = if (isSelected) PrimaryTeal else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(2.dp, if (isSelected) PrimaryTeal else Color.White.copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                fontSize = 20.scaledSp(fontScale),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
