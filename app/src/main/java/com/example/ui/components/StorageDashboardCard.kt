package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileMetadata
import com.example.data.model.StorageStats
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LaserEmerald
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.SteelSurfaceContainer
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextSteelMuted
import com.example.ui.theme.TextSteelSecondary
import com.example.ui.util.metallicPanel

@Composable
fun StorageDashboardCard(
    stats: StorageStats,
    storageFreedTotal: Long = 0L,
    onBatchRenameClick: () -> Unit = {},
    onCleanDuplicatesClick: () -> Unit = {},
    onOrganizeAllClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .metallicPanel(cornerRadius = 16.dp)
            .testTag("storage_dashboard_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Storage Overview Title & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCyan.copy(alpha = 0.15f))
                            .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Storage",
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "STORAGE & DEDUPLICATION VAULT",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = TextSilver,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${stats.totalFiles} files indexed • ${stats.formattedTotalSize}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSteelSecondary
                        )
                    }
                }

                // Storage Freed Celebration Badge
                if (storageFreedTotal > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LaserEmerald.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LaserEmerald.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleanHands,
                                contentDescription = "Space Freed",
                                tint = LaserEmerald,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${FileMetadata.formatFileSize(storageFreedTotal)} Freed",
                                style = MaterialTheme.typography.labelSmall,
                                color = LaserEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stat Badges Row: Duplicates, Pending Renames, Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Duplicates Card
                StatPill(
                    icon = Icons.Default.Delete,
                    title = "Duplicates",
                    value = "${stats.duplicateCount} files",
                    subValue = if (stats.duplicateSavingsBytes > 0) "Free ${stats.formattedDuplicateSavings}" else "Zero Dups",
                    badgeColor = if (stats.duplicateCount > 0) DangerRed else LaserEmerald,
                    modifier = Modifier.weight(1f)
                )

                // Renames Card
                StatPill(
                    icon = Icons.Default.DriveFileRenameOutline,
                    title = "To Rename",
                    value = "${stats.pendingRenameCount} ready",
                    subValue = "${stats.renamedCount} renamed",
                    badgeColor = CyberCyan,
                    modifier = Modifier.weight(1f)
                )

                // Tags & Metadata Card
                StatPill(
                    icon = Icons.Default.LocalOffer,
                    title = "Metadata",
                    value = "${stats.taggedCount} tagged",
                    subValue = "${stats.organizedCount} organized",
                    badgeColor = LaserEmerald,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Organization Progress
            val progress = if (stats.totalFiles > 0) {
                (stats.organizedCount + stats.renamedCount).toFloat() / (stats.totalFiles * 2).coerceAtLeast(1)
            } else 0f

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ORGANIZATION & METADATA ENRICHMENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSteelMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = CyberCyan,
                    trackColor = SteelSurfaceContainer
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (stats.duplicateCount > 0) {
                    Button(
                        onClick = onCleanDuplicatesClick,
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("dashboard_clean_duplicates_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clean", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Dups", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (stats.pendingRenameCount > 0) {
                    Button(
                        onClick = onBatchRenameClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = SteelSurface),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("dashboard_batch_rename_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = "Rename", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rename (${stats.pendingRenameCount})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                FilledTonalButton(
                    onClick = onOrganizeAllClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("dashboard_organize_all_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SteelSurfaceContainer,
                        contentColor = TextSilver
                    )
                ) {
                    Icon(Icons.Default.FolderSpecial, contentDescription = "Organize", modifier = Modifier.size(15.dp), tint = LaserEmerald)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Organize", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subValue: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = SteelSurfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, SteelBorder.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = badgeColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSteelMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = TextSilver
            )
            Text(
                text = subValue,
                style = MaterialTheme.typography.labelSmall,
                color = badgeColor,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            )
        }
    }
}
