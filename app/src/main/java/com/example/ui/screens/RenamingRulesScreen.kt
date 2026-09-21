package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RenamingRuleEntity
import com.example.ui.FileOrganizerViewModel
import com.example.ui.components.CustomRuleBuilderDialog
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
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
fun RenamingRulesScreen(
    viewModel: FileOrganizerViewModel,
    modifier: Modifier = Modifier
) {
    val allRules by viewModel.allRules.collectAsStateWithLifecycle()
    val activeRules by viewModel.activeRules.collectAsStateWithLifecycle()
    var showAddRuleDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header Control Card
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
                                imageVector = Icons.AutoMirrored.Filled.Rule,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Renaming Rule Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSilver
                            )
                            Text(
                                text = "${activeRules.size} of ${allRules.size} rules active • Offline formatting",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberCyan,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = { showAddRuleDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_rule_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Rule", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.applyActiveRulesToAll() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("apply_rules_to_all_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LaserEmerald,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply Active Rules to All Files (Offline)", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Rules List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(allRules, key = { it.id }) { rule ->
                RuleCard(
                    rule = rule,
                    onToggleActive = { viewModel.toggleRuleActive(rule) },
                    onDelete = { viewModel.deleteRule(rule) }
                )
            }
        }
    }

    if (showAddRuleDialog) {
        CustomRuleBuilderDialog(
            onDismiss = { showAddRuleDialog = false },
            onSaveRule = { name, targetCategory, extensionFilter, keywordPattern, outputFormat, casing, replaceSpacesWith, prefix, suffix ->
                viewModel.addCustomRule(
                    name = name,
                    targetCategory = targetCategory,
                    extensionFilter = extensionFilter,
                    keywordPattern = keywordPattern,
                    outputFormat = outputFormat,
                    casing = casing,
                    replaceSpacesWith = replaceSpacesWith,
                    prefix = prefix,
                    suffix = suffix
                )
            }
        )
    }
}

@Composable
private fun RuleCard(
    rule: RenamingRuleEntity,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .metallicPanel(
                cornerRadius = 12.dp,
                showBolts = false
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = rule.ruleName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSilver
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CyberCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = rule.targetCategory,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = rule.isActive,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = CyberCyan,
                            uncheckedThumbColor = TextSteelSecondary,
                            uncheckedTrackColor = SteelSurfaceVariant
                        ),
                        modifier = Modifier.testTag("switch_rule_${rule.id}")
                    )
                    if (!rule.isSystemRule) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Rule",
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Output pattern display
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SteelSurfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PATTERN: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSteelMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Text(
                        text = rule.outputFormat,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = CyberCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Options summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (rule.keywordPattern.isNotBlank()) {
                    Text(
                        text = "Keys: ${rule.keywordPattern}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSteelSecondary,
                        fontSize = 10.sp
                    )
                }
                if (rule.extensionFilter.isNotBlank()) {
                    Text(
                        text = "Ext: .${rule.extensionFilter}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSteelSecondary,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = "Casing: ${rule.casing}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSteelMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = "Space: '${rule.replaceSpacesWith}'",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSteelMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
