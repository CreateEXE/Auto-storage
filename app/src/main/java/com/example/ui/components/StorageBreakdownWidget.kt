package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.FileMetadata
import com.example.ui.theme.SteelSurfaceContainer
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextSteelMuted
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.LinearProgressIndicator
import com.example.ui.theme.HackCyan
import com.example.ui.theme.HackDeepOrange
import com.example.ui.theme.HackBlack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun StorageBreakdownWidget(
    storageBreakdown: Map<String, Long>,
    onRemove: () -> Unit,
    isHeld: Boolean = false
) {
    WidgetContainer(
        title = "Data Distribution",
        icon = Icons.Default.Category,
        iconColor = HackCyan,
        onRemove = onRemove,
        isHeld = isHeld
    ) {
        if (storageBreakdown.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text("No data nodes indexed", color = TextSteelMuted, fontSize = 11.sp)
            }
        } else {
            val total = storageBreakdown.values.sum().coerceAtLeast(1L)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                storageBreakdown.toList().sortedByDescending { it.second }.take(6).forEach { (label, size) ->
                    val percentage = size.toFloat() / total
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = label.uppercase(),
                            modifier = Modifier.weight(1f),
                            color = TextSilver,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(HackDeepOrange.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(percentage)
                                    .fillMaxHeight()
                                    .background(HackCyan)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(percentage * 100).toInt()}%",
                            color = HackCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
