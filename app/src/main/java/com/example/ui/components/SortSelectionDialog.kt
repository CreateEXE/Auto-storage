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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SortOption
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.LaserEmerald
import com.example.ui.theme.SteelBackground
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.SteelSurfaceContainer
import com.example.ui.theme.SteelSurfaceVariant
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextSteelMuted
import com.example.ui.theme.TextSteelSecondary

@Composable
fun SortSelectionDialog(
    currentSortOption: SortOption,
    isAutoSortEnabled: Boolean,
    onSelectSortOption: (SortOption) -> Unit,
    onToggleAutoSort: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onTriggerAutoSortNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOption by remember { mutableStateOf(currentSortOption) }
    var autoSort by remember { mutableStateOf(isAutoSortEnabled) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .testTag("sort_selection_dialog"),
            color = SteelSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                                .size(36.dp)
                                .background(CyberCyan.copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, CyberCyan.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SORT & ORDER",
                                color = TextSilver,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Configure smart vault indexing",
                                color = TextSteelMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSteelSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto-Sort Mode Toggle Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SteelSurfaceContainer),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (autoSort) LaserEmerald.copy(alpha = 0.4f) else SteelBorder
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = if (autoSort) LaserEmerald else TextSteelMuted,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Auto-Sort Engine",
                                    color = TextSilver,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (autoSort) "Dynamically re-indexes files" else "Manual static ordering",
                                    color = TextSteelMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = autoSort,
                            onCheckedChange = {
                                autoSort = it
                                onToggleAutoSort(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = LaserEmerald,
                                checkedTrackColor = LaserEmerald.copy(alpha = 0.25f),
                                uncheckedThumbColor = TextSteelSecondary,
                                uncheckedTrackColor = SteelBorder
                            ),
                            modifier = Modifier.testTag("dialog_auto_sort_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SELECT SORT CRITERION",
                    color = TextSteelSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // List of Sort Options
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(SortOption.values()) { option ->
                        val isSelected = option == selectedOption
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) CyberCyan.copy(alpha = 0.12f)
                                    else SteelSurfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CyberCyan else SteelBorder.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedOption = option
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("sort_option_${option.name}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option.displayName,
                                        color = if (isSelected) CyberCyan else TextSilver,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = option.description,
                                        color = TextSteelMuted,
                                        fontSize = 10.sp
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(CyberCyan, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = SteelBackground,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onTriggerAutoSortNow()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_run_auto_sort_button"),
                        border = ButtonDefaults.outlinedButtonBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(LaserEmerald.copy(alpha = 0.6f))
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            tint = LaserEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Auto-Sort All", color = LaserEmerald, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            onSelectSortOption(selectedOption)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_apply_sort_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Apply", color = SteelBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
