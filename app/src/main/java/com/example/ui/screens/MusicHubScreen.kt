package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FileMetadata
import com.example.ui.FileOrganizerViewModel
import com.example.ui.components.MusicMetadataDialog
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.LaserEmerald
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.SteelSurfaceContainer
import com.example.ui.theme.SteelSurfaceVariant
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextSteelMuted
import com.example.ui.theme.TextSteelSecondary
import com.example.ui.util.metallicPanel

@Composable
fun MusicHubScreen(
    viewModel: FileOrganizerViewModel,
    modifier: Modifier = Modifier
) {
    val audioFiles by viewModel.audioFiles.collectAsStateWithLifecycle()
    val uniqueArtists by viewModel.uniqueArtists.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    var selectedArtistFilter by remember { mutableStateOf<String?>(null) }
    var editingAudioFile by remember { mutableStateOf<FileMetadata?>(null) }

    val displayedTracks = remember(audioFiles, selectedArtistFilter) {
        if (selectedArtistFilter == null) {
            audioFiles
        } else {
            audioFiles.filter { it.artist.equals(selectedArtistFilter, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Music Hub Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .metallicPanel(cornerRadius = 14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
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
                                imageVector = Icons.Default.LibraryMusic,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Music & ID3 Core",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSilver
                            )
                            Text(
                                text = "${audioFiles.size} Tracks • ${uniqueArtists.size} Bands/Artists Indexed",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberCyan,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (isOnline) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = LaserEmerald.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LaserEmerald.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "ONLINE AI READY",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = LaserEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SteelSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SteelBorder)
                        ) {
                            Text(
                                text = "OFFLINE PARSER",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSteelSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.organizeMusicLibrary() },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("organize_music_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.FolderSpecial, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sort by Artist/Album", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (isOnline) {
                        Button(
                            onClick = { viewModel.enrichAllMetadataOnline() },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("ai_music_enrich_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = LaserEmerald, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enrich via AI", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Band / Artist Filter Chips
        if (uniqueArtists.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedArtistFilter == null,
                        onClick = { selectedArtistFilter = null },
                        label = { Text("All Artists (${audioFiles.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.25f),
                            selectedLabelColor = CyberCyan
                        )
                    )
                }
                items(uniqueArtists) { artist ->
                    val count = audioFiles.count { it.artist.equals(artist, ignoreCase = true) }
                    FilterChip(
                        selected = selectedArtistFilter == artist,
                        onClick = {
                            selectedArtistFilter = if (selectedArtistFilter == artist) null else artist
                        },
                        label = { Text("$artist ($count)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.25f),
                            selectedLabelColor = CyberCyan
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Music Tracks List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(displayedTracks, key = { it.id }) { track ->
                MusicTrackCard(
                    track = track,
                    onEditMetadata = { editingAudioFile = track }
                )
            }
        }
    }

    if (editingAudioFile != null) {
        MusicMetadataDialog(
            file = editingAudioFile!!,
            onDismiss = { editingAudioFile = null },
            onSaveMetadata = { artist, album, trackTitle, trackNumber, genre, year ->
                viewModel.updateAudioMetadata(
                    editingAudioFile!!,
                    artist,
                    album,
                    trackTitle,
                    trackNumber,
                    genre,
                    year
                )
            }
        )
    }
}

@Composable
private fun MusicTrackCard(
    track: FileMetadata,
    onEditMetadata: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .metallicPanel(cornerRadius = 12.dp, showBolts = false)
            .clickable { onEditMetadata() }
            .testTag("music_track_card_${track.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.trackTitle ?: track.currentName.substringBeforeLast('.'),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSilver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = buildString {
                        append(track.artist ?: "Unknown Artist")
                        if (!track.album.isNullOrBlank()) append(" • ${track.album}")
                        if (track.trackNumber != null) append(" • Trk #${track.trackNumber}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberCyan,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!track.genre.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SteelSurfaceContainer
                        ) {
                            Text(
                                text = track.genre!!,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSteelSecondary,
                                fontSize = 9.sp
                            )
                        }
                    }
                    if (!track.year.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SteelSurfaceContainer
                        ) {
                            Text(
                                text = track.year!!,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSteelMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                    Text(
                        text = track.formattedSize,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSteelMuted,
                        fontSize = 10.sp
                    )
                }
            }

            IconButton(
                onClick = onEditMetadata,
                modifier = Modifier.testTag("edit_music_metadata_${track.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit ID3 Metadata",
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
