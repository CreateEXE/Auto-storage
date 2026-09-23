package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DashboardWidget
import com.example.data.model.FileCategory
import com.example.data.model.FileMetadata
import com.example.data.model.StorageStats
import com.example.data.system.DeviceDiagnostics
import com.example.data.system.StorageImpactLevel
import com.example.ui.FileOrganizerViewModel
import com.example.ui.theme.*
import com.example.ui.util.metallicPanel

@Composable
fun WidgetContainer(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    onRemove: (() -> Unit)? = null,
    isHeld: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isHeld) 1.5.dp else 1.dp,
                color = if (isHeld) HackCyan else HackDeepOrange.copy(alpha = 0.3f),
                shape = RoundedCornerShape(2.dp)
            ),
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHeld) SteelSurfaceElevated else HackBlack.copy(alpha = 0.8f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isHeld) HackCyan else HackDeepOrange,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                if (onRemove != null) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "De-sync",
                            tint = HackDeepOrange.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(HackDeepOrange.copy(alpha = 0.2f))
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SearchWidget(
    query: String,
    onQueryChange: (String) -> Unit,
    onRemove: () -> Unit,
    isHeld: Boolean = false
) {
    WidgetContainer(
        title = "Universal Search",
        icon = Icons.Default.Search,
        iconColor = CyberCyan,
        onRemove = onRemove,
        isHeld = isHeld
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search entire device index...", color = TextSteelMuted, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = SteelBorder,
                focusedTextColor = TextSilver,
                unfocusedTextColor = TextSilver,
                focusedContainerColor = SteelSurfaceContainer,
                unfocusedContainerColor = SteelSurfaceContainer
            )
        )
    }
}

@Composable
fun StorageBreakdownList(categorySizes: Map<FileCategory, Long>) {
    val total = categorySizes.values.sum().coerceAtLeast(1L)
    
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categorySizes.filter { it.value > 0 }.toList().sortedByDescending { it.second }.take(5).forEach { (category, size) ->
            val percentage = (size.toFloat() / total)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = category.name.uppercase(),
                        color = TextSilver,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        color = HackCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { percentage },
                    modifier = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)),
                    color = HackDeepOrange,
                    trackColor = HackDeepOrange.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun StorageInfoWidget(
    stats: StorageStats,
    onRemove: () -> Unit,
    isHeld: Boolean = false
) {
    WidgetContainer(
        title = "Resource Integrity",
        icon = Icons.Default.DataUsage,
        iconColor = HackDeepOrange,
        onRemove = onRemove,
        isHeld = isHeld
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Total Payload", color = TextSteelMuted, fontSize = 9.sp)
                Text(stats.formattedTotalSize, color = TextSilver, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Redundancy", color = TextSteelMuted, fontSize = 9.sp)
                Text(stats.formattedDuplicateSavings, color = DangerRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        StorageBreakdownList(categorySizes = stats.categorySizes)
    }
}

@Composable
fun QuickActionsWidget(
    onScan: () -> Unit,
    onClean: () -> Unit,
    onOrganize: () -> Unit,
    onRemove: () -> Unit,
    isHeld: Boolean = false
) {
    WidgetContainer(
        title = "System Actions",
        icon = Icons.Default.Bolt,
        iconColor = Color.Yellow,
        onRemove = onRemove,
        isHeld = isHeld
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.Refresh,
                label = "Scan",
                color = CyberCyan,
                onClick = onScan,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.DeleteSweep,
                label = "Clean",
                color = DangerRed,
                onClick = onClean,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.FolderSpecial,
                label = "Sort",
                color = LaserEmerald,
                onClick = onOrganize,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(12.dp),
        color = SteelSurfaceContainer,
        border = BorderStroke(1.dp, SteelBorder)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSilver)
        }
    }
}

