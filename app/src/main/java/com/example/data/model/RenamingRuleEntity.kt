package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "renaming_rules")
data class RenamingRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleName: String,
    val targetCategory: String = "ALL", // "ALL", "AUDIO", "IMAGES", "DOCUMENTS", "VIDEO", "ARCHIVES"
    val extensionFilter: String = "", // e.g., "mp3, flac" or "png, jpg" or empty
    val keywordPattern: String = "", // Trigger keyword (e.g., "invoice", "screenshot", "mockup", "photo")
    val outputFormat: String, // e.g., "{artist} - {album} - {track} - {title}" or "{date}_{category}_{original}"
    val casing: String = "TITLE_CASE", // "ORIGINAL", "TITLE_CASE", "LOWERCASE", "UPPERCASE", "SNAKE_CASE"
    val replaceSpacesWith: String = "_", // "_", "-", " ", ""
    val prefix: String = "",
    val suffix: String = "",
    val isActive: Boolean = true,
    val isSystemRule: Boolean = false,
    val priority: Int = 0
) {
    fun matches(file: FileMetadata): Boolean {
        if (!isActive) return false
        if (targetCategory != "ALL" && !targetCategory.equals(file.category, ignoreCase = true)) {
            return false
        }
        if (extensionFilter.isNotBlank()) {
            val exts = extensionFilter.split(",").map { it.trim().lowercase().removePrefix(".") }
            if (file.extension.lowercase() !in exts) return false
        }
        if (keywordPattern.isNotBlank()) {
            val kw = keywordPattern.trim().lowercase()
            val matchesName = file.originalName.lowercase().contains(kw)
            val matchesTags = file.tags.lowercase().contains(kw)
            val matchesSubtype = file.imageSubtype?.lowercase()?.contains(kw) == true
            if (!matchesName && !matchesTags && !matchesSubtype) return false
        }
        return true
    }

    fun applyTo(file: FileMetadata): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val yearFormat = java.text.SimpleDateFormat("yyyy", java.util.Locale.US)
        val dateStr = dateFormat.format(java.util.Date(file.createdAt))
        val yearStr = yearFormat.format(java.util.Date(file.createdAt))

        val baseOriginal = file.originalName.substringBeforeLast(".")
        val rawTitle = file.trackTitle ?: baseOriginal
        val rawArtist = file.artist ?: "Unknown Artist"
        val rawAlbum = file.album ?: "Unknown Album"
        val rawTrack = file.trackNumber?.toString()?.padStart(2, '0') ?: "01"
        val rawGenre = file.genre ?: "General"

        var formatted = outputFormat
            .replace("{artist}", rawArtist, ignoreCase = true)
            .replace("{album}", rawAlbum, ignoreCase = true)
            .replace("{track}", rawTrack, ignoreCase = true)
            .replace("{title}", rawTitle, ignoreCase = true)
            .replace("{genre}", rawGenre, ignoreCase = true)
            .replace("{original}", baseOriginal, ignoreCase = true)
            .replace("{date}", dateStr, ignoreCase = true)
            .replace("{year}", yearStr, ignoreCase = true)
            .replace("{category}", file.category.lowercase().replaceFirstChar { it.uppercase() }, ignoreCase = true)
            .replace("{counter}", "01", ignoreCase = true)

        if (prefix.isNotBlank()) formatted = "$prefix$formatted"
        if (suffix.isNotBlank()) formatted = "$formatted$suffix"

        formatted = when (casing) {
            "LOWERCASE" -> formatted.lowercase()
            "UPPERCASE" -> formatted.uppercase()
            "TITLE_CASE" -> formatted.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.US) else it.toString() }
            }
            "SNAKE_CASE" -> formatted.replace(" ", "_").lowercase()
            else -> formatted
        }

        if (replaceSpacesWith != " ") {
            formatted = formatted.replace(" ", replaceSpacesWith)
        }

        val cleanExt = file.extension.removePrefix(".")
        return if (cleanExt.isNotBlank() && !formatted.endsWith(".$cleanExt", ignoreCase = true)) {
            "$formatted.$cleanExt"
        } else {
            formatted
        }
    }

    companion object {
        fun getDefaultRules(): List<RenamingRuleEntity> = listOf(
            RenamingRuleEntity(
                id = 1,
                ruleName = "Music Tracks (Artist - Album - Track)",
                targetCategory = "AUDIO",
                extensionFilter = "mp3, flac, m4a, wav, ogg",
                keywordPattern = "",
                outputFormat = "{artist} - {album} - {track} - {title}",
                casing = "TITLE_CASE",
                replaceSpacesWith = " ",
                isActive = true,
                isSystemRule = true,
                priority = 10
            ),
            RenamingRuleEntity(
                id = 2,
                ruleName = "Screenshots Timestamped",
                targetCategory = "IMAGES",
                extensionFilter = "png, jpg, webp",
                keywordPattern = "screenshot",
                outputFormat = "Screenshot_{date}_{counter}",
                casing = "ORIGINAL",
                replaceSpacesWith = "_",
                isActive = true,
                isSystemRule = true,
                priority = 20
            ),
            RenamingRuleEntity(
                id = 3,
                ruleName = "Product Designs & Mockups",
                targetCategory = "IMAGES",
                extensionFilter = "png, jpg, svg, webp",
                keywordPattern = "mockup",
                outputFormat = "Design_{original}_{date}",
                casing = "TITLE_CASE",
                replaceSpacesWith = "_",
                isActive = true,
                isSystemRule = true,
                priority = 30
            ),
            RenamingRuleEntity(
                id = 4,
                ruleName = "Information & Infographics",
                targetCategory = "IMAGES",
                extensionFilter = "png, jpg, pdf",
                keywordPattern = "infographic",
                outputFormat = "Info_{original}_{year}",
                casing = "TITLE_CASE",
                replaceSpacesWith = "_",
                isActive = true,
                isSystemRule = true,
                priority = 40
            ),
            RenamingRuleEntity(
                id = 5,
                ruleName = "Dated Documents by Category",
                targetCategory = "DOCUMENTS",
                extensionFilter = "pdf, doc, docx, txt",
                keywordPattern = "",
                outputFormat = "{date}_{category}_{original}",
                casing = "TITLE_CASE",
                replaceSpacesWith = "_",
                isActive = true,
                isSystemRule = true,
                priority = 50
            ),
            RenamingRuleEntity(
                id = 6,
                ruleName = "Camera Photos by Date",
                targetCategory = "IMAGES",
                extensionFilter = "jpg, jpeg, heic, dng",
                keywordPattern = "img",
                outputFormat = "{date}_Photo_{counter}",
                casing = "ORIGINAL",
                replaceSpacesWith = "_",
                isActive = true,
                isSystemRule = true,
                priority = 60
            )
        )
    }
}
