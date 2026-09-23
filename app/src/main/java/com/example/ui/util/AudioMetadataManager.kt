package com.example.ui.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.model.FileMetadata
import com.mpatric.mp3agic.ID3v24Tag
import com.mpatric.mp3agic.Mp3File
import java.io.File
import java.io.FileOutputStream

object AudioMetadataManager {
    private const val TAG = "AudioMetadataManager"

    fun updateId3Tags(
        context: Context,
        fileMetadata: FileMetadata,
        artist: String?,
        album: String?,
        title: String?,
        genre: String?,
        year: String?
    ): FileMetadata? {
        val uri = Uri.parse(fileMetadata.uri)
        val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.mp3")
        val updatedFile = File(context.cacheDir, "updated_${System.currentTimeMillis()}.mp3")

        return try {
            // Copy Uri to temp file for mp3agic to work with
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            val mp3File = Mp3File(tempFile.absolutePath)
            
            // Get existing or create new tag
            val tag = if (mp3File.hasId3v2Tag()) {
                mp3File.id3v2Tag
            } else {
                val newTag = ID3v24Tag()
                mp3File.id3v2Tag = newTag
                newTag
            }

            // Update fields
            artist?.let { tag.artist = it }
            album?.let { tag.album = it }
            title?.let { tag.title = it }
            genre?.let { 
                try {
                    tag.genreDescription = it
                } catch (e: Exception) {
                    Log.w(TAG, "Could not set genre description: $it")
                }
            }
            year?.let { tag.year = it }

            // Save to another temp file
            mp3File.save(updatedFile.absolutePath)

            // Write back to original Uri
            context.contentResolver.openOutputStream(uri, "wt")?.use { output ->
                updatedFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: return null

            // Clean up
            tempFile.delete()
            updatedFile.delete()

            fileMetadata.copy(
                artist = artist,
                album = album,
                trackTitle = title,
                genre = genre,
                year = year
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating ID3 tags", e)
            if (tempFile.exists()) tempFile.delete()
            if (updatedFile.exists()) updatedFile.delete()
            null
        }
    }
}
