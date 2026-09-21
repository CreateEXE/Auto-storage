package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileMetadata
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.LaserEmerald
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.SteelSurfaceVariant
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextSteelMuted
import com.example.ui.theme.TextSteelSecondary

@Composable
fun MusicMetadataDialog(
    file: FileMetadata,
    onDismiss: () -> Unit,
    onSaveMetadata: (
        artist: String,
        album: String,
        trackTitle: String,
        trackNumber: Int?,
        genre: String,
        year: String
    ) -> Unit
) {
    var artist by remember { mutableStateOf(file.artist ?: "") }
    var album by remember { mutableStateOf(file.album ?: "") }
    var trackTitle by remember { mutableStateOf(file.trackTitle ?: file.currentName.substringBeforeLast('.')) }
    var trackNumberText by remember { mutableStateOf(file.trackNumber?.toString() ?: "") }
    var genre by remember { mutableStateOf(file.genre ?: "") }
    var year by remember { mutableStateOf(file.year ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Music ID3 Metadata",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSilver
                        )
                        Text(
                            text = file.currentName,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSteelMuted,
                            maxLines = 1
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSteelSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Band / Artist
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Band / Artist") },
                    placeholder = { Text("e.g. Queen, Daft Punk") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("music_artist_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SteelBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Album
                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Album Title") },
                    placeholder = { Text("e.g. A Night at the Opera") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("music_album_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SteelBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Track title & track number
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = trackNumberText,
                        onValueChange = { trackNumberText = it },
                        label = { Text("Track #") },
                        placeholder = { Text("01") },
                        singleLine = true,
                        modifier = Modifier
                            .width(80.dp)
                            .testTag("music_track_no_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SteelBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = trackTitle,
                        onValueChange = { trackTitle = it },
                        label = { Text("Song / Track Title") },
                        placeholder = { Text("e.g. Bohemian Rhapsody") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("music_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SteelBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Genre & Year
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = genre,
                        onValueChange = { genre = it },
                        label = { Text("Genre") },
                        placeholder = { Text("Rock, Synthwave") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SteelBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Release Year") },
                        placeholder = { Text("1975") },
                        singleLine = true,
                        modifier = Modifier.width(110.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SteelBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Suggested standard name preview
                val trackNumParsed = trackNumberText.toIntOrNull()
                val formattedTrack = trackNumParsed?.let { String.format(java.util.Locale.US, "%02d", it) } ?: "01"
                val generatedFilename = "${artist.ifBlank { "Unknown Artist" }} - ${album.ifBlank { "Unknown Album" }} - $formattedTrack - ${trackTitle.ifBlank { "Track" }}.${file.extension}"

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SteelSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "SUGGESTED FILENAME:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = LaserEmerald,
                            fontSize = 10.sp
                        )
                        Text(
                            text = generatedFilename,
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberCyan
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveMetadata(
                        artist,
                        album,
                        trackTitle,
                        trackNumberText.toIntOrNull(),
                        genre,
                        year
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                modifier = Modifier.testTag("save_music_metadata_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Metadata", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = TextSteelSecondary)
            }
        },
        containerColor = SteelSurface
    )
}
