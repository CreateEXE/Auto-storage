package com.example.data.rules

import com.example.data.model.FileMetadata
import com.example.data.model.RenamingRuleEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RuleEngine {

    /**
     * Finds the best active matching rule for a file, or null if no rule applies.
     */
    fun findMatchingRule(
        file: FileMetadata,
        rules: List<RenamingRuleEntity>
    ): RenamingRuleEntity? {
        val activeRules = rules.filter { it.isActive }.sortedBy { it.priority }
        val ext = file.extension.lowercase()
        val originalLower = file.originalName.lowercase()
        val categoryUpper = file.category.uppercase()

        for (rule in activeRules) {
            // Category check
            if (rule.targetCategory != "ALL" && !rule.targetCategory.equals(categoryUpper, ignoreCase = true)) {
                continue
            }

            // Extension check
            if (rule.extensionFilter.isNotBlank()) {
                val allowedExts = rule.extensionFilter.split(",", ";", " ")
                    .map { it.trim().removePrefix(".").lowercase() }
                    .filter { it.isNotEmpty() }
                if (allowedExts.isNotEmpty() && ext !in allowedExts) {
                    continue
                }
            }

            // Keyword pattern check
            if (rule.keywordPattern.isNotBlank()) {
                val keywords = rule.keywordPattern.split(",", ";")
                    .map { it.trim().lowercase() }
                    .filter { it.isNotEmpty() }
                val matchesKeyword = keywords.any { kw ->
                    originalLower.contains(kw) ||
                            file.tags.contains(kw, ignoreCase = true) ||
                            file.extractedSummary.contains(kw, ignoreCase = true) ||
                            file.imageSubtype?.contains(kw, ignoreCase = true) == true
                }
                if (!matchesKeyword) {
                    continue
                }
            }

            return rule
        }

        return null
    }

    /**
     * Applies a specific renaming rule to a file entity and returns the generated filename.
     */
    fun applyRule(
        file: FileMetadata,
        rule: RenamingRuleEntity,
        counter: Int = 1
    ): String {
        var template = rule.outputFormat.ifBlank { "{original}" }

        val fileDate = Date(if (file.createdAt > 0) file.createdAt else System.currentTimeMillis())
        val dateIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(fileDate)
        val year = SimpleDateFormat("yyyy", Locale.US).format(fileDate)
        val month = SimpleDateFormat("MM", Locale.US).format(fileDate)
        val day = SimpleDateFormat("dd", Locale.US).format(fileDate)

        val cleanOriginal = file.originalName.substringBeforeLast(".")
            .replace(Regex("""[-_]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        val artist = file.artist?.ifBlank { null } ?: "Unknown Artist"
        val album = file.album?.ifBlank { null } ?: "Singles"
        val trackNum = file.trackNumber?.let { String.format(Locale.US, "%02d", it) } ?: "01"
        val title = file.trackTitle?.ifBlank { null } ?: cleanOriginal
        val genre = file.genre?.ifBlank { null } ?: "Music"
        val counterStr = String.format(Locale.US, "%03d", counter)

        val subtypeStr = when (file.imageSubtype) {
            "SCREENSHOT" -> "Screenshot"
            "PRODUCT_DESIGN" -> "Product_Design"
            "INFORMATION" -> "Information"
            "CAMERA_PHOTO" -> "Camera_Photo"
            "WALLPAPER" -> "Wallpaper"
            else -> file.category.lowercase().replaceFirstChar { it.uppercase() }
        }

        // Replace all tokens
        template = template.replace("{date}", dateIso, ignoreCase = true)
        template = template.replace("{year}", year, ignoreCase = true)
        template = template.replace("{month}", month, ignoreCase = true)
        template = template.replace("{day}", day, ignoreCase = true)
        template = template.replace("{category}", file.category.lowercase().replaceFirstChar { it.uppercase() }, ignoreCase = true)
        template = template.replace("{original}", cleanOriginal, ignoreCase = true)
        template = template.replace("{artist}", artist, ignoreCase = true)
        template = template.replace("{album}", album, ignoreCase = true)
        template = template.replace("{track}", trackNum, ignoreCase = true)
        template = template.replace("{title}", title, ignoreCase = true)
        template = template.replace("{genre}", genre, ignoreCase = true)
        template = template.replace("{subtype}", subtypeStr, ignoreCase = true)
        template = template.replace("{counter}", counterStr, ignoreCase = true)

        // Apply casing
        var processedName = when (rule.casing.uppercase()) {
            "TITLE_CASE" -> template.split(" ", "_", "-")
                .filter { it.isNotBlank() }
                .joinToString(" ") { it.replaceFirstChar { ch -> ch.uppercase() } }
            "LOWERCASE" -> template.lowercase(Locale.US)
            "UPPERCASE" -> template.uppercase(Locale.US)
            "SNAKE_CASE" -> template.lowercase(Locale.US).replace(" ", "_")
            else -> template
        }

        // Apply space replacement
        if (rule.replaceSpacesWith.isNotEmpty() && rule.replaceSpacesWith != " ") {
            processedName = processedName.replace(" ", rule.replaceSpacesWith)
        }

        // Add prefix / suffix
        if (rule.prefix.isNotBlank()) {
            processedName = "${rule.prefix.trim()}$processedName"
        }
        if (rule.suffix.isNotBlank()) {
            processedName = "$processedName${rule.suffix.trim()}"
        }

        // Sanitize forbidden filename characters
        processedName = processedName.replace(Regex("""[/\\?%*:|"<>]+"""), "_")
            .replace(Regex("""_+"""), "_")
            .trim('_', ' ')

        if (processedName.isBlank()) {
            processedName = "Organized_File"
        }

        return "$processedName.${file.extension}"
    }

    /**
     * Preview helper for testing rules live in the rule editor.
     */
    fun previewRule(
        rule: RenamingRuleEntity,
        sampleOriginal: String = "Queen - Bohemian Rhapsody (Live_1985).mp3",
        sampleArtist: String = "Queen",
        sampleAlbum: String = "A Night at the Opera",
        sampleTrack: Int = 4,
        sampleTitle: String = "Bohemian Rhapsody",
        sampleCategory: String = "AUDIO",
        sampleSubtype: String = "Screenshot"
    ): String {
        val dummyFile = FileMetadata(
            uri = "file:///sample.mp3",
            originalName = sampleOriginal,
            suggestedName = "",
            currentName = sampleOriginal,
            extension = sampleOriginal.substringAfterLast('.', "mp3"),
            mimeType = "audio/mpeg",
            category = sampleCategory,
            sizeBytes = 5_000_000L,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            fileHash = "sample_hash",
            artist = sampleArtist,
            album = sampleAlbum,
            trackNumber = sampleTrack,
            trackTitle = sampleTitle,
            genre = "Rock",
            imageSubtype = sampleSubtype
        )
        return applyRule(dummyFile, rule, counter = 1)
    }
}
