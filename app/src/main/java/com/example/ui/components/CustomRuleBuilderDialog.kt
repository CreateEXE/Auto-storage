package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RenamingRuleEntity
import com.example.data.rules.RuleEngine
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.LaserEmerald
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelBorderHighlight
import com.example.ui.theme.SteelSurface
import com.example.ui.theme.SteelSurfaceContainer
import com.example.ui.theme.SteelSurfaceVariant
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextSteelMuted
import com.example.ui.theme.TextSteelSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomRuleBuilderDialog(
    onDismiss: () -> Unit,
    onSaveRule: (
        name: String,
        targetCategory: String,
        extensionFilter: String,
        keywordPattern: String,
        outputFormat: String,
        casing: String,
        replaceSpacesWith: String,
        prefix: String,
        suffix: String
    ) -> Unit
) {
    var ruleName by remember { mutableStateOf("") }
    var targetCategory by remember { mutableStateOf("ALL") }
    var extensionFilter by remember { mutableStateOf("") }
    var keywordPattern by remember { mutableStateOf("") }
    var outputFormat by remember { mutableStateOf("{date}_{original}") }
    var casing by remember { mutableStateOf("TITLE_CASE") }
    var replaceSpacesWith by remember { mutableStateOf("_") }
    var prefix by remember { mutableStateOf("") }
    var suffix by remember { mutableStateOf("") }

    val categories = listOf("ALL", "AUDIO", "IMAGES", "DOCUMENTS", "VIDEO", "ARCHIVES")
    val casingOptions = listOf("TITLE_CASE", "ORIGINAL", "LOWERCASE", "UPPERCASE", "SNAKE_CASE")
    val separatorOptions = listOf("_", "-", " ", "")

    val tokens = listOf(
        "{artist}" to "Band/Artist",
        "{album}" to "Album",
        "{track}" to "Track #",
        "{title}" to "Song/Title",
        "{genre}" to "Genre",
        "{subtype}" to "Img Subtype",
        "{date}" to "YYYY-MM-DD",
        "{year}" to "YYYY",
        "{category}" to "Category",
        "{original}" to "Orig Name",
        "{counter}" to "001"
    )

    // Calculate live preview
    val previewResult = remember(
        outputFormat, casing, replaceSpacesWith, prefix, suffix, targetCategory
    ) {
        val tempRule = RenamingRuleEntity(
            ruleName = ruleName.ifBlank { "Preview Rule" },
            targetCategory = targetCategory,
            extensionFilter = extensionFilter,
            keywordPattern = keywordPattern,
            outputFormat = outputFormat,
            casing = casing,
            replaceSpacesWith = replaceSpacesWith,
            prefix = prefix,
            suffix = suffix
        )
        when (targetCategory) {
            "AUDIO" -> RuleEngine.previewRule(
                rule = tempRule,
                sampleOriginal = "Queen - Bohemian Rhapsody (Copy).mp3",
                sampleArtist = "Queen",
                sampleAlbum = "A Night at the Opera",
                sampleTrack = 4,
                sampleTitle = "Bohemian Rhapsody",
                sampleCategory = "AUDIO"
            )
            "IMAGES" -> RuleEngine.previewRule(
                rule = tempRule,
                sampleOriginal = "Screenshot_20240812-140231.png",
                sampleCategory = "IMAGES",
                sampleSubtype = "Screenshot"
            )
            else -> RuleEngine.previewRule(
                rule = tempRule,
                sampleOriginal = "aws_computing_invoice_final_v2.pdf",
                sampleCategory = "DOCUMENTS"
            )
        }
    }

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
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Custom Renaming Rule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSilver
                        )
                        Text(
                            text = "Pattern Engine & Token Formatter",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan
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
                // Live Interactive Test Preview Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SteelSurfaceVariant,
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = LaserEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE RULE PREVIEW",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LaserEmerald
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = previewResult,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = CyberCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rule Name
                OutlinedTextField(
                    value = ruleName,
                    onValueChange = { ruleName = it },
                    label = { Text("Rule Name (e.g. Music Standard, Screenshots)") },
                    placeholder = { Text("Enter rule identifier") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rule_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SteelBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Target Category
                Text(
                    text = "Target Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSteelSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = targetCategory == cat,
                            onClick = { targetCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.25f),
                                selectedLabelColor = CyberCyan
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Output Format Pattern
                OutlinedTextField(
                    value = outputFormat,
                    onValueChange = { outputFormat = it },
                    label = { Text("Desired Output Format") },
                    placeholder = { Text("{artist} - {album} - {track} - {title}") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rule_output_format_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SteelBorder
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Token insertion chips
                Text(
                    text = "Tap to Insert Pattern Tokens:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSteelMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tokens.forEach { (token, label) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SteelSurfaceContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    outputFormat = if (outputFormat.isBlank()) token else "$outputFormat$token"
                                }
                                .border(1.dp, SteelBorder, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "+$token",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = CyberCyan,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Keyword Pattern & Extensions
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = keywordPattern,
                        onValueChange = { keywordPattern = it },
                        label = { Text("Keywords (optional)") },
                        placeholder = { Text("e.g. invoice, mockup") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SteelBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = extensionFilter,
                        onValueChange = { extensionFilter = it },
                        label = { Text("Extensions") },
                        placeholder = { Text("mp3, flac") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SteelBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Casing
                Text(
                    text = "Letter Casing",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSteelSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    casingOptions.forEach { opt ->
                        FilterChip(
                            selected = casing == opt,
                            onClick = { casing = opt },
                            label = { Text(opt.replace("_", " "), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.25f),
                                selectedLabelColor = CyberCyan
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Space separator replacement
                Text(
                    text = "Space Separator",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSteelSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    separatorOptions.forEach { sep ->
                        val display = when (sep) {
                            "_" -> "Underscore (_)"
                            "-" -> "Hyphen (-)"
                            " " -> "Space ( )"
                            else -> "Remove spaces"
                        }
                        FilterChip(
                            selected = replaceSpacesWith == sep,
                            onClick = { replaceSpacesWith = sep },
                            label = { Text(display, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.25f),
                                selectedLabelColor = CyberCyan
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ruleName.isNotBlank() && outputFormat.isNotBlank()) {
                        onSaveRule(
                            ruleName.trim(),
                            targetCategory,
                            extensionFilter.trim(),
                            keywordPattern.trim(),
                            outputFormat.trim(),
                            casing,
                            replaceSpacesWith,
                            prefix.trim(),
                            suffix.trim()
                        )
                        onDismiss()
                    }
                },
                enabled = ruleName.isNotBlank() && outputFormat.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                modifier = Modifier.testTag("save_rule_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Rule", fontWeight = FontWeight.Bold)
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
