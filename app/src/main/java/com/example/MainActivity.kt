package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.termux.TermuxSetupManager
import com.example.ui.FileOrganizerViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: FileOrganizerViewModel by viewModels()
    private lateinit var termuxSetupManager: TermuxSetupManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        termuxSetupManager = TermuxSetupManager(this)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var showTermuxDialog by remember { mutableStateOf(false) }
                var showStorageDialog by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {

                        HomeScreen(viewModel = viewModel)

                        FloatingActionButton(
                            onClick = {
                                if (!hasFullStorageAccess()) {
                                    showStorageDialog = true
                                } else if (termuxSetupManager.isTermuxInstalled()) {
                                    showTermuxDialog = true
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Termux not installed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Terminal,
                                contentDescription = "Termux"
                            )
                        }
                    }

                    if (showStorageDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                showStorageDialog = false
                            },
                            title = {
                                Text("Storage Access Required")
                            },
                            text = {
                                Text(
                                    "Auto-storage needs full file access to scan and organize files across your device."
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showStorageDialog = false
                                        openFullStorageAccessSettings()
                                    }
                                ) {
                                    Text("Grant Access")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        showStorageDialog = false
                                    }
                                ) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    if (showTermuxDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                showTermuxDialog = false
                            },
                            title = {
                                Text("Termux Integration")
                            },
                            text = {
                                Text("Manage local Termux backend storage.")
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        if (!hasFullStorageAccess()) {
                                            showTermuxDialog = false
                                            showStorageDialog = true
                                        } else {
                                            termuxSetupManager.initializeStorage()
                                            showTermuxDialog = false
                                        }
                                    }
                                ) {
                                    Text("Setup Storage")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        termuxSetupManager.openTermux()
                                        showTermuxDialog = false
                                    }
                                ) {
                                    Text("Open Termux")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (::termuxSetupManager.isInitialized) {
            if (hasFullStorageAccess()) {
                Toast.makeText(
                    this,
                    "Full storage access granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun hasFullStorageAccess(): Boolean {
        return Environment.isExternalStorageManager()
    }

    private fun openFullStorageAccessSettings() {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:$packageName")
            )

            startActivity(intent)
        } catch (_: Exception) {
            val intent = Intent(
                Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            )

            startActivity(intent)
        }
    }
}
