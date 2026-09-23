package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import com.example.ui.theme.*
import com.example.data.model.FileMetadata
import com.example.ui.theme.TextSteelMuted

@Composable
fun TreemapWidget(
    files: List<FileMetadata>,
    onRemove: () -> Unit,
    isHeld: Boolean = false
) {
    val largestFiles = remember(files) {
        files.sortedByDescending { it.sizeBytes }.take(4)
    }

    WidgetContainer(
        title = "Resource Anomalies",
        icon = Icons.Default.Warning,
        iconColor = HackDeepOrange,
        onRemove = onRemove,
        isHeld = isHeld
    ) {
        if (largestFiles.isEmpty()) {
            Text("No large fragments detected.", color = TextSteelMuted, fontSize = 11.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                largestFiles.forEach { file ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = file.currentName.uppercase(),
                                color = TextSilver,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "CLASS: ${file.category}",
                                color = TextSteelMuted,
                                fontSize = 8.sp
                            )
                        }
                        Text(
                            text = file.formattedSize,
                            color = HackCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
