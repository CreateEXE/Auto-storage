package com.example.data.scanner

import android.content.Context
import android.net.Uri
import android.util.Log
import com.mpatric.mp3agic.Mp3File
import java.io.File
import java.io.FileOutputStream

object Mp3MetadataHelper {

    /**
     * Utilizes the mp3agic library to extract detailed ID3v1 and ID3v2 tags.
     * Note: This library requires a local File path.
     */
    fun extractId3Tags(context: Context, fileUri: String): ExtractedAudioMetadata? {
        val uri = Uri.parse(fileUri)
        var tempFile: File? = null
        
        return try {
            val fileToRead = if (uri.scheme == "file") {
                File(uri.path ?: return null)
            } else {
                // For content URIs, we need a temporary local file for mp3agic
                tempFile = File(context.cacheDir, "temp_metadata_${System.currentTimeMillis()}.mp3")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile
            }

            if (fileToRead != null && fileToRead.exists() && fileToRead.length() > 0) {
                val mp3file = Mp3File(fileToRead.absolutePath)
                
                var artist: String? = null
                var album: String? = null
                var title: String? = null
                var track: Int? = null
                var genre: String? = null
                var year: String? = null
                var duration: Long = mp3file.lengthInMilliseconds

                if (mp3file.hasId3v2Tag()) {
                    val tag = mp3file.id3v2Tag
                    artist = tag.artist
                    album = tag.album
                    title = tag.title
                    track = tag.track?.split("/")?.firstOrNull()?.toIntOrNull()
                    genre = tag.genreDescription ?: tag.genre.toString()
                    year = tag.year
                } else if (mp3file.hasId3v1Tag()) {
                    val tag = mp3file.id3v1Tag
                    artist = tag.artist
                    album = tag.album
                    title = tag.title
                    track = tag.track?.toIntOrNull()
                    genre = tag.genreDescription ?: tag.genre.toString()
                    year = tag.year
                }

                ExtractedAudioMetadata(
                    artist = artist,
                    album = album,
                    trackTitle = title,
                    trackNumber = track,
                    genre = genre,
                    year = year,
                    durationMs = duration
                )
            } else null
        } catch (e: Exception) {
            Log.e("Mp3MetadataHelper", "Error extracting ID3 tags: ${e.message}")
            null
        } finally {
            tempFile?.delete()
        }
    }
}
