package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            .background(HackBlack),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 60f
            for (x in 0..(size.width / step).toInt()) {
                drawLine(HackDeepOrange.copy(alpha = 0.05f), Offset(x * step, 0f), Offset(x * step, size.height))
            }
            for (y in 0..(size.height / step).toInt()) {
                drawLine(HackDeepOrange.copy(alpha = 0.05f), Offset(0f, y * step), Offset(size.width, y * step))
            }
        }

        // Setup Window
        Column(
            modifier = Modifier
                .width(420.dp)
                .heightIn(min = 450.dp, max = 600.dp)
                .background(HackSlate.copy(alpha = 0.9f))
                .border(1.dp, HackDeepOrange, RoundedCornerShape(2.dp))
                .padding(1.dp)
                .border(1.dp, HackDeepOrange.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
        ) {
            // Title Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HackDeepOrange.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Settings, null, tint = HackDeepOrange, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "INITIALIZING CORE SYSTEMS",
                    color = HackDeepOrange,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Progress Header
            LinearProgressIndicator(
                progress = { (currentStep + 1).toFloat() / totalSteps },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = HackDeepOrange,
                trackColor = HackDeepOrange.copy(alpha = 0.1f)
            )

            // Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
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
            
            // Bottom Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HackBlack.copy(alpha = 0.5f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                RetroButton(
                    text = "REVERT", 
                    enabled = currentStep > 0,
                    onClick = { currentStep-- }
                )
                Spacer(modifier = Modifier.width(12.dp))
                RetroButton(
                    text = if (currentStep == totalSteps - 1) "EXECUTE" else "PROCEED >>",
                    enabled = true,
                    onClick = { 
                        if (currentStep < totalSteps - 1) {
                            currentStep++
                        } else {
                            viewModel.completeSetup()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun WelcomeStep() {
    Column {
        Text(
            "ESTABLISHING NEURAL LINK",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = HackCyan,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Welcome to the Steel Vault system interface. This sequence will synchronize the file intelligence engine and establish secure backend protocols.",
            fontSize = 13.sp,
            color = TextSilver,
            lineHeight = 20.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(HackDeepOrange.copy(alpha = 0.05f), RoundedCornerShape(2.dp))
                .border(1.dp, HackDeepOrange.copy(alpha = 0.1f), RoundedCornerShape(2.dp)),
            contentAlignment = Alignment.Center
        ) {
           Text(
               "STATUS: STANDBY",
               color = HackDeepOrange.copy(alpha = 0.5f),
               fontFamily = FontFamily.Monospace,
               fontSize = 10.sp
           )
        }
    }
}

@Composable
fun PermissionsStep() {
    val context = LocalContext.current
    var hasAllFilesAccess by remember { mutableStateOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            false
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
            "PROTOCOL AUTHORIZATION",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = HackCyan,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Steel Vault requires absolute indexing authority over the local data clusters.",
            fontSize = 11.sp,
            color = TextSteelSecondary,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(20.dp))
        
        PermissionItem(
            title = "ROOT STORAGE ACCESS",
            description = "Allows the AI engine to restructure and optimize file hierarchies.",
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
    }
}

@Composable
fun TermuxStep() {
    val context = LocalContext.current
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text(
            "BACKEND INTEGRATION",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = HackCyan,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Deploying the Termux bridge enables advanced cryptographic and CLI-based file operations.",
            fontSize = 11.sp,
            color = TextSteelSecondary,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(20.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HackBlack)
                .border(1.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    "CRITICAL: EXTERNAL LINK REQUIRED",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = DangerRed,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "1. Access Termux Node\n2. Inject authorization vector:\n   mkdir -p ~/.termux && echo \"allow-external-apps = true\" >> ~/.termux/termux.properties\n3. Restart Node.",
                    fontSize = 10.sp,
                    color = TextSilver,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        val setupCommand = "mkdir -p ~/.termux && echo \"allow-external-apps = true\" >> ~/.termux/termux.properties"

        RetroButton(
            text = "COPY LINK VECTOR",
            onClick = {
                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(setupCommand))
            }
        )
    }
}

@Composable
fun CompletionStep(viewModel: FileOrganizerViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "SYNCHRONIZATION COMPLETE",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = LaserEmerald,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "System is now ready to manifest. Initial scan sequence will trigger upon entering the desktop environment.",
            fontSize = 12.sp,
            color = TextSilver,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        Icon(
            Icons.Default.Check, 
            contentDescription = null, 
            tint = LaserEmerald,
            modifier = Modifier.size(64.dp)
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
            .border(1.dp, if (isGranted) LaserEmerald.copy(alpha = 0.3f) else HackDeepOrange.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            .background(HackSlate.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (isGranted) LaserEmerald else HackDeepOrange, fontFamily = FontFamily.Monospace)
            Text(description, fontSize = 9.sp, color = TextSteelMuted, fontFamily = FontFamily.Monospace)
        }
        if (isGranted) {
            Icon(Icons.Default.Check, contentDescription = null, tint = LaserEmerald, modifier = Modifier.size(20.dp))
        } else {
            RetroButton(text = "GRANT", onClick = onAction)
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
        shape = RoundedCornerShape(2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HackDeepOrange.copy(alpha = 0.1f),
            contentColor = if (enabled) HackDeepOrange else TextSteelMuted,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = TextSteelMuted
        ),
        modifier = Modifier
            .height(32.dp)
            .border(1.dp, if (enabled) HackDeepOrange else TextSteelMuted.copy(alpha = 0.3f), RoundedCornerShape(2.dp)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
    }
}
