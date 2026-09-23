package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FileOrganizerViewModel
import com.example.ui.theme.*

@Composable
fun SetupWizardScreen(viewModel: FileOrganizerViewModel) {
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 4
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF008080)), // Win95 Teal
        contentAlignment = Alignment.Center
    ) {
        // Setup Window
        Column(
            modifier = Modifier
                .width(360.dp)
                .heightIn(min = 400.dp, max = 550.dp)
                .background(Color(0xFFC0C0C0)) // Win95 Gray
                .border(2.dp, Color.White, RoundedCornerShape(0.dp))
                .border(4.dp, Color(0xFF808080), RoundedCornerShape(0.dp))
                .padding(4.dp)
        ) {
            // Title Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF000080)) // Win95 Navy
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Steel Vault Setup Wizard",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFFC0C0C0))
                        .border(1.dp, Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp))
                }
            }
            
            // Content Area
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Sidebar Logo area
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF808080)),
                    contentAlignment = Alignment.TopCenter
                ) {
                   Text(
                       "STEEL\nVAULT", 
                       color = Color(0xFFC0C0C0),
                       fontWeight = FontWeight.Black,
                       fontSize = 20.sp,
                       modifier = Modifier.padding(top = 20.dp)
                   )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Step Content
                Box(modifier = Modifier.weight(1f)) {
                    when (currentStep) {
                        0 -> WelcomeStep()
                        1 -> PermissionsStep()
                        2 -> TermuxStep()
                        3 -> CompletionStep(viewModel)
                    }
                }
            }
            
            // Divider
            HorizontalDivider(color = Color(0xFF808080), thickness = 1.dp)
            HorizontalDivider(color = Color.White, thickness = 1.dp)
            
            // Bottom Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                RetroButton(
                    text = "Back", 
                    enabled = currentStep > 0,
                    onClick = { currentStep-- }
                )
                Spacer(modifier = Modifier.width(8.dp))
                RetroButton(
                    text = if (currentStep == totalSteps - 1) "Finish" else "Next >",
                    enabled = true,
                    onClick = { 
                        if (currentStep < totalSteps - 1) {
                            currentStep++
                        } else {
                            viewModel.completeSetup()
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                RetroButton(
                    text = "Cancel",
                    onClick = { /* Could close app */ }
                )
            }
        }
    }
}

@Composable
fun WelcomeStep() {
    Column {
        Text(
            "Welcome to the Steel Vault Setup Wizard",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "This wizard will help you configure the Steel Vault file intelligence engine and backend services.",
            fontSize = 12.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "To continue, click Next.",
            fontSize = 12.sp,
            color = Color.Black
        )
    }
}

@Composable
fun PermissionsStep() {
    val context = LocalContext.current
    var hasAllFilesAccess by remember { mutableStateOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            false // Fallback for older
        }
    ) }
    
    val manageStorageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hasAllFilesAccess = Environment.isExternalStorageManager()
        }
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text(
            "System Permissions",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Steel Vault requires access to your storage to analyze and organize files.",
            fontSize = 11.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        PermissionItem(
            title = "All Files Access",
            description = "Required for full storage analysis and vault operations.",
            isGranted = hasAllFilesAccess,
            onAction = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    manageStorageLauncher.launch(intent)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            var hasNotificationPermission by remember { mutableStateOf(false) } // Simplification
            PermissionItem(
                title = "Notifications",
                description = "Used for scan progress and system alerts.",
                isGranted = hasNotificationPermission,
                onAction = { /* Trigger notification request */ }
            )
        }
    }
}

@Composable
fun TermuxStep() {
    val context = LocalContext.current
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text(
            "Termux Backend Configuration",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "The optional Termux backend provides advanced Linux CLI tools for file deduplication and processing.",
            fontSize = 11.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            "CRITICAL: Manual Action Required",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color.Red
        )
        Text(
            "1. Open Termux app\n2. Run this command:\n   mkdir -p ~/.termux && echo \"allow-external-apps = true\" >> ~/.termux/termux.properties\n3. EXIT Termux and restart it.",
            fontSize = 10.sp,
            color = Color.Black,
            lineHeight = 14.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        val setupCommand = "mkdir -p ~/.termux && echo \"allow-external-apps = true\" >> ~/.termux/termux.properties"

        RetroButton(
            text = "Copy Setup Command",
            onClick = {
                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(setupCommand))
            }
        )
    }
}

@Composable
fun CompletionStep(viewModel: FileOrganizerViewModel) {
    Column {
        Text(
            "Completing Setup",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Steel Vault is now ready to use. Your initial scan will begin once you enter the desktop.",
            fontSize = 12.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(20.dp))
        Icon(
            Icons.Default.Check, 
            contentDescription = null, 
            tint = Color(0xFF008000),
            modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF808080))
            .background(Color.White)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
            Text(description, fontSize = 9.sp, color = Color.Gray)
        }
        if (isGranted) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF008000), modifier = Modifier.size(16.dp))
        } else {
            RetroButton(text = "Grant", onClick = onAction)
        }
    }
}

@Composable
fun RetroButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFC0C0C0),
            contentColor = if (enabled) Color.Black else Color.Gray,
            disabledContainerColor = Color(0xFFC0C0C0),
            disabledContentColor = Color.Gray
        ),
        modifier = Modifier
            .height(24.dp)
            .border(1.dp, if (enabled) Color.White else Color.Gray, RoundedCornerShape(0.dp))
            .padding(0.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Normal)
    }
}
