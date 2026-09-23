package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DuplicateGroup
import com.example.data.model.FileCategory
import com.example.data.model.FileMetadata
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import com.example.ui.util.metallicPanel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DuplicateGroupCard(
    group: DuplicateGroup,
    selectedFileIds: Set<Long>,
    onToggleSelectFile: (Long) -> Unit,
    onDeleteSingleDuplicate: (FileMetadata) -> Unit,
    onCleanGroupDuplicates: (DuplicateGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val clipboardManager = LocalClipboardManager.current
    var hashCopied by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, HackDeepOrange.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            .testTag("duplicate_group_${group.groupId}"),
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(containerColor = HackBlack.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Duplicate Count, SHA-256 Content Hash, Savings Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(DangerRed.copy(alpha = 0.15f))
                            .border(1.dp, DangerRed.copy(alpha = 0.3f), RoundedCornerShape(2.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate Set",
                            tint = DangerRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${group.duplicateFiles.size + 1} Exact Content Matches",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSilver
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(group.fileHash))
                                    hashCopied = true
                                }
                                .padding(horizontal = 2.dp, vertical = 1.dp)
                        ) {
                            Icon(
                                imageVector = if (hashCopied) Icons.Default.CheckCircle else Icons.Default.Fingerprint,
                                contentDescription = "Copy SHA-256 Hash",
                                tint = if (hashCopied) LaserEmerald else CyberCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (hashCopied) "SHA-256 Copied!" else "SHA-256: ${group.fileHash.take(12)}...${group.fileHash.takeLast(4)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = if (hashCopied) LaserEmerald else CyberCyan,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DangerRed.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Reclaim space",
                            tint = DangerRed,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Free ${group.formattedSavings}",
                            style = MaterialTheme.typography.labelSmall,
                            color = DangerRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = SteelBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Original File (KEEP)
            Surface(
                shape = RoundedCornerShape(2.dp),
                color = LaserEmerald.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, LaserEmerald.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Original to keep",
                        tint = LaserEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = LaserEmerald.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "ORIGINAL (KEEP)",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LaserEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = group.originalFile.formattedSize,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSilver,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = group.originalFile.currentName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSilver,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Created: ${dateFormat.format(Date(group.originalFile.createdAt))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSteelMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Duplicate Copies (DELETE)
            for (duplicate in group.duplicateFiles) {
                val isSelected = selectedFileIds.contains(duplicate.id)
                Surface(
                    shape = RoundedCornerShape(2.dp),
                    color = HackBlack.copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) DangerRed else HackDeepOrange.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelectFile(duplicate.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = DangerRed,
                                checkmarkColor = SteelSurface,
                                uncheckedColor = TextSteelMuted
                            ),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = DangerRed.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "DUPLICATE COPY",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DangerRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = duplicate.formattedSize,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSteelSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = duplicate.currentName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextSilver,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { onDeleteSingleDuplicate(duplicate) },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("delete_duplicate_${duplicate.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete this copy",
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer Quick Action: Clean This Group
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { onCleanGroupDuplicates(group) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("clean_group_button_${group.groupId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clean Duplicates",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Copies (${group.formattedSavings})", fontSize = 12.sp)
                }
            }
        }
    }
}
