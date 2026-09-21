package com.example.data.scanner

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.example.data.ai.GeminiFileAnalyzer
import com.example.data.model.FileCategory
import com.example.data.model.FileMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

class FileScannerEngine(
    private val context: Context,
    private val analyzer: GeminiFileAnalyzer = GeminiFileAnalyzer()
) {
    private val imageClassifier = ImageClassifier(context)

    /**
     * Seeds realistic sample files in app storage if none exist,
     * including exact duplicates with identical SHA-256 content hashes,
     * music tracks with band/album metadata, images of various subtypes
     * (screenshots, product designs, information charts, camera photos),
     * and documents.
     */
    suspend fun seedSampleFilesIfEmpty(isOnline: Boolean = true): List<FileMetadata> = withContext(Dispatchers.IO) {
        val sampleDir = File(context.filesDir, "SampleDeviceStorage")
        if (!sampleDir.exists()) {
            sampleDir.mkdirs()
        }

        // Exact content bytes definitions to ensure byte-for-byte duplicate content hashing
        val photoContent = "JPEG_EXIF_RAW_CAMERA_SENSOR_DATA_SUNSET_HAWAII_4K_HDR_F2.8_ISO100"
        val invoiceContent = "INVOICE #INV-2024-8841\nDate: 2024-03-15\nVendor: AWS Cloud Computing\nTotal Due: $412.80\nPayment Status: Paid"
        val receiptContent = "THE HOME DEPOT RECEIPT\nStore #4810\nItems: Drill Kit, Screws, Wood Glue\nTotal: $184.22\nDate: 2024-06-20"
        val screenshotContent = "PNG_PIXEL_DATA_MOBILE_SCREENSHOT_FLIGHT_TICKET_SFO_TO_JFK_CONFIRMATION"
        val queenAudioContent = "ID3_MPEG_AUDIO_LAYER_3_QUEEN_BOHEMIAN_RHAPSODY_STUDIO_MASTER_1975_FLAC_STREAM"
        val voiceMemoContent = "AUDIO_RECORDING_DATA_WEEKLY_ENGINEERING_STANDUP_NOTES_SEPTEMBER"

        val dummyFiles = listOf(
            // Music / Audio with rich ID3 info
            SampleFileDef(
                name = "Queen - A Night at the Opera - 04 - Bohemian Rhapsody.mp3",
                content = queenAudioContent,
                sizeOffset = 1024 * 1024 * 5, // 5 MB
                category = FileCategory.AUDIO,
                daysAgo = 60
            ),
            SampleFileDef(
                name = "Queen - Bohemian Rhapsody (Copy).mp3", // Exact SHA-256 duplicate of Bohemian Rhapsody!
                content = queenAudioContent,
                sizeOffset = 1024 * 1024 * 5,
                category = FileCategory.AUDIO,
                daysAgo = 20
            ),
            SampleFileDef(
                name = "Daft Punk - Discovery - 02 - One More Time.flac",
                content = "FLAC_AUDIO_MASTER_DAFT_PUNK_DISCOVERY_ONE_MORE_TIME_ELECTRONIC_DANCE",
                sizeOffset = 1024 * 1024 * 8,
                category = FileCategory.AUDIO,
                daysAgo = 120
            ),
            SampleFileDef(
                name = "Pink Floyd - Dark Side - 04 - Time.mp3",
                content = "ID3_AUDIO_PINK_FLOYD_DARK_SIDE_OF_THE_MOON_TIME_PROGRESSIVE_ROCK",
                sizeOffset = 1024 * 1024 * 6,
                category = FileCategory.AUDIO,
                daysAgo = 180
            ),
            SampleFileDef(
                name = "voice_memo_sync_meeting_003.m4a",
                content = voiceMemoContent,
                sizeOffset = 1024 * 1024 * 2,
                category = FileCategory.AUDIO,
                daysAgo = 14
            ),
            SampleFileDef(
                name = "voice_memo_sync_meeting_003_dup.m4a", // Exact duplicate voice memo!
                content = voiceMemoContent,
                sizeOffset = 1024 * 1024 * 2,
                category = FileCategory.AUDIO,
                daysAgo = 10
            ),

            // Images - Different Subtypes
            SampleFileDef(
                name = "Screenshot_20240812-140231.png", // Screenshot
                content = screenshotContent,
                sizeOffset = 1024 * 780,
                category = FileCategory.IMAGES,
                daysAgo = 38
            ),
            SampleFileDef(
                name = "Screenshot_20240812-140231_dup.png", // Exact duplicate screenshot!
                content = screenshotContent,
                sizeOffset = 1024 * 780,
                category = FileCategory.IMAGES,
                daysAgo = 30
            ),
            SampleFileDef(
                name = "mobile_app_dashboard_wireframe_mockup.png", // Product Design
                content = "PNG_VECTOR_FIGMA_EXPORT_MOBILE_APP_DASHBOARD_WIREFRAME_MOCKUP_V3",
                sizeOffset = 1024 * 920,
                category = FileCategory.IMAGES,
                daysAgo = 25
            ),
            SampleFileDef(
                name = "cad_spec_hardware_render_v2.png", // Product Design
                content = "CAD_3D_RENDER_PRECISION_HARDWARE_TITANIUM_CHASSIS_SPECIFICATION",
                sizeOffset = 1024 * 1024 * 2,
                category = FileCategory.IMAGES,
                daysAgo = 45
            ),
            SampleFileDef(
                name = "cloud_architecture_system_diagram_infographic.png", // Information
                content = "PNG_INFOGRAPHIC_DATA_CLOUD_ARCHITECTURE_KUBERNETES_SYSTEM_DIAGRAM",
                sizeOffset = 1024 * 650,
                category = FileCategory.IMAGES,
                daysAgo = 50
            ),
            SampleFileDef(
                name = "quarterly_sales_metrics_chart_infographic.png", // Information
                content = "PNG_INFOGRAPHIC_BAR_CHART_QUARTERLY_REVENUE_METRICS_2024",
                sizeOffset = 1024 * 512,
                category = FileCategory.IMAGES,
                daysAgo = 35
            ),
            SampleFileDef(
                name = "IMG_20231014_00234_sunset.jpg", // Camera Photo
                content = photoContent,
                sizeOffset = 1024 * 1024 * 3,
                category = FileCategory.IMAGES,
                daysAgo = 320
            ),
            SampleFileDef(
                name = "IMG_20231014_00234_sunset_copy.jpg", // Exact duplicate Camera Photo!
                content = photoContent,
                sizeOffset = 1024 * 1024 * 3,
                category = FileCategory.IMAGES,
                daysAgo = 280
            ),

            // Documents
            SampleFileDef(
                name = "Scan_10492_invoice_final_v2.pdf",
                content = invoiceContent,
                sizeOffset = 1024 * 180,
                category = FileCategory.DOCUMENTS,
                daysAgo = 180
            ),
            SampleFileDef(
                name = "Scan_10492_invoice_backup_copy.pdf", // Exact duplicate invoice!
                content = invoiceContent,
                sizeOffset = 1024 * 180,
                category = FileCategory.DOCUMENTS,
                daysAgo = 150
            ),
            SampleFileDef(
                name = "W2_Tax_Wage_Statement_2023.pdf",
                content = "FORM W-2 Wage and Tax Statement 2023. Employer: Tech Corp Inc. Wages: $98,400. Federal Tax Withheld: $16,200.",
                sizeOffset = 1024 * 310,
                category = FileCategory.DOCUMENTS,
                daysAgo = 220
            ),
            SampleFileDef(
                name = "reciept_home_depot_09482.pdf",
                content = receiptContent,
                sizeOffset = 1024 * 95,
                category = FileCategory.DOCUMENTS,
                daysAgo = 90
            ),
            SampleFileDef(
                name = "reciept_home_depot_09482_backup.pdf", // Exact duplicate receipt!
                content = receiptContent,
                sizeOffset = 1024 * 95,
                category = FileCategory.DOCUMENTS,
                daysAgo = 85
            ),
            SampleFileDef(
                name = "system_logs_dump_20240901.txt",
                content = "2024-09-01 08:30:12 [INFO] Database initialized successfully\n2024-09-01 08:30:15 [INFO] Sync worker completed 42 tasks",
                sizeOffset = 1024 * 42,
                category = FileCategory.DOCUMENTS,
                daysAgo = 18
            ),
            SampleFileDef(
                name = "project_assets_archive_v1.zip",
                content = "ZIP_ARCHIVE_DATA_ICONS_VECTOR_GRAPHICS_SOURCE_FILES",
                sizeOffset = 1024 * 1024 * 4,
                category = FileCategory.ARCHIVES,
                daysAgo = 60
            )
        )

        for (def in dummyFiles) {
            val file = File(sampleDir, def.name)
            if (!file.exists()) {
                val createdTime = System.currentTimeMillis() - (def.daysAgo.toLong() * 24 * 3600 * 1000L)
                FileOutputStream(file).use { fos ->
                    val baseBytes = def.content.toByteArray(Charsets.UTF_8)
                    fos.write(baseBytes)
                    val paddingNeeded = (def.sizeOffset - baseBytes.size).coerceAtLeast(0)
                    if (paddingNeeded > 0) {
                        val buffer = ByteArray(4096) { 0 }
                        var written = 0
                        while (written < paddingNeeded) {
                            val toWrite = minOf(buffer.size, paddingNeeded - written)
                            fos.write(buffer, 0, toWrite)
                            written += toWrite
                        }
                    }
                }
                file.setLastModified(createdTime)
            }
        }

        return@withContext scanDirectory(sampleDir, isSample = true, isOnline = isOnline)
    }

    /**
     * Scans a file directory recursively, calculating content hash for each file.
     */
    suspend fun scanDirectory(
        directory: File,
        isSample: Boolean = false,
        isOnline: Boolean = true,
        onProgress: ((currentFile: String, scannedCount: Int) -> Unit)? = null
    ): List<FileMetadata> = withContext(Dispatchers.IO) {
        val filesFound = mutableListOf<File>()
        collectFilesRecursive(directory, filesFound)

        val rawList = mutableListOf<RawFileMetadata>()
        for ((index, file) in filesFound.withIndex()) {
            onProgress?.invoke(file.name, index + 1)
            try {
                val hash = calculateFileHash(file)
                val ext = file.extension.lowercase()
                val category = FileCategory.fromExtension(ext)
                val snippet = if (file.length() < 500_000 && (ext in listOf("txt", "pdf", "json", "csv", "md", "log"))) {
                    try {
                        FileInputStream(file).bufferedReader().use { it.readText().take(500) }
                    } catch (e: Exception) {
                        null
                    }
                } else null

                rawList.add(
                    RawFileMetadata(
                        uri = Uri.fromFile(file).toString(),
                        name = file.name,
                        extension = ext,
                        mimeType = getMimeType(ext),
                        sizeBytes = file.length(),
                        createdAt = file.lastModified(),
                        modifiedAt = file.lastModified(),
                        fileHash = hash,
                        category = category.name,
                        contentSnippet = snippet,
                        isSample = isSample
                    )
                )
            } catch (e: Exception) {
                Log.w("FileScannerEngine", "Error scanning file ${file.name}: ${e.message}")
            }
        }

        return@withContext processRawFilesIntoEntities(rawList, isOnline)
    }

    /**
     * Scans a folder selected via Storage Access Framework (SAF DocumentFile).
     */
    suspend fun scanDocumentTree(
        treeUri: Uri,
        isOnline: Boolean = true,
        onProgress: ((currentFile: String, scannedCount: Int) -> Unit)? = null
    ): List<FileMetadata> = withContext(Dispatchers.IO) {
        val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
        val docFiles = mutableListOf<DocumentFile>()
        collectDocumentFilesRecursive(rootDoc, docFiles)

        val rawList = mutableListOf<RawFileMetadata>()
        for ((index, doc) in docFiles.withIndex()) {
            val name = doc.name ?: "Unknown"
            onProgress?.invoke(name, index + 1)
            try {
                val uri = doc.uri
                val hash = calculateUriHash(uri)
                val ext = name.substringAfterLast('.', "").lowercase()
                val category = FileCategory.fromMimeType(doc.type)
                val size = doc.length()
                val modified = doc.lastModified()

                rawList.add(
                    RawFileMetadata(
                        uri = uri.toString(),
                        name = name,
                        extension = ext,
                        mimeType = doc.type ?: getMimeType(ext),
                        sizeBytes = size,
                        createdAt = modified,
                        modifiedAt = modified,
                        fileHash = hash,
                        category = category.name,
                        contentSnippet = null,
                        isSample = false
                    )
                )
            } catch (e: Exception) {
                Log.w("FileScannerEngine", "Error scanning doc $name: ${e.message}")
            }
        }

        return@withContext processRawFilesIntoEntities(rawList, isOnline)
    }

    /**
     * Scans standard device media and downloads.
     */
    suspend fun scanDeviceMediaStore(
        isOnline: Boolean = true,
        onProgress: ((currentFile: String, scannedCount: Int) -> Unit)? = null
    ): List<FileMetadata> = withContext(Dispatchers.IO) {
        val rawList = mutableListOf<RawFileMetadata>()
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (downloadsDir != null && downloadsDir.exists() && downloadsDir.canRead()) {
            val filesFound = mutableListOf<File>()
            collectFilesRecursive(downloadsDir, filesFound, maxDepth = 2)
            for ((index, file) in filesFound.take(100).withIndex()) {
                onProgress?.invoke(file.name, index + 1)
                try {
                    val hash = calculateFileHash(file)
                    val ext = file.extension.lowercase()
                    rawList.add(
                        RawFileMetadata(
                            uri = Uri.fromFile(file).toString(),
                            name = file.name,
                            extension = ext,
                            mimeType = getMimeType(ext),
                            sizeBytes = file.length(),
                            createdAt = file.lastModified(),
                            modifiedAt = file.lastModified(),
                            fileHash = hash,
                            category = FileCategory.fromExtension(ext).name,
                            contentSnippet = null,
                            isSample = false
                        )
                    )
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
        return@withContext processRawFilesIntoEntities(rawList, isOnline)
    }

    /**
     * Processes raw scanned files:
     * - Content hashing duplicate detection (compares exact byte SHA-256 hash)
     * - Offline audio ID3 metadata extraction (band, album, track, genre, year)
     * - Offline image subtype classification (screenshot, product design, info, photo)
     * - Online AI enrichment if connected
     */
    private suspend fun processRawFilesIntoEntities(
        rawList: List<RawFileMetadata>,
        isOnline: Boolean
    ): List<FileMetadata> {
        // Group files by exact content SHA-256 hash and size to detect content duplicates!
        val hashGroups = rawList.groupBy { it.fileHash }

        val resultEntities = mutableListOf<FileMetadata>()

        for ((fileHash, group) in hashGroups) {
            val hasDuplicates = group.size > 1 && fileHash.isNotBlank()
            // Keep the earliest created file as original; newer ones are flagged as duplicates
            val sortedByDate = group.sortedBy { it.createdAt }
            val original = sortedByDate.first()

            for (file in group) {
                val isDuplicate = hasDuplicates && file != original
                val duplicateGroupId = if (hasDuplicates) fileHash else null

                // Extract audio metadata if category is AUDIO
                val extractedAudio = if (file.category == "AUDIO") {
                    AudioMetadataExtractor.extract(context, file.uri, file.name)
                } else null

                // Perform local image classification for subtypes if category is IMAGES
                val localImageSubtype = if (file.category == "IMAGES") {
                    imageClassifier.classifyImage(file.uri)
                } else null

                // Analyze file with offline heuristics or online Gemini API
                val analysis = analyzer.analyzeFile(
                    originalName = file.name,
                    extension = file.extension,
                    category = file.category,
                    sizeBytes = file.sizeBytes,
                    createdAt = file.createdAt,
                    contentSnippet = file.contentSnippet,
                    extractedAudio = extractedAudio,
                    imageSubtype = localImageSubtype,
                    isOnline = isOnline
                )

                resultEntities.add(
                    FileMetadata(
                        uri = file.uri,
                        originalName = file.name,
                        suggestedName = analysis.suggestedName,
                        currentName = file.name,
                        extension = file.extension,
                        mimeType = file.mimeType,
                        category = file.category,
                        sizeBytes = file.sizeBytes,
                        createdAt = file.createdAt,
                        modifiedAt = file.modifiedAt,
                        fileHash = file.fileHash,
                        contentHashAlgorithm = "SHA-256",
                        isDuplicate = isDuplicate,
                        duplicateGroupId = duplicateGroupId,
                        tags = (analysis.tags + (localImageSubtype?.let { listOf(it) } ?: emptyList())).distinct().joinToString(", "),
                        extractedSummary = analysis.summary,
                        status = "SCANNED",
                        organizationFolder = analysis.organizationFolder,
                        isSample = file.isSample,
                        artist = analysis.artist ?: extractedAudio?.artist,
                        album = analysis.album ?: extractedAudio?.album,
                        trackTitle = analysis.trackTitle ?: extractedAudio?.trackTitle,
                        trackNumber = analysis.trackNumber ?: extractedAudio?.trackNumber,
                        genre = analysis.genre ?: extractedAudio?.genre,
                        year = analysis.year ?: extractedAudio?.year,
                        durationMs = extractedAudio?.durationMs,
                        imageSubtype = localImageSubtype ?: analysis.imageSubtype
                    )
                )
            }
        }

        return resultEntities
    }

    /**
     * Renames a file on disk or via SAF DocumentFile. (100% Offline)
     */
    suspend fun renamePhysicalFile(fileEntity: FileMetadata, newName: String): String = withContext(Dispatchers.IO) {
        val uri = Uri.parse(fileEntity.uri)
        if (uri.scheme == "file") {
            val file = File(uri.path ?: return@withContext fileEntity.uri)
            val parent = file.parentFile ?: return@withContext fileEntity.uri
            val target = File(parent, newName)
            if (file.exists() && !target.exists()) {
                val success = file.renameTo(target)
                if (success) {
                    return@withContext Uri.fromFile(target).toString()
                }
            }
        } else if (uri.scheme == "content") {
            try {
                val doc = DocumentFile.fromSingleUri(context, uri)
                if (doc != null && doc.canWrite()) {
                    doc.renameTo(newName)
                    return@withContext doc.uri.toString()
                }
            } catch (e: Exception) {
                Log.w("FileScannerEngine", "SAF rename failed: ${e.message}")
            }
        }
        return@withContext fileEntity.uri
    }

    /**
     * Moves a physical file into an organized folder, creating target directories if needed. (100% Offline)
     */
    suspend fun movePhysicalFile(fileEntity: FileMetadata, targetRelativeFolder: String): String = withContext(Dispatchers.IO) {
        val uri = Uri.parse(fileEntity.uri)
        if (uri.scheme == "file") {
            val file = File(uri.path ?: return@withContext fileEntity.uri)
            val baseParent = file.parentFile?.parentFile ?: file.parentFile ?: return@withContext fileEntity.uri
            val targetDir = File(baseParent, targetRelativeFolder)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            val targetFile = File(targetDir, file.name)
            if (file.exists()) {
                val moved = file.renameTo(targetFile)
                if (moved) {
                    return@withContext Uri.fromFile(targetFile).toString()
                }
            }
        }
        return@withContext fileEntity.uri
    }

    /**
     * Creates a custom directory offline. (100% Offline)
     */
    suspend fun createCustomFolder(parentDirUri: String, folderName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(parentDirUri)
            if (uri.scheme == "file") {
                val parent = File(uri.path ?: return@withContext false)
                val newFolder = File(parent, folderName)
                return@withContext newFolder.mkdirs() || newFolder.exists()
            }
            return@withContext true
        } catch (e: Exception) {
            Log.e("FileScannerEngine", "Folder creation error: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Deletes a duplicate file from storage. (100% Offline)
     */
    suspend fun deletePhysicalFile(fileEntity: FileMetadata): Boolean = withContext(Dispatchers.IO) {
        val uri = Uri.parse(fileEntity.uri)
        return@withContext try {
            if (uri.scheme == "file") {
                val file = File(uri.path ?: return@withContext false)
                file.delete()
            } else if (uri.scheme == "content") {
                val doc = DocumentFile.fromSingleUri(context, uri)
                doc?.delete() ?: false
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("FileScannerEngine", "Delete failed for ${fileEntity.currentName}: ${e.message}")
            false
        }
    }

    /**
     * Moves a file to the app's private internal storage (Vault).
     */
    suspend fun vaultPhysicalFile(fileEntity: FileMetadata): String = withContext(Dispatchers.IO) {
        val vaultDir = File(context.filesDir, "SECURE_VAULT")
        if (!vaultDir.exists()) vaultDir.mkdirs()

        val uri = Uri.parse(fileEntity.uri)
        if (uri.scheme == "file") {
            val file = File(uri.path ?: return@withContext fileEntity.uri)
            val targetFile = File(vaultDir, "v_" + file.name)
            if (file.exists()) {
                val success = file.renameTo(targetFile)
                if (success) {
                    return@withContext Uri.fromFile(targetFile).toString()
                }
            }
        }
        return@withContext fileEntity.uri
    }

    /**
     * Moves a file back from the vault to the sample storage.
     */
    suspend fun unvaultPhysicalFile(fileEntity: FileMetadata): String = withContext(Dispatchers.IO) {
        val sampleDir = File(context.filesDir, "SampleDeviceStorage")
        if (!sampleDir.exists()) sampleDir.mkdirs()

        val uri = Uri.parse(fileEntity.uri)
        if (uri.scheme == "file") {
            val file = File(uri.path ?: return@withContext fileEntity.uri)
            val originalName = file.name.removePrefix("v_")
            val targetFile = File(sampleDir, originalName)
            if (file.exists()) {
                val success = file.renameTo(targetFile)
                if (success) {
                    return@withContext Uri.fromFile(targetFile).toString()
                }
            }
        }
        return@withContext fileEntity.uri
    }

    /**
     * Calculates SHA-256 cryptographic hash of actual file content bytes.
     */
    fun calculateFileHash(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(16384)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Calculates SHA-256 cryptographic hash of Uri content stream.
     */
    private fun calculateUriHash(uri: Uri): String {
        val md = MessageDigest.getInstance("SHA-256")
        context.contentResolver.openInputStream(uri)?.use { fis ->
            val buffer = ByteArray(16384)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
        } ?: return ""
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    private fun collectFilesRecursive(dir: File, result: MutableList<File>, currentDepth: Int = 0, maxDepth: Int = 4) {
        if (currentDepth > maxDepth || !dir.exists() || !dir.isDirectory) return
        val children = dir.listFiles() ?: return
        for (child in children) {
            if (child.isDirectory) {
                collectFilesRecursive(child, result, currentDepth + 1, maxDepth)
            } else if (child.isFile && child.length() > 0) {
                result.add(child)
            }
        }
    }

    private fun collectDocumentFilesRecursive(dir: DocumentFile, result: MutableList<DocumentFile>, currentDepth: Int = 0, maxDepth: Int = 3) {
        if (currentDepth > maxDepth || !dir.isDirectory) return
        val children = dir.listFiles()
        for (child in children) {
            if (child.isDirectory) {
                collectDocumentFilesRecursive(child, result, currentDepth + 1, maxDepth)
            } else if (child.isFile) {
                result.add(child)
            }
        }
    }

    private fun getMimeType(extension: String): String {
        return android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
    }

    private data class SampleFileDef(
        val name: String,
        val content: String,
        val sizeOffset: Int,
        val category: FileCategory,
        val daysAgo: Int
    )

    private data class RawFileMetadata(
        val uri: String,
        val name: String,
        val extension: String,
        val mimeType: String,
        val sizeBytes: Long,
        val createdAt: Long,
        val modifiedAt: Long,
        val fileHash: String,
        val category: String,
        val contentSnippet: String?,
        val isSample: Boolean
    )
}
