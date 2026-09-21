package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FileCategory
import com.example.data.model.FileMetadata
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LaserEmerald
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.SteelSurfaceContainer
import com.example.ui.theme.SteelSurfaceVariant
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextSteelMuted
import com.example.ui.theme.TextSteelSecondary
import com.example.ui.util.metallicPanel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FileDetailDialog(
    file: FileMetadata,
    onDismiss: () -> Unit,
    onRename: (newName: String) -> Unit,
    onSaveMetadata: (tags: String, summary: String, suggestedName: String) -> Unit,
    onDelete: () -> Unit,
    onToggleVault: () -> Unit
) {
    var editName by remember { mutableStateOf(file.currentName) }
    var editSuggestedName by remember { mutableStateOf(file.suggestedName) }
    var editTags by remember { mutableStateOf(file.tags) }
    var editSummary by remember { mutableStateOf(file.extractedSummary) }
    var isEditingName by remember { mutableStateOf(false) }

    val category = try {
        FileCategory.valueOf(file.category)
    } catch (e: Exception) {
        FileCategory.OTHER
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US)
    val creationDateStr = dateFormat.format(Date(file.createdAt))
    val modifiedDateStr = dateFormat.format(Date(file.modifiedAt))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .metallicPanel(cornerRadius = 18.dp)
                .testTag("file_detail_dialog"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SteelSurface.copy(alpha = 0.9f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
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
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (category) {
                                    FileCategory.AUDIO -> Icons.Default.MusicNote
                                    FileCategory.IMAGES -> Icons.Default.Image
                                    else -> category.icon
                                },
                                contentDescription = category.title,
                                tint = CyberCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "File Metadata & Inspection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSilver
                            )
                            Text(
                                text = "${file.category} • ${file.formattedSize}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberCyan
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSteelSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name Box / Rename Section
                if (isEditingName) {
                    Column {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Rename File") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_file_name_field"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = SteelBorder
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(onClick = { isEditingName = false }) {
                                Text("Cancel", color = TextSteelSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (editName.isNotBlank() && editName != file.currentName) {
                                        onRename(editName)
                                        isEditingName = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                                modifier = Modifier.testTag("confirm_rename_button")
                            ) {
                                Text("Apply Name", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SteelSurfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Current Filename",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSteelMuted
                                )
                                Text(
                                    text = file.currentName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSilver
                                )
                            }
                            IconButton(
                                onClick = { isEditingName = true },
                                modifier = Modifier.testTag("start_rename_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DriveFileRenameOutline,
                                    contentDescription = "Edit Name",
                                    tint = CyberCyan
                                )
                            }
                        }
                    }
                }

                // Smart Suggested Rename Banner if available
                if (file.suggestedName.isNotBlank() && file.suggestedName != file.currentName) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CyberCyan.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = "Suggested Name",
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Smart Renamed Suggestion",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = file.suggestedName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSilver,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Button(
                                onClick = {
                                    onRename(file.suggestedName)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                                modifier = Modifier.testTag("apply_suggested_rename_button")
                            ) {
                                Text("Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Duplicate Alert if duplicate
                if (file.isDuplicate) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DangerRed.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Duplicate",
                                tint = DangerRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Identified as exact SHA-256 byte duplicate. Safe to delete to free ${file.formattedSize}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DangerRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Music Rich Metadata Display
                if (file.category == "AUDIO") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SteelSurfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Album, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("MUSIC ID3 METADATA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CyberCyan)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Band/Artist: ${file.artist ?: "Unknown"}", style = MaterialTheme.typography.bodySmall, color = TextSilver)
                            Text("Album: ${file.album ?: "Unknown"}", style = MaterialTheme.typography.bodySmall, color = TextSilver)
                            Text("Track: #${file.trackNumber ?: 1} - ${file.trackTitle ?: file.currentName}", style = MaterialTheme.typography.bodySmall, color = TextSilver)
                            if (!file.genre.isNullOrBlank()) {
                                Text("Genre: ${file.genre} • Year: ${file.year ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = TextSteelSecondary)
                            }
                        }
                    }
                }

                // Image Subtype Display
                if (file.category == "IMAGES" && !file.imageSubtype.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = LaserEmerald.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LaserEmerald.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = LaserEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "IMAGE SUBTYPE: ${file.imageSubtype}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LaserEmerald
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Editable Tags Section
                Text(
                    text = "Metadata Tags (for instant search)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSilver
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = editTags,
                    onValueChange = { editTags = it },
                    placeholder = { Text("e.g. Work, Invoice, Tax, 2024, ACME") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_tags_input"),
                    leadingIcon = {
                        Icon(Icons.Default.LocalOffer, contentDescription = "Tags", tint = SecondaryTeal)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SteelBorder
                    )
                )

                // Render current tag chips
                val currentTagList = editTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                if (currentTagList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (tag in currentTagList) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SecondaryTeal.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "#$tag",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SecondaryTeal,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary / Extracted Information Section
                Text(
                    text = "Document Information & Summary",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSilver
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = editSummary,
                    onValueChange = { editSummary = it },
                    placeholder = { Text("Auto-filled information about document contents...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_summary_input"),
                    minLines = 2,
                    maxLines = 4,
                    leadingIcon = {
                        Icon(Icons.Default.Info, contentDescription = "Summary", tint = CyberCyan)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SteelBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Technical Metadata Details Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SteelSurfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        MetadataRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Creation Date",
                            value = creationDateStr
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        MetadataRow(
                            icon = Icons.Default.Storage,
                            label = "Last Modified",
                            value = modifiedDateStr
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        MetadataRow(
                            icon = Icons.Default.Folder,
                            label = "Target Folder",
                            value = file.organizationFolder.ifBlank { "Organized/${file.category}" }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        MetadataRow(
                            icon = Icons.Default.Fingerprint,
                            label = "SHA-256 Hash",
                            value = file.fileHash,
                            isMonospace = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Vault Action
                Button(
                    onClick = {
                        onToggleVault()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (file.isVaulted) CyberCyan else LaserEmerald,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("toggle_vault_detail_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (file.isVaulted) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (file.isVaulted) "RELEASE FROM VAULT" else "TRANSFER TO SECURE VAULT",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("delete_file_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete")
                    }

                    Button(
                        onClick = {
                            onSaveMetadata(editTags, editSummary, editSuggestedName)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                        modifier = Modifier.testTag("save_metadata_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = TextSteelMuted,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = TextSteelMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TextSilver,
            fontWeight = FontWeight.Medium,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            fontSize = if (isMonospace) 10.sp else 11.sp
        )
    }
}
