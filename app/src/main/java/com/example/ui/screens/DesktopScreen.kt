package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.InstalledApp
import com.example.ui.FileOrganizerViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.util.metallicPanel
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class DesktopApp(val title: String, val icon: ImageVector, val color: Color) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, CyberCyan),
    FILE_EXPLORER("Files", Icons.Default.Folder, LaserEmerald),
    DUPLICATES("Duplicates", Icons.Default.ContentCopy, WarningAmber),
    RULES("Rules", Icons.AutoMirrored.Filled.Rule, SynthPurple),
    VAULT("Vault", Icons.Default.Lock, Color.White),
    MUSIC("Music", Icons.Default.MusicNote, CyberCyan),
    TERMUX("Termux", Icons.Default.Terminal, ElectricBlue),
    SETTINGS("Settings", Icons.Default.Settings, TextSteelSecondary),
    AUDIO_FILE("Audio", Icons.Default.MusicNote, CyberCyan) // Representation for audio files
}

data class DesktopIcon(
    val app: DesktopApp?,
    val position: Offset = Offset.Zero,
    val filePath: String? = null,
    val packageName: String? = null,
    val label: String? = null
)

data class WindowState(
    val app: DesktopApp,
    var position: Offset = Offset(50f, 50f),
    var size: IntSize = IntSize(800, 1000), // Px
    var isMinimized: Boolean = false,
    var zIndex: Float = 0f
)

