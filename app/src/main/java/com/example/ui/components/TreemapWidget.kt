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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.data.model.FileMetadata
import com.example.ui.theme.SteelSurfaceContainer
import com.example.ui.theme.TextSilver

@Composable
fun TreemapWidget(
    files: List<FileMetadata>,
    onRemove: () -> Unit,
    isHeld: Boolean = false
) {
    val treeData = remember(files) {
        files.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.sizeBytes } }
            .toList()
            .sortedByDescending { it.second }
    }

    val totalSize = remember(treeData) { treeData.sumOf { it.second }.coerceAtLeast(1) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .background(SteelSurfaceContainer)
                .padding(16.dp)
        ) {
            Text("Storage Treemap", style = MaterialTheme.typography.titleSmall, color = TextSilver)
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                var currentX = 0f
                val chartHeight = size.height
                val chartWidth = size.width

                treeData.forEach { (category, sizeBytes) ->
                    val width = (sizeBytes.toFloat() / totalSize) * chartWidth
                    
                    // "3D" effect using gradients and slight offset
                    val color = when(category) {
                        "IMAGES" -> Color(0xFF64B5F6)
                        "VIDEO" -> Color(0xFFE57373)
                        "AUDIO" -> Color(0xFF81C784)
                        "DOCUMENTS" -> Color(0xFFFFD54F)
                        else -> Color(0xFF90A4AE)
                    }

                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(color.copy(alpha = 0.9f), color.copy(alpha = 0.6f))
                        ),
                        topLeft = Offset(currentX, 0f),
                        size = Size(width, chartHeight)
                    )
                    
                    currentX += width
                }
            }
        }
    }
}
