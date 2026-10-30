package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.RxNavDrugDetail
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.RxSafetyViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RxNavSearchScreen(
    viewModel: RxSafetyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val searchQuery by viewModel.rxSearchQuery.collectAsState()
    val searchResults by viewModel.rxSearchResults.collectAsState()
    val isSearching by viewModel.isRxSearching.collectAsState()
    val ttyFilter by viewModel.rxSearchTtyFilter.collectAsState()
    val stagedMeds by viewModel.stagedMedications.collectAsState()

    var showExplanationDialog by remember { mutableStateOf(false) }

    val popularSearches = listOf(
        "Warfarin",
        "Lisinopril",
        "Metformin",
        "Eliquis",
        "Tramadol",
        "Fluoxetine",
        "Atorvastatin",
        "Aspirin",
        "Clopidogrel",
        "Omeprazole"
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("rxnav_search_screen"),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = if (stagedMeds.isNotEmpty()) 180.dp else 100.dp
            )
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(modifier = Modifier.widthIn(max = 680.dp)) {
                        // Screen Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.rx_search_title),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = stringResource(R.string.rx_search_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600
                                )
                            }
                            IconButton(
                                onClick = { showExplanationDialog = !showExplanationDialog },
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .testTag("rxcui_info_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = stringResource(R.string.rx_what_is_rxcui),
                                    tint = TealPrimary
                                )
                            }
                        }

                        // Collapsible RxCUI educational card
                        AnimatedVisibility(
                            visible = showExplanationDialog,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = TealContainer.copy(alpha = 0.45f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = TealDark,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.rx_what_is_rxcui),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TealDark
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(R.string.rx_what_is_rxcui_desc),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate700,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Search Input Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onRxSearchQueryChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("rxnav_search_input"),
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.rx_search_placeholder),
                                    fontSize = 13.sp,
                                    color = Slate600
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = TealPrimary
                                )
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { viewModel.onRxSearchQueryChanged("") },
                                            modifier = Modifier.testTag("clear_rx_search_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = stringResource(R.string.clear_all),
                                                tint = Slate600
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { viewModel.openScannerDialog() },
                                        modifier = Modifier.testTag("camera_scan_rx_search_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCodeScanner,
                                            contentDescription = stringResource(R.string.scan_bottle_btn),
                                            tint = TealPrimary
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    focusManager.clearFocus()
                                    viewModel.executeRxSearch(searchQuery)
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Term Type (TTY) Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = ttyFilter == "ALL",
                                onClick = { viewModel.setRxSearchTtyFilter("ALL") },
                                label = { Text(stringResource(R.string.rx_filter_all), fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_chip_all")
                            )

                            FilterChip(
                                selected = ttyFilter == "SCD",
                                onClick = { viewModel.setRxSearchTtyFilter("SCD") },
                                label = { Text(stringResource(R.string.rx_filter_clinical), fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_chip_scd")
                            )

                            FilterChip(
                                selected = ttyFilter == "SBD",
                                onClick = { viewModel.setRxSearchTtyFilter("SBD") },
                                label = { Text(stringResource(R.string.rx_filter_branded), fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_chip_sbd")
                            )

                            FilterChip(
                                selected = ttyFilter == "IN",
                                onClick = { viewModel.setRxSearchTtyFilter("IN") },
                                label = { Text(stringResource(R.string.rx_filter_ingredients), fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_chip_in")
                            )

                            FilterChip(
                                selected = ttyFilter == "BN",
                                onClick = { viewModel.setRxSearchTtyFilter("BN") },
                                label = { Text(stringResource(R.string.rx_filter_brand_names), fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_chip_bn")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Popular Lookups Row
                        Text(
                            text = stringResource(R.string.rx_search_popular_title),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate600
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            popularSearches.forEach { presetDrug ->
                                AssistChip(
                                    onClick = {
                                        viewModel.onRxSearchQueryChanged(presetDrug)
                                        viewModel.executeRxSearch(presetDrug)
                                    },
                                    label = { Text(presetDrug, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.testTag("popular_chip_$presetDrug")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Results Header or Status
                        if (isSearching) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = TealPrimary,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = stringResource(R.string.rx_searching_label),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TealDark
                                )
                            }
                        } else if (searchResults.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.rx_found_count, searchResults.size),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Results List
            if (!isSearching && searchResults.isNotEmpty()) {
                items(searchResults, key = { it.rxcui + it.name }) { drug ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        RxNavDrugResultCard(
                            drug = drug,
                            onToggleStage = { viewModel.toggleStageMedication(drug) },
                            onSaveToDaily = {
                                viewModel.saveDrugToDailyMedications(drug)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.toast_saved_to_daily, drug.name),
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onInspectFda = {
                                viewModel.inspectDrugFromSearch(drug.name)
                            },
                            onCopyRxCui = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("RxCUI", drug.rxcui)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.toast_rxcui_copied, drug.rxcui),
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.widthIn(max = 680.dp)
                        )
                    }
                }
            } else if (!isSearching && searchQuery.isNotBlank() && searchResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 500.dp)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Slate600,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.rx_no_results_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.rx_no_results_subtitle, searchQuery),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate600,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            } else if (!isSearching && searchQuery.isBlank() && searchResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .widthIn(max = 680.dp)
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Medication,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = stringResource(R.string.rx_empty_hero_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.rx_empty_hero_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Bottom Staging Tray for Interaction Checking
        AnimatedVisibility(
            visible = stagedMeds.isNotEmpty(),
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("interaction_staging_tray"),
                tonalElevation = 12.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 680.dp)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.rx_tray_title, stagedMeds.size),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = stringResource(R.string.rx_clear_staged),
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate600,
                                modifier = Modifier
                                    .clickable { viewModel.clearStagedMedications() }
                                    .padding(4.dp)
                                    .testTag("clear_staged_tray_button")
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Horizontal list of staged pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            stagedMeds.forEach { stagedDrug ->
                                AssistChip(
                                    onClick = { viewModel.removeStagedMedication(stagedDrug.rxcui) },
                                    label = {
                                        Text(
                                            text = stagedDrug.name.take(20) + if (stagedDrug.name.length > 20) "…" else "",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = TealContainer.copy(alpha = 0.7f)
                                    ),
                                    modifier = Modifier.testTag("staged_chip_${stagedDrug.rxcui}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (stagedMeds.size < 2) {
                                stringResource(R.string.rx_tray_hint_single)
                            } else {
                                stringResource(R.string.rx_tray_hint_ready)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (stagedMeds.size < 2) Slate600 else TealDark,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.sendStagedToInteractionChecker() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("run_interaction_check_from_tray_button"),
                            enabled = stagedMeds.size >= 2,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TealPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.rx_run_check_button, stagedMeds.size),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RxNavDrugResultCard(
    drug: RxNavDrugDetail,
    onToggleStage: () -> Unit,
    onSaveToDaily: () -> Unit,
    onInspectFda: () -> Unit,
    onCopyRxCui: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rxnav_card_${drug.rxcui}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (drug.isStagedForCheck) TealContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Drug Name + RxCUI Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = drug.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!drug.synonym.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.rx_synonym_prefix, drug.synonym),
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // RxCUI Badge with Copy Action
                Surface(
                    onClick = onCopyRxCui,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier
                        .testTag("copy_rxcui_${drug.rxcui}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.rx_cui_badge, drug.rxcui),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy RxCUI",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata Badges (TTY, Score)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = drug.termTypeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (!drug.score.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.rx_score_prefix, drug.score),
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary Action: Stage for Interaction Checking
                Button(
                    onClick = onToggleStage,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (drug.isStagedForCheck) TealPrimary else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (drug.isStagedForCheck) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .testTag("stage_button_${drug.rxcui}")
                ) {
                    Icon(
                        imageVector = if (drug.isStagedForCheck) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (drug.isStagedForCheck) stringResource(R.string.rx_staged_button) else stringResource(R.string.rx_stage_button),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Secondary Action: Save to Daily Meds
                OutlinedButton(
                    onClick = onSaveToDaily,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("save_daily_button_${drug.rxcui}")
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdd,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.rx_save_daily_button),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Tertiary Action: Inspect FDA Monograph
                IconButton(
                    onClick = onInspectFda,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("inspect_fda_button_${drug.rxcui}")
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = stringResource(R.string.rx_inspect_fda_button),
                        tint = TealDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
