package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.FileMetadata
import com.example.ui.FileOrganizerViewModel
import com.example.ui.components.BatchRenameDialog
import com.example.ui.components.FileCard
import com.example.ui.components.FileDetailDialog
import com.example.ui.components.StorageDashboardCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LaserEmerald
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.SteelSurfaceContainer
import com.example.ui.theme.SteelSurfaceVariant
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextSteelMuted
import com.example.ui.theme.TextSteelSecondary
import com.example.ui.util.metallicPanel
import kotlinx.coroutines.delay

@Composable
fun StorageVaultScreen(
    viewModel: FileOrganizerViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.storageStats.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val storageFreed by viewModel.storageFreed.collectAsStateWithLifecycle()
    val allFiles by viewModel.allFiles.collectAsStateWithLifecycle()
    val isLocked by viewModel.isVaultLocked.collectAsStateWithLifecycle()
    val vaultedFiles by viewModel.vaultedFiles.collectAsStateWithLifecycle()

    var showBatchRenameDialog by remember { mutableStateOf(false) }
    var selectedFileForDetail by remember { mutableStateOf<FileMetadata?>(null) }

    // TAP PATTERN STATE
    // Pattern: 1 - 1 - 1 - 111 (Single, Single, Single, Triple)
    var tapBurstCount by remember { mutableStateOf(0) }
    val tapSequence = remember { mutableStateListOf<Int>() }
    var lastTapTime by remember { mutableStateOf(0L) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "MimicScale"
    )

    // Reset pattern if inactive for 1.5 seconds
    LaunchedEffect(lastTapTime) {
        if (lastTapTime > 0) {
            delay(1500)
            if (System.currentTimeMillis() - lastTapTime >= 1500) {
                tapSequence.clear()
                tapBurstCount = 0
            }
        }
    }

    // Process tap bursts
    LaunchedEffect(tapBurstCount, lastTapTime) {
        if (lastTapTime > 0) {
            delay(350) // Wait for potential next tap in burst
            if (System.currentTimeMillis() - lastTapTime >= 350 && tapBurstCount > 0) {
                tapSequence.add(tapBurstCount)
                tapBurstCount = 0
                
                // Check pattern: 1, 1, 1, 3
                if (tapSequence.size == 4) {
                    if (tapSequence[0] == 1 && tapSequence[1] == 1 && tapSequence[2] == 1 && tapSequence[3] == 3) {
                        viewModel.unlockVaultByPattern()
                    }
                    tapSequence.clear()
                } else if (tapSequence.size > 4) {
                    tapSequence.clear()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 80.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Storage Dashboard
        StorageDashboardCard(
            stats = stats,
            modifier = Modifier.metallicPanel(cornerRadius = 14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // MIMIC SECURE VAULT ACCESS
        Text(
            text = "SECURE MIMIC VAULT",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = LaserEmerald,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .metallicPanel(cornerRadius = 16.dp)
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        if (isLocked) {
                            lastTapTime = System.currentTimeMillis()
                            tapBurstCount++
                        } else {
                            viewModel.lockVault()
                        }
                    }
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(160.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_mimic_vault),
                        contentDescription = "Mimic Vault Icon",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                2.dp,
                                if (isLocked) DangerRed.copy(alpha = 0.5f) else LaserEmerald.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentScale = ContentScale.Crop
                    )

                    if (isLocked) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(8.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = LaserEmerald.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = LaserEmerald,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isLocked) "CHAINED MIMIC SEALED" else "MIMIC UNLEASHED",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isLocked) TextSilver else LaserEmerald,
                    letterSpacing = 1.sp
                )

                Text(
                    text = if (isLocked) "Tap secret pattern to unchain" else "Click to seal vault again",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLocked) TextSteelMuted else LaserEmerald.copy(alpha = 0.7f)
                )
                
                // Visual feedback for pattern progress (debug/feedback)
                if (isLocked && tapSequence.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(tapSequence.size) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(LaserEmerald))
                        }
                    }
                }

                if (!isLocked && vaultedFiles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "SECURED ENTITIES: ${vaultedFiles.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        vaultedFiles.forEach { file ->
                            FileCard(
                                file = file,
                                onClick = { selectedFileForDetail = file },
                                onQuickRename = { newName -> viewModel.renameSingleFile(file, newName) }
                            )
                        }
                    }
                } else if (!isLocked && vaultedFiles.isEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "VAULT IS EMPTY. Move files here to hide them.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSteelMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Hybrid Engine Connection Telemetry
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .metallicPanel(cornerRadius = 14.dp, showBolts = false),
            colors = CardDefaults.cardColors(containerColor = SteelSurface.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isOnline) LaserEmerald.copy(alpha = 0.2f)
                                    else SteelSurfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (isOnline) LaserEmerald else TextSteelSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isOnline) "ONLINE ENRICHMENT ACTIVE" else "OFFLINE CORE ACTIVE",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) LaserEmerald else TextSteelSecondary
                            )
                            Text(
                                text = if (isOnline) "Connected to Gemini AI for deep metadata" else "Offline SHA-256 & ID3 regex parsing",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSteelMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SteelSurfaceContainer,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "CORE HASHING",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSteelMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "SHA-256 (Offline)",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SteelSurfaceContainer,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "FILE SYSTEM",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSteelMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "SAF / App Storage",
                                style = MaterialTheme.typography.bodySmall,
                                color = LaserEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Batch Action Operations
        Text(
            text = "STORAGE VAULT OPERATIONS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = CyberCyan,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Online AI Enrichment Button
        if (isOnline) {
            Button(
                onClick = { viewModel.enrichAllMetadataOnline() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("enrich_all_ai_button"),
                colors = ButtonDefaults.buttonColors(containerColor = LaserEmerald, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enrich All Metadata with Gemini AI", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 2. Batch Rename All
        Button(
            onClick = { showBatchRenameDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("batch_rename_all_button"),
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Review & Batch Rename (${stats.pendingRenameCount} Pending)", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Sort Images by Subtype
        Button(
            onClick = { viewModel.organizeImagesBySubtype() },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("sort_images_subtype_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = SteelSurfaceVariant,
                contentColor = TextSilver
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp), tint = LaserEmerald)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sort Images by Subtype (Screenshots, Designs, Info)", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Organize All Files by Category Folders
        Button(
            onClick = { viewModel.organizeAllFiles() },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("organize_all_categories_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = SteelSurfaceVariant,
                contentColor = TextSilver
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.FolderSpecial, contentDescription = null, modifier = Modifier.size(18.dp), tint = CyberCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sort All Files into Category Directories", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Clean All Duplicates Immediately
        if (stats.duplicateCount > 0) {
            Button(
                onClick = { viewModel.cleanAllDuplicates() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("clean_all_duplicates_vault_button"),
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Purge All ${stats.duplicateCount} Duplicates (${stats.formattedDuplicateSavings})", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 6. Reload Sample Files
        OutlinedButton(
            onClick = { viewModel.scanSampleStorage() },
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .testTag("reload_sample_files_button"),
            border = androidx.compose.foundation.BorderStroke(1.dp, SteelBorder),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSteelSecondary)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reload Device Test Files with Hashes", color = TextSteelSecondary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 7. Clear Scanned Index
        OutlinedButton(
            onClick = { viewModel.clearAllData() },
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .testTag("clear_database_button"),
            border = androidx.compose.foundation.BorderStroke(1.dp, SteelBorder),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSteelMuted)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset Scanned Database", color = TextSteelMuted)
        }
    }


    if (showBatchRenameDialog) {
        val candidates = allFiles.filter {
            it.suggestedName.isNotBlank() && it.suggestedName != it.currentName
        }
        BatchRenameDialog(
            filesToRename = candidates,
            onDismiss = { showBatchRenameDialog = false },
            onConfirm = {
                showBatchRenameDialog = false
                viewModel.batchRenameAll()
            }
        )
    }

    selectedFileForDetail?.let { file ->
        FileDetailDialog(
            file = file,
            onDismiss = { selectedFileForDetail = null },
            onRename = { newName -> viewModel.renameSingleFile(file, newName) },
            onSaveMetadata = { tags, summary, suggestedName ->
                viewModel.updateMetadata(file, tags, summary, suggestedName)
            },
            onDelete = {
                viewModel.deleteDuplicateFile(file)
            },
            onToggleVault = {
                viewModel.toggleVault(file)
            }
        )
    }
}
