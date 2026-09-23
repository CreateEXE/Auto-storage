package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FileOrganizerViewModel
import com.example.ui.theme.*

@Composable
fun TerminalWidget(
    viewModel: FileOrganizerViewModel,
    onClose: () -> Unit
) {
    var command by remember { mutableStateOf("") }
    val logs = remember { mutableStateListOf<String>("SYSTEM CORE :: THE WORLD OS V1.0", "WARNING: RESOURCE INTEGRITY CHECK IN PROGRESS...") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HackBlack)
            .padding(16.dp)
    ) {
        // Output Area
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            color = HackBlack,
            shape = RoundedCornerShape(2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, HackDeepOrange.copy(alpha = 0.3f))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(logs) { log ->
                    Text(
                        text = log.uppercase(),
                        color = if (log.startsWith(">")) HackCyan else if (log.startsWith("!")) DangerRed else HackDeepOrange,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Area
        Row(
            modifier = Modifier.fillMaxWidth().background(HackDeepOrange.copy(alpha = 0.05f)).border(1.dp, HackDeepOrange.copy(alpha = 0.2f), RoundedCornerShape(2.dp)).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = ">>",
                color = HackCyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("CORE_CMD_INPUT...", fontSize = 11.sp, color = TextSteelMuted, fontFamily = FontFamily.Monospace) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            )
            
            IconButton(
                onClick = {
                    if (command.isNotBlank()) {
                        logs.add("> $command")
                        logs.add("ACCESSING CORE FRAGMENT...")
                        command = ""
                    }
                }
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = HackCyan)
            }
        }
    }
}
