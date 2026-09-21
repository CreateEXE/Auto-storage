package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AutoSortMode
import com.example.data.model.SortOption
import com.example.ui.FileOrganizerViewModel
import com.example.ui.components.SortSelectionDialog
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LaserEmerald
import com.example.ui.theme.SteelBackground
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.SteelSurfaceContainer
import com.example.ui.theme.SteelSurfaceVariant
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextSteelMuted
import com.example.ui.theme.TextSteelSecondary
import com.example.ui.util.metallicPanel
import com.example.ui.util.metalPlateOverlay

@Composable
fun SettingsScreen(
    viewModel: FileOrganizerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val currentSortOption by viewModel.currentSortOption.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val allFiles by viewModel.allFiles.collectAsStateWithLifecycle()

    var showSortDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SteelBackground)
            .metalPlateOverlay(alpha = 0.05f)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
            .testTag("settings_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(SteelSurface, CircleShape)
                    .border(1.dp, SteelBorder, CircleShape)
                    .testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = CyberCyan
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "SETTINGS & ENGINE",
                    color = TextSilver,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Auto-sort algorithms, vault rules, and CI pipeline",
                    color = TextSteelMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --------------------------------------------------------------------
        // 1. AUTO-SORT & SORTING CONFIGURATION
        // --------------------------------------------------------------------
        SettingsSectionHeader(
            icon = Icons.AutoMirrored.Filled.Sort,
            title = "AUTO-SORT & ORDERING ENGINE",
            accentColor = CyberCyan
        )

        Card(
            modifier = Modifier.fillMaxWidth().metallicPanel(cornerRadius = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Auto-Sort Mode Toggle
                SettingsToggleRow(
                    title = "Auto-Sort Library",
                    description = "Automatically sort and organize files dynamically",
                    icon = Icons.Default.AutoFixHigh,
                    iconTint = LaserEmerald,
                    checked = userSettings.autoSortEnabled,
                    onCheckedChange = { viewModel.setAutoSortEnabled(it) },
                    testTag = "settings_auto_sort_toggle"
                )

                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SteelBorder.copy(alpha = 0.6f)))
                Spacer(modifier = Modifier.height(14.dp))

                // Default Sort Criterion Picker
                Text(
                    text = "ACTIVE SORT CRITERION",
                    color = TextSteelSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSortDialog = true }
                        .testTag("settings_sort_picker_card"),
                    colors = CardDefaults.cardColors(containerColor = SteelSurfaceContainer),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(SteelBorder)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentSortOption.displayName,
                                color = CyberCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentSortOption.description,
                                color = TextSteelMuted,
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(CyberCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Change",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SteelBorder.copy(alpha = 0.6f)))
                Spacer(modifier = Modifier.height(14.dp))

                // Target Auto-Sort Folder Hierarchy
                Text(
                    text = "TARGET AUTO-SORT FOLDER STRUCTURE",
                    color = TextSteelSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AutoSortMode.values().forEach { mode ->
                        val isSelected = userSettings.autoSortDestinationMode == mode
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) CyberCyan.copy(alpha = 0.12f)
                                    else SteelSurfaceVariant.copy(alpha = 0.4f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) CyberCyan else SteelBorder.copy(alpha = 0.5f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setAutoSortDestinationMode(mode) }
                                .padding(12.dp)
                                .testTag("settings_autosort_mode_${mode.name}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode.displayName,
                                        color = if (isSelected) CyberCyan else TextSilver,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = mode.description,
                                        color = TextSteelMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
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

                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SteelBorder.copy(alpha = 0.6f)))
                Spacer(modifier = Modifier.height(14.dp))

                // Auto-Sort on Scan toggle
                SettingsToggleRow(
                    title = "Auto-Sort on Import / Scan",
                    description = "Categorize and route files immediately after indexing",
                    icon = Icons.Default.Folder,
                    iconTint = CyberCyan,
                    checked = userSettings.autoSortOnScan,
                    onCheckedChange = { viewModel.setAutoSortOnScan(it) },
                    testTag = "settings_auto_sort_on_scan_toggle"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Auto-Suggest Renames toggle
                SettingsToggleRow(
                    title = "Auto-Suggest Renaming Rules",
                    description = "Evaluate active rules on matching files during auto-sort",
                    icon = Icons.Default.DriveFileRenameOutline,
                    iconTint = LaserEmerald,
                    checked = userSettings.autoSuggestRenamesOnScan,
                    onCheckedChange = { viewModel.setAutoSuggestRenamesOnScan(it) },
                    testTag = "settings_auto_suggest_renames_toggle"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action: Run Auto-Sort Now
                Button(
                    onClick = { viewModel.autoSortAllFiles() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_run_autosort_now_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = LaserEmerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        tint = SteelBackground,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Run Auto-Sort Pipeline Now (${allFiles.size} Files)",
                        color = SteelBackground,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --------------------------------------------------------------------
        // 2. RENAMING ENGINE DEFAULTS
        // --------------------------------------------------------------------
        SettingsSectionHeader(
            icon = Icons.Default.DriveFileRenameOutline,
            title = "DEFAULT RENAMING FORMATS",
            accentColor = LaserEmerald
        )

        Card(
            modifier = Modifier.fillMaxWidth().metallicPanel(cornerRadius = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DEFAULT CASING CONVENTION",
                    color = TextSteelSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val casingOptions = listOf("TITLE_CASE", "SNAKE_CASE", "LOWERCASE", "UPPERCASE")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    casingOptions.forEach { casing ->
                        val isSelected = userSettings.defaultCasing == casing
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) LaserEmerald.copy(alpha = 0.15f)
                                    else SteelSurfaceContainer
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) LaserEmerald else SteelBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setDefaultCasing(casing) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = casing.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                color = if (isSelected) LaserEmerald else TextSilver,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "DEFAULT SPACE REPLACEMENT",
                    color = TextSteelSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val spaceOptions = listOf(
                    "_" to "Underscore (_)",
                    "-" to "Hyphen (-)",
                    " " to "Preserve ( )"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    spaceOptions.forEach { (rep, label) ->
                        val isSelected = userSettings.defaultSpaceReplacement == rep
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) LaserEmerald.copy(alpha = 0.15f)
                                    else SteelSurfaceContainer
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) LaserEmerald else SteelBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setDefaultSpaceReplacement(rep) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) LaserEmerald else TextSilver,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --------------------------------------------------------------------
        // 3. DEDUPLICATION & INTEGRITY
        // --------------------------------------------------------------------
        SettingsSectionHeader(
            icon = Icons.Default.Security,
            title = "INTEGRITY & DEDUPLICATION",
            accentColor = CyberCyan
        )

        Card(
            modifier = Modifier.fillMaxWidth().metallicPanel(cornerRadius = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsToggleRow(
                    title = "Cryptographic SHA-256 Checksums",
                    description = "Calculate exact SHA-256 byte hashes for 100% duplicate accuracy",
                    icon = Icons.Default.Security,
                    iconTint = CyberCyan,
                    checked = userSettings.autoHashSha256OnScan,
                    onCheckedChange = { viewModel.setAutoHashSha256OnScan(it) },
                    testTag = "settings_sha256_toggle"
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsToggleRow(
                    title = "Group Duplicates by Hash",
                    description = "Isolate redundant files in Duplicates tab with batch cleaner",
                    icon = Icons.Default.ContentCopy,
                    iconTint = CyberCyan,
                    checked = userSettings.groupDuplicatesByHash,
                    onCheckedChange = { viewModel.setGroupDuplicatesByHash(it) },
                    testTag = "settings_group_duplicates_toggle"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --------------------------------------------------------------------
        // 4. AI & CLOUD INTELLIGENCE
        // --------------------------------------------------------------------
        SettingsSectionHeader(
            icon = Icons.Default.AutoAwesome,
            title = "HYBRID INTELLIGENCE & GEMINI AI",
            accentColor = LaserEmerald
        )

        Card(
            modifier = Modifier.fillMaxWidth().metallicPanel(cornerRadius = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsToggleRow(
                    title = "Gemini AI Enrichment",
                    description = "Generate smart document summaries and category tags when online",
                    icon = Icons.Default.AutoAwesome,
                    iconTint = LaserEmerald,
                    checked = userSettings.enableGeminiOnlineEnrichment,
                    onCheckedChange = { viewModel.setGeminiOnlineEnrichment(it) },
                    testTag = "settings_gemini_ai_toggle"
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SteelSurfaceContainer, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (isOnline) LaserEmerald else TextSteelMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOnline) "Network Status: Online (Gemini AI Ready)" else "Network Status: Offline (Local Heuristics Only)",
                        color = if (isOnline) LaserEmerald else TextSteelMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --------------------------------------------------------------------
        // 5. GITHUB ACTIONS CI/CD BUILD PIPELINE
        // --------------------------------------------------------------------
        SettingsSectionHeader(
            icon = Icons.Default.Code,
            title = "GITHUB ACTIONS CI/CD PIPELINE",
            accentColor = CyberCyan
        )

        Card(
            modifier = Modifier.fillMaxWidth().metallicPanel(cornerRadius = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(CyberCyan.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "CI/CD WORKFLOW CONFIGURED",
                            color = TextSilver,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = ".github/workflows/build.yml",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Automated continuous integration pipeline ready for GitHub repositories:",
                    color = TextSteelSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SteelSurfaceContainer, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CiPipelineStepItem("1. Trigger", "Pushes & PRs to main/master, manual dispatch")
                    CiPipelineStepItem("2. Environment", "Ubuntu Latest + Temurin JDK 21 + Android SDK")
                    CiPipelineStepItem("3. Gradle Wrapper", "Gradle 9.3.1 with automated executable permissions")
                    CiPipelineStepItem("4. Verification", "Automated Robolectric Unit Tests (45 test suite)")
                    CiPipelineStepItem("5. Build Artifact", "Generates and uploads signed Debug APK (14-day retention)")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --------------------------------------------------------------------
        // 6. MAINTENANCE & RESTORE
        // --------------------------------------------------------------------
        SettingsSectionHeader(
            icon = Icons.Default.RestartAlt,
            title = "MAINTENANCE & RESTORE",
            accentColor = TextSteelSecondary
        )

        Card(
            modifier = Modifier.fillMaxWidth().metallicPanel(cornerRadius = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.resetSettingsToDefault() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_reset_defaults_button"),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(SteelBorder)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = TextSilver,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Settings to Default", color = TextSilver, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.clearAllData() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_clear_index_button"),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(DangerRed.copy(alpha = 0.5f))
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear File Index", color = DangerRed, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showSortDialog) {
        SortSelectionDialog(
            currentSortOption = currentSortOption,
            isAutoSortEnabled = userSettings.autoSortEnabled,
            onSelectSortOption = { viewModel.setSortOption(it) },
            onToggleAutoSort = { viewModel.setAutoSortEnabled(it) },
            onDismiss = { showSortDialog = false },
            onTriggerAutoSortNow = { viewModel.autoSortAllFiles() }
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = accentColor,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconTint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = TextSilver,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    color = TextSteelMuted,
                    fontSize = 11.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = LaserEmerald,
                checkedTrackColor = LaserEmerald.copy(alpha = 0.25f),
                uncheckedThumbColor = TextSteelSecondary,
                uncheckedTrackColor = SteelBorder
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun CiPipelineStepItem(
    step: String,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = step,
            color = CyberCyan,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = detail,
            color = TextSteelSecondary,
            fontSize = 11.sp
        )
    }
}
