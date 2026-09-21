package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.LaserEmerald
import com.example.ui.theme.SteelBackground
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.TextSilver
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Wraps any item with an intuitive long-press gesture that:
 * 1. Reveals the item's name/title in a floating pill while pressed/held until released.
 * 2. Visualizes "picking it up" by elevating, scaling up with a cyber-cyan glow.
 * 3. Allows freeform dragging/moving under the user's finger.
 * 4. Animates cleanly back or executes reorder when released.
 */
@Composable
fun LongPressDraggableItem(
    itemName: String,
    modifier: Modifier = Modifier,
    itemCategory: String? = null,
    enableDrag: Boolean = true,
    onDragDelta: ((deltaY: Float) -> Unit)? = null,
    onDragFinished: (() -> Unit)? = null,
    content: @Composable (isHeld: Boolean) -> Unit
) {
    var isHeld by remember { mutableStateOf(false) }
    val dragOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .zIndex(if (isHeld) 50f else 0f)
            .offset {
                IntOffset(
                    x = dragOffset.value.x.roundToInt(),
                    y = dragOffset.value.y.roundToInt()
                )
            }
            .scale(if (isHeld) 1.03f else 1.0f)
            .shadow(
                elevation = if (isHeld) 16.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = CyberCyan,
                ambientColor = CyberCyan
            )
            .pointerInput(itemName, enableDrag) {
                if (!enableDrag) return@pointerInput

                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        isHeld = true
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            dragOffset.snapTo(dragOffset.value + dragAmount)
                        }
                        onDragDelta?.invoke(dragAmount.y)
                    },
                    onDragEnd = {
                        isHeld = false
                        coroutineScope.launch {
                            dragOffset.animateTo(
                                targetValue = Offset.Zero,
                                animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f)
                            )
                        }
                        onDragFinished?.invoke()
                    },
                    onDragCancel = {
                        isHeld = false
                        coroutineScope.launch {
                            dragOffset.animateTo(
                                targetValue = Offset.Zero,
                                animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f)
                            )
                        }
                    }
                )
            }
    ) {
        content(isHeld)

        // Floating name badge displayed when held/long-pressed until released
        if (isHeld) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-36).dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = CyberCyan)
                    .testTag("floating_name_badge"),
                shape = RoundedCornerShape(20.dp),
                color = SteelBackground.copy(alpha = 0.96f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberCyan)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PanTool,
                        contentDescription = "Picked Up",
                        tint = CyberCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = itemName,
                        color = TextSilver,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (itemCategory != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• $itemCategory",
                            color = LaserEmerald,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
