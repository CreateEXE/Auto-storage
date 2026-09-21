package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class FileCategory(
    val title: String,
    val color: Color
) {
    DOCUMENTS("Documents", Color(0xFF38BDF8)),
    IMAGES("Photos & Images", Color(0xFFEC4899)),
    AUDIO("Audio", Color(0xFFA855F7)),
    VIDEO("Videos", Color(0xFFF97316)),
    ARCHIVES("Archives", Color(0xFFEAB308)),
    OTHER("Other", Color(0xFF64748B));

    val icon: ImageVector
        get() = when (this) {
            DOCUMENTS -> Icons.Default.Description
            IMAGES -> Icons.Default.Image
            AUDIO -> Icons.Default.AudioFile
            VIDEO -> Icons.Default.VideoFile
            ARCHIVES -> Icons.Default.FolderZip
            OTHER -> Icons.AutoMirrored.Filled.InsertDriveFile
        }

    companion object {
        fun fromExtension(ext: String): FileCategory {
            val clean = ext.lowercase().trimStart('.')
            return when (clean) {
                "pdf", "doc", "docx", "txt", "md", "rtf", "odt", "xls", "xlsx", "csv", "ppt", "pptx" -> DOCUMENTS
                "jpg", "jpeg", "png", "webp", "gif", "svg", "bmp", "heic", "raw" -> IMAGES
                "mp3", "wav", "m4a", "flac", "aac", "ogg", "wma" -> AUDIO
                "mp4", "mkv", "mov", "avi", "webm", "3gp", "flv" -> VIDEO
                "zip", "rar", "7z", "tar", "gz", "bz2" -> ARCHIVES
                else -> OTHER
            }
        }

        fun fromMimeType(mime: String?): FileCategory {
            if (mime == null) return OTHER
            val lower = mime.lowercase()
            return when {
                lower.startsWith("image/") -> IMAGES
                lower.startsWith("video/") -> VIDEO
                lower.startsWith("audio/") -> AUDIO
                lower.contains("pdf") || lower.contains("document") || lower.contains("text/") ||
                        lower.contains("sheet") || lower.contains("presentation") -> DOCUMENTS
                lower.contains("zip") || lower.contains("compressed") || lower.contains("tar") -> ARCHIVES
                else -> OTHER
            }
        }
    }
}