@Composable
fun DesktopScreen(viewModel: FileOrganizerViewModel) {
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val desktopIcons = remember { mutableStateListOf<DesktopIcon>() }
    val openWindows = remember { mutableStateListOf<WindowState>() }
    var isStartMenuOpen by remember { mutableStateOf(false) }
    var nextZIndex by remember { mutableIntStateOf(1) }
    
    var selectedAppNames by remember { mutableStateOf(setOf<String>()) }
    var selectedFile by remember { mutableStateOf<String?>(null) }
    var marqueeRect by remember { mutableStateOf<Rect?>(null) }
    var showTagEditor by remember { mutableStateOf<String?>(null) }

    val storageStats by viewModel.storageStats.collectAsStateWithLifecycle()
    val deviceDiagnostics by viewModel.deviceDiagnostics.collectAsStateWithLifecycle()
    val isVaultLocked by viewModel.isVaultLocked.collectAsStateWithLifecycle()
    val termuxLinked by viewModel.termuxLinked.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    // Load icons based on settings
    LaunchedEffect(userSettings.desktopApps, installedApps) {
        if (installedApps.isEmpty()) {
            viewModel.loadInstalledApps()
        }
        
        desktopIcons.clear()
        val appsToLoad = if (userSettings.desktopApps.isEmpty()) {
            listOf("FILE_EXPLORER", "TERMUX", "DASHBOARD", "VAULT", "MUSIC", "SETTINGS")
        } else {
            userSettings.desktopApps
        }
        
        val spacingPx = with(density) { 120.dp.toPx() }
        val marginPx = with(density) { 24.dp.toPx() }
        
        appsToLoad.forEachIndexed { index, appName ->
            try {
                val app = DesktopApp.valueOf(appName)
                val row = index % 6
                val col = index / 6
                desktopIcons.add(DesktopIcon(app, Offset(marginPx + (col * spacingPx), marginPx + (row * spacingPx))))
            } catch (e: Exception) {
                // If not an internal app, check if it's an installed package
                val installedApp = installedApps.find { it.packageName == appName }
                if (installedApp != null) {
                    val row = index % 6
                    val col = index / 6
                    desktopIcons.add(DesktopIcon(
                        app = null,
                        position = Offset(marginPx + (col * spacingPx), marginPx + (row * spacingPx)),
                        packageName = installedApp.packageName,
                        label = installedApp.label
                    ))
                }
            }
        }
        
        // Add a dummy audio file for demo
        desktopIcons.add(DesktopIcon(DesktopApp.AUDIO_FILE, Offset(marginPx + (3 * spacingPx), marginPx), "/sdcard/Sample.mp3"))
    }

    var showDesktopContextMenu by remember { mutableStateOf(false) }
    var contextMenuPosition by remember { mutableStateOf(Offset.Zero) }

    val infiniteTransition = rememberInfiniteTransition(label = "scanline")
    val scanlinePos by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanlinePos"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HackBlack)
            .pointerInput(Unit) {
                var lastTapTime = 0L
                var lastTapPos = Offset.Zero
                
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downTime = System.currentTimeMillis()
                    val iconSize = with(density) { Size(100.dp.toPx(), 140.dp.toPx()) }
                    
                    val isSecondTap = (downTime - lastTapTime) < viewConfiguration.doubleTapTimeoutMillis &&
                            (down.position - lastTapPos).getDistance() < 48.dp.toPx()
                    
                    if (isSecondTap) {
                        // Double tap sequence detected. Check if it's a hold.
                        val up = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                            waitForUpOrCancellation()
                        }
                        
                        if (up == null) {
                            // HOLD DETECTED (Marquee Start)
                            var dragStartPos = down.position
                            marqueeRect = Rect(dragStartPos, Size.Zero)
                            
                            drag(down.id) { change ->
                                change.consume()
                                val currentPos = change.position
                                marqueeRect = Rect(
                                    left = min(dragStartPos.x, currentPos.x),
                                    top = min(dragStartPos.y, currentPos.y),
                                    right = max(dragStartPos.x, currentPos.x),
                                    bottom = max(dragStartPos.y, currentPos.y)
                                )
                                
                                // Selection Update
                                val newSelection = mutableSetOf<String>()
                                val currentMarquee = marqueeRect!!
                                desktopIcons.forEach { icon ->
                                    val iconRect = Rect(icon.position, iconSize)
                                    if (currentMarquee.overlaps(iconRect)) {
                                        val id = icon.app?.name ?: icon.packageName ?: ""
                                        if (id.isNotEmpty()) newSelection.add(id)
                                    }
                                }
                                selectedAppNames = newSelection
                            }
                            marqueeRect = null
                            lastTapTime = 0
                        } else {
                            // Quick Second Tap (Right Click)
                            val hitIcon = desktopIcons.find { 
                                Rect(it.position, iconSize).contains(down.position) 
                            }
                            if (hitIcon != null && hitIcon.app == DesktopApp.AUDIO_FILE) {
                                showTagEditor = hitIcon.filePath
                            } else {
                                contextMenuPosition = down.position
                                showDesktopContextMenu = true
                            }
                            lastTapTime = 0
                        }
                    } else {
                        // First tap down. Wait for release to confirm click or track for double tap.
                        val up = waitForUpOrCancellation()
                        if (up != null) {
                            // Tap up detected
                            val hitIcon = desktopIcons.find { 
                                Rect(it.position, iconSize).contains(down.position) 
                            }
                            if (hitIcon != null) {
                                selectedAppNames = setOf(hitIcon.app?.name ?: hitIcon.packageName ?: "")
                                selectedFile = hitIcon.filePath
                            } else {
                                selectedAppNames = emptySet()
                                selectedFile = null
                                isStartMenuOpen = false
                            }
                            lastTapTime = downTime
                            lastTapPos = down.position
                        } else {
                            lastTapTime = 0
                        }
                    }
                }
            }
    ) {
        // Desktop Wallpaper
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.hack_sign_wallpaper_1790155017253),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            alpha = 0.4f // Keep it dark for the terminal aesthetic
        )

        // Desktop Grid Pattern Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 1f
            val step = 80f
            for (x in 0..(this.size.width / step).toInt()) {
                drawLine(
                    color = HackDeepOrange.copy(alpha = 0.05f),
                    start = Offset(x * step, 0f),
                    end = Offset(x * step, this.size.height),
                    strokeWidth = strokeWidth
                )
            }
            for (y in 0..(this.size.height / step).toInt()) {
                drawLine(
                    color = HackDeepOrange.copy(alpha = 0.05f),
                    start = Offset(0f, y * step),
                    end = Offset(this.size.width, y * step),
                    strokeWidth = strokeWidth
                )
            }
        }

        // Static CRT Scanlines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scanlineCount = (this.size.height / 4.dp.toPx()).toInt()
            for (i in 0..scanlineCount) {
                val y = i * 4.dp.toPx()
                drawLine(
                    color = Color.Black.copy(alpha = 0.15f),
                    start = Offset(0f, y),
                    end = Offset(this.size.width, y),
                    strokeWidth = 1f
                )
            }
        }

        // Animated Moving Scanline
        Canvas(modifier = Modifier.fillMaxSize()) {
            val y = this.size.height * scanlinePos
            
            // Faint glow around moving line
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        HackCyan.copy(alpha = 0.03f),
                        HackCyan.copy(alpha = 0.08f),
                        HackCyan.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    startY = y - 40.dp.toPx(),
                    endY = y + 40.dp.toPx()
                ),
                topLeft = Offset(0f, y - 40.dp.toPx()),
                size = Size(this.size.width, 80.dp.toPx())
            )
            
            // Main sharp scanline
            drawLine(
                color = HackCyan.copy(alpha = 0.15f),
                start = Offset(0f, y),
                end = Offset(this.size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Desktop Area (Icons)
        Box(modifier = Modifier.fillMaxSize()) {
            desktopIcons.forEachIndexed { index, icon ->
                val iconId = icon.app?.name ?: icon.packageName ?: ""
                DesktopIconView(
                    icon = icon,
                    isSelected = iconId in selectedAppNames,
                    onPositionChange = { newOffset ->
                        desktopIcons[index] = icon.copy(position = newOffset)
                    },
                    onOpen = {
                        if (icon.app != null) {
                            val existing = openWindows.find { it.app == icon.app }
                            if (existing != null) {
                                existing.isMinimized = false
                                existing.zIndex = nextZIndex++.toFloat()
                            } else {
                                openWindows.add(WindowState(app = icon.app, zIndex = nextZIndex++.toFloat()))
                            }
                        } else if (icon.packageName != null) {
                            viewModel.launchApp(icon.packageName)
                        }
                        selectedAppNames = setOf(iconId)
                    }
                )
            }
        }

        // Marquee Selection Box & Transparent Overlay
        val currentMarquee = marqueeRect
        if (currentMarquee != null) {
            // Transparent selection overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HackCyan.copy(alpha = 0.05f))
            )
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = HackCyan.copy(alpha = 0.1f),
                    topLeft = currentMarquee.topLeft,
                    size = currentMarquee.size
                )
                drawRect(
                    color = HackCyan.copy(alpha = 0.5f),
                    topLeft = currentMarquee.topLeft,
                    size = currentMarquee.size,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        // Floating Windows
        Box(modifier = Modifier.fillMaxSize()) {
            openWindows.filter { !it.isMinimized }.forEach { windowState ->
                key(windowState.app) {
                    FloatingWindow(
                        windowState = windowState,
                        onClose = { openWindows.remove(windowState) },
                        onMinimize = { windowState.isMinimized = true },
                        onFocus = { windowState.zIndex = nextZIndex++.toFloat() },
                        content = {
                            AppWindowContent(app = windowState.app, viewModel = viewModel, onClose = { openWindows.remove(windowState) })
                        }
                    )
                }
            }
        }

        // Taskbar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(48.dp),
            color = HackBlack.copy(alpha = 0.85f)
        ) {
            Column {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HackDeepOrange.copy(alpha = 0.3f)))
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                // Start Button (HUD Logo)
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(80.dp)
                        .background(if (isStartMenuOpen) HackDeepOrange.copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, HackDeepOrange, RoundedCornerShape(2.dp))
                        .clickable { isStartMenuOpen = !isStartMenuOpen }
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Apps, contentDescription = null, tint = HackDeepOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "CORE", 
                            color = HackDeepOrange, 
                            fontWeight = FontWeight.Black, 
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Taskbar Icons (Running Apps)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    openWindows.forEach { window ->
                        TaskbarAppIcon(
                            app = window.app, 
                            isActive = !window.isMinimized && window.zIndex == (nextZIndex - 1).toFloat()
                        ) {
                            if (window.isMinimized) {
                                window.isMinimized = false
                                window.zIndex = nextZIndex++.toFloat()
                            } else if (window.zIndex == (nextZIndex - 1).toFloat()) {
                                window.isMinimized = true
                            } else {
                                window.zIndex = nextZIndex++.toFloat()
                            }
                        }
                    }
                }

                // System Tray
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .border(1.dp, HackDeepOrange.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
                    Icon(
                        imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = "Network",
                        tint = if (isOnline) HackCyan else DangerRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    SystemClock()
                }
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
                                .padding(bottom = 50.dp, start = 8.dp)
                                .width(280.dp)
                                .heightIn(max = 500.dp),
                            color = HackBlack.copy(alpha = 0.95f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HackDeepOrange.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    "THE WORLD OS",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(8.dp),
                                    color = HackDeepOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                )
                                HorizontalDivider(color = HackDeepOrange.copy(alpha = 0.2f))
                                
                                Column(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        "SYSTEM ENTITIES",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSteelMuted,
                                        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp),
                                        fontSize = 9.sp
                                    )
                                    DesktopApp.entries.filter { it != DesktopApp.AUDIO_FILE }.forEach { app ->
                                        StartMenuItem(label = app.title, icon = app.icon, color = app.color) {
                                            val existing = openWindows.find { it.app == app }
                                            if (existing != null) {
                                                existing.isMinimized = false
                                                existing.zIndex = nextZIndex++.toFloat()
                                            } else {
                                                openWindows.add(WindowState(app = app, zIndex = nextZIndex++.toFloat()))
                                            }
                                            isStartMenuOpen = false
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "EXTERNAL DEPLOYS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSteelMuted,
                                        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp),
                                        fontSize = 9.sp
                                    )
                                    installedApps.forEach { app ->
                                        StartMenuItem(label = app.label, packageName = app.packageName) {
                                            viewModel.launchApp(app.packageName)
                                            isStartMenuOpen = false
                                        }
                                    }
                                }
                            }
                        }
            }
        }

        // Context Menu Overlay
        if (showDesktopContextMenu) {
            DesktopContextMenu(
                position = contextMenuPosition,
                onDismiss = { showDesktopContextMenu = false },
                onAddApp = { id ->
                    val current = userSettings.desktopApps.toMutableList()
                    if (!current.contains(id)) {
                        current.add(id)
                        viewModel.updateSettings(userSettings.copy(desktopApps = current))
                    }
                },
                onRemoveApp = { id ->
                     val current = userSettings.desktopApps.toMutableList()
                     current.remove(id)
                     viewModel.updateSettings(userSettings.copy(desktopApps = current))
                },
                onOpenSettings = {
                    val existing = openWindows.find { it.app == DesktopApp.SETTINGS }
                    if (existing != null) {
                        existing.isMinimized = false
                        existing.zIndex = nextZIndex++.toFloat()
                    } else {
                        openWindows.add(WindowState(app = DesktopApp.SETTINGS, zIndex = nextZIndex++.toFloat()))
                    }
                },
                availableInternalApps = DesktopApp.entries.filter { it != DesktopApp.AUDIO_FILE }.toList(),
                installedApps = installedApps,
                currentAppNames = userSettings.desktopApps
            )
        }

        // ID3 Tag Editor Dialog
        showTagEditor?.let { path ->
            AudioTagEditorDialog(
                filePath = path,
                onDismiss = { showTagEditor = null },
                onSave = { title, artist, album, genre, year ->
                    viewModel.updateAudioTags(path, title, artist, album, genre, year)
                    showTagEditor = null
                }
            )
        }
    }
}

