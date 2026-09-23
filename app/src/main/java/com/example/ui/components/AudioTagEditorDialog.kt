package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.mpatric.mp3agic.Mp3File
import com.mpatric.mp3agic.ID3v2
import com.mpatric.mp3agic.ID3v24Tag
import java.io.File

@Composable
fun AudioTagEditorDialog(
    filePath: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(filePath) {
        try {
            val mp3file = Mp3File(filePath)
            if (mp3file.hasId3v2Tag()) {
                val tag = mp3file.id3v2Tag
                title = tag.title ?: ""
                artist = tag.artist ?: ""
                album = tag.album ?: ""
                genre = tag.genreDescription ?: ""
                year = tag.year ?: ""
            }
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(420.dp)
                .background(HackBlack, RoundedCornerShape(2.dp))
                .border(1.dp, HackDeepOrange, RoundedCornerShape(2.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MusicNote, null, tint = HackCyan)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "AUDIO METADATA EDITOR",
                    color = HackDeepOrange,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                CircularProgressIndicator(color = HackDeepOrange, modifier = Modifier.align(Alignment.CenterHorizontally).padding(20.dp))
            } else if (error != null) {
                Text("ERROR: $error", color = DangerRed, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("TITLE", color = TextSteelSecondary, fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextSilver,
                            unfocusedTextColor = TextSilver,
                            focusedBorderColor = HackCyan,
                            unfocusedBorderColor = HackDeepOrange.copy(alpha = 0.3f)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = artist,
                        onValueChange = { artist = it },
                        label = { Text("ARTIST", color = TextSteelSecondary, fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextSilver,
                            unfocusedTextColor = TextSilver,
                            focusedBorderColor = HackCyan,
                            unfocusedBorderColor = HackDeepOrange.copy(alpha = 0.3f)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = album,
                        onValueChange = { album = it },
                        label = { Text("ALBUM", color = TextSteelSecondary, fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextSilver,
                            unfocusedTextColor = TextSilver,
                            focusedBorderColor = HackCyan,
                            unfocusedBorderColor = HackDeepOrange.copy(alpha = 0.3f)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = genre,
                            onValueChange = { genre = it },
                            label = { Text("GENRE", color = TextSteelSecondary, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextSilver,
                                unfocusedTextColor = TextSilver,
                                focusedBorderColor = HackCyan,
                                unfocusedBorderColor = HackDeepOrange.copy(alpha = 0.3f)
                            ),
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                        )
                        OutlinedTextField(
                            value = year,
                            onValueChange = { year = it },
                            label = { Text("YEAR", color = TextSteelSecondary, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextSilver,
                                unfocusedTextColor = TextSilver,
                                focusedBorderColor = HackCyan,
                                unfocusedBorderColor = HackDeepOrange.copy(alpha = 0.3f)
                            ),
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) {
                            Text("ABORT", color = TextSteelSecondary, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onSave(title, artist, album, genre, year) },
                            colors = ButtonDefaults.buttonColors(containerColor = HackDeepOrange),
                            shape = RoundedCornerShape(2.dp)
                        ) {
                            Text("WRITE TAGS", color = HackBlack, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}
