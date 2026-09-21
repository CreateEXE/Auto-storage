package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FileMetadata
import com.example.data.system.DeviceDiagnostics
import com.example.data.system.HardwareSpecs
import com.example.data.system.StorageImpactLevel
import com.example.data.system.StorageLagAssessment
import com.example.data.system.SystemMemoryInfo
import com.example.data.system.SystemStorageInfo
import com.example.ui.FileOrganizerViewModel
import com.example.ui.theme.CyberCrimson
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GunmetalBackground
import com.example.ui.theme.LaserEmerald
import com.example.ui.theme.SteelBackground
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelBorderHighlight
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.SteelSurfaceContainer
import com.example.ui.theme.SteelSurfaceElevated
import com.example.ui.theme.SteelSurfaceVariant
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextSteelMuted
import com.example.ui.theme.TextSteelSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.util.metallicPanel
import com.example.ui.util.metalPlateOverlay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSpecsScreen(
    viewModel: FileOrganizerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val diagnostics by viewModel.deviceDiagnostics.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedSection by remember { mutableStateOf(0) } // 0: All, 1: Storage Lag, 2: Memory, 3: Specs

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(SteelBackground)
            .metalPlateOverlay(alpha = 0.05f),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SYSTEM TELEMETRY",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextSilver,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CyberCyan.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "LIVE SPECS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "RAM Usage, Hardware & Storage System Impact",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSteelMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("specs_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSilver
                        )
                    }
                },
                actions = {
                    // Quick Memory Trim Action
                    IconButton(
                        onClick = {
                            val freed = viewModel.optimizeMemory()
                            scope.launch {
                                val freedFormatted = FileMetadata.formatFileSize(freed)
                                snackbarHostState.showSnackbar("Trimmed JVM heap: $freedFormatted reclaimed")
                            }
                        },
                        modifier = Modifier.testTag("quick_trim_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = "Trim Memory",
                            tint = LaserEmerald
                        )
                    }

                    // Refresh Button
                    IconButton(
                        onClick = {
                            viewModel.refreshDiagnostics()
                            scope.launch {
                                snackbarHostState.showSnackbar("Telemetry refreshed")
                            }
                        },
                        modifier = Modifier.testTag("refresh_telemetry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Telemetry",
                            tint = CyberCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SteelSurface.copy(alpha = 0.95f),
                    titleContentColor = TextSilver,
                    actionIconContentColor = CyberCyan
                ),
                modifier = Modifier
                    .border(0.dp, Color.Transparent)
                    .metallicPanel(cornerRadius = 0.dp, showBolts = true)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filter Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sections = listOf("ALL", "STORAGE LAG", "RAM MEMORY", "SPECS")
                    sections.forEachIndexed { index, title ->
                        FilterChip(
                            selected = selectedSection == index,
                            onClick = { selectedSection = index },
                            label = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                selectedLabelColor = CyberCyan,
                                containerColor = SteelSurfaceContainer,
                                labelColor = TextSteelSecondary
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (selectedSection == index) CyberCyan else SteelBorder
                            ),
                            modifier = Modifier.testTag("section_chip_$index")
                        )
                    }
                }
            }

            // SECTION 1: Storage Space Impact on System Speed (The core requirement)
            if (selectedSection == 0 || selectedSection == 1) {
                item {
                    StorageLagImpactCard(
                        storage = diagnostics.storage,
                        assessment = diagnostics.lagAssessment,
                        onPurgeDuplicates = {
                            viewModel.cleanAllDuplicates()
                            viewModel.refreshDiagnostics()
                            scope.launch {
                                snackbarHostState.showSnackbar("Duplicate purge sequence initiated")
                            }
                        }
                    )
                }

                item {
                    StorageDegradationMechanismsCard(assessment = diagnostics.lagAssessment)
                }
            }

            // SECTION 2: RAM & Memory Telemetry
            if (selectedSection == 0 || selectedSection == 2) {
                item {
                    RamMemoryCard(
                        memory = diagnostics.memory,
                        onTrimMemory = {
                            val freed = viewModel.optimizeMemory()
                            scope.launch {
                                val freedFormatted = FileMetadata.formatFileSize(freed)
                                snackbarHostState.showSnackbar("JVM Heap Trimmed: $freedFormatted recovered")
                            }
                        }
                    )
                }
            }

            // SECTION 3: Device Hardware Specifications
            if (selectedSection == 0 || selectedSection == 3) {
                item {
                    HardwareSpecsCard(hardware = diagnostics.hardware)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// -----------------------------------------------------------------------------
// STORAGE LAG & SYSTEM PERFORMANCE IMPACT CARDS
// -----------------------------------------------------------------------------

@Composable
fun StorageLagImpactCard(
    storage: SystemStorageInfo,
    assessment: StorageLagAssessment,
    onPurgeDuplicates: () -> Unit
) {
    val levelColor = when (assessment.impactLevel) {
        StorageImpactLevel.OPTIMAL -> LaserEmerald
        StorageImpactLevel.MODERATE_IMPACT -> WarningAmber
        StorageImpactLevel.DEGRADED_PERFORMANCE -> CyberCrimson
        StorageImpactLevel.CRITICAL_THROTTLING -> CyberCrimson
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("storage_lag_impact_card"),
        colors = CardDefaults.cardColors(containerColor = SteelSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, levelColor.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(levelColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = levelColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "STORAGE LAG IMPACT",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = TextSilver,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "How available space governs Android speed",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSteelMuted
                        )
                    }
                }

                // Impact Level Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = levelColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, levelColor)
                ) {
                    Text(
                        text = assessment.impactLevel.title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = levelColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Summary Explanation
            Text(
                text = assessment.impactLevel.summary,
                style = MaterialTheme.typography.bodySmall,
                color = TextSteelSecondary,
                lineHeight = 18.sp
            )

            // Storage Utilization Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Internal Disk Saturation",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSteelMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${storage.storageUsedPercent}% Full (${storage.storageFreePercent}% Free)",
                        style = MaterialTheme.typography.labelSmall,
                        color = levelColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                LinearProgressIndicator(
                    progress = { (storage.storageUsedPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = levelColor,
                    trackColor = SteelSurfaceContainer,
                    strokeCap = StrokeCap.Round
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Used: ${storage.formattedUsed}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSteelMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Total: ${storage.formattedTotal}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSilver,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Available: ${storage.formattedAvailable}",
                        style = MaterialTheme.typography.labelSmall,
                        color = LaserEmerald,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Metrics Telemetry Grid (I/O Latency, Write Amplification, TRIM, Risk Score)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SteelSurfaceContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, SteelBorder.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "EST. I/O LATENCY",
                            value = assessment.estimatedIoLatencyMs,
                            tint = if (assessment.impactLevel.severity >= 2) CyberCrimson else CyberCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = "WRITE AMPLIFICATION (WAF)",
                            value = assessment.writeAmplificationFactor,
                            tint = if (assessment.impactLevel.severity >= 2) CyberCrimson else ElectricBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "FLASH TRIM & WEAR",
                            value = assessment.trimStatus,
                            tint = if (assessment.impactLevel.severity >= 2) WarningAmber else LaserEmerald,
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = "SYSTEM LAG RISK SCORE",
                            value = "${assessment.lagRiskScore} / 100",
                            tint = levelColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Action to alleviate storage pressure
            if (storage.storageUsedPercent >= 75) {
                Button(
                    onClick = onPurgeDuplicates,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarningAmber.copy(alpha = 0.2f),
                        contentColor = WarningAmber
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, WarningAmber),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alleviate_lag_button")
                ) {
                    Icon(imageVector = Icons.Default.CopyAll, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PURGE DUPLICATES TO RESTORE BUFFER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StorageDegradationMechanismsCard(assessment: StorageLagAssessment) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("storage_degradation_mechanisms_card"),
        colors = CardDefaults.cardColors(containerColor = SteelSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, SteelBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WHY LOW STORAGE SLOWS DOWN ANDROID",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSilver
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextSteelSecondary
                )
            }

            Text(
                text = "Solid-state flash memory (UFS / eMMC) does not slow down mechanically. Instead, architectural constraints in block erasure, virtual memory, and SQLite cause exponential system lag when free storage dips below 15-20%.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSteelMuted,
                lineHeight = 18.sp
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    assessment.rootCauses.forEach { factor ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SteelSurfaceContainer,
                            border = BorderStroke(1.dp, SteelBorderHighlight.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = factor.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = SteelSurfaceElevated,
                                        border = BorderStroke(1.dp, SteelBorder)
                                    ) {
                                        Text(
                                            text = factor.impactScore,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (factor.impactScore == "Critical" || factor.impactScore == "Severe") CyberCrimson else LaserEmerald,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = factor.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSteelMuted,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = factor.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSteelSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            if (!isExpanded) {
                Text(
                    text = "Tap to view in-depth details on NAND Flash WAF, ZRAM eviction, SQLite WAL locks, and ART compiler starvation.",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    modifier = Modifier.clickable { isExpanded = true }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// RAM & SYSTEM MEMORY CARD
// -----------------------------------------------------------------------------

@Composable
fun RamMemoryCard(
    memory: SystemMemoryInfo,
    onTrimMemory: () -> Unit
) {
    val ramColor = when {
        memory.isLowMemory || memory.ramUsedPercent >= 90 -> CyberCrimson
        memory.ramUsedPercent >= 75 -> WarningAmber
        else -> LaserEmerald
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ram_memory_card"),
        colors = CardDefaults.cardColors(containerColor = SteelSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, SteelBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ramColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = ramColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "RAM & MEMORY TELEMETRY",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = TextSilver,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Physical System Memory & JVM Allocation",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSteelMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (memory.isLowMemory) CyberCrimson.copy(alpha = 0.2f) else LaserEmerald.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (memory.isLowMemory) CyberCrimson else LaserEmerald.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (memory.isLowMemory) "LOW MEMORY ALERT" else "NORMAL PRESSURE",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (memory.isLowMemory) CyberCrimson else LaserEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // System RAM Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Physical RAM Load",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSteelMuted
                    )
                    Text(
                        text = "${memory.formattedUsedRam} / ${memory.formattedTotalRam} (${memory.ramUsedPercent}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = ramColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                LinearProgressIndicator(
                    progress = { (memory.ramUsedPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = ramColor,
                    trackColor = SteelSurfaceContainer,
                    strokeCap = StrokeCap.Round
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Avail: ${memory.formattedAvailRam}",
                        style = MaterialTheme.typography.labelSmall,
                        color = LaserEmerald,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "LMK Kill Threshold: ${memory.formattedThreshold}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSteelMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // JVM Heap Utilization
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SteelSurfaceContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, SteelBorder.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "APP RUNTIME HEAP (JVM)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                        Text(
                            text = "${memory.formattedJvmUsed} / ${memory.formattedJvmMax} (${memory.jvmUsedPercent}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricBlue,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (memory.jvmUsedPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ElectricBlue,
                        trackColor = SteelSurfaceVariant,
                        strokeCap = StrokeCap.Round
                    )

                    Text(
                        text = "The JVM heap manages active file indexes, thumbnails, and cache in RAM.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSteelMuted,
                        fontSize = 10.sp
                    )
                }
            }

            // Quick Trim Button
            Button(
                onClick = onTrimMemory,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan.copy(alpha = 0.15f),
                    contentColor = CyberCyan
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trim_jvm_heap_button")
            ) {
                Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TRIGGER HEAP GARBAGE COLLECTION",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// HARDWARE SPECS CARD
// -----------------------------------------------------------------------------

@Composable
fun HardwareSpecsCard(hardware: HardwareSpecs) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hardware_specs_card"),
        colors = CardDefaults.cardColors(containerColor = SteelSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, SteelBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeveloperBoard,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "DEVICE HARDWARE SPECS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = TextSilver,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Processor, Board, Architecture & Android OS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSteelMuted
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SteelSurfaceContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, SteelBorder.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SpecRow(label = "Device Model", value = "${hardware.manufacturer} ${hardware.model} (${hardware.deviceCode})")
                    SpecRow(label = "Processor Cores", value = "${hardware.cpuCores} Cores Available")
                    SpecRow(label = "Hardware / Board", value = "${hardware.hardware} / ${hardware.board}")
                    SpecRow(label = "CPU Architecture", value = hardware.supportedAbis)
                    SpecRow(label = "Android Version", value = "Android ${hardware.androidVersion} (API ${hardware.apiLevel})")
                    SpecRow(label = "Security Patch", value = hardware.securityPatch)
                    SpecRow(label = "Build ID", value = hardware.buildId)
                    SpecRow(label = "System Uptime", value = hardware.formattedUptime)
                }
            }
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSteelMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = TextSilver,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSteelMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Black,
            color = tint,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp
        )
    }
}
