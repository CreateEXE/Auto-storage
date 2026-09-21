package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileCategory
import com.example.data.model.FileMetadata
import com.example.ui.theme.CleanGreen
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
fun FileCard(
    file: FileMetadata,
    onClick: () -> Unit,
    onQuickRename: (suggestedName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val category = try {
        FileCategory.valueOf(file.category)
    } catch (e: Exception) {
        FileCategory.OTHER
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val formattedDate = dateFormat.format(Date(file.createdAt))
    val hasSuggestedRename = file.suggestedName.isNotBlank() && file.suggestedName != file.currentName

    Card(
        modifier = modifier
            .fillMaxWidth()
            .metallicPanel(cornerRadius = 14.dp, showBolts = false)
            .clickable(onClick = onClick)
            .testTag("file_card_${file.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Category Icon, Filename, Size, Duplicate / Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (category) {
                                FileCategory.AUDIO -> CyberCyan.copy(alpha = 0.18f)
                                FileCategory.IMAGES -> LaserEmerald.copy(alpha = 0.18f)
                                else -> category.color.copy(alpha = 0.18f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            category == FileCategory.AUDIO -> Icons.Default.MusicNote
                            category == FileCategory.IMAGES -> Icons.Default.Image
                            else -> category.icon
                        },
                        contentDescription = category.title,
                        tint = when (category) {
                            FileCategory.AUDIO -> CyberCyan
                            FileCategory.IMAGES -> LaserEmerald
                            else -> category.color
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name and Meta
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.currentName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSilver,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = TextSteelMuted,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSteelSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSteelMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = file.formattedSize,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSilver,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Duplicate / Status Tag
                if (file.isDuplicate) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DangerRed.copy(alpha = 0.18f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Duplicate",
                                tint = DangerRed,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "DUP",
                                style = MaterialTheme.typography.labelSmall,
                                color = DangerRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                } else if (file.status == "RENAMED") {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CleanGreen.copy(alpha = 0.18f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CleanGreen.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Renamed",
                                tint = CleanGreen,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "RENAMED",
                                style = MaterialTheme.typography.labelSmall,
                                color = CleanGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                } else if (file.status == "ORGANIZED") {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyberCyan.copy(alpha = 0.18f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "SORTED",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Music rich tags display if audio
            if (file.category == "AUDIO" && (!file.artist.isNullOrBlank() || !file.album.isNullOrBlank())) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SteelSurfaceContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = buildString {
                                if (!file.artist.isNullOrBlank()) append(file.artist)
                                if (!file.album.isNullOrBlank()) append(" • ${file.album}")
                                if (file.trackNumber != null) append(" • Trk #${file.trackNumber}")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!file.genre.isNullOrBlank()) {
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CyberCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = file.genre!!,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }

            // Image Subtype badge if image
            if (file.category == "IMAGES" && !file.imageSubtype.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val subtypeName = when (file.imageSubtype) {
                        "SCREENSHOT" -> "Screenshot"
                        "PRODUCT_DESIGN" -> "Product Design / Wireframe"
                        "INFORMATION" -> "Information / Infographic"
                        "CAMERA_PHOTO" -> "Camera Photo"
                        "WALLPAPER" -> "Wallpaper"
                        else -> file.imageSubtype!!
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = LaserEmerald.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LaserEmerald.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "SUBTYPE: $subtypeName",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = LaserEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Content hash snippet badge
            if (file.fileHash.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Content Hash",
                        tint = TextSteelMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SHA-256: ${file.fileHash.take(12)}...${file.fileHash.takeLast(4)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextSteelMuted,
                        fontSize = 10.sp
                    )
                    if (file.organizationFolder.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = TextSteelMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = file.organizationFolder,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSteelMuted,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Smart Suggested Rename Banner if available
            if (hasSuggestedRename) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CyberCyan.copy(alpha = 0.10f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = "Suggested Rename",
                                tint = CyberCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = file.suggestedName,
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberCyan,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        FilledTonalButton(
                            onClick = { onQuickRename(file.suggestedName) },
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("quick_rename_button_${file.id}"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = CyberCyan.copy(alpha = 0.25f),
                                contentColor = CyberCyan
                            ),
                            contentPadding = ButtonDefaults.TextButtonContentPadding
                        ) {
                            Text("Rename", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Summary / Information preview if present
            if (file.extractedSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = file.extractedSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSteelSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
            }

            // Tags row
            if (file.tagList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (tag in file.tagList.take(5)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SteelSurfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SteelBorder)
                        ) {
                            Text(
                                text = "#$tag",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryTeal,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (file.tagList.size > 5) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SteelSurfaceContainer
                        ) {
                            Text(
                                text = "+${file.tagList.size - 5}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSteelMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
