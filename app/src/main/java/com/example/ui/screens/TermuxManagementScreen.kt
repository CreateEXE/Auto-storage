package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.termux.TermuxBridge
import com.example.termux.TermuxSetupManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermuxManagementScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val termuxSetupManager = remember { TermuxSetupManager(context) }
    
    var isInstalled by remember { mutableStateOf(termuxSetupManager.isTermuxInstalled()) }
    var storageSetup by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Simple check initially
        storageSetup = TermuxBridge(context).checkStorageSetup()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Termux Backend Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Backend Status", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Installed: ${if (isInstalled) "Yes" else "No"}")
                        Text("Storage Setup: ${if (storageSetup) "Yes" else "Checking..."}")
                    }
                }
            }
            
            item {
                Text("Diagnostics", style = MaterialTheme.typography.titleSmall)
            }
            
            item {
                Button(
                    onClick = {
                        termuxSetupManager.initializeStorage()
                        Toast.makeText(context, "Setup triggered", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trigger 'termux-setup-storage'")
                }
            }
            
            item {
                OutlinedButton(
                    onClick = { termuxSetupManager.openTermux() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Termux App")
                }
            }
        }
    }
}
