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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FileOrganizerViewModel
import com.example.ui.theme.GunmetalBackground
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextSteelMuted

@Composable
fun TerminalWidget(
    viewModel: FileOrganizerViewModel,
    onClose: () -> Unit
) {
    var command by remember { mutableStateOf("") }
    val logs = remember { mutableStateListOf<String>("Termux Terminal Environment.", "Note: Ensure 'Allow external apps' is enabled in Termux settings.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GunmetalBackground)
            .padding(16.dp)
    ) {
        // Output Area
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            color = Color.Black,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SteelBorder)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(logs) { log ->
                    Text(
                        text = log,
                        color = if (log.startsWith(">")) Color.Cyan else if (log.startsWith("!")) Color.Red else Color.Green,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Area
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ ",
                color = Color.Green,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
            
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("enter shell command...", fontSize = 12.sp, color = TextSteelMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
            
            IconButton(
                onClick = {
                    if (command.isNotBlank()) {
                        logs.add("> $command")
                        // In a real app, we'd use TermuxBridge.runCommand(command)
                        logs.add("Executing in Termux backend...")
                        command = ""
                    }
                }
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = Color.Cyan)
            }
        }
    }
}