@Composable
fun RecentFilesWidget(
    files: List<FileMetadata>,
    onFileClick: (FileMetadata) -> Unit,
    onRemove: () -> Unit,
    isHeld: Boolean = false
) {
    WidgetContainer(
        title = "Recent Entities",
        icon = Icons.Default.History,
        iconColor = SecondaryTeal,
        onRemove = onRemove,
        isHeld = isHeld
    ) {
        if (files.isEmpty()) {
            Text("No recent files discovered.", color = TextSteelMuted, style = MaterialTheme.typography.bodySmall)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(files) { file ->
                    Column(
                        modifier = Modifier
                            .width(80.dp)
                            .clickable { onFileClick(file) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = SteelSurfaceContainer,
                            border = BorderStroke(1.dp, SteelBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = file.currentName,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSilver,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VaultStatusWidget(
    isLocked: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    isHeld: Boolean = false
) {
    WidgetContainer(
        title = "Mimic Vault",
        icon = Icons.Default.Lock,
        iconColor = if (isLocked) DangerRed else LaserEmerald,
        onRemove = onRemove,
        isHeld = isHeld
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = if (isLocked) DangerRed.copy(alpha = 0.1f) else LaserEmerald.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, if (isLocked) DangerRed.copy(alpha = 0.5f) else LaserEmerald.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = if (isLocked) DangerRed else LaserEmerald
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isLocked) "VAULT SEALED" else "VAULT UNLEASHED",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isLocked) DangerRed else LaserEmerald
                    )
                    Text(
                        text = if (isLocked) "Tap to unchain mimic" else "Secure entities accessible",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSteelMuted
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceSpecsWidget(
    diagnostics: DeviceDiagnostics,
    onOpenDiagnostics: () -> Unit,
    onRemove: () -> Unit,
    isHeld: Boolean = false
) {
    val levelColor = when (diagnostics.lagAssessment.impactLevel) {
        StorageImpactLevel.OPTIMAL -> LaserEmerald
        StorageImpactLevel.MODERATE_IMPACT -> WarningAmber
        StorageImpactLevel.DEGRADED_PERFORMANCE -> CyberCrimson
        StorageImpactLevel.CRITICAL_THROTTLING -> CyberCrimson
    }

    WidgetContainer(
        title = "System Pulse",
        icon = Icons.Default.Memory,
        iconColor = HackCyan,
        onRemove = onRemove,
        isHeld = isHeld
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hardware summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${diagnostics.hardware.model}".uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSilver,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "KERNEL V${diagnostics.hardware.androidVersion} • NODES: ${diagnostics.hardware.cpuCores}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSteelMuted,
                        fontSize = 9.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(2.dp),
                    color = levelColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, levelColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = diagnostics.lagAssessment.impactLevel.title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = levelColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // RAM Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "BUFFER MEMORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSteelMuted,
                        fontSize = 9.sp
                    )
                    Text(
                        text = "${diagnostics.memory.ramUsedPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (diagnostics.memory.isLowMemory) CyberCrimson else HackCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = { (diagnostics.memory.ramUsedPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = if (diagnostics.memory.isLowMemory) CyberCrimson else HackCyan,
                    trackColor = HackCyan.copy(alpha = 0.1f)
                )
            }

            // Storage & Lag Status
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Storage Lag Impact",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSteelMuted
                    )
                    Text(
                        text = "Risk: ${diagnostics.lagAssessment.lagRiskScore}/100 • Latency: ${diagnostics.lagAssessment.estimatedIoLatencyMs}",
                        style = MaterialTheme.typography.labelSmall,
                        color = levelColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = { (diagnostics.storage.storageUsedPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = levelColor,
                    trackColor = SteelSurfaceVariant
                )
            }

            // Open Full Diagnostics Button
            Button(
                onClick = onOpenDiagnostics,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SteelSurfaceElevated,
                    contentColor = CyberCyan
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SteelBorder)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VIEW FULL SPECS & STORAGE LAG ANALYZER",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
