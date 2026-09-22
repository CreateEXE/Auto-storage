package com.example.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.termux.TermuxCommandReceiver
import com.example.ui.theme.SteelSurfaceContainer
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextSteelMuted

@Composable
fun TerminalWidget(
    onRemove: () -> Unit,
    isHeld: Boolean = false
) {
    val context = LocalContext.current
    val logs = remember { mutableStateListOf<String>() }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val stdout = intent?.getStringExtra(TermuxCommandReceiver.EXTRA_STDOUT) ?: ""
                val stderr = intent?.getStringExtra(TermuxCommandReceiver.EXTRA_STDERR) ?: ""
                val exitCode = intent?.getIntExtra(TermuxCommandReceiver.EXTRA_EXIT_CODE, -1) ?: -1
                
                if (stdout.isNotBlank()) logs.add("> $stdout")
                if (stderr.isNotBlank()) logs.add("! $stderr")
                if (exitCode != -1) logs.add("Exit: $exitCode")
            }
        }
        
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(TermuxCommandReceiver.ACTION_COMMAND_RESULT),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .background(SteelSurfaceContainer)
                .padding(16.dp)
        ) {
            Text("Termux Command Logs", style = MaterialTheme.typography.titleSmall, color = TextSilver)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.Black, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                LazyColumn {
                    items(logs) { log ->
                        Text(
                            text = log,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = if (log.startsWith("!")) Color.Red else Color.Green
                        )
                    }
                }
            }
        }
    }
}
