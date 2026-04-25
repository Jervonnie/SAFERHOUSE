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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saferhouseui.ui.theme.DarkBackground
import com.example.saferhouseui.ui.theme.PrimaryTeal
import kotlinx.coroutines.launch

@Composable
fun ElderlyDashboardScreen(
    elderName: String,
    caretakerName: String,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("dashboard") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ElderlyDrawerContent(
                        currentScreen = currentScreen,
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
                            "dashboard" -> ElderlyDashboardContent(elderName)
                            "profile" -> ElderlyProfileContent(elderName, caretakerName) { currentScreen = "dashboard" }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ElderlyDashboardContent(elderName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ELDER MONITORING",
                color = PrimaryTeal.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Text(
                text = "Welcome, $elderName",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Status Pill - Slightly smaller but still clear
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
                    text = "System Secure",
                    color = Color(0xFF1A1A1A),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Emergency Button - Scaled down to "Just Right"
        EmergencyButtonRefined()

        // Helper Text
        Text(
            text = "Press the button if you need help",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 50.dp)
        )
        
        // System Status Indicator
        Row(verticalAlignment = Alignment.CenterVertically) {
            PulseIndicator(color = PrimaryTeal)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "MONITORING ACTIVE",
                color = PrimaryTeal.copy(alpha = 0.4f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun ElderlyProfileContent(
    name: String,
    caretaker: String,
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
            Text(text = "Profile", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

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
                // Modernized Avatar
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(PrimaryTeal.copy(alpha = 0.1f))
                        .border(2.dp, PrimaryTeal.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(50.dp))
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(text = name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text(text = "SECURED MEMBER", color = PrimaryTeal, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)

                Spacer(modifier = Modifier.height(30.dp))

                Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    ProfileDetailItem("GUARDIAN", caretaker, Icons.Default.Shield)
                    ProfileDetailItem("EMERGENCY", "911 / Primary", Icons.Default.Emergency)
                    ProfileDetailItem("SYSTEM", "Active / 88%", Icons.Default.Bolt)
                }
                
                Spacer(modifier = Modifier.height(30.dp))
                
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.5f))
                ) {
                    Text("DONE", color = PrimaryTeal, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String, icon: ImageVector) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmergencyButtonRefined() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer Glow
        Box(
            modifier = Modifier
                .size(230.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Color(0xFFFF1744).copy(alpha = 0.1f))
        )
        
        Surface(
            modifier = Modifier
                .size(180.dp)
                .clickable { /* Trigger SOS */ },
            color = Color(0xFFFF1744),
            shape = CircleShape,
            shadowElevation = 15.dp,
            border = BorderStroke(4.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "HELP",
                    color = Color.White,
                    fontSize = 32.sp,
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
            text = "SaferHouse",
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
                    text = "MENU",
                    color = PrimaryTeal,
                    fontSize = 24.sp,
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
                DrawerMenuItem("Dashboard", Icons.Default.Dashboard, currentScreen == "dashboard") { onNavigate("dashboard") }
                DrawerMenuItem("Profile", Icons.Default.Person, currentScreen == "profile") { onNavigate("profile") }
                DrawerMenuItem("History", Icons.Default.History, false) { /* History */ }
                DrawerMenuItem("Settings", Icons.Default.Settings, false) { /* Settings */ }
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
                            text = "LOGOUT",
                            color = Color(0xFFFF1744),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "End session",
                            color = Color.Gray,
                            fontSize = 11.sp
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
    val scale by animateFloatAsState(if (isSelected) 1.02f else 1f, label = "scale")
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .scale(scale)
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
                fontSize = 20.sp,
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