@Composable
fun DesktopContextMenu(
    position: Offset,
    onDismiss: () -> Unit,
    onAddApp: (String) -> Unit,
    onRemoveApp: (String) -> Unit,
    onOpenSettings: () -> Unit,
    availableInternalApps: List<DesktopApp>,
    installedApps: List<InstalledApp>,
    currentAppNames: List<String>
) {
    Box(modifier = Modifier.fillMaxSize().clickable { onDismiss() }) {
        Surface(
            modifier = Modifier
                .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
                .width(240.dp)
                .heightIn(max = 400.dp),
            color = HackBlack.copy(alpha = 0.95f),
            border = androidx.compose.foundation.BorderStroke(1.dp, HackDeepOrange.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(2.dp)
        ) {
            Column(modifier = Modifier.padding(4.dp).verticalScroll(rememberScrollState())) {
                Text(
                    "SYSTEM OVERRIDE",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(8.dp),
                    color = HackDeepOrange,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                
                HorizontalDivider(color = HackDeepOrange.copy(alpha = 0.2f))

                DropdownMenuItem(
                    text = { Text("DISPLAY CONFIG", color = TextSilver, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    onClick = { onOpenSettings(); onDismiss() },
                    leadingIcon = { Icon(Icons.Default.Monitor, null, tint = HackCyan, modifier = Modifier.size(16.dp)) },
                    colors = MenuDefaults.itemColors(textColor = TextSilver)
                )

                var showAddSubmenu by remember { mutableStateOf(false) }
                DropdownMenuItem(
                    text = { Text("DEPLOY ENTITY...", color = TextSilver, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    onClick = { showAddSubmenu = !showAddSubmenu },
                    leadingIcon = { Icon(Icons.Default.Add, null, tint = LaserEmerald, modifier = Modifier.size(16.dp)) },
                    trailingIcon = { Icon(if (showAddSubmenu) Icons.Default.ExpandLess else Icons.Default.ChevronRight, null, tint = TextSteelMuted, modifier = Modifier.size(16.dp)) }
                )

                if (showAddSubmenu) {
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text("SYSTEM", color = TextSteelMuted, fontSize = 9.sp, modifier = Modifier.padding(start = 12.dp, top = 4.dp))
                        availableInternalApps.filter { it.name !in currentAppNames }.forEach { app ->
                            DropdownMenuItem(
                                text = { Text(app.title.uppercase(), color = TextSilver, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                                onClick = { onAddApp(app.name); onDismiss() }
                            )
                        }
                        Text("EXTERNAL", color = TextSteelMuted, fontSize = 9.sp, modifier = Modifier.padding(start = 12.dp, top = 4.dp))
                        installedApps.filter { it.packageName !in currentAppNames }.forEach { app ->
                            DropdownMenuItem(
                                text = { Text(app.label.uppercase(), color = TextSilver, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                                onClick = { onAddApp(app.packageName); onDismiss() }
                            )
                        }
                    }
                }

                var showRemoveSubmenu by remember { mutableStateOf(false) }
                DropdownMenuItem(
                    text = { Text("RECALL ENTITY...", color = TextSilver, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    onClick = { showRemoveSubmenu = !showRemoveSubmenu },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = DangerRed, modifier = Modifier.size(16.dp)) },
                    trailingIcon = { Icon(if (showRemoveSubmenu) Icons.Default.ExpandLess else Icons.Default.ChevronRight, null, tint = TextSteelMuted, modifier = Modifier.size(16.dp)) }
                )

                if (showRemoveSubmenu) {
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        currentAppNames.forEach { appName ->
                            val label = try { DesktopApp.valueOf(appName).title } catch(e: Exception) { 
                                installedApps.find { it.packageName == appName }?.label ?: appName
                            }
                            DropdownMenuItem(
                                text = { Text(label.uppercase(), color = TextSilver, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                                onClick = { onRemoveApp(appName); onDismiss() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingWindow(
    windowState: WindowState,
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    onFocus: () -> Unit,
    content: @Composable () -> Unit
) {
    var position by remember { mutableStateOf(windowState.position) }
    var windowSize by remember { mutableStateOf(windowState.size) }

    Surface(
        modifier = Modifier
            .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
            .size(width = with(LocalDensity.current) { windowSize.width.toDp() }, height = with(LocalDensity.current) { windowSize.height.toDp() })
            .pointerInput(Unit) {
                detectDragGestures(onDragStart = { onFocus() }) { change, dragAmount ->
                    change.consume()
                    position += dragAmount
                    windowState.position = position
                }
            }
            .zIndex(windowState.zIndex)
            .border(1.dp, HackDeepOrange.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            .padding(1.dp),
        color = HackBlack.copy(alpha = 0.9f),
        shape = RoundedCornerShape(2.dp)
    ) {
        Column {
            // HUD Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                color = HackDeepOrange.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = windowState.app.icon,
                        contentDescription = null,
                        tint = HackDeepOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = windowState.app.title.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = HackDeepOrange,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onMinimize, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Minimize, null, tint = HackDeepOrange, modifier = Modifier.size(14.dp))
                        }
                        IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, tint = HackDeepOrange, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
            
            // Decorative line
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(HackDeepOrange.copy(alpha = 0.3f)))

            // Window Content
            Box(modifier = Modifier.weight(1f)) {
                content()
                
                // Resize Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newWidth = (windowSize.width + dragAmount.x).roundToInt().coerceAtLeast(300)
                                val newHeight = (windowSize.height + dragAmount.y).roundToInt().coerceAtLeast(200)
                                windowSize = IntSize(newWidth, newHeight)
                                windowState.size = windowSize
                            }
                        }
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                        val w = size.width
                        val h = size.height
                        drawLine(HackDeepOrange, Offset(x = w * 0.2f, y = h), Offset(x = w, y = h * 0.2f), 1.5f)
                        drawLine(HackDeepOrange, Offset(x = w * 0.6f, y = h), Offset(x = w, y = h * 0.6f), 1.5f)
                    }
                }
            }
        }
    }
}

@Composable
fun RetroWindowButton(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .background(Color(0xFFC0C0C0))
            .border(1.dp, Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Black)
    }
}

@Composable
fun AppWindowContent(
    app: DesktopApp,
    viewModel: FileOrganizerViewModel,
    onClose: () -> Unit
) {
    when (app) {
        DesktopApp.DASHBOARD -> HomeScreenContent(viewModel = viewModel, initialTab = 0)
        DesktopApp.FILE_EXPLORER -> HomeScreenContent(viewModel = viewModel, initialTab = 1)
        DesktopApp.DUPLICATES -> HomeScreenContent(viewModel = viewModel, initialTab = 2)
        DesktopApp.RULES -> HomeScreenContent(viewModel = viewModel, initialTab = 3)
        DesktopApp.MUSIC -> HomeScreenContent(viewModel = viewModel, initialTab = 4)
        DesktopApp.VAULT -> HomeScreenContent(viewModel = viewModel, initialTab = 5)
        DesktopApp.TERMUX -> TerminalWidget(viewModel = viewModel, onClose = onClose)
        DesktopApp.SETTINGS -> SettingsScreen(viewModel = viewModel, onNavigateBack = onClose)
        DesktopApp.AUDIO_FILE -> { /* No window for file entity, handled by dialog */ }
    }
}

@Composable
fun TermuxStatusWidget(linked: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SteelSurfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, SteelBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (linked) LaserEmerald.copy(alpha = 0.1f) else DangerRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = if (linked) LaserEmerald else DangerRed,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "TERMUX BACKEND",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSteelMuted,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (linked) "CONNECTED" else "DISCONNECTED",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (linked) LaserEmerald else DangerRed,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun DesktopIconView(
    icon: DesktopIcon,
    isSelected: Boolean,
    onPositionChange: (Offset) -> Unit,
    onOpen: () -> Unit
) {
    var offset by remember { mutableStateOf(icon.position) }

    Column(
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = { onPositionChange(offset) },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                    }
                )
            }
            .padding(12.dp)
            .width(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(if (isSelected) HackCyan.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(4.dp))
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp, 
                    color = if (isSelected) HackCyan else (icon.app?.color ?: HackDeepOrange).copy(alpha = 0.2f), 
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable { onOpen() }
                .padding(4.dp)
                .border(
                    width = 1.dp, 
                    color = if (isSelected) HackCyan.copy(alpha = 0.5f) else (icon.app?.color ?: HackDeepOrange).copy(alpha = 0.1f), 
                    shape = CircleShape
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (icon.app != null) {
                Icon(
                    imageVector = icon.app.icon,
                    contentDescription = icon.app.title,
                    tint = if (isSelected) HackCyan else icon.app.color,
                    modifier = Modifier.size(28.dp)
                )
            } else if (icon.packageName != null) {
                // Fetch app icon via package manager
                val context = LocalContext.current
                val appIcon = remember(icon.packageName) {
                    try {
                        context.packageManager.getApplicationIcon(icon.packageName)
                    } catch (e: Exception) {
                        null
                    }
                }
                AsyncImage(
                    model = appIcon,
                    contentDescription = icon.label,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = (icon.app?.title ?: icon.label ?: "").uppercase(),
            color = if (isSelected) HackCyan else (icon.app?.color ?: HackDeepOrange),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            modifier = if (isSelected) Modifier.background(HackCyan.copy(alpha = 0.1f)).padding(horizontal = 4.dp) else Modifier
        )
    }
}

@Composable
fun StartMenuItem(
    label: String,
    icon: ImageVector? = null,
    packageName: String? = null,
    color: Color = HackDeepOrange,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        } else if (packageName != null) {
            val context = LocalContext.current
            val appIcon = remember(packageName) {
                try {
                    context.packageManager.getApplicationIcon(packageName)
                } catch (e: Exception) {
                    null
                }
            }
            AsyncImage(
                model = appIcon,
                contentDescription = label,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label.uppercase(),
            color = TextSilver,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun TaskbarAppIcon(app: DesktopApp, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .height(36.dp)
            .width(140.dp)
            .background(if (isActive) HackDeepOrange.copy(alpha = 0.2f) else Color.Transparent)
            .border(1.dp, if (isActive) HackDeepOrange else HackDeepOrange.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = app.icon,
                contentDescription = null,
                tint = if (isActive) HackDeepOrange else HackDeepOrange.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = app.title.uppercase(),
                color = if (isActive) TextSilver else TextSteelMuted,
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.8f)
                    .height(2.dp)
                    .background(HackDeepOrange)
            )
        }
    }
}

@Composable
fun HomeScreenContent(viewModel: FileOrganizerViewModel, initialTab: Int) {
    HomeScreen(viewModel = viewModel, initialTab = initialTab, showNavigation = false)
}

@Composable
fun SystemClock() {
    var timeText by remember { mutableStateOf("") }
    val orbitronFont = FontFamily(Font(R.font.orbitron, FontWeight.Normal))

    LaunchedEffect(Unit) {
        while (true) {
            timeText = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    Text(
        text = timeText,
        color = HackCyan,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        fontFamily = orbitronFont,
        letterSpacing = 1.sp
    )
}
