package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scanned_files",
    indices = [
        Index(value = ["fileHash"]),
        Index(value = ["category"]),
        Index(value = ["isDuplicate"]),
        Index(value = ["artist"]),
        Index(value = ["imageSubtype"]),
        Index(value = ["isVaulted"])
    ]
)
data class FileMetadata(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val originalName: String,
    val suggestedName: String,
    val currentName: String,
    val extension: String,
    val mimeType: String,
    val category: String, // "DOCUMENTS", "IMAGES", "AUDIO", "VIDEO", "ARCHIVES", "OTHER"
    val sizeBytes: Long,
    val createdAt: Long,
    val modifiedAt: Long,
    val fileHash: String, // Cryptographic SHA-256 hash of file content
    val contentHashAlgorithm: String = "SHA-256",
    val isDuplicate: Boolean = false,
    val duplicateGroupId: String? = null,
    val tags: String = "",
    val extractedSummary: String = "",
    val status: String = "SCANNED", // "SCANNED", "RENAMED", "ORGANIZED"
    val organizationFolder: String = "",
    val isSample: Boolean = false,
    val isVaulted: Boolean = false,

    // Music & Audio Rich Metadata
    val artist: String? = null, // Band / Artist
    val album: String? = null, // Album
    val trackTitle: String? = null, // Track title
    val trackNumber: Int? = null,
    val genre: String? = null,
    val year: String? = null,
    val durationMs: Long? = null,

    // Image Nuanced Classification Subtype
    // Values: "SCREENSHOT", "PRODUCT_DESIGN", "INFORMATION", "CAMERA_PHOTO", "WALLPAPER", "MEME", "OTHER"
    val imageSubtype: String? = null,

    // 3D Model Metadata Cache
    val modelType: String? = null, // "VRM", "GLB"
    val modelPreviewPath: String? = null
) {
    val tagList: List<String>
        get() = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val formattedSize: String
        get() = formatFileSize(sizeBytes)

    val formattedMusicTitle: String
        get() = when {
            !artist.isNullOrBlank() && !trackTitle.isNullOrBlank() -> "$artist - $trackTitle"
            !trackTitle.isNullOrBlank() -> trackTitle
            else -> currentName
        }

    val imageSubtypeDisplay: String?
        get() = when (imageSubtype) {
            "SCREENSHOT" -> "Screenshot"
            "PRODUCT_DESIGN" -> "Product Design"
            "INFORMATION" -> "Information / Infographic"
            "CAMERA_PHOTO" -> "Camera Photo"
            "WALLPAPER" -> "Wallpaper / Art"
            "MEME" -> "Meme / Graphic"
            else -> null
        }

    companion object {
        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
            val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
            return String.format(java.util.Locale.US, "%.1f %s", value, units[digitGroups])
        }
    }
}

enum class DashboardWidget(val title: String, val description: String, val iconName: String) {
    STORAGE_INFO("Storage Analyzer", "Visual breakdown of your storage space.", "Storage"),
    STORAGE_BREAKDOWN("Storage Breakdown", "Detailed bar chart of storage usage by category.", "Chart"),
    TREEMAP("Storage Treemap", "3D-style visualization of folder distribution.", "Map"),
    SEARCH_BAR("Universal Search", "Search all indexed files on the device.", "Search"),
    QUICK_ACTIONS("System Actions", "Scan, clean, and organize shortcuts.", "Bolt"),
    RECENT_FILES("Recent Entities", "Last discovered or modified files.", "History"),
    DUPLICATE_STATS("De-Dupe Status", "Duplicate file count and savings.", "Copy"),
    MUSIC_PREVIEW("Audio Engine", "Quick access to music library stats.", "Music"),
    VAULT_STATUS("Mimic Vault", "Secure vault seal status.", "Lock"),
    DEVICE_SPECS("System & RAM Health", "Hardware specs, RAM pressure, and storage lag telemetry.", "Memory"),
    TERMINAL("Terminal Logs", "Real-time log of shell commands.", "Terminal")
}
