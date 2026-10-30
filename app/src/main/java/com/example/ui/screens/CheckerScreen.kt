package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Medication
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealDark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.SeverityLevel
import com.example.ui.components.FoodInteractionCard
import com.example.ui.components.InteractionCard
import com.example.ui.components.RecallCard
import com.example.ui.theme.OnSeverityCriticalContainer
import com.example.ui.theme.OnSeveritySafeContainer
import com.example.ui.theme.SeverityCriticalContainer
import com.example.ui.theme.SeverityCriticalRed
import com.example.ui.theme.SeverityModerateAmber
import com.example.ui.theme.SeverityModerateContainer
import com.example.ui.theme.SeveritySafeContainer
import com.example.ui.theme.SeveritySafeGreen
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.RxSafetyViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckerScreen(
    viewModel: RxSafetyViewModel,
    modifier: Modifier = Modifier
) {
    val selectedMeds by viewModel.selectedMedications.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val auditResult by viewModel.auditResult.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val shouldShowHomeBanner by viewModel.shouldShowHomeBanner.collectAsState()
    val isSupporter by viewModel.isSupporter.collectAsState()

    var selectedResultTab by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("checker_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 640.dp)
                ) {
                    // Home Compact Donation Banner (Triggers after 3+ operations, 3+ launches, 2+ days)
                    com.example.ui.components.HomeDonationBanner(
                        visible = shouldShowHomeBanner,
                        onOpenSupportDialog = { viewModel.openDonationDialog() },
                        onDismiss = { viewModel.dismissHomeBanner() }
                    )

                    // Header Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.checker_title),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = stringResource(R.string.checker_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                        }
                        if (selectedMeds.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clearAllMedications() },
                                modifier = Modifier.testTag("clear_all_meds_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.clear_all),
                                    tint = Slate600
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dedicated NIH RxNav Search Button Launcher
                    Button(
                        onClick = { viewModel.setTab(1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_rxnav_search_button"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = TealContainer.copy(alpha = 0.65f),
                            contentColor = TealDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.rx_search_title),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Quick-Pick Chips
                    Text(
                        text = stringResource(R.string.clinical_presets),
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PresetChip(
                            label = "Warfarin + Aspirin",
                            onClick = { viewModel.loadPresetPair("Warfarin", "Aspirin") },
                            isHighRisk = true
                        )
                        PresetChip(
                            label = "Lisinopril + Spironolactone",
                            onClick = { viewModel.loadPresetPair("Lisinopril", "Spironolactone") },
                            isHighRisk = true
                        )
                        PresetChip(
                            label = "Simvastatin + Clarithromycin",
                            onClick = { viewModel.loadPresetPair("Simvastatin", "Clarithromycin") },
                            isHighRisk = true
                        )
                        PresetChip(
                            label = "Sertraline + Tramadol",
                            onClick = { viewModel.loadPresetPair("Sertraline", "Tramadol") },
                            isHighRisk = true
                        )
                        PresetChip(
                            label = "Sildenafil + Nitroglycerin",
                            onClick = { viewModel.loadPresetPair("Sildenafil", "Nitroglycerin") },
                            isHighRisk = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Drug Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drug_search_input"),
                        placeholder = { Text(stringResource(R.string.search_drug_placeholder)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TealPrimary)
                        },
                        trailingIcon = {
                            if (isSearching) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.clear_all))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Autocomplete Dropdown list
                    if (searchResults.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = stringResource(R.string.nih_rxnav_matches),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate600,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                                searchResults.take(6).forEach { candidate ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.addMedication(candidate.name) }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                            .testTag("candidate_${candidate.rxcui}"),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Medication,
                                                contentDescription = null,
                                                tint = TealPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = candidate.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Surface(
                                            color = TealContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.rxcui_prefix, candidate.rxcui),
                                                color = TealPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selected Medications Chips Row
                    Text(
                        text = stringResource(R.string.medications_to_evaluate, selectedMeds.size),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (selectedMeds.isEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = stringResource(R.string.empty_meds_notice),
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedMeds.forEach { med ->
                                InputChip(
                                    selected = true,
                                    onClick = { },
                                    label = { Text(med, fontWeight = FontWeight.SemiBold) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.cancel),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { viewModel.removeMedication(med) }
                                        )
                                    },
                                    colors = InputChipDefaults.inputChipColors(
                                        selectedContainerColor = TealContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Audit Status Summary Card
                    if (isAnalyzing) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TealPrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(stringResource(R.string.evaluating_pharmacology), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    } else if (auditResult != null) {
                        val result = auditResult!!
                        AuditSummaryHeader(result = result)
                        
                        // Post-Operation Success Card (Value Realization Moment)
                        com.example.ui.components.PostOpSuccessCard(
                            isSupporter = isSupporter,
                            onOpenSupportDialog = { viewModel.openDonationDialog() }
                        )
                    }
                }
            }
        }

        // Tabbed Results Section (Drug Interactions / Food Warnings / Active Recalls)
        if (auditResult != null) {
            val result = auditResult!!
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(modifier = Modifier.widthIn(max = 640.dp)) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedResultTab,
                            edgePadding = 0.dp,
                            containerColor = Color.Transparent
                        ) {
                            Tab(
                                selected = selectedResultTab == 0,
                                onClick = { selectedResultTab = 0 },
                                text = {
                                    Text(
                                        stringResource(R.string.tab_drug_conflicts, result.drugInteractions.size),
                                        fontWeight = if (selectedResultTab == 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                            Tab(
                                selected = selectedResultTab == 1,
                                onClick = { selectedResultTab = 1 },
                                text = {
                                    Text(
                                        stringResource(R.string.tab_dietary, result.foodInteractions.size),
                                        fontWeight = if (selectedResultTab == 1) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                            Tab(
                                selected = selectedResultTab == 2,
                                onClick = { selectedResultTab = 2 },
                                text = {
                                    Text(
                                        stringResource(R.string.tab_fda_recalls, result.activeRecalls.size),
                                        fontWeight = if (selectedResultTab == 2) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Tab Content Items
            when (selectedResultTab) {
                0 -> {
                    if (result.drugInteractions.isEmpty()) {
                        item {
                            EmptyCheckNotice(
                                title = stringResource(R.string.no_severe_conflicts_title),
                                subtitle = stringResource(R.string.no_severe_conflicts_subtitle)
                            )
                        }
                    } else {
                        items(result.drugInteractions, key = { it.id }) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.widthIn(max = 640.dp)) {
                                    InteractionCard(interaction = item)
                                }
                            }
                        }
                    }
                }
                1 -> {
                    if (result.foodInteractions.isEmpty()) {
                        item {
                            EmptyCheckNotice(
                                title = stringResource(R.string.no_dietary_title),
                                subtitle = stringResource(R.string.no_dietary_subtitle)
                            )
                        }
                    } else {
                        items(result.foodInteractions, key = { it.id }) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.widthIn(max = 640.dp)) {
                                    FoodInteractionCard(item = item)
                                }
                            }
                        }
                    }
                }
                2 -> {
                    if (result.activeRecalls.isEmpty()) {
                        item {
                            EmptyCheckNotice(
                                title = stringResource(R.string.no_recalls_match_title),
                                subtitle = stringResource(R.string.no_recalls_match_subtitle)
                            )
                        }
                    } else {
                        items(result.activeRecalls, key = { it.recallNumber }) { recall ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.widthIn(max = 640.dp)) {
                                    RecallCard(recall = recall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetChip(
    label: String,
    onClick: () -> Unit,
    isHighRisk: Boolean
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
        leadingIcon = {
            Icon(
                imageVector = if (isHighRisk) Icons.Default.Warning else Icons.Default.Medication,
                contentDescription = null,
                tint = if (isHighRisk) SeverityCriticalRed else TealPrimary,
                modifier = Modifier.size(14.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isHighRisk) SeverityCriticalContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun AuditSummaryHeader(result: com.example.data.model.AuditResult) {
    val (cardBg, iconTint, titleText) = when (result.highestSeverity) {
        SeverityLevel.CRITICAL -> Triple(
            SeverityCriticalContainer,
            SeverityCriticalRed,
            "⚠️ Severe Risk: Immediate Medical Review Advised"
        )
        SeverityLevel.MODERATE -> Triple(
            SeverityModerateContainer,
            SeverityModerateAmber,
            "⚡ Moderate Risk: Caution & Monitoring Required"
        )
        SeverityLevel.LOW -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            TealPrimary,
            "ℹ️ Minor Potential Interactions Noted"
        )
        SeverityLevel.SAFE -> Triple(
            SeveritySafeContainer,
            SeveritySafeGreen,
            "✅ Verified: No Known Severe Conflicts"
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audit_summary_card"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (result.highestSeverity == SeverityLevel.SAFE)
                        Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (result.highestSeverity == SeverityLevel.CRITICAL)
                        OnSeverityCriticalContainer else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.conflicts_count_bullet, result.drugInteractions.size),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.dietary_count_bullet, result.foodInteractions.size),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.recalls_count_bullet, result.activeRecalls.size),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun EmptyCheckNotice(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.widthIn(max = 500.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SeveritySafeGreen,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
