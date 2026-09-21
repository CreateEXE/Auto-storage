package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FileMetadata
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.LaserEmerald
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.SteelSurfaceContainer
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextSteelMuted
import com.example.ui.theme.TextSteelSecondary

@Composable
fun BatchRenameDialog(
    filesToRename: List<FileMetadata>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.82f)
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, SteelBorder, RoundedCornerShape(20.dp))
                .testTag("batch_rename_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SteelSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberCyan.copy(alpha = 0.15f))
                                .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = "Batch Rename",
                                tint = CyberCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "BATCH AUTO-RENAME PREVIEW",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextSilver,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${filesToRename.size} files queued for intelligent rename",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSteelSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSteelSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // List of rename previews
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filesToRename, key = { it.id }) { file ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SteelSurfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SteelBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = file.currentName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSteelMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "to",
                                        tint = LaserEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = file.suggestedName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = LaserEmerald,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SteelBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = TextSteelMuted)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = SteelSurface),
                        modifier = Modifier.testTag("confirm_batch_rename_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply Rename (${filesToRename.size})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
