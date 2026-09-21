package com.example.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A modifier that adds a metallic steel plate look with decorative bolts.
 */
fun Modifier.metallicPanel(
    cornerRadius: Dp = 12.dp,
    showBolts: Boolean = true,
    accentColor: Color = Color(0xFF94A3B8) // SteelChrome
): Modifier = this.then(
    Modifier.drawBehind {
        val width = size.width
        val height = size.height
        val radiusPx = cornerRadius.toPx()

        // 1. Base Metallic Gradient
        val metallicBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF1E293B), // Dark slate
                Color(0xFF334155), // Slate 700
                Color(0xFF1E293B), // Dark slate
                Color(0xFF0F172A)  // Very dark slate
            ),
            start = Offset(0f, 0f),
            end = Offset(width, height)
        )
        
        drawRoundRect(
            brush = metallicBrush,
            size = size,
            cornerRadius = CornerRadius(radiusPx)
        )

        // 2. Subtle Brushed Metal Texture (Horizontal Lines)
        val lineCount = (height / 8).toInt()
        for (i in 0 until lineCount) {
            val y = i * 8f
            drawLine(
                color = Color.White.copy(alpha = 0.03f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        // 3. Plate Borders (Beveled Look)
        // Top highlight
        drawRoundRect(
            color = Color.White.copy(alpha = 0.1f),
            size = size,
            cornerRadius = CornerRadius(radiusPx),
            style = Stroke(width = 2f)
        )
        
        // Inner shadow/darkening at bottom
        val path = Path().apply {
            moveTo(0f, height - radiusPx)
            quadraticTo(0f, height, radiusPx, height)
            lineTo(width - radiusPx, height)
            quadraticTo(width, height, width, height - radiusPx)
        }
        drawPath(
            path = path,
            color = Color.Black.copy(alpha = 0.3f),
            style = Stroke(width = 3f)
        )

        // 4. Decorative Bolts
        if (showBolts) {
            val boltSize = 8.dp.toPx()
            val padding = 10.dp.toPx()
            
            val boltCoords = listOf(
                Offset(padding, padding),
                Offset(width - padding, padding),
                Offset(padding, height - padding),
                Offset(width - padding, height - padding)
            )

            boltCoords.forEach { coord ->
                // Bolt Hole
                drawCircle(
                    color = Color.Black.copy(alpha = 0.5f),
                    radius = boltSize / 2,
                    center = coord
                )
                
                // Bolt Head
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFCBD5E1), Color(0xFF64748B)),
                        center = coord,
                        radius = boltSize / 2.5f
                    ),
                    radius = boltSize / 2.5f,
                    center = coord
                )
                
                // Screw Slot
                drawLine(
                    color = Color.Black.copy(alpha = 0.4f),
                    start = Offset(coord.x - boltSize/4, coord.y),
                    end = Offset(coord.x + boltSize/4, coord.y),
                    strokeWidth = 2f
                )
            }
        }
    }
)

/**
 * Decorative overlapping metal plate effect
 */
fun Modifier.metalPlateOverlay(
    alpha: Float = 0.1f
): Modifier = this.then(
    Modifier.drawBehind {
        val width = size.width
        val height = size.height
        
        // Draw a diagonal "strip" as if another plate is welded on
        val path = Path().apply {
            moveTo(width * 0.7f, 0f)
            lineTo(width, 0f)
            lineTo(width, height * 0.3f)
            close()
        }
        
        drawPath(
            path = path,
            color = Color.White.copy(alpha = alpha)
        )
        
        // Weld points
        val weldSize = 4.dp.toPx()
        drawCircle(Color.White.copy(alpha = alpha * 2), radius = weldSize, center = Offset(width * 0.85f, height * 0.05f))
    }
)
