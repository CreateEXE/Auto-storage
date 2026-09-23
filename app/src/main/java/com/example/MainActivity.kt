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
import com.example.ui.screens.DesktopScreen
import com.example.ui.screens.HomeScreen
import androidx.compose.runtime.collectAsState
import com.example.ui.screens.SetupWizardScreen
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
                val settings by viewModel.userSettings.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!settings.setupCompleted) {
                        SetupWizardScreen(viewModel = viewModel)
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            DesktopScreen(viewModel = viewModel)
                        }
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
