package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiFileAnalyzer
import com.example.data.local.AppDatabase
import com.example.data.local.FileRepository
import com.example.data.local.SettingsRepository
import com.example.data.model.AutoSortMode
import com.example.data.model.DashboardWidget
import com.example.data.model.DuplicateGroup
import com.example.data.model.FileCategory
import com.example.data.model.FileMetadata
import com.example.data.model.RenamingRuleEntity
import com.example.data.model.SortOption
import com.example.data.model.StorageStats
import com.example.data.model.UserSettings
import com.example.data.rules.RuleEngine
import com.example.data.scanner.AudioMetadataExtractor
import com.example.data.scanner.FileScannerEngine
import com.example.data.scanner.FileScanService
import com.example.data.system.DeviceDiagnostics
import com.example.data.system.DeviceDiagnosticsProvider
import com.example.data.worker.Sha256WorkerScheduler
import com.example.data.worker.Sha256WorkerState
import com.example.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FileOrganizerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FileRepository
    private val settingsRepository: SettingsRepository
    private val scannerEngine: FileScannerEngine
    private val networkMonitor = NetworkMonitor(application)
    private val analyzer = GeminiFileAnalyzer()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FileRepository(database.fileDao(), database.renamingRuleDao())
        settingsRepository = SettingsRepository(application)
        scannerEngine = FileScannerEngine(application, analyzer)
    }

    // User settings flow
    val userSettings: StateFlow<UserSettings> = settingsRepository.settings

    // Sort & Auto-Sort state
    private val _currentSortOption = MutableStateFlow(settingsRepository.settings.value.defaultSortOption)
    val currentSortOption: StateFlow<SortOption> = _currentSortOption.asStateFlow()

    private val _autoSortEnabled = MutableStateFlow(settingsRepository.settings.value.autoSortEnabled)
    val autoSortEnabled: StateFlow<Boolean> = _autoSortEnabled.asStateFlow()

    // Network connectivity state
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnlineFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), networkMonitor.isOnline())

    // All files in database
    val allFiles: StateFlow<List<FileMetadata>> = repository.allFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Renaming rules
    val allRules: StateFlow<List<RenamingRuleEntity>> = repository.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeRules: StateFlow<List<RenamingRuleEntity>> = repository.activeRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filters and search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<FileCategory?>(null)
    val selectedCategory: StateFlow<FileCategory?> = _selectedCategory.asStateFlow()

    private val _selectedImageSubtype = MutableStateFlow<String?>(null)
    val selectedImageSubtype: StateFlow<String?> = _selectedImageSubtype.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    // Scanner state
    private val _localIsScanning = MutableStateFlow(false)
    private val _localProgressText = MutableStateFlow("")

    val isScanning: StateFlow<Boolean> = combine(_localIsScanning, FileScanService.isScanning) { local, service ->
        local || service
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val scanProgressText: StateFlow<String> = combine(_localProgressText, FileScanService.progressText) { local, service ->
        if (local.isNotBlank()) local else service
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // Vault state
    private val _isVaultLocked = MutableStateFlow(true)
    val isVaultLocked: StateFlow<Boolean> = _isVaultLocked.asStateFlow()

    val vaultedFiles: StateFlow<List<FileMetadata>> = allFiles
        .combine(_isVaultLocked) { files, locked ->
            if (locked) emptyList() else files.filter { it.isVaulted }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()
    val lastActionMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun dismissUserMessage() {
        _userMessage.value = null
    }

    fun dismissActionMessage() {
        _userMessage.value = null
    }

    private val _storageFreed = MutableStateFlow(0L)
    val storageFreed: StateFlow<Long> = _storageFreed.asStateFlow()

    // Selected duplicate item IDs for batch review/deletion
    private val _selectedDuplicateIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedDuplicateIds: StateFlow<Set<Long>> = _selectedDuplicateIds.asStateFlow()

    // SHA-256 Background Worker State & Telemetry
    val sha256WorkerState: StateFlow<Sha256WorkerState> =
        Sha256WorkerScheduler.observeWorkerState(application)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                Sha256WorkerState()
            )

    fun startSha256DuplicateWorker(forceRehash: Boolean = false) {
        Sha256WorkerScheduler.enqueueSha256DuplicateWorker(
            context = getApplication(),
            forceRehash = forceRehash
        )
        _userMessage.value = if (forceRehash) {
            "SHA-256 worker: Initiated full re-hashing of all files..."
        } else {
            "SHA-256 worker: Calculating cryptographic hashes to identify duplicates..."
        }
    }

    fun cancelSha256DuplicateWorker() {
        Sha256WorkerScheduler.cancelSha256DuplicateWorker(getApplication())
        _userMessage.value = "Cancelling SHA-256 duplicate worker..."
    }

    // Active screen navigation tab
    // 0: Dashboard, 1: Files, 2: Duplicates, 3: Rules, 4: Music Hub, 5: Storage Vault
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Dashboard Customization
    private val _dashboardWidgets = MutableStateFlow<List<DashboardWidget>>(DashboardWidget.values().toList())
    val dashboardWidgets: StateFlow<List<DashboardWidget>> = _dashboardWidgets.asStateFlow()

    fun toggleDashboardWidget(widget: DashboardWidget) {
        val current = _dashboardWidgets.value.toMutableList()
        if (current.contains(widget)) {
            current.remove(widget)
        } else {
            current.add(widget)
        }
        _dashboardWidgets.value = current
    }

    fun reorderDashboardWidgets(newList: List<DashboardWidget>) {
        _dashboardWidgets.value = newList
    }

    // Recent Files
    val recentFiles: StateFlow<List<FileMetadata>> = allFiles.combine(isScanning) { files, _ ->
        files.sortedByDescending { it.createdAt }.take(10)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCurrentTab(tab: Int) {
        _currentTab.value = tab
    }

    // Filtered files based on search, category, image subtype, tag, and sort settings
    val filteredFiles: StateFlow<List<FileMetadata>> = combine(
        allFiles,
        _searchQuery,
        _selectedCategory,
        _selectedImageSubtype,
        combine(_selectedTag, _currentSortOption, _autoSortEnabled) { tag, sortOption, autoSort ->
            Triple(tag, sortOption, autoSort)
        }
    ) { files, query, category, imageSubtype, (tag, sortOption, autoSort) ->
        val filtered = files.filter { file ->
            if (file.isVaulted) return@filter false // HIDE VAULTED FILES FROM REGULAR VIEW

            val matchesQuery = if (query.isBlank()) true else {
                file.currentName.contains(query, ignoreCase = true) ||
                        file.originalName.contains(query, ignoreCase = true) ||
                        file.tags.contains(query, ignoreCase = true) ||
                        file.extractedSummary.contains(query, ignoreCase = true) ||
                        (file.artist?.contains(query, ignoreCase = true) == true) ||
                        (file.album?.contains(query, ignoreCase = true) == true) ||
                        (file.trackTitle?.contains(query, ignoreCase = true) == true) ||
                        (file.genre?.contains(query, ignoreCase = true) == true)
            }
            val matchesCategory = category == null || file.category.equals(category.name, ignoreCase = true)
            val matchesImageSubtype = imageSubtype == null || file.imageSubtype.equals(imageSubtype, ignoreCase = true)
            val matchesTag = tag == null || file.tagList.any { it.equals(tag, ignoreCase = true) }

            matchesQuery && matchesCategory && matchesImageSubtype && matchesTag
        }
        sortFilesList(filtered, sortOption, autoSort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Group duplicates strictly by SHA-256 content hash
    val duplicateGroups: StateFlow<List<DuplicateGroup>> = allFiles.combine(isScanning) { files, _ ->
        val dupFiles = files.filter { it.fileHash.isNotBlank() }
        val grouped = dupFiles.groupBy { it.fileHash }

        grouped.mapNotNull { (contentHash, groupList) ->
            if (groupList.size <= 1) return@mapNotNull null
            // The original is the one with isDuplicate == false, or earliest created
            val original = groupList.firstOrNull { !it.isDuplicate } ?: groupList.minByOrNull { it.createdAt } ?: groupList.first()
            val duplicates = groupList.filter { it.id != original.id }
            DuplicateGroup(
                groupId = contentHash,
                fileHash = contentHash,
                originalFile = original,
                duplicateFiles = duplicates
            )
        }.sortedByDescending { it.totalSavingsBytes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Music & Audio tracks
    val audioFiles: StateFlow<List<FileMetadata>> = allFiles.combine(isScanning) { files, _ ->
        files.filter { it.category.equals("AUDIO", ignoreCase = true) }
            .sortedWith(compareBy({ it.artist ?: "ZZZ" }, { it.album ?: "ZZZ" }, { it.trackNumber ?: 999 }, { it.currentName }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unique bands/artists
    val uniqueArtists: StateFlow<List<String>> = audioFiles.combine(isScanning) { audios, _ ->
        audios.mapNotNull { it.artist?.trim() }.filter { it.isNotBlank() }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic storage statistics
    val storageStats: StateFlow<StorageStats> = allFiles.combine(duplicateGroups) { files, dupGroups ->
        val totalFiles = files.size
        val totalBytes = files.sumOf { it.sizeBytes }
        val duplicateCount = dupGroups.sumOf { it.duplicateFiles.size }
        val duplicateSavings = dupGroups.sumOf { it.totalSavingsBytes }
        val renamedCount = files.count { it.status == "RENAMED" || it.currentName != it.originalName }
        val pendingRenameCount = files.count { it.suggestedName.isNotBlank() && it.suggestedName != it.currentName }
        val organizedCount = files.count { it.status == "ORGANIZED" }
        val taggedCount = files.count { it.tags.isNotBlank() }

        val categorySizes = mutableMapOf<FileCategory, Long>()
        files.forEach { file ->
            val category = try {
                FileCategory.valueOf(file.category.uppercase())
            } catch (e: Exception) {
                FileCategory.OTHER
            }
            categorySizes[category] = (categorySizes[category] ?: 0L) + file.sizeBytes
        }

        StorageStats(
            totalFiles = totalFiles,
            totalBytes = totalBytes,
            duplicateCount = duplicateCount,
            duplicateSavingsBytes = duplicateSavings,
            renamedCount = renamedCount,
            pendingRenameCount = pendingRenameCount,
            organizedCount = organizedCount,
            taggedCount = taggedCount,
            categorySizes = categorySizes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StorageStats())

    // All unique tags across all files
    val allUniqueTags: StateFlow<List<String>> = allFiles.combine(isScanning) { files, _ ->
        files.flatMap { it.tagList }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Device Hardware, RAM & Storage Lag Diagnostics
    private val _deviceDiagnostics = MutableStateFlow(DeviceDiagnosticsProvider.getDiagnostics(application))
    val deviceDiagnostics: StateFlow<DeviceDiagnostics> = _deviceDiagnostics.asStateFlow()

    fun refreshDiagnostics() {
        _deviceDiagnostics.value = DeviceDiagnosticsProvider.getDiagnostics(getApplication())
    }

    fun optimizeMemory(): Long {
        val freed = DeviceDiagnosticsProvider.triggerGarbageCollection()
        refreshDiagnostics()
        return freed
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: FileCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
        if (_selectedCategory.value != FileCategory.IMAGES) {
            _selectedImageSubtype.value = null
        }
    }

    fun selectImageSubtype(subtype: String?) {
        _selectedImageSubtype.value = if (_selectedImageSubtype.value == subtype) null else subtype
    }

    fun selectTag(tag: String?) {
        _selectedTag.value = if (_selectedTag.value == tag) null else tag
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    /**
     * Initial automated scan: seeds realistic sample storage if empty.
     */
    fun checkAndSeedInitialData() {
        viewModelScope.launch {
            val existing = allFiles.value
            if (existing.isEmpty()) {
                scanSampleStorage()
            }
        }
    }

    fun scanSampleStorage() {
        FileScanService.startScan(getApplication())
    }

    fun scanCustomFolder(treeUri: Uri) {
        FileScanService.startScan(getApplication(), treeUri)
    }

    fun scanCustomDirectory(treeUri: Uri) {
        scanCustomFolder(treeUri)
    }

    fun scanDeviceStorage() {
        FileScanService.startScan(getApplication())
    }

    fun applyBatchOperationsToDuplicates(
        file: FileMetadata,
        renameRule: RenamingRuleEntity?,
        tags: String?
    ) {
        viewModelScope.launch {
            val duplicateFiles = repository.getFilesByHash(file.fileHash)
            if (duplicateFiles.isEmpty()) return@launch

            val updatedList = mutableListOf<FileMetadata>()

            for (f in duplicateFiles) {
                var updatedFile = f
                
                // Apply renaming rule
                if (renameRule != null) {
                    val newName = RuleEngine.applyRule(f, renameRule)
                    if (newName != f.currentName) {
                        try {
                            val updatedUri = scannerEngine.renamePhysicalFile(f, newName)
                            updatedFile = updatedFile.copy(currentName = newName, suggestedName = newName, uri = updatedUri, status = "RENAMED")
                        } catch (e: Exception) {
                            // continue
                        }
                    }
                }

                // Apply tags
                if (!tags.isNullOrBlank()) {
                    val existingTags = updatedFile.tagList.toMutableSet()
                    tags.split(",").map { it.trim() }.forEach { existingTags.add(it) }
                    updatedFile = updatedFile.copy(tags = existingTags.joinToString(", "))
                }

                if (updatedFile != f) {
                    updatedList.add(updatedFile)
                }
            }

            if (updatedList.isNotEmpty()) {
                repository.updateFiles(updatedList)
                _userMessage.value = "Applied batch operations to ${updatedList.size} duplicate files!"
            }
        }
    }

    // ------------------------------------------------------------------------
    // DUPLICATE SELECTION & DELETION (Content Hash Comparison)

    fun toggleDuplicateSelection(fileId: Long) {
        val current = _selectedDuplicateIds.value.toMutableSet()
        if (current.contains(fileId)) {
            current.remove(fileId)
        } else {
            current.add(fileId)
        }
        _selectedDuplicateIds.value = current
    }

    fun selectAllDuplicates(keepOldest: Boolean = true) {
        val groups = duplicateGroups.value
        val toSelect = mutableSetOf<Long>()
        for (group in groups) {
            val duplicateFiles = if (keepOldest) {
                group.duplicateFiles
            } else {
                // Keep the newest, select the others
                val allInGroup = listOf(group.originalFile) + group.duplicateFiles
                val newest = allInGroup.maxByOrNull { it.createdAt }
                allInGroup.filter { it.id != newest?.id }
            }
            toSelect.addAll(duplicateFiles.map { it.id })
        }
        _selectedDuplicateIds.value = toSelect
    }

    fun deselectAllDuplicates() {
        _selectedDuplicateIds.value = emptySet()
    }

    fun deleteSingleDuplicate(file: FileMetadata) {
        viewModelScope.launch {
            try {
                scannerEngine.deletePhysicalFile(file)
                repository.deleteFile(file)
                _storageFreed.value += file.sizeBytes
                _selectedDuplicateIds.value = _selectedDuplicateIds.value - file.id
                _userMessage.value = "Deleted duplicate. Freed ${file.formattedSize}!"
            } catch (e: Exception) {
                _userMessage.value = "Failed to delete: ${e.message}"
            }
        }
    }

    fun deleteDuplicateFile(file: FileMetadata) {
        deleteSingleDuplicate(file)
    }

    fun deleteSelectedDuplicates(ids: Set<Long>? = null) {
        viewModelScope.launch {
            val idsToDelete = ids ?: _selectedDuplicateIds.value
            if (idsToDelete.isEmpty()) {
                _userMessage.value = "No duplicates selected."
                return@launch
            }

            val filesToDelete = allFiles.value.filter { it.id in idsToDelete }
            var freed = 0L
            for (f in filesToDelete) {
                try {
                    scannerEngine.deletePhysicalFile(f)
                    freed += f.sizeBytes
                } catch (e: Exception) {
                    // continue
                }
            }
            repository.deleteFiles(filesToDelete)
            _storageFreed.value += freed
            _selectedDuplicateIds.value = emptySet()
            _userMessage.value = "Deleted ${filesToDelete.size} duplicates! Reclaimed ${FileMetadata.formatFileSize(freed)}."
        }
    }

    fun cleanAllDuplicates() {
        viewModelScope.launch {
            val groups = duplicateGroups.value
            val duplicateFilesToDelete = groups.flatMap { it.duplicateFiles }
            if (duplicateFilesToDelete.isEmpty()) {
                _userMessage.value = "No duplicates found to delete."
                return@launch
            }

            var freedBytes = 0L
            for (file in duplicateFilesToDelete) {
                try {
                    scannerEngine.deletePhysicalFile(file)
                    freedBytes += file.sizeBytes
                } catch (e: Exception) {
                    // continue
                }
            }
            repository.deleteFiles(duplicateFilesToDelete)
            _storageFreed.value += freedBytes
            _selectedDuplicateIds.value = emptySet()
            _userMessage.value = "Cleaned ${duplicateFilesToDelete.size} duplicates! Freed ${FileMetadata.formatFileSize(freedBytes)}."
        }
    }

    // ------------------------------------------------------------------------
    // CUSTOM RENAMING RULES
    // ------------------------------------------------------------------------

    fun addCustomRule(
        name: String,
        targetCategory: String,
        extensionFilter: String,
        keywordPattern: String,
        outputFormat: String,
        casing: String,
        replaceSpacesWith: String,
        prefix: String,
        suffix: String
    ) {
        viewModelScope.launch {
            val newRule = RenamingRuleEntity(
                ruleName = name,
                targetCategory = targetCategory,
                extensionFilter = extensionFilter,
                keywordPattern = keywordPattern,
                outputFormat = outputFormat,
                casing = casing,
                replaceSpacesWith = replaceSpacesWith,
                prefix = prefix,
                suffix = suffix,
                isActive = true,
                isSystemRule = false,
                priority = 5
            )
            repository.insertRule(newRule)
            _userMessage.value = "Added custom rule: '$name'"
        }
    }

    fun toggleRuleActive(rule: RenamingRuleEntity) {
        viewModelScope.launch {
            repository.updateRule(rule.copy(isActive = !rule.isActive))
        }
    }

    fun deleteRule(rule: RenamingRuleEntity) {
        viewModelScope.launch {
            repository.deleteRule(rule)
            _userMessage.value = "Deleted rule: '${rule.ruleName}'"
        }
    }

    /**
     * Applies active renaming rules to all files, renames them offline on disk, and updates database.
     */
    fun applyActiveRulesToAll() {
        viewModelScope.launch {
            val rules = activeRules.value
            if (rules.isEmpty()) {
                _userMessage.value = "No active renaming rules enabled."
                return@launch
            }

            val files = allFiles.value
            var renamedCount = 0
            val updatedList = mutableListOf<FileMetadata>()

            for ((index, file) in files.withIndex()) {
                val matchingRule = RuleEngine.findMatchingRule(file, rules)
                if (matchingRule != null) {
                    val newName = RuleEngine.applyRule(file, matchingRule, counter = index + 1)
                    if (newName != file.currentName) {
                        try {
                            val updatedUri = scannerEngine.renamePhysicalFile(file, newName)
                            updatedList.add(
                                file.copy(
                                    currentName = newName,
                                    suggestedName = newName,
                                    uri = updatedUri,
                                    status = "RENAMED"
                                )
                            )
                            renamedCount++
                        } catch (e: Exception) {
                            // continue
                        }
                    }
                }
            }

            if (updatedList.isNotEmpty()) {
                repository.updateFiles(updatedList)
                _userMessage.value = "Applied rules: renamed $renamedCount files!"
            } else {
                _userMessage.value = "All files already comply with active rules!"
            }
        }
    }

    fun applyRuleToSingleFile(file: FileMetadata, rule: RenamingRuleEntity) {
        viewModelScope.launch {
            val newName = RuleEngine.applyRule(file, rule)
            renameSingleFile(file, newName)
        }
    }

    // ------------------------------------------------------------------------
    // MUSIC & AUDIO RICH METADATA
    // ------------------------------------------------------------------------

    fun updateAudioMetadata(
        file: FileMetadata,
        artist: String,
        album: String,
        trackTitle: String,
        trackNumber: Int?,
        genre: String,
        year: String
    ) {
        viewModelScope.launch {
            val newSuggested = "$artist - $album - ${trackNumber?.let { String.format(java.util.Locale.US, "%02d", it) } ?: "01"} - $trackTitle.${file.extension}"
            val updated = file.copy(
                artist = artist.trim(),
                album = album.trim(),
                trackTitle = trackTitle.trim(),
                trackNumber = trackNumber,
                genre = genre.trim(),
                year = year.trim(),
                suggestedName = newSuggested
            )
            repository.updateFile(updated)
            _userMessage.value = "Updated music tags for '$trackTitle' by $artist"
        }
    }

    fun organizeMusicLibrary() {
        viewModelScope.launch {
            val audios = audioFiles.value
            if (audios.isEmpty()) {
                _userMessage.value = "No audio files found."
                return@launch
            }

            val updatedList = mutableListOf<FileMetadata>()
            for (file in audios) {
                val artist = file.artist ?: "Unknown Artist"
                val album = file.album ?: "Singles"
                val targetFolder = "Music/$artist/$album"
                try {
                    val movedUri = scannerEngine.movePhysicalFile(file, targetFolder)
                    updatedList.add(
                        file.copy(
                            organizationFolder = targetFolder,
                            uri = movedUri,
                            status = "ORGANIZED"
                        )
                    )
                } catch (e: Exception) {
                    // skip
                }
            }
            repository.updateFiles(updatedList)
            _userMessage.value = "Organized ${updatedList.size} music tracks into Artist/Album library!"
        }
    }

    // ------------------------------------------------------------------------
    // IMAGES SUBTYPES & ORGANIZING
    // ------------------------------------------------------------------------

    fun organizeImagesBySubtype() {
        viewModelScope.launch {
            val images = allFiles.value.filter { it.category.equals("IMAGES", ignoreCase = true) }
            if (images.isEmpty()) {
                _userMessage.value = "No images found to organize."
                return@launch
            }

            val updatedList = mutableListOf<FileMetadata>()
            for (img in images) {
                val targetFolder = when (img.imageSubtype) {
                    "SCREENSHOT" -> "Images/Screenshots"
                    "PRODUCT_DESIGN" -> "Images/Product_Designs"
                    "INFORMATION" -> "Images/Information_Infographics"
                    "CAMERA_PHOTO" -> "Images/Camera_Photos"
                    "WALLPAPER" -> "Images/Wallpapers"
                    else -> "Images/General"
                }
                try {
                    val movedUri = scannerEngine.movePhysicalFile(img, targetFolder)
                    updatedList.add(
                        img.copy(
                            organizationFolder = targetFolder,
                            uri = movedUri,
                            status = "ORGANIZED"
                        )
                    )
                } catch (e: Exception) {
                    // continue
                }
            }
            repository.updateFiles(updatedList)
            _userMessage.value = "Organized ${updatedList.size} images by subtype into structured folders!"
        }
    }

    // ------------------------------------------------------------------------
    // ONLINE AI METADATA ENRICHMENT
    // ------------------------------------------------------------------------

    fun enrichAllMetadataOnline() {
        viewModelScope.launch {
            if (!isOnline.value) {
                _userMessage.value = "⚠️ Device is offline. Connect to internet for online AI enrichment."
                return@launch
            }

            _localIsScanning.value = true
            _localProgressText.value = "⚡ Querying Gemini AI to enrich metadata, bands, albums, and tags..."

            val files = allFiles.value
            val updatedList = mutableListOf<FileMetadata>()

            for ((index, file) in files.withIndex()) {
                _localProgressText.value = "Enriching (${index + 1}/${files.size}): ${file.currentName}"
                try {
                    val extractedAudio = if (file.category == "AUDIO") {
                        AudioMetadataExtractor.extract(getApplication(), file.uri, file.currentName)
                    } else null

                    val analysis = analyzer.analyzeFile(
                        originalName = file.originalName,
                        extension = file.extension,
                        category = file.category,
                        sizeBytes = file.sizeBytes,
                        createdAt = file.createdAt,
                        extractedAudio = extractedAudio,
                        imageSubtype = file.imageSubtype,
                        isOnline = true
                    )

                    updatedList.add(
                        file.copy(
                            suggestedName = analysis.suggestedName,
                            tags = (file.tagList + analysis.tags).distinct().joinToString(", "),
                            extractedSummary = analysis.summary.ifBlank { file.extractedSummary },
                            organizationFolder = analysis.organizationFolder.ifBlank { file.organizationFolder },
                            artist = analysis.artist ?: file.artist,
                            album = analysis.album ?: file.album,
                            trackTitle = analysis.trackTitle ?: file.trackTitle,
                            trackNumber = analysis.trackNumber ?: file.trackNumber,
                            genre = analysis.genre ?: file.genre,
                            year = analysis.year ?: file.year,
                            imageSubtype = analysis.imageSubtype ?: file.imageSubtype
                        )
                    )
                } catch (e: Exception) {
                    // continue
                }
            }

            repository.updateFiles(updatedList)
            _localIsScanning.value = false
            _userMessage.value = "⚡ Successfully enriched metadata for ${updatedList.size} files with AI!"
        }
    }

    // ------------------------------------------------------------------------
    // GENERAL FILE OPERATIONS
    // ------------------------------------------------------------------------

    fun renameSingleFile(file: FileMetadata, newName: String) {
        viewModelScope.launch {
            try {
                val updatedUri = scannerEngine.renamePhysicalFile(file, newName)
                val updated = file.copy(
                    currentName = newName,
                    uri = updatedUri,
                    status = "RENAMED"
                )
                repository.updateFile(updated)
                _userMessage.value = "Renamed to '$newName'"
            } catch (e: Exception) {
                _userMessage.value = "Rename failed: ${e.message}"
            }
        }
    }

    fun batchRenameAll() {
        viewModelScope.launch {
            val toRename = allFiles.value.filter {
                it.suggestedName.isNotBlank() && it.suggestedName != it.currentName
            }
            if (toRename.isEmpty()) {
                _userMessage.value = "All files already have optimal names!"
                return@launch
            }

            var successCount = 0
            val updatedList = mutableListOf<FileMetadata>()
            for (file in toRename) {
                try {
                    val updatedUri = scannerEngine.renamePhysicalFile(file, file.suggestedName)
                    updatedList.add(
                        file.copy(
                            currentName = file.suggestedName,
                            uri = updatedUri,
                            status = "RENAMED"
                        )
                    )
                    successCount++
                } catch (e: Exception) {
                    // skip
                }
            }
            repository.updateFiles(updatedList)
            _userMessage.value = "Successfully batch-renamed $successCount files!"
        }
    }

    fun updateMetadata(file: FileMetadata, newTags: String, newSummary: String, newSuggestedName: String) {
        viewModelScope.launch {
            val updated = file.copy(
                tags = newTags,
                extractedSummary = newSummary,
                suggestedName = newSuggestedName
            )
            repository.updateFile(updated)
            _userMessage.value = "Metadata updated for ${file.currentName}"
        }
    }

    // ------------------------------------------------------------------------
    // SECURE VAULT OPERATIONS
    // ------------------------------------------------------------------------

    fun unlockVault(passcode: String) {
        if (passcode == "STEEL" || passcode == "1234") {
            _isVaultLocked.value = false
            _userMessage.value = "VAULT UNLOCKED. Accessing secure sector."
        } else {
            _userMessage.value = "ACCESS DENIED. Invalid biometric/passcode."
        }
    }

    fun unlockVaultByPattern() {
        _isVaultLocked.value = false
        _userMessage.value = "MIMIC UNCHAINED. Tap pattern accepted."
    }

    fun lockVault() {
        _isVaultLocked.value = true
        _userMessage.value = "VAULT SEALED. Data encrypted."
    }

    fun toggleVault(file: FileMetadata) {
        viewModelScope.launch {
            try {
                val newVaultState = !file.isVaulted
                val newUri = if (newVaultState) {
                    scannerEngine.vaultPhysicalFile(file)
                } else {
                    scannerEngine.unvaultPhysicalFile(file)
                }
                
                val updated = file.copy(
                    isVaulted = newVaultState,
                    uri = newUri
                )
                repository.updateFile(updated)
                _userMessage.value = if (newVaultState) "File moved to secure vault." else "File removed from vault."
            } catch (e: Exception) {
                _userMessage.value = "Vault operation failed: ${e.message}"
            }
        }
    }

    fun organizeAllFiles() {
        autoSortAllFiles()
    }

    /**
     * Comprehensive Auto-Sort Engine:
     * Automatically sorts all files into targeted directory structures based on AutoSortMode
     * (Smart Subtypes, Category Only, Date Archives), evaluates active renaming rules,
     * updates organization status, and saves changes.
     */
    fun autoSortAllFiles() {
        viewModelScope.launch {
            val all = allFiles.value
            if (all.isEmpty()) {
                _userMessage.value = "No indexed files to auto-sort."
                return@launch
            }

            val mode = userSettings.value.autoSortDestinationMode
            val rules = activeRules.value
            var sortedCount = 0

            val updatedFiles = all.map { file ->
                val newTargetFolder = when (mode) {
                    AutoSortMode.SMART_SUBTYPES -> {
                        when (file.category.uppercase()) {
                            "IMAGES" -> when (file.imageSubtype?.uppercase()) {
                                "SCREENSHOT" -> "Internal/Images/Screenshots"
                                "PRODUCT_DESIGN" -> "Internal/Images/Designs"
                                "INFOGRAPHIC" -> "Internal/Images/Infographics"
                                else -> "Internal/Images/Photos"
                            }
                            "AUDIO" -> {
                                val artist = file.artist?.trim()?.ifBlank { "Unknown Artist" } ?: "Unknown Artist"
                                val album = file.album?.trim()?.ifBlank { "Unknown Album" } ?: "Unknown Album"
                                "Internal/Music/$artist/$album"
                            }
                            "DOCUMENTS" -> "Internal/Documents/${file.extension.uppercase().ifBlank { "DOCS" }}"
                            "VIDEO" -> "Internal/Videos"
                            "ARCHIVES" -> "Internal/Archives"
                            else -> "Internal/Other"
                        }
                    }
                    AutoSortMode.CATEGORY_ONLY -> {
                        "Internal/${file.category.lowercase().replaceFirstChar { it.uppercase() }}"
                    }
                    AutoSortMode.DATE_ARCHIVES -> {
                        val year = java.text.SimpleDateFormat("yyyy", java.util.Locale.US).format(java.util.Date(file.createdAt))
                        val month = java.text.SimpleDateFormat("MM", java.util.Locale.US).format(java.util.Date(file.createdAt))
                        "Internal/Archive/$year/$month/${file.category}"
                    }
                }

                // Check active rules for smart renaming suggestion
                val matchedRule = rules.firstOrNull { it.matches(file) }
                val newSuggested = matchedRule?.applyTo(file) ?: file.suggestedName

                if (file.organizationFolder != newTargetFolder || file.status != "ORGANIZED" || file.suggestedName != newSuggested) {
                    sortedCount++
                    file.copy(
                        organizationFolder = newTargetFolder,
                        status = "ORGANIZED",
                        suggestedName = newSuggested
                    )
                } else {
                    file
                }
            }

            if (sortedCount > 0) {
                repository.updateFiles(updatedFiles)
                _userMessage.value = "Auto-sorted $sortedCount files using ${mode.displayName}!"
            } else {
                _userMessage.value = "All files are organized and up to date."
            }
        }
    }

    // ------------------------------------------------------------------------
    // SORTING & SETTINGS CONTROLS
    // ------------------------------------------------------------------------

    private fun sortFilesList(
        files: List<FileMetadata>,
        sortOption: SortOption,
        autoSort: Boolean
    ): List<FileMetadata> {
        if (!autoSort) {
            return files.sortedByDescending { it.createdAt }
        }
        return when (sortOption) {
            SortOption.DATE_DESC -> files.sortedByDescending { it.createdAt }
            SortOption.DATE_ASC -> files.sortedBy { it.createdAt }
            SortOption.NAME_ASC -> files.sortedBy { it.currentName.lowercase() }
            SortOption.NAME_DESC -> files.sortedByDescending { it.currentName.lowercase() }
            SortOption.SIZE_DESC -> files.sortedByDescending { it.sizeBytes }
            SortOption.SIZE_ASC -> files.sortedBy { it.sizeBytes }
            SortOption.CATEGORY -> files.sortedWith(
                compareBy({ it.category }, { it.extension.lowercase() }, { it.currentName.lowercase() })
            )
            SortOption.DUPLICATES_FIRST -> files.sortedWith(
                compareByDescending<FileMetadata> { it.isDuplicate }
                    .thenByDescending { it.sizeBytes }
            )
            SortOption.PENDING_RENAME_FIRST -> files.sortedWith(
                compareByDescending<FileMetadata> { it.suggestedName.isNotBlank() && it.suggestedName != it.currentName }
                    .thenByDescending { it.createdAt }
            )
        }
    }

    fun setSortOption(option: SortOption) {
        _currentSortOption.value = option
        settingsRepository.updateDefaultSortOption(option)
    }

    fun setAutoSortEnabled(enabled: Boolean) {
        _autoSortEnabled.value = enabled
        settingsRepository.updateAutoSortEnabled(enabled)
    }

    fun setAutoSortDestinationMode(mode: AutoSortMode) {
        settingsRepository.updateAutoSortDestinationMode(mode)
    }

    fun setAutoSortOnScan(enabled: Boolean) {
        settingsRepository.updateAutoSortOnScan(enabled)
    }

    fun setAutoSuggestRenamesOnScan(enabled: Boolean) {
        settingsRepository.updateAutoSuggestRenamesOnScan(enabled)
    }

    fun setAutoHashSha256OnScan(enabled: Boolean) {
        settingsRepository.updateAutoHashSha256OnScan(enabled)
    }

    fun setGeminiOnlineEnrichment(enabled: Boolean) {
        settingsRepository.updateGeminiOnlineEnrichment(enabled)
    }

    fun setDefaultCasing(casing: String) {
        settingsRepository.updateDefaultCasing(casing)
    }

    fun setDefaultSpaceReplacement(replacement: String) {
        settingsRepository.updateDefaultSpaceReplacement(replacement)
    }

    fun setGroupDuplicatesByHash(enabled: Boolean) {
        settingsRepository.updateGroupDuplicatesByHash(enabled)
    }

    fun resetSettingsToDefault() {
        settingsRepository.resetToDefaults()
        _currentSortOption.value = SortOption.DATE_DESC
        _autoSortEnabled.value = true
        _userMessage.value = "Settings reset to defaults."
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
            _selectedDuplicateIds.value = emptySet()
            _userMessage.value = "Cleared scanned file index."
        }
    }
}
