package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.data.worker.Sha256WorkerState
import com.example.ui.FileOrganizerViewModel
import com.example.ui.components.*
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LaserEmerald
import com.example.ui.theme.SecondaryTeal
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FileOrganizerViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableIntStateOf(0) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    var selectedFileForDetail by remember { mutableStateOf<FileMetadata?>(null) }
    var showBatchRenameDialog by remember { mutableStateOf(false) }
    var selectedDuplicateIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var selectedImageSubtype by remember { mutableStateOf<String?>(null) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showDeviceSpecsScreen by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // State collection from ViewModel
    val allFiles by viewModel.allFiles.collectAsStateWithLifecycle()
    val filteredFiles by viewModel.filteredFiles.collectAsStateWithLifecycle()
    val storageStats by viewModel.storageStats.collectAsStateWithLifecycle()
    val duplicateGroups by viewModel.duplicateGroups.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanProgressText by viewModel.scanProgressText.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val storageFreed by viewModel.storageFreed.collectAsStateWithLifecycle()
    val allUniqueTags by viewModel.allUniqueTags.collectAsStateWithLifecycle()
    val lastActionMessage by viewModel.lastActionMessage.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val currentSortOption by viewModel.currentSortOption.collectAsStateWithLifecycle()
    val autoSortEnabled by viewModel.autoSortEnabled.collectAsStateWithLifecycle()
    val dashboardWidgets by viewModel.dashboardWidgets.collectAsStateWithLifecycle()
    val recentFiles by viewModel.recentFiles.collectAsStateWithLifecycle()
    val isVaultLocked by viewModel.isVaultLocked.collectAsStateWithLifecycle()
    val deviceDiagnostics by viewModel.deviceDiagnostics.collectAsStateWithLifecycle()
    val sha256WorkerState by viewModel.sha256WorkerState.collectAsStateWithLifecycle()

    var showWidgetPicker by remember { mutableStateOf(false) }

    if (showDeviceSpecsScreen) {
        DeviceSpecsScreen(
            viewModel = viewModel,
            onNavigateBack = { showDeviceSpecsScreen = false }
        )
        return
    }

    if (showSettingsScreen) {
        SettingsScreen(
            viewModel = viewModel,
            onNavigateBack = { showSettingsScreen = false }
        )
        return
    }

    // SAF Directory Picker Launcher
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.scanCustomDirectory(uri)
        }
    }

    // Display snackbar message upon events
    LaunchedEffect(lastActionMessage) {
        lastActionMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissActionMessage()
        }
    }

    // Filter files if image subtype is selected
    val activeFilesList = remember(filteredFiles, selectedImageSubtype) {
        if (selectedImageSubtype == null) {
            filteredFiles
        } else {
            filteredFiles.filter { it.imageSubtype == selectedImageSubtype }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(SteelBackground)
            .metalPlateOverlay(alpha = 0.05f),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "STEEL VAULT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextSilver,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Online / Offline Status Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isOnline) LaserEmerald.copy(alpha = 0.18f) else SteelSurfaceContainer,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isOnline) LaserEmerald.copy(alpha = 0.4f) else SteelBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isOnline) LaserEmerald else TextSteelMuted)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isOnline) "ONLINE AI" else "OFFLINE CORE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isOnline) LaserEmerald else TextSteelMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "HIGH-TECH FILE INTELLIGENCE & DEDUP ENGINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SteelSurface.copy(alpha = 0.95f),
                    titleContentColor = TextSilver,
                    actionIconContentColor = CyberCyan
                ),
                modifier = Modifier.border(0.dp, Color.Transparent).metallicPanel(cornerRadius = 0.dp, showBolts = true),
                actions = {
                    // Pick Folder SAF
                    IconButton(
                        onClick = { folderPickerLauncher.launch(null) },
                        modifier = Modifier.testTag("open_saf_folder_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Select Storage Folder",
                            tint = CyberCyan
                        )
                    }

                    // Rescan Files Button
                    IconButton(
                        onClick = { viewModel.scanSampleStorage() },
                        modifier = Modifier.testTag("rescan_storage_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan Storage",
                            tint = TextSilver
                        )
                    }

                    // Device Specs & System Lag Telemetry
                    IconButton(
                        onClick = { showDeviceSpecsScreen = true },
                        modifier = Modifier.testTag("top_bar_specs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Device Specs & Storage Lag",
                            tint = LaserEmerald
                        )
                    }

                    // Settings Screen Button
                    IconButton(
                        onClick = { showSettingsScreen = true },
                        modifier = Modifier.testTag("top_bar_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = CyberCyan
                        )
                    }

                    // Overflow Menu
                    Box {
                        IconButton(
                            onClick = { showDropdownMenu = true },
                            modifier = Modifier.testTag("overflow_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = TextSilver
                            )
                        }

                        DropdownMenu(
                            expanded = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Device Specs & RAM / Storage Impact") },
                                onClick = {
                                    showDropdownMenu = false
                                    showDeviceSpecsScreen = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = LaserEmerald)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings & Auto-Sort Options") },
                                onClick = {
                                    showDropdownMenu = false
                                    showSettingsScreen = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null, tint = CyberCyan)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort Images by Subtype (Screenshots/Designs)") },
                                onClick = {
                                    showDropdownMenu = false
                                    viewModel.organizeImagesBySubtype()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = LaserEmerald)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Organize All into Categories") },
                                onClick = {
                                    showDropdownMenu = false
                                    viewModel.organizeAllFiles()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = CyberCyan)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Enrich All Metadata with Gemini AI") },
                                onClick = {
                                    showDropdownMenu = false
                                    viewModel.enrichAllMetadataOnline()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LaserEmerald)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Apply Active Renaming Rules") },
                                onClick = {
                                    showDropdownMenu = false
                                    viewModel.applyActiveRulesToAll()
                                },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.Rule, contentDescription = null, tint = CyberCyan)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Run SHA-256 Duplicate Worker") },
                                onClick = {
                                    showDropdownMenu = false
                                    viewModel.startSha256DuplicateWorker(false)
                                    currentTab = 2
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = CyberCyan)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Purge All Duplicates") },
                                onClick = {
                                    showDropdownMenu = false
                                    viewModel.cleanAllDuplicates()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = DangerRed)
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .metallicPanel(cornerRadius = 16.dp),
                containerColor = Color.Transparent,
                tonalElevation = 8.dp
            ) {
                // 0. Dashboard
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.setCurrentTab(0) },
                    icon = {
                        Icon(Icons.Default.Dashboard, contentDescription = "Dashboard")
                    },
                    label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan.copy(alpha = 0.15f),
                        unselectedIconColor = TextSteelSecondary,
                        unselectedTextColor = TextSteelMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                // 1. Files
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.setCurrentTab(1) },
                    icon = {
                        Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = "Files")
                    },
                    label = { Text("Files", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan.copy(alpha = 0.15f),
                        unselectedIconColor = TextSteelSecondary,
                        unselectedTextColor = TextSteelMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_files")
                )

                // 2. Duplicates
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.setCurrentTab(2) },
                    icon = {
                        Box {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicates")
                            if (storageStats.duplicateCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd)
                                        .background(DangerRed, CircleShape)
                                )
                            }
                        }
                    },
                    label = { Text("Duplicates", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DangerRed,
                        selectedTextColor = DangerRed,
                        indicatorColor = DangerRed.copy(alpha = 0.15f),
                        unselectedIconColor = TextSteelSecondary,
                        unselectedTextColor = TextSteelMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_duplicates")
                )

                // 3. Custom Rules
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { viewModel.setCurrentTab(3) },
                    icon = {
                        Icon(Icons.AutoMirrored.Filled.Rule, contentDescription = "Rules")
                    },
                    label = { Text("Rules", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan.copy(alpha = 0.15f),
                        unselectedIconColor = TextSteelSecondary,
                        unselectedTextColor = TextSteelMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_rules")
                )

                // 4. Music Hub
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { viewModel.setCurrentTab(4) },
                    icon = {
                        Icon(Icons.Default.LibraryMusic, contentDescription = "Music")
                    },
                    label = { Text("Music", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan.copy(alpha = 0.15f),
                        unselectedIconColor = TextSteelSecondary,
                        unselectedTextColor = TextSteelMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_music")
                )

                // 5. Storage Vault
                NavigationBarItem(
                    selected = currentTab == 5,
                    onClick = { viewModel.setCurrentTab(5) },
                    icon = {
                        Icon(Icons.Default.Storage, contentDescription = "Vault")
                    },
                    label = { Text("Vault", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LaserEmerald,
                        selectedTextColor = LaserEmerald,
                        indicatorColor = LaserEmerald.copy(alpha = 0.15f),
                        unselectedIconColor = TextSteelSecondary,
                        unselectedTextColor = TextSteelMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_vault")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> DashboardTab(
                    widgets = dashboardWidgets,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    storageStats = storageStats,
                    onScan = { viewModel.scanSampleStorage() },
                    onClean = { viewModel.cleanAllDuplicates() },
                    onOrganize = { viewModel.organizeAllFiles() },
                    recentFiles = recentFiles,
                    isVaultLocked = isVaultLocked,
                    onVaultClick = { viewModel.setCurrentTab(5) },
                    onFileClick = { selectedFileForDetail = it },
                    onRemoveWidget = { viewModel.toggleDashboardWidget(it) },
                    onAddWidgetClick = { showWidgetPicker = true },
                    diagnostics = deviceDiagnostics,
                    onOpenDiagnostics = { showDeviceSpecsScreen = true }
                )

                1 -> FilesTab(
                    files = activeFilesList,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    selectedCategory = selectedCategory,
                    onSelectCategory = {
                        viewModel.selectCategory(it)
                        selectedImageSubtype = null
                    },
                    selectedImageSubtype = selectedImageSubtype,
                    onSelectImageSubtype = { selectedImageSubtype = it },
                    selectedTag = selectedTag,
                    onSelectTag = { viewModel.selectTag(it) },
                    allTags = allUniqueTags,
                    currentSortOption = currentSortOption,
                    autoSortEnabled = autoSortEnabled,
                    onOpenSortDialog = { showSortDialog = true },
                    onToggleAutoSort = { viewModel.setAutoSortEnabled(it) },
                    onAutoSortAll = { viewModel.autoSortAllFiles() },
                    onFileClick = { selectedFileForDetail = it },
                    onQuickRename = { file, newName -> viewModel.renameSingleFile(file, newName) }
                )

                2 -> DuplicatesTab(
                    duplicateGroups = duplicateGroups,
                    storageStats = storageStats,
                    workerState = sha256WorkerState,
                    onStartSha256Worker = { forceRehash -> viewModel.startSha256DuplicateWorker(forceRehash) },
                    onCancelSha256Worker = { viewModel.cancelSha256DuplicateWorker() },
                    selectedFileIds = selectedDuplicateIds,
                    onToggleSelectFile = { id ->
                        selectedDuplicateIds = if (selectedDuplicateIds.contains(id)) {
                            selectedDuplicateIds - id
                        } else {
                            selectedDuplicateIds + id
                        }
                    },
                    onSelectAllDuplicates = {
                        selectedDuplicateIds = duplicateGroups.flatMap { it.duplicateFiles.map { f -> f.id } }.toSet()
                    },
                    onDeselectAll = {
                        selectedDuplicateIds = emptySet()
                    },
                    onDeleteSingleDuplicate = { viewModel.deleteDuplicateFile(it) },
                    onCleanGroupDuplicates = { group ->
                        val ids = group.duplicateFiles.map { it.id }.toSet()
                        viewModel.deleteSelectedDuplicates(ids)
                    },
                    onCleanAllDuplicates = { viewModel.cleanAllDuplicates() },
                    onDeleteSelected = {
                        viewModel.deleteSelectedDuplicates(selectedDuplicateIds)
                        selectedDuplicateIds = emptySet()
                    }
                )

                3 -> RenamingRulesScreen(viewModel = viewModel)

                4 -> MusicHubScreen(viewModel = viewModel)

                5 -> StorageVaultScreen(viewModel = viewModel)
            }

            // Scanning progress overlay
            if (isScanning) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SteelBackground.copy(alpha = 0.90f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = CyberCyan,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "ANALYZING & COMPUTING SHA-256 HASHES...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSilver,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = scanProgressText,
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberCyan,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog
    selectedFileForDetail?.let { file ->
        FileDetailDialog(
            file = file,
            onDismiss = { selectedFileForDetail = null },
            onRename = { newName ->
                viewModel.renameSingleFile(file, newName)
            },
            onSaveMetadata = { tags, summary, suggestedName ->
                viewModel.updateMetadata(file, tags, summary, suggestedName)
            },
            onDelete = {
                viewModel.deleteDuplicateFile(file)
            },
            onToggleVault = {
                viewModel.toggleVault(file)
            }
        )
    }

    // Batch Rename Preview Dialog
    if (showBatchRenameDialog) {
        val candidates = allFiles.filter {
            it.suggestedName.isNotBlank() && it.suggestedName != it.currentName
        }
        BatchRenameDialog(
            filesToRename = candidates,
            onDismiss = { showBatchRenameDialog = false },
            onConfirm = { viewModel.batchRenameAll() }
        )
    }

    // Sort Selection Dialog
    if (showSortDialog) {
        SortSelectionDialog(
            currentSortOption = currentSortOption,
            isAutoSortEnabled = autoSortEnabled,
            onSelectSortOption = { viewModel.setSortOption(it) },
            onToggleAutoSort = { viewModel.setAutoSortEnabled(it) },
            onDismiss = { showSortDialog = false },
            onTriggerAutoSortNow = { viewModel.autoSortAllFiles() }
        )
    }

    if (showWidgetPicker) {
        WidgetPicker(
            activeWidgets = dashboardWidgets,
            onToggleWidget = { viewModel.toggleDashboardWidget(it) },
            onDismiss = { showWidgetPicker = false }
        )
    }
}

@Composable
private fun DashboardTab(
    widgets: List<DashboardWidget>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    storageStats: com.example.data.model.StorageStats,
    onScan: () -> Unit,
    onClean: () -> Unit,
    onOrganize: () -> Unit,
    recentFiles: List<FileMetadata>,
    isVaultLocked: Boolean,
    onVaultClick: () -> Unit,
    onFileClick: (FileMetadata) -> Unit,
    onRemoveWidget: (DashboardWidget) -> Unit,
    onAddWidgetClick: () -> Unit,
    diagnostics: com.example.data.system.DeviceDiagnostics,
    onOpenDiagnostics: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "COMMAND CENTER",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = LaserEmerald,
                letterSpacing = 2.sp
            )
        }

        items(widgets) { widget ->
            when (widget) {
                DashboardWidget.SEARCH_BAR -> SearchWidget(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onRemove = { onRemoveWidget(widget) }
                )
                DashboardWidget.STORAGE_INFO -> StorageInfoWidget(
                    stats = storageStats,
                    onRemove = { onRemoveWidget(widget) }
                )
                DashboardWidget.DEVICE_SPECS -> DeviceSpecsWidget(
                    diagnostics = diagnostics,
                    onOpenDiagnostics = onOpenDiagnostics,
                    onRemove = { onRemoveWidget(widget) }
                )
                DashboardWidget.QUICK_ACTIONS -> QuickActionsWidget(
                    onScan = onScan,
                    onClean = onClean,
                    onOrganize = onOrganize,
                    onRemove = { onRemoveWidget(widget) }
                )
                DashboardWidget.RECENT_FILES -> RecentFilesWidget(
                    files = recentFiles,
                    onFileClick = onFileClick,
                    onRemove = { onRemoveWidget(widget) }
                )
                DashboardWidget.VAULT_STATUS -> VaultStatusWidget(
                    isLocked = isVaultLocked,
                    onClick = onVaultClick,
                    onRemove = { onRemoveWidget(widget) }
                )
                else -> { /* Other widgets handled as needed */ }
            }
        }

        item {
            OutlinedButton(
                onClick = onAddWidgetClick,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, SteelBorder.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = CyberCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Customize Dashboard / Add Widgets", color = TextSteelSecondary)
            }
        }
    }
}

@Composable
private fun WidgetPicker(
    activeWidgets: List<DashboardWidget>,
    onToggleWidget: (DashboardWidget) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SteelSurface,
        title = {
            Text("DASHBOARD CUSTOMIZATION", color = TextSilver, fontWeight = FontWeight.Black, fontSize = 16.sp)
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(DashboardWidget.values()) { widget ->
                    val isActive = activeWidgets.contains(widget)
                    Surface(
                        onClick = { onToggleWidget(widget) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isActive) LaserEmerald.copy(alpha = 0.1f) else SteelSurfaceContainer,
                        border = BorderStroke(1.dp, if (isActive) LaserEmerald else SteelBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isActive,
                                onCheckedChange = { onToggleWidget(widget) },
                                colors = CheckboxDefaults.colors(checkedColor = LaserEmerald)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = widget.title, color = TextSilver, fontWeight = FontWeight.Bold)
                                Text(text = widget.description, color = TextSteelMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("DONE", color = CyberCyan)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilesTab(
    files: List<FileMetadata>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: FileCategory?,
    onSelectCategory: (FileCategory?) -> Unit,
    selectedImageSubtype: String?,
    onSelectImageSubtype: (String?) -> Unit,
    selectedTag: String?,
    onSelectTag: (String?) -> Unit,
    allTags: List<String>,
    currentSortOption: SortOption,
    autoSortEnabled: Boolean,
    onOpenSortDialog: () -> Unit,
    onToggleAutoSort: (Boolean) -> Unit,
    onAutoSortAll: () -> Unit,
    onFileClick: (FileMetadata) -> Unit,
    onQuickRename: (FileMetadata, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("files_list_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Search Box
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search by filename, tags, artist, summary...", color = TextSteelMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CyberCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search", tint = TextSteelSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_files_field"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = SteelBorder,
                    focusedTextColor = TextSilver,
                    unfocusedTextColor = TextSilver,
                    focusedContainerColor = SteelSurface,
                    unfocusedContainerColor = SteelSurface
                )
            )
        }

        // 2. Auto-Sort & Quick Sorting Controls Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SteelSurface)
                    .border(1.dp, SteelBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Auto-Sort Mode toggle chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (autoSortEnabled) LaserEmerald.copy(alpha = 0.15f)
                            else SteelSurfaceVariant.copy(alpha = 0.4f)
                        )
                        .border(
                            1.dp,
                            if (autoSortEnabled) LaserEmerald.copy(alpha = 0.5f) else SteelBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggleAutoSort(!autoSortEnabled) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("auto_sort_quick_toggle"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        tint = if (autoSortEnabled) LaserEmerald else TextSteelMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (autoSortEnabled) "Auto-Sort: ON" else "Auto-Sort: OFF",
                        color = if (autoSortEnabled) LaserEmerald else TextSteelMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Active Sort Criterion Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCyan.copy(alpha = 0.12f))
                        .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { onOpenSortDialog() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("quick_sort_picker_chip"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sort: ${currentSortOption.shortName}",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Auto-Sort All button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LaserEmerald)
                        .clickable { onAutoSortAll() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("quick_auto_sort_all_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sort All",
                        color = SteelBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Category Filter Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onSelectCategory(null) },
                        label = { Text("All Files", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.25f),
                            selectedLabelColor = CyberCyan,
                            containerColor = SteelSurface,
                            labelColor = TextSteelSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == null,
                            borderColor = if (selectedCategory == null) CyberCyan else SteelBorder
                        ),
                        modifier = Modifier.testTag("filter_all")
                    )
                }

                items(FileCategory.values()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onSelectCategory(category) },
                        label = { Text(category.title, fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.title,
                                modifier = Modifier.size(15.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = category.color.copy(alpha = 0.25f),
                            selectedLabelColor = category.color,
                            containerColor = SteelSurface,
                            labelColor = TextSteelSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == category,
                            borderColor = if (selectedCategory == category) category.color else SteelBorder
                        ),
                        modifier = Modifier.testTag("filter_${category.name.lowercase()}")
                    )
                }
            }
        }

        // 3. Image Subtype sub-filter row (Screenshots, Product Designs, Infographics, Camera Photos)
        if (selectedCategory == null || selectedCategory == FileCategory.IMAGES) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SteelSurfaceContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SteelBorder)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "IMAGE CLASSIFICATION FILTER",
                            style = MaterialTheme.typography.labelSmall,
                            color = LaserEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val subtypes = listOf(
                                null to "All Subtypes",
                                "SCREENSHOT" to "Screenshots",
                                "PRODUCT_DESIGN" to "Product Designs",
                                "INFORMATION" to "Information",
                                "CAMERA_PHOTO" to "Camera Photos"
                            )
                            items(subtypes) { (key, label) ->
                                FilterChip(
                                    selected = selectedImageSubtype == key,
                                    onClick = { onSelectImageSubtype(key) },
                                    label = { Text(label, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = LaserEmerald.copy(alpha = 0.25f),
                                        selectedLabelColor = LaserEmerald,
                                        containerColor = SteelSurface,
                                        labelColor = TextSteelSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedImageSubtype == key,
                                        borderColor = if (selectedImageSubtype == key) LaserEmerald else SteelBorder
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Tags Filter Row (if tags exist)
        if (allTags.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = "Tags",
                        tint = SecondaryTeal,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tags Filter: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSteelMuted,
                        fontSize = 11.sp
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedTag == null,
                                onClick = { onSelectTag(null) },
                                label = { Text("Any Tag", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondaryTeal.copy(alpha = 0.25f),
                                    selectedLabelColor = SecondaryTeal,
                                    containerColor = SteelSurface,
                                    labelColor = TextSteelSecondary
                                )
                            )
                        }
                        items(allTags.take(12)) { tag ->
                            FilterChip(
                                selected = selectedTag == tag,
                                onClick = { onSelectTag(if (selectedTag == tag) null else tag) },
                                label = { Text("#$tag", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondaryTeal.copy(alpha = 0.25f),
                                    selectedLabelColor = SecondaryTeal,
                                    containerColor = SteelSurface,
                                    labelColor = TextSteelSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Files List Count / Empty
        if (files.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SteelSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SteelBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                            contentDescription = "No Files",
                            tint = TextSteelMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Files Matched",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSilver
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing filters or search queries, or tap the Refresh icon to re-index device storage.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSteelMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(files, key = { it.id }) { file ->
                FileCard(
                    file = file,
                    onClick = { onFileClick(file) },
                    onQuickRename = { newName -> onQuickRename(file, newName) }
                )
            }
        }
    }
}

@Composable
private fun DuplicatesTab(
    duplicateGroups: List<DuplicateGroup>,
    storageStats: com.example.data.model.StorageStats,
    workerState: Sha256WorkerState,
    onStartSha256Worker: (Boolean) -> Unit,
    onCancelSha256Worker: () -> Unit,
    selectedFileIds: Set<Long>,
    onToggleSelectFile: (Long) -> Unit,
    onSelectAllDuplicates: () -> Unit,
    onDeselectAll: () -> Unit,
    onDeleteSingleDuplicate: (FileMetadata) -> Unit,
    onCleanGroupDuplicates: (DuplicateGroup) -> Unit,
    onCleanAllDuplicates: () -> Unit,
    onDeleteSelected: () -> Unit
) {
    var showConfirmDeleteAllDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteSelectedDialog by remember { mutableStateOf(false) }
    var showForceRehashConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteAllDialog = false },
            containerColor = SteelSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = DangerRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Purge All Duplicate Files?", color = TextSilver, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "This will permanently delete ${storageStats.duplicateCount} redundant duplicate copies across ${duplicateGroups.size} groups to reclaim ${storageStats.formattedDuplicateSavings} of storage space.\n\nThe original file in each group will be safely preserved.",
                    color = TextSteelSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDeleteAllDialog = false
                        onCleanAllDuplicates()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White)
                ) {
                    Text("Purge All Duplicates")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmDeleteAllDialog = false },
                    border = BorderStroke(1.dp, SteelBorder)
                ) {
                    Text("Cancel", color = TextSilver)
                }
            }
        )
    }

    if (showConfirmDeleteSelectedDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteSelectedDialog = false },
            containerColor = SteelSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Selected Copies?", color = TextSilver, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Permanently delete the ${selectedFileIds.size} selected duplicate file(s)?",
                    color = TextSteelSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDeleteSelectedDialog = false
                        onDeleteSelected()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White)
                ) {
                    Text("Delete Selected")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmDeleteSelectedDialog = false },
                    border = BorderStroke(1.dp, SteelBorder)
                ) {
                    Text("Cancel", color = TextSilver)
                }
            }
        )
    }

    if (showForceRehashConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showForceRehashConfirmDialog = false },
            containerColor = SteelSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = CyberCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Force SHA-256 Full Re-Hash?", color = TextSilver, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "This will recalculate 256-bit cryptographic byte checksums for all files in storage via the background worker. Existing hashes will be refreshed and all byte duplicates re-grouped.",
                    color = TextSteelSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showForceRehashConfirmDialog = false
                        onStartSha256Worker(true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) {
                    Text("Start Full Re-Hash")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showForceRehashConfirmDialog = false },
                    border = BorderStroke(1.dp, SteelBorder)
                ) {
                    Text("Cancel", color = TextSilver)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("duplicates_list_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. SHA-256 Background Worker Control Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        if (workerState.isRunning) CyberCyan.copy(alpha = 0.6f) else SteelBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .testTag("sha256_worker_controller_card"),
                colors = CardDefaults.cardColors(containerColor = SteelSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header row: Icon, Title, Status badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (workerState.isRunning) CyberCyan.copy(alpha = 0.2f)
                                        else LaserEmerald.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "SHA-256 Engine",
                                    tint = if (workerState.isRunning) CyberCyan else LaserEmerald,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SHA-256 Background Worker",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSilver
                                )
                                Text(
                                    text = "Cryptographic byte hashing to identify duplicates",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSteelMuted
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (workerState.isRunning) CyberCyan.copy(alpha = 0.2f) else SteelSurfaceContainer,
                            border = BorderStroke(1.dp, if (workerState.isRunning) CyberCyan else SteelBorder)
                        ) {
                            Text(
                                text = if (workerState.isRunning) "RUNNING" else "READY",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (workerState.isRunning) CyberCyan else LaserEmerald
                            )
                        }
                    }

                    // Running State UI
                    if (workerState.isRunning) {
                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { workerState.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = CyberCyan,
                            trackColor = SteelSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${workerState.progressPercent}% (${workerState.processedCount} of ${workerState.totalCount} hashed)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSilver
                            )
                            Text(
                                text = "${workerState.duplicatesFound} duplicates identified",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DangerRed
                            )
                        }

                        if (workerState.currentFilename.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Hashing: ${workerState.currentFilename}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = CyberCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onCancelSha256Worker,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                            border = BorderStroke(1.dp, DangerRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cancel Background Worker", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        // Idle / Completed state
                        workerState.summary?.let { summary ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = SteelSurfaceContainer,
                                border = BorderStroke(1.dp, SteelBorder.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = summary.statusMessage,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSilver
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = CyberCyan.copy(alpha = 0.12f),
                                            border = BorderStroke(0.5.dp, CyberCyan.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = "${summary.hashesComputed} Hashed",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 10.sp,
                                                color = CyberCyan
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = DangerRed.copy(alpha = 0.12f),
                                            border = BorderStroke(0.5.dp, DangerRed.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = "${summary.duplicatesFound} Duplicates",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 10.sp,
                                                color = DangerRed
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = LaserEmerald.copy(alpha = 0.12f),
                                            border = BorderStroke(0.5.dp, LaserEmerald.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = "${summary.formattedSavings} Reclaimable",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 10.sp,
                                                color = LaserEmerald
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onStartSha256Worker(false) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(38.dp)
                                    .testTag("run_sha256_worker_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Run SHA-256 Worker", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showForceRehashConfirmDialog = true },
                                border = BorderStroke(1.dp, SteelBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("force_rehash_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSilver)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Force Re-Hash", fontSize = 11.sp, color = TextSilver)
                            }
                        }
                    }
                }
            }
        }

        // 2. Hero Header Card for Duplicates & Deletion
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        if (duplicateGroups.isNotEmpty()) DangerRed.copy(alpha = 0.5f) else LaserEmerald.copy(alpha = 0.5f),
                        RoundedCornerShape(16.dp)
                    )
                    .testTag("duplicate_cleaner_hero"),
                colors = CardDefaults.cardColors(containerColor = SteelSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (duplicateGroups.isNotEmpty()) DangerRed.copy(alpha = 0.2f)
                                        else LaserEmerald.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Duplicate Cleaner",
                                    tint = if (duplicateGroups.isNotEmpty()) DangerRed else LaserEmerald,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (duplicateGroups.isNotEmpty()) "Identified Duplicates" else "Storage 100% Unique",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSilver
                                )
                                Text(
                                    text = if (duplicateGroups.isNotEmpty())
                                        "${storageStats.duplicateCount} redundant copies (${storageStats.formattedDuplicateSavings} reclaimable)"
                                    else
                                        "Zero redundant byte duplicates detected across storage.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (duplicateGroups.isNotEmpty()) DangerRed else LaserEmerald
                                )
                            }
                        }
                    }

                    if (duplicateGroups.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showConfirmDeleteAllDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("clean_all_duplicates_hero_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Delete All (${storageStats.formattedDuplicateSavings})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            if (selectedFileIds.isNotEmpty()) {
                                Button(
                                    onClick = { showConfirmDeleteSelectedDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.3f), contentColor = DangerRed),
                                    border = BorderStroke(1.dp, DangerRed),
                                    modifier = Modifier
                                        .height(40.dp)
                                        .testTag("delete_selected_duplicates_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Delete Selected (${selectedFileIds.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Multi-select helper buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onSelectAllDuplicates,
                                modifier = Modifier.weight(1f).height(34.dp),
                                border = BorderStroke(1.dp, SteelBorder),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Select All Copies", fontSize = 11.sp, color = TextSilver)
                            }
                            OutlinedButton(
                                onClick = onDeselectAll,
                                modifier = Modifier.weight(1f).height(34.dp),
                                border = BorderStroke(1.dp, SteelBorder),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Deselect", fontSize = 11.sp, color = TextSteelMuted)
                            }
                        }
                    }
                }
            }
        }

        // 3. Duplicates List or Empty State
        if (duplicateGroups.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SteelSurface),
                    border = BorderStroke(1.dp, SteelBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Clean",
                            tint = LaserEmerald,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Duplicate Files Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSilver
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "All files in your storage have unique SHA-256 content hashes. Zero space is wasted on duplicates.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSteelMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onStartSha256Worker(false) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run SHA-256 Worker Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(duplicateGroups, key = { it.groupId }) { group ->
                DuplicateGroupCard(
                    group = group,
                    selectedFileIds = selectedFileIds,
                    onToggleSelectFile = onToggleSelectFile,
                    onDeleteSingleDuplicate = onDeleteSingleDuplicate,
                    onCleanGroupDuplicates = onCleanGroupDuplicates
                )
            }
        }
    }
}
