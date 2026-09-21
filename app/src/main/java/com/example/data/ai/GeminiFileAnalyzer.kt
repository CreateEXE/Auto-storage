package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.scanner.AudioMetadataExtractor
import com.example.data.scanner.ExtractedAudioMetadata
import com.example.data.scanner.ImageClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class FileAnalysisResult(
    val suggestedName: String,
    val tags: List<String>,
    val summary: String,
    val organizationFolder: String,
    val artist: String? = null,
    val album: String? = null,
    val trackTitle: String? = null,
    val trackNumber: Int? = null,
    val genre: String? = null,
    val year: String? = null,
    val imageSubtype: String? = null
)

class GeminiFileAnalyzer {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeFile(
        originalName: String,
        extension: String,
        category: String,
        sizeBytes: Long,
        createdAt: Long,
        contentSnippet: String? = null,
        extractedAudio: ExtractedAudioMetadata? = null,
        imageSubtype: String? = null,
        isOnline: Boolean = true
    ): FileAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        // Use online Gemini API when connected and API key is present
        if (isOnline && apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val aiResult = callGeminiApi(
                    apiKey = apiKey,
                    originalName = originalName,
                    extension = extension,
                    category = category,
                    sizeBytes = sizeBytes,
                    createdAt = createdAt,
                    contentSnippet = contentSnippet,
                    extractedAudio = extractedAudio
                )
                if (aiResult != null) {
                    return@withContext aiResult
                }
            } catch (e: Exception) {
                Log.w("GeminiFileAnalyzer", "Online AI analysis fallback to offline engine: ${e.message}")
            }
        }

        // Local smart heuristic analyzer (100% offline capable)
        return@withContext analyzeWithLocalHeuristics(
            originalName = originalName,
            extension = extension,
            category = category,
            sizeBytes = sizeBytes,
            createdAt = createdAt,
            contentSnippet = contentSnippet,
            extractedAudio = extractedAudio,
            imageSubtype = imageSubtype
        )
    }

    private fun callGeminiApi(
        apiKey: String,
        originalName: String,
        extension: String,
        category: String,
        sizeBytes: Long,
        createdAt: Long,
        contentSnippet: String?,
        extractedAudio: ExtractedAudioMetadata?
    ): FileAnalysisResult? {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(createdAt))
        val prompt = """
            You are a precision file organizer AI. Given the following file details:
            - Original filename: "$originalName"
            - Extension: "$extension"
            - Category: "$category"
            - Size: $sizeBytes bytes
            - Creation date: $dateStr
            ${if (extractedAudio?.artist != null) "- Audio artist hint: ${extractedAudio.artist}" else ""}
            ${if (extractedAudio?.album != null) "- Audio album hint: ${extractedAudio.album}" else ""}
            ${if (extractedAudio?.trackTitle != null) "- Audio track title: ${extractedAudio.trackTitle}" else ""}
            ${if (!contentSnippet.isNullOrBlank()) "- Content snippet: $contentSnippet" else ""}

            Task instructions:
            1. If it's AUDIO/MUSIC: identify the Band/Artist, Album, Track Title, Track Number, Genre, and Year.
            2. If it's an IMAGE: classify into imageSubtype: "SCREENSHOT", "PRODUCT_DESIGN", "INFORMATION", "CAMERA_PHOTO", or "WALLPAPER".
            3. Provide a clean, standardized suggested filename (ending with .$extension).
            4. Provide 3-5 searchable tags.
            5. Provide a 1-sentence summary.
            6. Provide the recommended organizationFolder (e.g. "Music/Queen/A Night at the Opera" or "Images/Product_Designs" or "Documents/Finance").

            Return ONLY valid JSON matching this exact structure without markdown backticks:
            {
              "suggestedName": "Queen - A Night at the Opera - 04 - Bohemian Rhapsody.mp3",
              "tags": ["Rock", "Queen", "ClassicRock", "1975"],
              "summary": "Epic progressive rock masterpiece by Queen from A Night at the Opera.",
              "organizationFolder": "Music/Queen/A Night at the Opera",
              "artist": "Queen",
              "album": "A Night at the Opera",
              "trackTitle": "Bohemian Rhapsody",
              "trackNumber": 4,
              "genre": "Rock",
              "year": "1975",
              "imageSubtype": null
            }
        """.trimIndent()

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val root = JSONObject(body)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            val text = parts.getJSONObject(0).optString("text", "").trim()

            val cleanJsonText = text.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = JSONObject(cleanJsonText)

            var suggestedName = parsed.optString("suggestedName", "").trim()
            if (!suggestedName.endsWith(".$extension", ignoreCase = true)) {
                suggestedName = "$suggestedName.$extension"
            }

            val tagsList = mutableListOf<String>()
            val tagsArray = parsed.optJSONArray("tags")
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    tagsList.add(tagsArray.getString(i).trim())
                }
            }

            val summary = parsed.optString("summary", "")
            val folder = parsed.optString("organizationFolder", "Organized/$category")

            val artist = parsed.optString("artist", "").ifBlank { null }
            val album = parsed.optString("album", "").ifBlank { null }
            val trackTitle = parsed.optString("trackTitle", "").ifBlank { null }
            val trackNumber = if (parsed.has("trackNumber") && !parsed.isNull("trackNumber")) parsed.optInt("trackNumber") else null
            val genre = parsed.optString("genre", "").ifBlank { null }
            val year = parsed.optString("year", "").ifBlank { null }
            val imageSubtype = parsed.optString("imageSubtype", "").ifBlank { null }

            return FileAnalysisResult(
                suggestedName = suggestedName,
                tags = tagsList,
                summary = summary,
                organizationFolder = folder,
                artist = artist,
                album = album,
                trackTitle = trackTitle,
                trackNumber = trackNumber,
                genre = genre,
                year = year,
                imageSubtype = imageSubtype
            )
        }
    }

    fun analyzeWithLocalHeuristics(
        originalName: String,
        extension: String,
        category: String,
        sizeBytes: Long,
        createdAt: Long,
        contentSnippet: String?,
        extractedAudio: ExtractedAudioMetadata? = null,
        imageSubtype: String? = null
    ): FileAnalysisResult {
        val baseName = originalName.substringBeforeLast(".")
        val dateObj = Date(if (createdAt > 0) createdAt else System.currentTimeMillis())
        val dateIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(dateObj)
        val yearStr = SimpleDateFormat("yyyy", Locale.US).format(dateObj)

        val lower = baseName.lowercase()
        val tags = mutableListOf<String>()
        var cleanTitle = baseName
            .replace(Regex("""[-_]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        var organizationFolder = "Organized/$category"
        var summary = ""
        var artist = extractedAudio?.artist
        var album = extractedAudio?.album
        var trackTitle = extractedAudio?.trackTitle
        var trackNumber = extractedAudio?.trackNumber
        var genre = extractedAudio?.genre
        var releaseYear = extractedAudio?.year
        var subtype = imageSubtype

        // Audio & Music handling
        if (category.equals("AUDIO", ignoreCase = true)) {
            val audioInfo = extractedAudio ?: AudioMetadataExtractor.extract(
                context = null as? android.content.Context ?: return fallbackAudioAnalysis(originalName, extension, dateIso, yearStr),
                fileUri = "",
                fileName = originalName
            )
            artist = audioInfo.artist ?: "Unknown Artist"
            album = audioInfo.album ?: "Singles"
            trackTitle = audioInfo.trackTitle ?: cleanTitle
            trackNumber = audioInfo.trackNumber ?: 1
            genre = audioInfo.genre ?: "Music"
            releaseYear = audioInfo.year ?: yearStr

            tags.addAll(listOf("Music", artist, genre, yearStr).filter { it.isNotBlank() })
            organizationFolder = "Music/$artist/$album"
            val trackPadded = String.format(Locale.US, "%02d", trackNumber)
            val suggested = "$artist - $album - $trackPadded - $trackTitle.$extension"
            summary = "Audio track '$trackTitle' by $artist from album '$album' ($genre)."

            return FileAnalysisResult(
                suggestedName = suggested,
                tags = tags.distinct(),
                summary = summary,
                organizationFolder = organizationFolder,
                artist = artist,
                album = album,
                trackTitle = trackTitle,
                trackNumber = trackNumber,
                genre = genre,
                year = releaseYear,
                imageSubtype = null
            )
        }

        // Image nuanced classification
        if (category.equals("IMAGES", ignoreCase = true)) {
            organizationFolder = when (subtype) {
                "SCREENSHOT" -> "Images/Screenshots"
                "PRODUCT_DESIGN" -> "Images/Designs"
                "INFORMATION" -> "Images/Infographics"
                "PORTRAIT" -> "Images/Portraits"
                "LANDSCAPE" -> "Images/Landscapes"
                else -> "Images/General"
            }
            if (subtype != null) tags.add(subtype)

            when (subtype) {
                "SCREENSHOT" -> {
                    summary = "Device screenshot snapshot captured on $dateIso."
                    val suggested = "${dateIso}_Screenshot_${baseName.takeLast(6).replace(Regex("""\D"""), "")}.$extension"
                    return FileAnalysisResult(
                        suggestedName = suggested,
                        tags = tags.distinct(),
                        summary = summary,
                        organizationFolder = organizationFolder,
                        imageSubtype = subtype
                    )
                }
                "PRODUCT_DESIGN" -> {
                    summary = "Product design asset, UI mockup, and CAD specification."
                    val cleanDesign = cleanTitle.split(" ")
                        .filter { it.isNotBlank() }
                        .joinToString("_") { it.replaceFirstChar { ch -> ch.uppercase() } }
                    val suggested = "Design_${cleanDesign}_$dateIso.$extension"
                    return FileAnalysisResult(
                        suggestedName = suggested,
                        tags = tags.distinct(),
                        summary = summary,
                        organizationFolder = organizationFolder,
                        imageSubtype = imageSubtype
                    )
                }
                "INFORMATION" -> {
                    summary = "Information chart, infographic diagram, and data visualization."
                    val cleanInfo = cleanTitle.split(" ")
                        .filter { it.isNotBlank() }
                        .joinToString("_") { it.replaceFirstChar { ch -> ch.uppercase() } }
                    val suggested = "Info_${cleanInfo}_$yearStr.$extension"
                    return FileAnalysisResult(
                        suggestedName = suggested,
                        tags = tags.distinct(),
                        summary = summary,
                        organizationFolder = organizationFolder,
                        imageSubtype = imageSubtype
                    )
                }
                "CAMERA_PHOTO" -> {
                    summary = "High resolution camera photo taken on $dateIso."
                    val suggested = "${dateIso}_Photo_${baseName.takeLast(5)}.$extension"
                    return FileAnalysisResult(
                        suggestedName = suggested,
                        tags = tags.distinct(),
                        summary = summary,
                        organizationFolder = organizationFolder,
                        imageSubtype = imageSubtype
                    )
                }
            }
        }

        // Documents, Invoices, Taxes, etc.
        when {
            lower.contains("invoice") || lower.contains("inv") || lower.contains("bill") -> {
                tags.addAll(listOf("Finance", "Invoice", "Payment", yearStr))
                organizationFolder = "Documents/Finance/$yearStr"
                summary = "Billing invoice record and payment statement for $yearStr."
                cleanTitle = cleanTitle.replace(Regex("""(?i)\b(invoice|inv|bill)\b"""), "").trim()
                cleanTitle = if (cleanTitle.isBlank()) "Invoice_Statement" else "${cleanTitle.replace(" ", "_")}_Invoice"
            }
            lower.contains("receipt") || lower.contains("reciept") -> {
                tags.addAll(listOf("Receipt", "Expense", "Accounting", yearStr))
                organizationFolder = "Documents/Receipts/$yearStr"
                summary = "Purchase receipt and expense voucher."
                cleanTitle = cleanTitle.replace(Regex("""(?i)\b(receipt|reciept)\b"""), "").trim()
                cleanTitle = if (cleanTitle.isBlank()) "Purchase_Receipt" else "${cleanTitle.replace(" ", "_")}_Receipt"
            }
            lower.contains("tax") || lower.contains("w2") || lower.contains("1099") || lower.contains("w9") -> {
                tags.addAll(listOf("Tax", "Finance", "Official", yearStr))
                organizationFolder = "Documents/Taxes/$yearStr"
                summary = "Official tax statement and income filing documentation."
                cleanTitle = "Tax_Statement_${cleanTitle.replace(" ", "_")}"
            }
            lower.contains("medical") || lower.contains("health") || lower.contains("prescription") -> {
                tags.addAll(listOf("Health", "Medical", "Records"))
                organizationFolder = "Documents/Medical"
                summary = "Healthcare record and medical consultation information."
                cleanTitle = "${dateIso}_Medical_${cleanTitle.replace(" ", "_")}"
            }
            lower.contains("resume") || lower.contains("cv") -> {
                tags.addAll(listOf("Career", "Resume", "Work"))
                organizationFolder = "Documents/Career"
                summary = "Professional career resume and curriculum vitae."
                cleanTitle = "${yearStr}_Professional_Resume"
            }
            else -> {
                tags.addAll(listOf("Document", category.lowercase().replaceFirstChar { it.uppercase() }, yearStr))
                organizationFolder = "Organized/$category"
                summary = "Document file created on $dateIso, organized into $category."
                cleanTitle = cleanTitle.replace(Regex("""\(\d+\)"""), "")
                    .replace(Regex("""copy""", RegexOption.IGNORE_CASE), "")
                    .trim()
                if (cleanTitle.isBlank()) cleanTitle = "Organized_Document"
                cleanTitle = cleanTitle.split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString("_") { it.replaceFirstChar { ch -> ch.uppercase() } }
            }
        }

        var formattedName = cleanTitle
            .replace(Regex("""[^a-zA-Z0-9_-]"""), "_")
            .replace(Regex("""_+"""), "_")
            .trim('_')

        if (!formattedName.startsWith("20") && dateIso.isNotBlank()) {
            formattedName = "${dateIso}_$formattedName"
        }

        return FileAnalysisResult(
            suggestedName = "$formattedName.$extension",
            tags = tags.distinct(),
            summary = summary,
            organizationFolder = organizationFolder,
            artist = artist,
            album = album,
            trackTitle = trackTitle,
            trackNumber = trackNumber,
            genre = genre,
            year = releaseYear,
            imageSubtype = imageSubtype
        )
    }

    private fun fallbackAudioAnalysis(
        originalName: String,
        extension: String,
        dateIso: String,
        yearStr: String
    ): FileAnalysisResult {
        return FileAnalysisResult(
            suggestedName = "${dateIso}_Audio_Track.$extension",
            tags = listOf("Audio", "Music", yearStr),
            summary = "Audio recording file.",
            organizationFolder = "Music/Organized",
            artist = "Unknown Artist",
            album = "Singles",
            trackTitle = originalName.substringBeforeLast("."),
            trackNumber = 1,
            genre = "Music",
            year = yearStr
        )
    }
}
