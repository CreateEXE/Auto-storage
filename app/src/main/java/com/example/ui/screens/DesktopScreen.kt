package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FileOrganizerViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.util.metallicPanel
import kotlin.math.roundToInt

enum class DesktopApp(val title: String, val icon: ImageVector, val color: Color) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, CyberCyan),
    FILE_EXPLORER("Files", Icons.Default.Folder, LaserEmerald),
    DUPLICATES("Duplicates", Icons.Default.ContentCopy, WarningAmber),
    RULES("Rules", Icons.AutoMirrored.Filled.Rule, SynthPurple),
    VAULT("Vault", Icons.Default.Lock, Color.White),
    TERMUX("Termux", Icons.Default.Terminal, ElectricBlue),
    SETTINGS("Settings", Icons.Default.Settings, TextSteelSecondary)
}

data class DesktopIcon(
    val app: DesktopApp,
    val position: Offset = Offset.Zero
)

@Composable
fun DesktopScreen(viewModel: FileOrganizerViewModel) {
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val desktopIcons = remember { mutableStateListOf<DesktopIcon>() }
    var openApp by remember { mutableStateOf<DesktopApp?>(null) }
    var isStartMenuOpen by remember { mutableStateOf(false) }

    val storageStats by viewModel.storageStats.collectAsStateWithLifecycle()
    val dashboardWidgets by viewModel.dashboardWidgets.collectAsStateWithLifecycle()
    val deviceDiagnostics by viewModel.deviceDiagnostics.collectAsStateWithLifecycle()
    val recentFiles by viewModel.recentFiles.collectAsStateWithLifecycle()
    val isVaultLocked by viewModel.isVaultLocked.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GunmetalBackground, Color(0xFF0F172A))
                )
            )
    ) {
        // Desktop Area
        Box(modifier = Modifier.fillMaxSize()) {
            desktopIcons.forEachIndexed { index, icon ->
                var offset by remember { mutableStateOf(icon.position) }
                
                DesktopIconView(
                    icon = icon,
                    offset = offset,
                    onPositionChange = { newOffset ->
                        offset = newOffset
                        desktopIcons[index] = icon.copy(position = newOffset)
                    },
                    onOpen = { openApp = icon.app }
                )
            }
        }

        // Widgets (Hybrid Android Desktop)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 24.dp)
                .width(320.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "SYSTEM WIDGETS",
                style = MaterialTheme.typography.labelSmall,
                color = CyberCyan.copy(alpha = 0.7f),
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            
            DeviceSpecsWidget(
                diagnostics = deviceDiagnostics,
                onOpenDiagnostics = { openApp = DesktopApp.DASHBOARD },
                onRemove = {}
            )
            
            VaultStatusWidget(
                isLocked = isVaultLocked,
                onClick = { viewModel.toggleVaultLock() },
                onRemove = {}
            )

            StorageInfoWidget(
                stats = storageStats,
                onRemove = {}
            )
        }

        // Taskbar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(56.dp),
            color = SteelSurface.copy(alpha = 0.95f),
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, SteelBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start Button
                IconButton(
                    onClick = { isStartMenuOpen = !isStartMenuOpen },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isStartMenuOpen) CyberCyan.copy(alpha = 0.2f) else Color.Transparent
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "Start",
                        tint = if (isStartMenuOpen) CyberCyan else TextSilver
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                VerticalDivider(modifier = Modifier.height(24.dp), color = SteelBorder)
                Spacer(modifier = Modifier.width(8.dp))

                // Running Apps (Taskbar Icons)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DesktopApp.entries.forEach { app ->
                        // Show in taskbar if it's open
                        if (openApp == app) {
                            TaskbarAppIcon(app = app, isActive = true) {
                                openApp = null // "Minimize"
                            }
                        }
                    }
                }

                // System Tray
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
                    Icon(
                        imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = "Network",
                        tint = if (isOnline) LaserEmerald else DangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                        color = TextSilver,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Start Menu Overlay
        if (isStartMenuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isStartMenuOpen = false }
            ) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 64.dp, start = 8.dp)
                        .width(280.dp)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(12.dp),
                    color = SteelSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SteelBorder),
                    tonalElevation = 12.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Applications",
                            style = MaterialTheme.typography.titleSmall,
                            color = CyberCyan,
                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                        )
                        
                        DesktopApp.entries.forEach { app ->
                            StartMenuItem(app) {
                                openApp = app
                                isStartMenuOpen = false
                                // If not on desktop, add it? (Optional behavior)
                                if (desktopIcons.none { it.app == app }) {
                                    desktopIcons.add(DesktopIcon(app, Offset(100f, 100f)))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Full Screen "App Window" Overlays
        AnimatedVisibility(
            visible = openApp != null,
            enter = fadeIn() + slideInVertically { it / 10 },
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GunmetalBackground)
            ) {
                openApp?.let { app ->
                    AppWindow(
                        app = app,
                        viewModel = viewModel,
                        onClose = { openApp = null }
                    )
                }
            }
        }
    }
}

@Composable
fun DesktopIconView(
    icon: DesktopIcon,
    offset: Offset,
    onPositionChange: (Offset) -> Unit,
    onOpen: () -> Unit
) {
    Column(
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onPositionChange(offset + dragAmount)
                }
            }
            .clickable { onOpen() }
            .padding(16.dp)
            .width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(12.dp),
            color = SteelSurfaceContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, SteelBorder),
            tonalElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon.app.icon,
                    contentDescription = icon.app.title,
                    tint = icon.app.color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = icon.app.title,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun StartMenuItem(app: DesktopApp, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = app.icon,
            contentDescription = null,
            tint = app.color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = app.title,
            color = TextSilver,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun TaskbarAppIcon(app: DesktopApp, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) Color.White.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = app.icon,
            contentDescription = null,
            tint = app.color,
            modifier = Modifier.size(24.dp)
        )
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
                    .size(width = 16.dp, height = 2.dp)
                    .background(app.color, CircleShape)
            )
        }
    }
}

@Composable
fun AppWindow(
    app: DesktopApp,
    viewModel: FileOrganizerViewModel,
    onClose: () -> Unit
) {
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SteelSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, SteelBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = app.icon,
                        contentDescription = null,
                        tint = app.color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = app.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSilver,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSteelMuted)
                    }
                }
            }
        },
        containerColor = GunmetalBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (app) {
                DesktopApp.DASHBOARD -> {
                    // We need to extract the dashboard part of HomeScreen
                    // For now, I'll placeholder it or use a simplified view
                    HomeScreenContent(viewModel = viewModel, initialTab = 0)
                }
                DesktopApp.FILE_EXPLORER -> {
                    HomeScreenContent(viewModel = viewModel, initialTab = 1)
                }
                DesktopApp.DUPLICATES -> {
                    HomeScreenContent(viewModel = viewModel, initialTab = 2)
                }
                DesktopApp.RULES -> {
                    HomeScreenContent(viewModel = viewModel, initialTab = 3)
                }
                DesktopApp.VAULT -> {
                    HomeScreenContent(viewModel = viewModel, initialTab = 4)
                }
                DesktopApp.TERMUX -> {
                    TerminalWidget(
                        viewModel = viewModel,
                        onClose = onClose
                    )
                }
                DesktopApp.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = onClose
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreenContent(viewModel: FileOrganizerViewModel, initialTab: Int) {
    // This is a wrapper around the current HomeScreen logic, but simplified to only show one tab
    // We will refactor HomeScreen.kt to allow this.
    HomeScreen(viewModel = viewModel, initialTab = initialTab, showNavigation = false)
}
