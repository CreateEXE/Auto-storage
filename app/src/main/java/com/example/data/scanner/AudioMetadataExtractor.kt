package com.example.data.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import java.io.File

data class ExtractedAudioMetadata(
    val artist: String? = null,
    val album: String? = null,
    val trackTitle: String? = null,
    val trackNumber: Int? = null,
    val genre: String? = null,
    val year: String? = null,
    val durationMs: Long? = null
)

object AudioMetadataExtractor {

    /**
     * Extracts ID3 and audio metadata using Android's MediaMetadataRetriever offline,
     * with graceful filename regex pattern fallback.
     */
    fun extract(context: Context, fileUri: String, fileName: String): ExtractedAudioMetadata {
        // Primary attempt: Use Mp3MetadataHelper (mp3agic) for MP3 files
        if (fileUri.lowercase().endsWith(".mp3")) {
            val mp3Metadata = Mp3MetadataHelper.extractId3Tags(context, fileUri)
            if (mp3Metadata != null) return mp3Metadata
        }

        var artist: String? = null
        var album: String? = null
        var title: String? = null
        var trackNumber: Int? = null
        var genre: String? = null
        var year: String? = null
        var duration: Long? = null

        // Attempt MediaMetadataRetriever if it's a real file/URI
        try {
            val retriever = MediaMetadataRetriever()
            val uri = Uri.parse(fileUri)
            if (uri.scheme == "file") {
                val file = File(uri.path ?: "")
                if (file.exists() && file.length() > 2048) {
                    retriever.setDataSource(file.absolutePath)
                    artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                    title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                    year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                    val trackStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                    trackNumber = trackStr?.split("/")?.firstOrNull()?.toIntOrNull()
                    val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    duration = durStr?.toLongOrNull()
                }
            } else if (uri.scheme == "content") {
                retriever.setDataSource(context, uri)
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                val trackStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                trackNumber = trackStr?.split("/")?.firstOrNull()?.toIntOrNull()
            }
            retriever.release()
        } catch (e: Throwable) {
            Log.d("AudioExtractor", "MediaMetadataRetriever offline fallback: ${e.message}")
        }

        // Fallback: parse filename patterns if metadata fields are empty
        val baseName = fileName.substringBeforeLast(".")
            .replace(Regex("""[-_]copy.*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\(\d+\)"""), "")
            .trim()

        if (artist.isNullOrBlank() || title.isNullOrBlank()) {
            val parts = baseName.split(" - ").map { it.trim() }
            when (parts.size) {
                // "Artist - Album - 01 - Title"
                4 -> {
                    if (artist.isNullOrBlank()) artist = parts[0]
                    if (album.isNullOrBlank()) album = parts[1]
                    if (trackNumber == null) trackNumber = parts[2].toIntOrNull()
                    if (title.isNullOrBlank()) title = parts[3]
                }
                // "Artist - Album - Title" or "Artist - 01 - Title"
                3 -> {
                    if (artist.isNullOrBlank()) artist = parts[0]
                    val possibleTrack = parts[1].toIntOrNull()
                    if (possibleTrack != null) {
                        if (trackNumber == null) trackNumber = possibleTrack
                        if (title.isNullOrBlank()) title = parts[2]
                    } else {
                        if (album.isNullOrBlank()) album = parts[1]
                        if (title.isNullOrBlank()) title = parts[2]
                    }
                }
                // "Artist - Title"
                2 -> {
                    if (artist.isNullOrBlank()) artist = parts[0]
                    if (title.isNullOrBlank()) title = parts[1]
                }
                1 -> {
                    if (title.isNullOrBlank()) title = baseName.replace("_", " ")
                }
            }
        }

        // Detect legendary band defaults if recognized in filename
        val lower = fileName.lowercase()
        if (artist.isNullOrBlank()) {
            when {
                lower.contains("queen") -> artist = "Queen"
                lower.contains("daft punk") || lower.contains("daft_punk") -> artist = "Daft Punk"
                lower.contains("pink floyd") || lower.contains("pink_floyd") -> artist = "Pink Floyd"
                lower.contains("beatles") -> artist = "The Beatles"
                lower.contains("led zeppelin") -> artist = "Led Zeppelin"
                lower.contains("nirvana") -> artist = "Nirvana"
                lower.contains("metallica") -> artist = "Metallica"
                lower.contains("radiohead") -> artist = "Radiohead"
            }
        }

        if (genre.isNullOrBlank()) {
            when {
                lower.contains("rock") || lower.contains("queen") || lower.contains("floyd") -> genre = "Rock"
                lower.contains("electronic") || lower.contains("synth") || lower.contains("daft") -> genre = "Electronic"
                lower.contains("jazz") -> genre = "Jazz"
                lower.contains("classical") || lower.contains("symphony") -> genre = "Classical"
                lower.contains("voice") || lower.contains("memo") || lower.contains("standup") -> genre = "Voice Memo"
            }
        }

        return ExtractedAudioMetadata(
            artist = artist?.trim(),
            album = album?.trim(),
            trackTitle = title?.trim(),
            trackNumber = trackNumber,
            genre = genre?.trim(),
            year = year?.trim(),
            durationMs = duration
        )
    }
}
